const { app, BrowserWindow, ipcMain, shell, dialog, Menu } = require('electron');
const path = require('path');
const fs = require('fs');
const http = require('http');
const { spawn } = require('child_process');
const {
    loadProvidersConfig,
    saveProvidersConfig,
} = require('./lib/configStore');
const { streamOpenAIChat } = require('./lib/openaiStream');
const {
    loginPassword,
    loginSms,
    registerUser,
    fetchPictureCode,
    requestSmsCode,
} = require('./lib/systemAuth');
const {
    getUserInfo,
    updateUser,
    uploadAvatarFromPath,
    voToSessionUser,
    updateUserPassword,
} = require('./lib/systemApi');
const {
    loadMailConfig,
    saveMailConfig,
    maskForRenderer,
    mergeSavedMail,
} = require('./lib/mailConfigStore');
const mailQueue = require('./lib/mailQueueStore');
const { runMailPollRound } = require('./lib/mailPollService');
const { collectStreamOpenAIChat } = require('./lib/streamCollect');

const DEFAULT_MODEL_FILE =
    (
        process.env.UCLAW_MODEL || 'qwen2.5-3b-instruct-q8_0.gguf'
    ).trim() || 'qwen2.5-3b-instruct-q8_0.gguf';

/** Qwen ChatML：im_start / im_end 与 gguf 词表一致，运行时由下方常量拼接 */
const QWEN_IM_START = '<|' + 'im_start' + '|>';
const QWEN_IM_END = '<|' + 'im_end' + '|>';

let currentChild = null;
let runGeneration = 0;
let httpAbort = null;
let mainWindow = null;
let mailPollTimer = null;

function getAppRoot() {
    if (app.isPackaged) {
        return path.dirname(app.getPath('exe'));
    }
    return __dirname;
}

function createMailTransporter(mc) {
    const nodemailer = require('nodemailer');
    return nodemailer.createTransport({
        host: mc.smtp.host,
        port: Number(mc.smtp.port) || 587,
        secure: mc.smtp.ssl === true,
        auth: { user: mc.smtp.user, pass: mc.smtp.password },
        requireTLS: mc.smtp.starttls !== false && mc.smtp.ssl !== true,
    });
}

/** 发件人：显式「发件地址」优先；未填或为教程占位时，用 SMTP 账号（完整邮箱） */
function resolveMailFromAddress(mc) {
    const smtp = mc && mc.smtp;
    if (!smtp) {
        return '';
    }
    const raw = (smtp.fromAddress && String(smtp.fromAddress).trim()) || '';
    const user = (smtp.user && String(smtp.user).trim()) || '';
    const placeholder =
        !raw || /^noreply@example\.com$/i.test(raw);
    if (placeholder && user.includes('@')) {
        return user;
    }
    if (raw) {
        return raw;
    }
    return user.includes('@') ? user : '';
}

/**
 * 新版 llama.cpp：`llama-cli` 为交互/会话；一次性补全请用 `llama-completion`（与官方 README 一致）
 * @see https://github.com/ggml-org/llama.cpp/blob/master/tools/completion/README.md
 */
function getLlamaCompletionPath() {
    const name =
        process.platform === 'win32' ? 'llama-completion.exe' : 'llama-completion';
    return path.join(getAppRoot(), 'bin', name);
}

function getLlamaServerPath() {
    const name =
        process.platform === 'win32' ? 'llama-server.exe' : 'llama-server';
    return path.join(getAppRoot(), 'bin', name);
}

// ──────────────────────────────────────────────────────────────────────────────
// llama-server 常驻服务管理
// 模型在应用生命周期内只加载一次；切换模型时自动重启。
// ──────────────────────────────────────────────────────────────────────────────
const LLAMA_SERVER_PORT = 18768;

/**
 * 状态：{ child, modelPath, port, readyPromise, ready }
 * readyPromise 非 null 表示正在启动中，并发请求需 await 同一个 Promise。
 */
let llamaServerState = null;

function httpGetJson(url, timeoutMs) {
    return new Promise((resolve, reject) => {
        const req = http.get(url, { timeout: timeoutMs || 2000 }, (res) => {
            let body = '';
            res.on('data', (c) => (body += c));
            res.on('end', () => resolve({ status: res.statusCode, body }));
        });
        req.on('error', reject);
        req.on('timeout', () => {
            req.destroy();
            reject(new Error('timeout'));
        });
    });
}

async function waitForLlamaServer(port, timeoutMs) {
    const deadline = Date.now() + (timeoutMs || 120000);
    const url = `http://127.0.0.1:${port}/health`;
    while (Date.now() < deadline) {
        try {
            const r = await httpGetJson(url, 1500);
            if (r.status === 200) return;
        } catch (_) {}
        await new Promise((r) => setTimeout(r, 700));
    }
    throw new Error(
        `llama-server 启动超时（${((timeoutMs || 120000) / 1000).toFixed(0)}s）。` +
        `请确认模型文件存在且内存充足。`
    );
}

/**
 * 确保 llama-server 已就绪并返回监听端口。
 * - 同一模型复用已有进程；
 * - 模型变更时先 kill 旧进程再重启；
 * - 并发调用共享同一个启动 Promise，不会重复启动。
 */
async function ensureLlamaServer(provider) {
    const modelPath = getLocalModelPath(provider);

    // 已有同模型实例且未退出，直接复用
    if (
        llamaServerState &&
        llamaServerState.modelPath === modelPath &&
        llamaServerState.child &&
        !llamaServerState.child.killed
    ) {
        if (llamaServerState.readyPromise) {
            await llamaServerState.readyPromise;
        }
        return llamaServerState.port;
    }

    // 旧实例（模型不同或已崩溃）先清理
    if (llamaServerState && llamaServerState.child) {
        try { llamaServerState.child.kill(); } catch (_) {}
        llamaServerState = null;
    }

    const exe = getLlamaServerPath();
    if (!fs.existsSync(exe)) {
        throw new Error(
            `未找到 llama-server.exe，请将其从 bin/ 的同版构建中确认存在。`
        );
    }
    if (!fs.existsSync(modelPath)) {
        throw new Error(`未找到模型文件：models/${path.basename(modelPath)}`);
    }

    const binDir = path.dirname(exe);
    const childEnv = { ...process.env };
    if (process.platform === 'win32') {
        childEnv.PATH = binDir + path.delimiter + (childEnv.PATH || '');
    }

    const child = spawn(
        exe,
        [
            '-m', modelPath,
            '--port', String(LLAMA_SERVER_PORT),
            '--host', '127.0.0.1',
            '-c', '4096',   // context window
            '-lv', '0',     // 静默日志
        ],
        {
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
            cwd: binDir,
            env: childEnv,
        }
    );

    const readyPromise = waitForLlamaServer(LLAMA_SERVER_PORT, 120000);
    llamaServerState = { child, modelPath, port: LLAMA_SERVER_PORT, readyPromise, ready: false };

    child.on('error', (err) => {
        console.error('[llama-server] spawn error:', err.message);
        if (llamaServerState && llamaServerState.child === child) {
            llamaServerState = null;
        }
    });
    child.on('close', (code) => {
        console.log('[llama-server] exited, code:', code);
        if (llamaServerState && llamaServerState.child === child) {
            llamaServerState = null;
        }
    });

    try {
        await readyPromise;
    } finally {
        if (llamaServerState && llamaServerState.child === child) {
            llamaServerState.readyPromise = null;
            llamaServerState.ready = true;
        }
    }
    return llamaServerState.port;
}

function getLocalModelPath(provider) {
    const file =
        (provider && provider.modelFile) || DEFAULT_MODEL_FILE;
    return path.join(getAppRoot(), 'models', file);
}

/** 邮件轮询等非 UI 场景：单次补全整段回复 */
async function runMailLlmComplete(providerId, userPrompt) {
    const cfg = loadProvidersConfig(getAppRoot);
    const pid =
        providerId && String(providerId).trim()
            ? String(providerId).trim()
            : cfg.lastProviderId || 'local';
    const provider =
        cfg.providers.find((p) => p.id === pid) || cfg.providers[0];
    if (!provider) {
        throw new Error('没有可用模型源');
    }
    const messages = [{ role: 'user', content: userPrompt }];
    const ac = new AbortController();
    if (provider.kind === 'local') {
        const port = await ensureLlamaServer(provider);
        return collectStreamOpenAIChat({
            baseUrl: `http://127.0.0.1:${port}/v1`,
            apiKey: '',
            model: path.basename(getLocalModelPath(provider), '.gguf'),
            messages,
            signal: ac.signal,
            temperature: 0.85,
            maxTokens: 512,
        });
    }
    if (provider.kind === 'openai_compat') {
        return collectStreamOpenAIChat({
            baseUrl: provider.baseUrl || 'https://api.openai.com/v1',
            apiKey: provider.apiKey || '',
            model: provider.model || '',
            messages,
            signal: ac.signal,
        });
    }
    throw new Error('未知 provider 类型: ' + (provider.kind || ''));
}

function notifyMailQueueUpdated() {
    if (mainWindow && !mainWindow.isDestroyed()) {
        mainWindow.webContents.send('mail:queueUpdated');
    }
}

function scheduleMailPolling() {
    if (mailPollTimer) {
        clearInterval(mailPollTimer);
        mailPollTimer = null;
    }
    const mc = loadMailConfig(getAppRoot);
    if (!mc.enabled) {
        return;
    }
    const ms = Math.max(30000, Number(mc.pollIntervalMs) || 120000);
    const tick = () => {
        runMailPollRound({
            getAppRoot,
            loadSession: loadSystemSession,
            loadMailConfig: () => loadMailConfig(getAppRoot),
            runMailLlm: runMailLlmComplete,
            notify: notifyMailQueueUpdated,
        }).catch((e) => console.warn('[mail] poll', e.message));
    };
    mailPollTimer = setInterval(tick, ms);
    tick();
}

/**
 * Qwen2.5 等 Instruct 模型应使用 ChatML 风格，续写在 <|im_start|>assistant 之后。
 * 勿把大段自然语言说明写进 -f 文件，否则补全会当作正文复述。
 * @see Qwen2.5 默认 chat template
 */
function buildLocalPrompt(messages) {
    const system = (messages.find((m) => m.role === 'system') || {}).content
        || 'You are a helpful assistant.';
    const withoutSys = messages.filter((m) => m.role !== 'system');

    let out =
        QWEN_IM_START + 'system\n' + system + '\n' + QWEN_IM_END + '\n';

    for (const m of withoutSys) {
        if (m.role === 'user') {
            out +=
                QWEN_IM_START +
                'user\n' +
                m.content +
                '\n' +
                QWEN_IM_END +
                '\n';
        } else if (m.role === 'assistant') {
            out +=
                QWEN_IM_START +
                'assistant\n' +
                m.content +
                '\n' +
                QWEN_IM_END +
                '\n';
        }
    }

    out += QWEN_IM_START + 'assistant\n';
    return out;
}

function sendEvent(webContents, data) {
    if (!webContents || webContents.isDestroyed()) return;
    webContents.send('llm:event', data);
}

/** Windows 下子进程退出码（常为 32 位无符号或负的 int） */
function describeWindowsLlamaExit(code) {
    if (code == null) return '';
    const u = code >>> 0;
    const hex = '0x' + u.toString(16).toUpperCase();
    if (u === 0xc0000135) {
        return `${hex}：缺少依赖 DLL（STATUS_DLL_NOT_FOUND）。请把与 llama-completion 同一次构建产物的 .dll 一并复制到 bin/，或安装 VC++ 2015–2022 x64 可再发行包；GPU 版需本机 CUDA/cuBLAS。`;
    }
    if (u === 0xc0000005) {
        return `${hex}：访问冲突，多为与当前 CPU/指令集不匹配的构建。请换用官方预编译或在本机重新编译 llama.cpp。`;
    }
    return `${hex}`;
}

/** 管道输出里含 CJK 的行视为模型正文，不做日志过滤 */
function lineHasCjk(s) {
    return /[\u3400-\u9fff\uf900-\ufaff]/.test(s);
}

/**
 * 只匹配「整行、典型日志」；禁止对正文做子串级过滤（会误伤整段输出）。
 * UCLAW_NO_LLAMA_LOG_FILTER=1 时关闭过滤。
 */
function isLlamaPipeNoiseLine(line) {
    const t = line.replace(/\r$/, '');
    if (lineHasCjk(t)) {
        return false;
    }
    const s = t.trim();
    if (s.length === 0) {
        return false;
    }
    if (s.length > 2000) {
        return false;
    }
    const low = s.toLowerCase();
    if (low.startsWith('load_backend:')) {
        return true;
    }
    if (/^ggml_/.test(s)) {
        return true;
    }
    if (
        /^llama_model_load_from_file|^llama_init_from_model|^llama_init_from|llama_context:/i.test(
            s
        ) &&
        s.length < 200
    ) {
        return true;
    }
    if (
        low.startsWith('common_') &&
        /print|breakdown|info|mem/i.test(s) &&
        s.length < 300
    ) {
        return true;
    }
    if (
        /^\s*\|\s*memory breakdown\s*\[mib\]/i.test(s) ||
        /^\s*\|\s*-\s*host\s*\|/i.test(s)
    ) {
        return true;
    }
    if (
        /^\[?\s*Prompt:\s*[\d.]+\s*t\/s.*[Gg]eneration:/i.test(s) &&
        s.length < 350
    ) {
        return true;
    }
    if (/^exiting\.{0,3}$/i.test(s)) {
        return true;
    }
    if ((/^build\s*:\s*[a-z0-9-]{4,}/i.test(s) || /^build\s*:\s*b\d{3,5}/i.test(s)) && s.length < 120) {
        return true;
    }
    if (
        /^model\s*:\s*[^\n]*\.(gguf|ggml)/i.test(s) ||
        /^modalities\s*:\s*text$/i.test(s)
    ) {
        return true;
    }
    if (/^available commands:/.test(s) || /^loading model\.{0,3}$/i.test(s)) {
        return true;
    }
    if (
        /^>\s*system:/i.test(s) ||
        /^>\s*user:/i.test(s) ||
        /^>\s*assistant:?\s*$/i.test(s)
    ) {
        return true;
    }
    if (s.length < 200 && /^sampler chain|^evaluated tokens|decode batch|^main:\s*memory usage/i.test(s)) {
        return true;
    }
    if (
        s.length < 200 &&
        /^main: (prompt|chat template|encode|using chat)/i.test(s)
    ) {
        return true;
    }
    if (
        /[\u2580-\u259f\u2500-\u257f]/.test(s) &&
        !/[a-zA-Z]{2,}/.test(s) &&
        s.length > 2 &&
        s.length < 200
    ) {
        return true;
    }
    return false;
}

function createLlamaLineFilter(emitLine) {
    let rest = '';
    return {
        push(chunk) {
            rest += chunk;
            const parts = rest.split(/\r?\n/);
            rest = parts.pop() || '';
            for (const line of parts) {
                if (!isLlamaPipeNoiseLine(line)) {
                    emitLine(line + '\n');
                }
            }
        },
        end() {
            if (!rest.length) {
                rest = '';
                return;
            }
            const parts = rest.split(/\r?\n/);
            rest = '';
            for (let i = 0; i < parts.length; i++) {
                const line = parts[i];
                if (isLlamaPipeNoiseLine(line)) {
                    continue;
                }
                if (i < parts.length - 1) {
                    emitLine(line + '\n');
                } else {
                    emitLine(line);
                }
            }
        },
    };
}

const KEY_PLACEHOLDER = '********';

function maskProvidersForRenderer(cfg) {
    return {
        lastProviderId: cfg.lastProviderId,
        providers: cfg.providers.map((p) => {
            if (p.kind === 'openai_compat') {
                return {
                    ...p,
                    apiKey: p.apiKey ? KEY_PLACEHOLDER : '',
                    hasKey: !!p.apiKey,
                };
            }
            return { ...p };
        }),
    };
}

function mergeSavedProviders(incoming, previous) {
    const prevById = new Map(previous.providers.map((p) => [p.id, p]));
    const providers = incoming.providers.map((np) => {
        const op = prevById.get(np.id);
        if (
            np.kind === 'openai_compat' &&
            op &&
            op.kind === 'openai_compat' &&
            np.apiKey === KEY_PLACEHOLDER
        ) {
            return { ...np, apiKey: op.apiKey || '' };
        }
        return np;
    });
    return { lastProviderId: incoming.lastProviderId, providers };
}

ipcMain.handle('providers:get', () => {
    const cfg = loadProvidersConfig(getAppRoot);
    return maskProvidersForRenderer(cfg);
});

ipcMain.handle('providers:save', (_e, incoming) => {
    if (!incoming || !Array.isArray(incoming.providers)) {
        return { ok: false, error: '无效配置' };
    }
    const prev = loadProvidersConfig(getAppRoot);
    const merged = mergeSavedProviders(incoming, prev);
    saveProvidersConfig(getAppRoot, merged);
    return { ok: true };
});

ipcMain.handle('providers:setLast', (_e, id) => {
    if (typeof id !== 'string' || !id) {
        return { ok: false };
    }
    const c = loadProvidersConfig(getAppRoot);
    c.lastProviderId = id;
    saveProvidersConfig(getAppRoot, c);
    return { ok: true };
});

/** 对话输入：选择本地文本文件插入（UTF-8，体积上限内） */
ipcMain.handle('chat:pickFile', async (event) => {
    const win = BrowserWindow.fromWebContents(event.sender);
    const r = await dialog.showOpenDialog(win || undefined, {
        title: '选择要插入到输入框的文件',
        properties: ['openFile'],
        filters: [
            {
                name: '文本与数据',
                extensions: [
                    'txt',
                    'md',
                    'json',
                    'csv',
                    'log',
                    'xml',
                    'html',
                    'htm',
                    'yaml',
                    'yml',
                ],
            },
            { name: '所有文件', extensions: ['*'] },
        ],
    });
    if (r.canceled || !r.filePaths.length) {
        return { ok: false, canceled: true };
    }
    const p = r.filePaths[0];
    const MAX = 512 * 1024;
    let buf;
    try {
        buf = fs.readFileSync(p);
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
    if (buf.length > MAX) {
        return {
            ok: false,
            error: '文件过大（上限 512KB），请拆分或手动粘贴片段',
        };
    }
    const z = buf.indexOf(0);
    if (z >= 0 && z < Math.min(buf.length, 4096)) {
        return {
            ok: false,
            error: '暂仅支持文本类文件；二进制请改为说明或复制可读内容',
        };
    }
    let text;
    try {
        text = buf.toString('utf8');
    } catch (e) {
        return { ok: false, error: '无法按 UTF-8 解码该文件' };
    }
    return {
        ok: true,
        fileName: path.basename(p),
        text,
    };
});

ipcMain.handle('mail:getConfig', () => maskForRenderer(loadMailConfig(getAppRoot)));

ipcMain.handle('mail:saveConfig', (_e, incoming) => {
    if (!incoming || typeof incoming !== 'object') {
        return { ok: false, error: '无效配置' };
    }
    const prev = loadMailConfig(getAppRoot);
    const merged = mergeSavedMail(incoming, prev);
    saveMailConfig(getAppRoot, merged);
    scheduleMailPolling();
    return { ok: true };
});

ipcMain.handle('mail:getQueue', () => mailQueue.loadQueue(getAppRoot));

ipcMain.handle('mail:updateDraft', (_e, payload) => {
    const id = payload && payload.id;
    const suggestedBody =
        payload && payload.suggestedBody != null
            ? String(payload.suggestedBody)
            : '';
    if (!id) {
        return { ok: false };
    }
    mailQueue.updateItem(getAppRoot, id, { suggestedBody });
    return { ok: true };
});

ipcMain.handle('mail:discardMail', (_e, id) => {
    if (!id) {
        return { ok: false };
    }
    mailQueue.updateItem(getAppRoot, id, { status: 'discarded' });
    notifyMailQueueUpdated();
    return { ok: true };
});

ipcMain.handle('mail:pollNow', async () => {
    await runMailPollRound({
        getAppRoot,
        loadSession: loadSystemSession,
        loadMailConfig: () => loadMailConfig(getAppRoot),
        runMailLlm: runMailLlmComplete,
        notify: notifyMailQueueUpdated,
    });
    return { ok: true };
});

ipcMain.handle('mail:sendReply', async (_e, id) => {
    if (!id) {
        return { ok: false, error: '缺少 id' };
    }
    const item = mailQueue.getItem(getAppRoot, id);
    if (!item) {
        return { ok: false, error: '记录不存在' };
    }
    if (item.status !== 'pending') {
        return { ok: false, error: '仅待发送记录可发出' };
    }
    const text = (item.suggestedBody || '').trim();
    if (!text) {
        return { ok: false, error: '正文为空' };
    }
    const mc = loadMailConfig(getAppRoot);
    const fromAddr = resolveMailFromAddress(mc);
    if (!mc.smtp.host || !fromAddr) {
        return { ok: false, error: '请配置 SMTP 与发件地址' };
    }
    const transporter = createMailTransporter(mc);
    const subj = item.subject || '';
    const reSub = /^re:/i.test(subj.trim()) ? subj : `Re: ${subj}`;
    const normId = (mid) => {
        const t = (mid || '').trim();
        if (!t) {
            return '';
        }
        if (t.startsWith('<') && t.endsWith('>')) {
            return t;
        }
        return '<' + t.replace(/[<>]/g, '') + '>';
    };
    const midRaw =
        (item.rawHeaders && item.rawHeaders['Message-ID']) || item.messageId;
    const inReply = normId(midRaw);
    const refsRaw = (item.rawHeaders && item.rawHeaders.References) || '';
    const refLine = (refsRaw.trim() ? refsRaw.trim() + ' ' : '') + inReply;
    try {
        await transporter.sendMail({
            from: fromAddr,
            to: (item.fromAddr || '').trim(),
            subject: reSub,
            text,
            headers: {
                'In-Reply-To': inReply,
                References: refLine.trim(),
            },
        });
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
    mailQueue.updateItem(getAppRoot, id, { status: 'sent' });
    notifyMailQueueUpdated();
    return { ok: true };
});

ipcMain.handle('mail:sendCompose', async (_e, payload) => {
    const to = payload && payload.to != null ? String(payload.to).trim() : '';
    const subject =
        payload && payload.subject != null ? String(payload.subject).trim() : '';
    const text =
        payload && payload.text != null ? String(payload.text).trim() : '';
    if (!to || !text) {
        return { ok: false, error: '缺少收件人或正文' };
    }
    const mc = loadMailConfig(getAppRoot);
    const fromAddr = resolveMailFromAddress(mc);
    if (!mc.smtp.host || !fromAddr) {
        return { ok: false, error: '请配置 SMTP 与发件地址' };
    }
    const transporter = createMailTransporter(mc);
    try {
        await transporter.sendMail({
            from: fromAddr,
            to,
            subject: subject || 'PX-Claw 消息',
            text,
        });
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
    return { ok: true };
});

function getSessionPath() {
    return path.join(getAppRoot(), 'config', 'session.json');
}

function loadSystemSession() {
    try {
        const p = getSessionPath();
        if (fs.existsSync(p)) {
            const data = JSON.parse(fs.readFileSync(p, 'utf8'));
            if (data && typeof data.baseUrl === 'string' && typeof data.token === 'string') {
                return data;
            }
        }
    } catch (e) {
        console.error('loadSystemSession', e);
    }
    return null;
}

function saveSystemSession(session) {
    const dir = path.join(getAppRoot(), 'config');
    if (!fs.existsSync(dir)) {
        fs.mkdirSync(dir, { recursive: true });
    }
    fs.writeFileSync(getSessionPath(), JSON.stringify(session, null, 2), 'utf8');
}

function clearSystemSession() {
    try {
        const p = getSessionPath();
        if (fs.existsSync(p)) {
            fs.unlinkSync(p);
        }
    } catch (e) {
        console.error('clearSystemSession', e);
    }
}

function systemApiErrorResult(e) {
    if (e && e.sessionExpired) {
        clearSystemSession();
    }
    return {
        ok: false,
        error: (e && e.message) || String(e),
        sessionExpired: !!(e && e.sessionExpired),
    };
}

ipcMain.handle('system:session:get', () => loadSystemSession());

ipcMain.handle('system:session:login', async (_e, payload) => {
    try {
        const baseUrl = payload && payload.baseUrl;
        const mode =
            payload && payload.mode === 'sms' ? 'sms' : 'password';
        let result;
        if (mode === 'sms') {
            const mobile = payload && String(payload.mobile || '').trim();
            const smsCode = payload && String(payload.smsCode || '').trim();
            if (!mobile || !smsCode) {
                return { ok: false, error: '请输入手机号和短信验证码' };
            }
            result = await loginSms(baseUrl, mobile, smsCode);
        } else {
            const username = payload && payload.username;
            const password = payload && payload.password;
            if (!username || !password) {
                return { ok: false, error: '请输入账号和密码' };
            }
            result = await loginPassword(baseUrl, username, password);
        }
        const session = {
            baseUrl: result.baseUrl,
            token: result.token,
            user: result.user,
        };
        saveSystemSession(session);
        return { ok: true, session };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:captcha:get', async (_e, payload) => {
    try {
        const baseUrl = payload && payload.baseUrl;
        const data = await fetchPictureCode(baseUrl);
        return { ok: true, data };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:sms:send', async (_e, payload) => {
    try {
        const baseUrl = payload && payload.baseUrl;
        const mobile = payload && String(payload.mobile || '').trim();
        const key = payload && String(payload.key || '').trim();
        const pictureCode = payload && String(payload.pictureCode || '').trim();
        if (!mobile || !key || !pictureCode) {
            return { ok: false, error: '请填写手机号与图形验证码' };
        }
        const msg = await requestSmsCode(baseUrl, mobile, key, pictureCode);
        return { ok: true, message: msg };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:user:register', async (_e, payload) => {
    try {
        const baseUrl = payload && payload.baseUrl;
        const username = payload && String(payload.username || '').trim();
        const passwordPlain = payload && payload.passwordPlain;
        const nickname = payload && payload.nickname;
        const mobile = payload && String(payload.mobile || '').trim();
        const smsCode = payload && String(payload.smsCode || '').trim();
        if (!username || !passwordPlain || !mobile || !smsCode) {
            return {
                ok: false,
                error: '请填写账号、密码、手机号与短信验证码',
            };
        }
        await registerUser(baseUrl, {
            username,
            passwordPlain,
            nickname,
            mobile,
            smsCode,
        });
        return { ok: true };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:session:logout', () => {
    clearSystemSession();
    return { ok: true };
});

ipcMain.handle('system:openExternal', (_e, url) => {
    try {
        const u = String(url || '').trim();
        if (!/^https?:\/\//i.test(u)) {
            return { ok: false, error: '仅允许 http(s) 链接' };
        }
        shell.openExternal(u);
        return { ok: true };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:user:fetchProfile', async () => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '未登录' };
    }
    try {
        const vo = await getUserInfo(s.baseUrl, s.token);
        s.user = { ...s.user, ...voToSessionUser(vo) };
        saveSystemSession(s);
        return { ok: true, user: vo };
    } catch (e) {
        return systemApiErrorResult(e);
    }
});

ipcMain.handle('system:user:saveProfile', async (_e, payload) => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '未登录' };
    }
    try {
        const body = {};
        if (payload && payload.nickname != null) {
            body.nickname = String(payload.nickname).trim();
        }
        if (payload && payload.headImg != null) {
            body.headImg = String(payload.headImg).trim();
        }
        if (payload && payload.sex !== '' && payload.sex != null) {
            body.sex = Number(payload.sex);
        }
        if (payload && payload.address != null) {
            body.address = String(payload.address).trim();
        }
        if (payload && payload.birthday) {
            body.birthday = String(payload.birthday).trim();
        }
        await updateUser(s.baseUrl, s.token, body);
        const vo = await getUserInfo(s.baseUrl, s.token);
        s.user = voToSessionUser(vo);
        saveSystemSession(s);
        return { ok: true, user: vo };
    } catch (e) {
        return systemApiErrorResult(e);
    }
});

ipcMain.handle('system:user:updatePassword', async (_e, payload) => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '未登录' };
    }
    try {
        const mobile = payload && String(payload.mobile || '').trim();
        const smsCode = payload && String(payload.smsCode || '').trim();
        const newPassword =
            payload && payload.newPassword != null
                ? String(payload.newPassword)
                : '';
        if (!mobile || !smsCode || !newPassword) {
            return { ok: false, error: '请填写手机号、短信验证码和新密码' };
        }
        await updateUserPassword(s.baseUrl, s.token, mobile, smsCode, newPassword);
        clearSystemSession();
        return { ok: true, needRelogin: true };
    } catch (e) {
        return systemApiErrorResult(e);
    }
});

ipcMain.handle('system:user:pickAvatar', async (event) => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '请先登录' };
    }
    const win = BrowserWindow.fromWebContents(event.sender);
    const r = await dialog.showOpenDialog(win || undefined, {
        title: '选择头像图片',
        filters: [
            {
                name: '图片',
                extensions: ['jpg', 'jpeg', 'png', 'gif', 'webp'],
            },
        ],
        properties: ['openFile'],
    });
    if (r.canceled || !r.filePaths.length) {
        return { ok: false, canceled: true };
    }
    try {
        const url = await uploadAvatarFromPath(
            s.baseUrl,
            s.token,
            r.filePaths[0]
        );
        return { ok: true, url };
    } catch (e) {
        return systemApiErrorResult(e);
    }
});

/** 仅选择文件路径，供渲染进程裁剪后再上传 */
ipcMain.handle('system:user:pickAvatarFile', async (event) => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '请先登录' };
    }
    const win = BrowserWindow.fromWebContents(event.sender);
    const r = await dialog.showOpenDialog(win || undefined, {
        title: '选择头像图片',
        filters: [
            {
                name: '图片',
                extensions: ['jpg', 'jpeg', 'png', 'gif', 'webp'],
            },
        ],
        properties: ['openFile'],
    });
    if (r.canceled || !r.filePaths.length) {
        return { ok: false, canceled: true };
    }
    return { ok: true, filePath: r.filePaths[0] };
});

ipcMain.handle('system:user:readImageFile', async (_e, filePath) => {
    try {
        const p = String(filePath || '').trim();
        if (!p) {
            return { ok: false, error: '路径无效' };
        }
        const buf = fs.readFileSync(p);
        const ext = path.extname(p).toLowerCase();
        const mime =
            ext === '.png'
                ? 'image/png'
                : ext === '.gif'
                  ? 'image/gif'
                  : ext === '.webp'
                    ? 'image/webp'
                    : 'image/jpeg';
        const dataUrl = `data:${mime};base64,${buf.toString('base64')}`;
        return { ok: true, dataUrl };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
});

ipcMain.handle('system:user:uploadAvatarBase64', async (_e, payload) => {
    const s = loadSystemSession();
    if (!s) {
        return { ok: false, error: '请先登录' };
    }
    const b64 =
        payload && typeof payload.base64 === 'string' ? payload.base64.trim() : '';
    if (!b64) {
        return { ok: false, error: '无图片数据' };
    }
    const tmpPath = path.join(
        require('os').tmpdir(),
        `uclaw-avatar-${Date.now()}.jpg`
    );
    try {
        fs.writeFileSync(tmpPath, Buffer.from(b64, 'base64'));
        const url = await uploadAvatarFromPath(s.baseUrl, s.token, tmpPath);
        return { ok: true, url };
    } catch (e) {
        return systemApiErrorResult(e);
    } finally {
        try {
            fs.unlinkSync(tmpPath);
        } catch (_) {}
    }
});

function runLocalLlama(event, messages, provider, thisGen) {
    const exe = getLlamaCompletionPath();
    const model = getLocalModelPath(provider);

    if (!fs.existsSync(exe)) {
        sendEvent(event.sender, {
            type: 'error',
            message:
                `未找到 ${path.basename(exe)}。请从与 llama-cli 同版的 llama.cpp 构建产物中，将 llama-completion 可执行文件及同目录 .dll 复制到 bin/（官方说明：一次性补全应使用 llama-completion，而非 llama-cli）。`,
        });
        sendEvent(event.sender, { type: 'done' });
        return;
    }
    if (!fs.existsSync(model)) {
        sendEvent(event.sender, {
            type: 'error',
            message: `未找到模型文件：models/${path.basename(model)}`,
        });
        sendEvent(event.sender, { type: 'done' });
        return;
    }

    if (currentChild) {
        try {
            currentChild.kill();
        } catch (_) {}
        currentChild = null;
    }

    const fullPrompt = buildLocalPrompt(messages);

    /** Windows 下 -p 传参易乱码，且应用目录常有中文；用 UTF-8 文件最稳 */
    const tmpPrompt = path.join(
        app.getPath('temp'),
        `uclaw-prompt-${Date.now()}-${process.pid}.txt`
    );
    let tmpRemoved = false;
    const cleanupTmp = () => {
        if (tmpRemoved) return;
        tmpRemoved = true;
        try {
            if (fs.existsSync(tmpPrompt)) {
                fs.unlinkSync(tmpPrompt);
            }
        } catch (_) {}
    };
    try {
        fs.writeFileSync(tmpPrompt, fullPrompt, 'utf8');
    } catch (e) {
        sendEvent(event.sender, {
            type: 'error',
            message: '无法写入临时提示文件: ' + (e && e.message),
        });
        sendEvent(event.sender, { type: 'done' });
        return;
    }

    const binDir = path.dirname(exe);
    const childEnv = { ...process.env };
    if (process.platform === 'win32') {
        childEnv.PATH = binDir + path.delimiter + (childEnv.PATH || '');
    }

    // llama-completion：-no-cnv 为一次性补全；不要用 llama-cli（其不支持 -no-cnv）
    // 遇下一轮 ChatML user 头即停止，减轻小模型复读「user / assistant」伪对话
    const child = spawn(
        exe,
        [
            '-m',
            model,
            '-f',
            tmpPrompt,
            '-n',
            '512',
            '-no-cnv',
            '-r',
            QWEN_IM_START + 'user',
            '--repeat-penalty',
            '1.08',
            '--temp',
            '0.85',
            // 注意：不要加 --log-disable。管道模式下会一并关掉「补全文本」的写出，导致界面 0 字。
            '-lv',
            '0',
            '--color',
            'off',
        ],
        {
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true,
            cwd: binDir,
            env: childEnv,
        }
    );
    currentChild = child;

    child.stdout.setEncoding('utf8');
    child.stderr.setEncoding('utf8');

    let localEmitted = 0;
    const rawForward = (out) => {
        if (thisGen === runGeneration && out) {
            localEmitted += String(out).length;
            sendEvent(event.sender, { type: 'chunk', text: out });
        }
    };

    // 在管道/非 TTY 下，不少构建把补全写在 stderr，stdout 可能为空
    const useRaw =
        process.env.UCLAW_NO_LLAMA_LOG_FILTER === '1' ||
        process.env.UCLAW_NO_LLAMA_LOG_FILTER === 'true';
    const outFilter = useRaw
        ? null
        : createLlamaLineFilter((text) => rawForward(text));
    const errFilter = useRaw
        ? null
        : createLlamaLineFilter((text) => rawForward(text));

    child.stdout.on('data', (data) => {
        if (useRaw) {
            rawForward(data);
        } else {
            outFilter.push(data);
        }
    });
    child.stderr.on('data', (data) => {
        if (process.env.UCLAW_LOG_LLAMA_STDERR) {
            console.error('[llama stderr]', data.toString('utf8'));
        }
        if (useRaw) {
            rawForward(data);
        } else {
            errFilter.push(data);
        }
    });

    child.on('error', (err) => {
        cleanupTmp();
        if (thisGen === runGeneration) {
            sendEvent(event.sender, { type: 'error', message: err.message });
            sendEvent(event.sender, { type: 'done' });
        }
    });

    child.on('close', (code) => {
        if (!useRaw) {
            outFilter.end();
            errFilter.end();
        }
        cleanupTmp();
        if (thisGen !== runGeneration) return;
        if (currentChild === child) {
            currentChild = null;
        }
        if (code !== 0 && code != null) {
            const hint =
                process.platform === 'win32'
                    ? describeWindowsLlamaExit(code)
                    : String(code);
            sendEvent(event.sender, {
                type: 'error',
                message: `llama-completion 异常退出: ${hint}`,
            });
        } else if (!useRaw && localEmitted === 0) {
            sendEvent(event.sender, {
                type: 'error',
                message:
                    '本机模型管道内没有可显示文本（已尝试过滤日志）。请 PowerShell 执行: $env:UCLAW_NO_LLAMA_LOG_FILTER=1; npm start 查看原始流；或确认 bin/llama-completion 与 .dll 为同一套构建。',
            });
        }
        sendEvent(event.sender, { type: 'done' });
    });
}

/**
 * 预热本地模型服务器：在用户选中本地 provider 时立即开始加载，
 * 不阻塞 UI，失败也只打日志（用户下次发消息时会再次尝试并报错）。
 */
ipcMain.handle('llm:warmup', (_e, providerId) => {
    const cfg = loadProvidersConfig(getAppRoot);
    const pid = providerId || cfg.lastProviderId || 'local';
    const provider = cfg.providers.find((p) => p.id === pid) || cfg.providers[0];
    if (!provider || provider.kind !== 'local') return;
    ensureLlamaServer(provider).catch((err) => {
        console.warn('[llm:warmup] 预热失败:', err.message);
    });
});

ipcMain.on('llm:send', (event, payload) => {
    const replyTarget =
        payload && payload.target === 'ai-cs' ? 'ai-cs' : 'chat';

    const { messages, providerId } = payload || {};
    if (!Array.isArray(messages) || messages.length === 0) {
        sendEvent(event.sender, {
            type: 'error',
            message: '无效对话内容',
            target: replyTarget,
        });
        return;
    }

    const cfg = loadProvidersConfig(getAppRoot);
    const pid = providerId || cfg.lastProviderId || 'local';
    const provider = cfg.providers.find((p) => p.id === pid) || cfg.providers[0];

    if (!provider) {
        sendEvent(event.sender, {
            type: 'error',
            message: '没有可用模型源',
            target: replyTarget,
        });
        return;
    }

    const myGen = ++runGeneration;

    if (httpAbort) {
        try {
            httpAbort.abort();
        } catch (_) {}
        httpAbort = null;
    }
    if (currentChild) {
        try {
            currentChild.kill();
        } catch (_) {}
        currentChild = null;
    }

    if (provider.kind === 'local') {
        const ac = new AbortController();
        httpAbort = ac;

        const send = (data) => {
            if (myGen !== runGeneration) return;
            sendEvent(event.sender, {
                ...(data || {}),
                target: replyTarget,
            });
        };

        ensureLlamaServer(provider)
            .then((port) => {
                if (myGen !== runGeneration) {
                    send({ type: 'done' });
                    return;
                }
                return streamOpenAIChat({
                    baseUrl: `http://127.0.0.1:${port}/v1`,
                    apiKey: '',
                    model: path.basename(getLocalModelPath(provider), '.gguf'),
                    messages,
                    signal: ac.signal,
                    temperature: 0.85,
                    maxTokens: 512,
                    send,
                });
            })
            .catch((err) => {
                if (myGen !== runGeneration) return;
                send({ type: 'error', message: err.message || String(err) });
                send({ type: 'done' });
            })
            .finally(() => {
                if (httpAbort === ac) httpAbort = null;
            });
        return;
    }

    if (provider.kind === 'openai_compat') {
        const ac = new AbortController();
        httpAbort = ac;

        const send = (data) => {
            if (myGen !== runGeneration) return;
            sendEvent(event.sender, {
                ...(data || {}),
                target: replyTarget,
            });
        };

        streamOpenAIChat({
            baseUrl: provider.baseUrl || 'https://api.openai.com/v1',
            apiKey: provider.apiKey || '',
            model: provider.model || '',
            messages,
            signal: ac.signal,
            send,
        }).finally(() => {
            if (httpAbort === ac) {
                httpAbort = null;
            }
        });
        return;
    }

    sendEvent(event.sender, {
        type: 'error',
        message: '未知 provider 类型: ' + (provider.kind || ''),
        target: replyTarget,
    });
    sendEvent(event.sender, { type: 'done', target: replyTarget });
});

app.whenReady().then(() => {
    // 去掉 Electron 默认菜单栏（File / Edit / View …）
    Menu.setApplicationMenu(null);

    const win = new BrowserWindow({
        width: 1000,
        height: 800,
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            contextIsolation: true,
            nodeIntegration: false,
        },
    });
    mainWindow = win;
    win.loadFile(path.join(__dirname, 'renderer', 'index.html'));
    scheduleMailPolling();
});

app.on('window-all-closed', () => {
    if (mailPollTimer) {
        clearInterval(mailPollTimer);
        mailPollTimer = null;
    }
    if (httpAbort) {
        try { httpAbort.abort(); } catch (_) {}
    }
    if (currentChild) {
        try { currentChild.kill(); } catch (_) {}
    }
    if (llamaServerState && llamaServerState.child) {
        try { llamaServerState.child.kill(); } catch (_) {}
        llamaServerState = null;
    }
    app.quit();
});
