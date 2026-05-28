const SYSTEM_MESSAGE = {
    role: 'system',
    content:
        '你是一个友善、专业的中文助手。可以进行日常寒暄与常识问答；回答简洁自然，不要编造事实。' +
        '请不要独占一行输出 user、assistant、SYSTEM 等角色标记（含 Markdown 加粗），不要复述对话模板或重复用户刚说的话；直接写出面向用户的正文。' +
        '当用户说「继续」「再来一个」等时，请给出新的笑话或新段落，不要重复上一轮助手已经输出过的全文或大段原文。',
};

/** 曾写入 localStorage 的旧文案；载入时替换为当前 SYSTEM_MESSAGE，避免沿用误导寒暄的旧指令 */
const LEGACY_SYSTEM_TEXT =
    '你是一个中文AI助手，只回答用户问题，不要编造无关内容。';

/** 未包含「勿输出 user/assistant」提示的旧版系统指令；载入时升级为当前 SYSTEM_MESSAGE */
const SYSTEM_CONTENT_BEFORE_ROLE_HINT =
    '你是一个友善、专业的中文助手。可以进行日常寒暄与常识问答；回答简洁自然，不要编造事实。';

/** 已含角色提示、尚未含「继续时不要重复上一轮」的旧文案 */
const SYSTEM_CONTENT_BEFORE_CONTINUE_HINT =
    '你是一个友善、专业的中文助手。可以进行日常寒暄与常识问答；回答简洁自然，不要编造事实。' +
    '请不要独占一行输出 user、assistant、SYSTEM 等角色标记（含 Markdown 加粗），不要复述对话模板或重复用户刚说的话；直接写出面向用户的正文。';

const LS_KEY = 'uclaw.v1.conversations';
const MAX_CONVERSATIONS = 100;
/** 发送给模型的最大对话轮数（user+assistant 各算 1 条），超出时保留 system + 最近 N 条 */
const MAX_CONTEXT_MESSAGES = 20;

const chatEl = document.getElementById('chat');
const inputEl = document.getElementById('input');
const sendBtn = document.getElementById('send-btn');
const selectEl = document.getElementById('active-provider');
const aiCsProviderEl = document.getElementById('ai-cs-provider');
const providerForms = document.getElementById('provider-forms');
const saveMsg = document.getElementById('save-msg');
const viewChat = document.getElementById('view-chat');
const viewAiCs = document.getElementById('view-ai-cs');
const viewSettings = document.getElementById('view-settings');
const viewMail = document.getElementById('view-mail');
const navChat = document.getElementById('nav-chat');
const navAiCs = document.getElementById('nav-ai-cs');
const navMail = document.getElementById('nav-mail');
const navSettings = document.getElementById('nav-settings');
const btnNewChat = document.getElementById('btn-new-chat');
const convListEl = document.getElementById('conv-list');
const convSearchEl = document.getElementById('conv-search');
const ctxMenu = document.getElementById('ctx-menu');

const aiCsMsgs = document.getElementById('ai-cs-msgs');
const aiCsInput = document.getElementById('ai-cs-input');
const aiCsSend = document.getElementById('ai-cs-send');
const aiCsSyncKnowledge = document.getElementById('ai-cs-sync-knowledge');
const aiCsScriptMgmt = document.getElementById('ai-cs-script-mgmt');
const aiCsScriptsOverlay = document.getElementById('ai-cs-scripts-overlay');
const aiCsScriptsClose = document.getElementById('ai-cs-scripts-close');
const aiCsScriptsTbody = document.getElementById('ai-cs-scripts-tbody');
const aiCsScriptsPagination = document.getElementById('ai-cs-scripts-pagination');
const aiCsScriptsPageLabel = document.getElementById('ai-cs-scripts-page-label');
const aiCsScriptsPagePrev = document.getElementById('ai-cs-scripts-page-prev');
const aiCsScriptsPageNext = document.getElementById('ai-cs-scripts-page-next');
const aiCsScriptsEmpty = document.getElementById('ai-cs-scripts-empty');
const aiCsScriptsTemplate = document.getElementById('ai-cs-scripts-template');
const aiCsScriptsImport = document.getElementById('ai-cs-scripts-import');
const aiCsScriptsFile = document.getElementById('ai-cs-scripts-file');
const aiCsScriptsPaneList = document.getElementById('ai-cs-scripts-pane-list');
const aiCsScriptsPaneEdit = document.getElementById('ai-cs-scripts-pane-edit');
const aiCsScriptEditQ = document.getElementById('ai-cs-script-edit-q');
const aiCsScriptEditS = document.getElementById('ai-cs-script-edit-s');
const aiCsScriptEditE = document.getElementById('ai-cs-script-edit-e');
const aiCsScriptEditSave = document.getElementById('ai-cs-script-edit-save');
const aiCsScriptEditCancel = document.getElementById('ai-cs-script-edit-cancel');

const loginOverlay = document.getElementById('login-overlay');
const accountModeLogin = document.getElementById('account-mode-login');
const accountModeProfile = document.getElementById('account-mode-profile');
const loginStatusEl = document.getElementById('login-status');
const loginUsername = document.getElementById('login-username');
const loginPassword = document.getElementById('login-password');
const loginSubmit = document.getElementById('login-submit');
const loginLogout = document.getElementById('login-logout');
const loginCancel = document.getElementById('login-cancel');
const loginMsg = document.getElementById('login-msg');
const erpAuthTitle = document.getElementById('erp-auth-title');
const erpAuthSubtitle = document.getElementById('erp-auth-subtitle');
const erpLoginStack = document.getElementById('erp-login-stack');
const erpRegisterStack = document.getElementById('erp-register-stack');
const loginTabPassword = document.getElementById('login-tab-password');
const loginTabSms = document.getElementById('login-tab-sms');
const erpPanePassword = document.getElementById('erp-pane-password');
const erpPaneMobile = document.getElementById('erp-pane-mobile');
const loginPhone = document.getElementById('login-phone');
const loginCaptchaInput = document.getElementById('login-captcha-input');
const loginCaptchaImg = document.getElementById('login-captcha-img');
const loginCaptchaKey = document.getElementById('login-captcha-key');
const loginCaptchaHit = document.getElementById('login-captcha-hit');
const loginSmsInput = document.getElementById('login-sms-input');
const loginBtnSms = document.getElementById('login-btn-sms');
const linkOpenRegister = document.getElementById('link-open-register');
const registerCancel = document.getElementById('register-cancel');
const registerSubmit = document.getElementById('register-submit');
const regUsername = document.getElementById('reg-username');
const regPassword = document.getElementById('reg-password');
const regPassword2 = document.getElementById('reg-password2');
const regNickname = document.getElementById('reg-nickname');
const regMobile = document.getElementById('reg-mobile');
const regCaptchaInput = document.getElementById('reg-captcha-input');
const regCaptchaImg = document.getElementById('reg-captcha-img');
const regCaptchaKey = document.getElementById('reg-captcha-key');
const regCaptchaHit = document.getElementById('reg-captcha-hit');
const regSmsInput = document.getElementById('reg-sms-input');
const regBtnSms = document.getElementById('reg-btn-sms');
const registerMsg = document.getElementById('register-msg');
const navUserBtn = document.getElementById('nav-user');
const navUserAvatarImg = document.getElementById('nav-user-avatar-img');
const navUserAvatarFallback = document.getElementById('nav-user-avatar-fallback');
const navUserLabel = document.getElementById('nav-user-label');

const profileNickname = document.getElementById('profile-nickname');
const profileSex = document.getElementById('profile-sex');
const profileBirthday = document.getElementById('profile-birthday');
const profileHeadImg = document.getElementById('profile-head-img');
const profileAvatarImg = document.getElementById('profile-avatar-img');
const profileAvatarFallback = document.getElementById('profile-avatar-fallback');
const profileAvatarHit = document.getElementById('profile-avatar-hit');
const avatarCropOverlay = document.getElementById('avatar-crop-overlay');
const avatarCropCanvas = document.getElementById('avatar-crop-canvas');
const avatarCropZoom = document.getElementById('avatar-crop-zoom');
const avatarCropCancel = document.getElementById('avatar-crop-cancel');
const avatarCropOk = document.getElementById('avatar-crop-ok');
const profileSave = document.getElementById('profile-save');
const profileMsg = document.getElementById('profile-msg');
const profileDisplayName = document.getElementById('profile-display-name');
const profileDisplaySub = document.getElementById('profile-display-sub');
const profileReadonlyMobile = document.getElementById('profile-readonly-mobile');
const pwdSms = document.getElementById('pwd-sms');
const pwdCaptchaInput = document.getElementById('pwd-captcha-input');
const pwdCaptchaImg = document.getElementById('pwd-captcha-img');
const pwdCaptchaKey = document.getElementById('pwd-captcha-key');
const pwdCaptchaHit = document.getElementById('pwd-captcha-hit');
const pwdBtnSms = document.getElementById('pwd-btn-sms');
const pwdNew = document.getElementById('pwd-new');
const pwdConfirm = document.getElementById('pwd-confirm');
const profilePasswordSubmit = document.getElementById('profile-password-submit');
const pwdOverlay = document.getElementById('pwd-overlay');
const pwdSheet = pwdOverlay
    ? pwdOverlay.querySelector('.pwd-sheet')
    : null;
const pwdClose = document.getElementById('pwd-close');
const pwdMsg = document.getElementById('pwd-msg');
const profileOpenPassword = document.getElementById('profile-open-password');

/** @type {string} */
let profileUserMobile = '';

const ERP_COPY = {
    titleLogin: 'PX-Claw',
    subLogin: '登录后进入 PX-Claw 工作台',
    titleReg: '用户注册',
    subReg: '请填写以下信息完成注册',
};

/** @type {'password'|'sms'} */
let loginTabMode = 'password';
/** @type {ReturnType<typeof setInterval> | null} */
let smsCooldownLoginId = null;
/** @type {ReturnType<typeof setInterval> | null} */
let smsCooldownRegId = null;
/** @type {ReturnType<typeof setInterval> | null} */
let smsCooldownPwdId = null;

/** @type {null | { baseUrl: string, token: string, user: { id?: number, username?: string, nickname?: string, role?: string, headImg?: string, sex?: number, birthday?: string, mobile?: string } }} */
let systemSession = null;

let appConfig = { lastProviderId: 'local', providers: [] };

/** @type {{ id: string, title: string, updatedAt: number, customTitle?: boolean, messages: Array<{role: string, content: string, createdAt?: number}> }[]} */
let conversations = [];
let activeId = null;
let ctxTargetId = null;

const LS_KEY_AI_CS = 'uclaw.v1.aiCsConversations';

/** AI 客服：空会话时仅界面展示的欢迎语（不写入 messages，避免进入模型上下文） */
const AI_CS_WELCOME_TEXT = '您好，有什么可以帮助您的吗？';

/** @type {{ id: string, title: string, updatedAt: number, customTitle?: boolean, messages: Array<{role: string, content: string, createdAt?: number, ragContext?: string}> }[]} */
let aiCsConversations = [];
let activeAiCsId = null;

/** @type {HTMLElement | null} */
const workspaceSplit = document.getElementById('workspace-split');
/** @type {HTMLElement | null} */
const convSidebarLabel = document.getElementById('conv-sidebar-label');

function generateId() {
    return 'c_' + Date.now() + '_' + Math.random().toString(36).slice(2, 10);
}

function getActiveConversation() {
    return conversations.find((c) => c.id === activeId) || null;
}

function conversationTitle(msgs) {
    const u = msgs.find((m) => m.role === 'user');
    if (u && u.content && u.content.trim()) {
        const t = u.content.trim().replace(/\s+/g, ' ');
        return t.length > 40 ? t.slice(0, 40) + '…' : t;
    }
    return '新对话';
}

function isAiCsViewActive() {
    return !!(viewAiCs && viewAiCs.classList.contains('view--active'));
}

function getActiveAiCsConversation() {
    return aiCsConversations.find((c) => c.id === activeAiCsId) || null;
}

function syncConvSidebarLabel() {
    if (!convSidebarLabel) return;
    convSidebarLabel.textContent = isAiCsViewActive() ? 'AI 客服历史' : '对话历史';
}

function aiCsConversationTitle(msgs) {
    const u = msgs.find((m) => m.role === 'user');
    if (u && u.content && u.content.trim()) {
        const t = u.content.trim().replace(/\s+/g, ' ');
        return t.length > 40 ? t.slice(0, 40) + '…' : t;
    }
    return '新客服会话';
}

function touchActiveAiCsMeta() {
    const c = getActiveAiCsConversation();
    if (!c) return;
    if (!c.customTitle) {
        c.title = aiCsConversationTitle(c.messages);
    }
    c.updatedAt = Date.now();
}

function migrateAiCsConversationTimes(c) {
    if (!c.messages || !c.messages.length) return;
    migrateConversationMessageTimes({
        messages: c.messages,
        updatedAt: c.updatedAt,
    });
}

function persistAiCsConversations() {
    try {
        localStorage.setItem(
            LS_KEY_AI_CS,
            JSON.stringify({
                v: 1,
                activeAiCsId,
                conversations: aiCsConversations,
            })
        );
    } catch (e) {
        console.error('persistAiCsConversations', e);
    }
}

function loadAiCsConversationsFromStorage() {
    try {
        const raw = localStorage.getItem(LS_KEY_AI_CS);
        if (!raw) return false;
        const data = JSON.parse(raw);
        if (!data || !Array.isArray(data.conversations)) return false;
        for (const c of data.conversations) {
            if (!c.id || !Array.isArray(c.messages)) continue;
            if (typeof c.customTitle !== 'boolean') c.customTitle = false;
            migrateAiCsConversationTimes(c);
        }
        aiCsConversations = data.conversations.filter(
            (c) => c.id && Array.isArray(c.messages)
        );
        activeAiCsId = data.activeAiCsId;
        if (
            activeAiCsId &&
            !aiCsConversations.find((cc) => cc.id === activeAiCsId)
        ) {
            activeAiCsId = aiCsConversations[0] ? aiCsConversations[0].id : null;
        }
        return aiCsConversations.length > 0;
    } catch (e) {
        console.error('loadAiCsConversationsFromStorage', e);
        return false;
    }
}

function ensureSeedAiCsConversation() {
    if (aiCsConversations.length) {
        if (!activeAiCsId) activeAiCsId = aiCsConversations[0].id;
        return;
    }
    const id = generateId();
    activeAiCsId = id;
    aiCsConversations = [
        {
            id,
            title: '新客服会话',
            updatedAt: Date.now(),
            customTitle: false,
            messages: [],
        },
    ];
}

function trimAiCsConversations() {
    if (aiCsConversations.length <= MAX_CONVERSATIONS) return;
    const byTime = [...aiCsConversations].sort((a, b) => b.updatedAt - a.updatedAt);
    const keep = new Set(byTime.slice(0, MAX_CONVERSATIONS).map((c) => c.id));
    aiCsConversations = aiCsConversations.filter((c) => keep.has(c.id));
    if (
        activeAiCsId &&
        !aiCsConversations.find((c) => c.id === activeAiCsId) &&
        aiCsConversations.length
    ) {
        activeAiCsId = aiCsConversations[0].id;
    }
}

async function flushAiCsSessionSnapshotRemote(conv) {
    if (!conv || !systemSession || !systemSession.token) return;
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/ai-cs/session/snapshot`;
    const msgs = (conv.messages || [])
        .filter((m) => m.role === 'user' || m.role === 'assistant')
        .map((m) => ({
            role: m.role,
            content: String(m.content || ''),
            ragContext:
                typeof m.ragContext === 'string' && m.ragContext.length
                    ? m.ragContext
                    : null,
            createdAt:
                typeof m.createdAt === 'number' && Number.isFinite(m.createdAt)
                    ? m.createdAt
                    : undefined,
        }));
    const body = {
        clientSessionId: conv.id,
        title: conv.title || '',
        providerId:
            (aiCsProviderEl && aiCsProviderEl.value) ||
            appConfig.lastProviderId ||
            '',
        clientUpdatedAt: conv.updatedAt,
        messages: msgs,
    };
    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                Authorization: systemSession.token,
            },
            body: JSON.stringify(body),
        });
        const text = await res.text();
        let data;
        try {
            data = JSON.parse(text);
        } catch {
            return;
        }
        if (data.code === 401) {
            applySessionExpiredUi(data.message || '登录已过期', false);
        }
    } catch {
        /* ignore 网络错误，仅本地仍有记录 */
    }
}

function renderAiCsFromMessages() {
    if (!aiCsMsgs) return;
    aiCsMsgs.innerHTML = '';
    const c = getActiveAiCsConversation();
    if (!c) return;
    const hasExchange = c.messages.some(
        (m) => m.role === 'user' || m.role === 'assistant'
    );
    if (!hasExchange) {
        const wrap = document.createElement('div');
        wrap.className = 'ai-cs-msg ai-cs-msg--assistant ai-cs-msg--welcome';
        const bubble = document.createElement('div');
        bubble.className = 'ai-cs-msg__bubble';
        bubble.textContent = AI_CS_WELCOME_TEXT;
        wrap.appendChild(bubble);
        aiCsMsgs.appendChild(wrap);
    }
    for (const m of c.messages) {
        if (m.role === 'user') {
            appendAiCsBubble('user', m.content, m.createdAt);
        } else if (m.role === 'assistant') {
            appendAiCsBubble(
                'assistant',
                stripReasoningArtifactsOnly(m.content || ''),
                m.createdAt
            );
        }
    }
    aiCsMsgs.scrollTop = aiCsMsgs.scrollHeight;
}

/** @param {{ focus?: boolean } | undefined} opts — 初始化批量创建会话时可设 focus:false，避免抢走主「对话」输入框焦点 */
function startNewAiCsConversation(opts) {
    const focus = !opts || opts.focus !== false;
    const id = generateId();
    aiCsConversations.unshift({
        id,
        title: '新客服会话',
        updatedAt: Date.now(),
        customTitle: false,
        messages: [],
    });
    trimAiCsConversations();
    activeAiCsId = id;
    renderConvList();
    renderAiCsFromMessages();
    persistAiCsConversations();
    if (aiCsInput) {
        aiCsInput.value = '';
        if (focus) {
            aiCsInput.focus();
        }
    }
}

function touchActiveMeta() {
    const c = getActiveConversation();
    if (!c) return;
    if (!c.customTitle) {
        c.title = conversationTitle(c.messages);
    }
    c.updatedAt = Date.now();
}

function persistConversations() {
    try {
        const payload = {
            v: 1,
            activeId,
            conversations,
        };
        localStorage.setItem(LS_KEY, JSON.stringify(payload));
    } catch (e) {
        console.error('persistConversations', e);
    }
}

function loadConversations() {
    try {
        const raw = localStorage.getItem(LS_KEY);
        if (!raw) return false;
        const data = JSON.parse(raw);
        if (!data || !Array.isArray(data.conversations) || !data.conversations.length) {
            return false;
        }
        for (const c of data.conversations) {
            if (!c.id || !Array.isArray(c.messages) || c.messages.length < 1) {
                continue;
            }
            if (typeof c.customTitle !== 'boolean') {
                c.customTitle = false;
            }
            const hasSystem = c.messages.some((m) => m.role === 'system');
            if (!hasSystem) {
                c.messages = [SYSTEM_MESSAGE, ...c.messages];
            } else if (
                c.messages[0] &&
                c.messages[0].role === 'system' &&
                c.messages[0].content === LEGACY_SYSTEM_TEXT
            ) {
                c.messages[0] = { ...SYSTEM_MESSAGE };
            } else if (
                c.messages[0] &&
                c.messages[0].role === 'system' &&
                c.messages[0].content === SYSTEM_CONTENT_BEFORE_ROLE_HINT
            ) {
                c.messages[0] = { ...SYSTEM_MESSAGE };
            } else if (
                c.messages[0] &&
                c.messages[0].role === 'system' &&
                c.messages[0].content === SYSTEM_CONTENT_BEFORE_CONTINUE_HINT
            ) {
                c.messages[0] = { ...SYSTEM_MESSAGE };
            }
            migrateConversationMessageTimes(c);
        }
        conversations = data.conversations.filter(
            (c) => c.id && Array.isArray(c.messages) && c.messages.length
        );
        activeId = data.activeId;
        if (!conversations.find((c) => c.id === activeId)) {
            activeId = conversations[0].id;
        }
        return conversations.length > 0;
    } catch (e) {
        console.error('loadConversations', e);
        return false;
    }
}

function ensureSeedConversation() {
    if (conversations.length) {
        if (!activeId) activeId = conversations[0].id;
        return;
    }
    const id = generateId();
    activeId = id;
    conversations = [
        {
            id,
            title: '新对话',
            updatedAt: Date.now(),
            customTitle: false,
            messages: [{ ...SYSTEM_MESSAGE }],
        },
    ];
}

function trimConversations() {
    if (conversations.length <= MAX_CONVERSATIONS) return;
    const byTime = [...conversations].sort((a, b) => b.updatedAt - a.updatedAt);
    const keep = new Set(
        byTime.slice(0, MAX_CONVERSATIONS).map((c) => c.id)
    );
    conversations = conversations.filter((c) => keep.has(c.id));
    if (!conversations.find((c) => c.id === activeId) && conversations.length) {
        activeId = conversations[0].id;
    }
}

function getSortedFilteredConversations() {
    const list = isAiCsViewActive() ? aiCsConversations : conversations;
    const q = (convSearchEl && convSearchEl.value
        ? convSearchEl.value
        : ''
    )
        .trim()
        .toLowerCase();
    const sorted = [...list].sort((a, b) => b.updatedAt - a.updatedAt);
    if (!q) {
        return sorted;
    }
    return sorted.filter((c) => {
        if ((c.title || '').toLowerCase().includes(q)) {
            return true;
        }
        for (const m of c.messages) {
            if (m.role === 'system') {
                continue;
            }
            if (String(m.content || '').toLowerCase().includes(q)) {
                return true;
            }
        }
        return false;
    });
}

function renderConvList() {
    convListEl.innerHTML = '';
    const sorted = getSortedFilteredConversations();
    const curActive = isAiCsViewActive() ? activeAiCsId : activeId;
    const q = (convSearchEl && convSearchEl.value
        ? convSearchEl.value
        : ''
    ).trim();
    if (sorted.length === 0 && q) {
        const empty = document.createElement('p');
        empty.className = 'conv-list-empty';
        empty.textContent = '没有匹配的会话';
        convListEl.appendChild(empty);
        return;
    }
    for (const c of sorted) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'conv-item';
        btn.dataset.id = c.id;
        if (c.id === curActive) btn.setAttribute('aria-current', 'true');
        const title = document.createElement('div');
        title.className = 'conv-item__title';
        title.textContent = c.title || '新对话';
        title.title = '双击重命名';
        title.addEventListener('dblclick', (e) => {
            e.stopPropagation();
            e.preventDefault();
            renameConversation(c.id);
        });
        const time = document.createElement('div');
        time.className = 'conv-item__time';
        const listTs =
            getConversationFirstUserMessageTime(c) ??
            (typeof c.updatedAt === 'number' && Number.isFinite(c.updatedAt)
                ? c.updatedAt
                : Date.now());
        time.textContent = formatMsgDateTime(listTs);
        btn.appendChild(title);
        btn.appendChild(time);
        btn.addEventListener('click', () => selectConversation(c.id));
        btn.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            showCtxMenu(e.clientX, e.clientY, c.id);
        });
        convListEl.appendChild(btn);
    }
}

function updateCtxMenuItemStates() {
    if (!ctxMenu) return;
    const blocking =
        isWaiting ||
        (isAiCsViewActive() && aiCsSend && aiCsSend.disabled);
    for (const btn of ctxMenu.querySelectorAll('button[data-action]')) {
        const a = btn.dataset.action;
        btn.disabled = blocking && a !== 'copy-title';
    }
}

function hideCtxMenu() {
    if (!ctxMenu) return;
    ctxMenu.hidden = true;
    ctxTargetId = null;
}

function showCtxMenu(clientX, clientY, id) {
    if (!ctxMenu) return;
    ctxTargetId = id;
    updateCtxMenuItemStates();
    ctxMenu.hidden = false;
    ctxMenu.style.left = '0px';
    ctxMenu.style.top = '0px';
    const w = ctxMenu.offsetWidth;
    const h = ctxMenu.offsetHeight;
    let left = clientX;
    let top = clientY;
    if (left + w > window.innerWidth - 8) {
        left = window.innerWidth - w - 8;
    }
    if (top + h > window.innerHeight - 8) {
        top = window.innerHeight - h - 8;
    }
    if (left < 8) {
        left = 8;
    }
    if (top < 8) {
        top = 8;
    }
    ctxMenu.style.left = left + 'px';
    ctxMenu.style.top = top + 'px';
}

/** 左侧列表：取该会话第一条用户消息时间（视为发起对话时刻）；无用户消息时用 null */
function getConversationFirstUserMessageTime(c) {
    if (!c.messages || !c.messages.length) return null;
    const firstUser = c.messages.find((m) => m.role === 'user');
    if (!firstUser) return null;
    const t = firstUser.createdAt;
    return typeof t === 'number' && Number.isFinite(t) ? t : null;
}

/** 消息气泡：年月日 + 时分秒（本地时区） */
function formatMsgDateTime(ts) {
    const t =
        typeof ts === 'number' && Number.isFinite(ts) ? ts : Date.now();
    const d = new Date(t);
    const p = (n) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
}

/**
 * 旧版 localStorage 无 createdAt 时，按会话内顺序用 updatedAt 倒推，便于显示与排序。
 */
function migrateConversationMessageTimes(c) {
    if (!c.messages || !c.messages.length) return;
    const base =
        typeof c.updatedAt === 'number' && Number.isFinite(c.updatedAt)
            ? c.updatedAt
            : Date.now();
    const nonSystem = c.messages.filter((m) => m.role !== 'system');
    const n = nonSystem.length;
    let k = 0;
    for (const m of c.messages) {
        if (m.role === 'system') continue;
        if (typeof m.createdAt !== 'number' || !Number.isFinite(m.createdAt)) {
            m.createdAt = base - (n - 1 - k) * 1000;
        }
        k += 1;
    }
}

function removeEmptyPlaceholder() {
    const el = chatEl.querySelector('.chat-empty');
    if (el) el.remove();
}

function renderEmptyPlaceholder() {
    const c = getActiveConversation();
    if (!c) return;
    const hasChat = c.messages.some(
        (m) => m.role === 'user' || m.role === 'assistant'
    );
    if (hasChat) return;
    const empty = document.createElement('div');
    empty.className = 'chat-empty';
    empty.innerHTML =
        '向模型发一条消息开始对话<br/><span class="chat-empty__hint">在下方输入，<kbd>Enter</kbd> 发送 · <kbd>Shift+Enter</kbd> 换行</span><br/><span class="chat-empty__hint"><kbd>Ctrl</kbd> + <kbd>N</kbd> 可快速新建对话</span>';
    chatEl.appendChild(empty);
}

function renderChatFromMessages() {
    chatEl.innerHTML = '';
    const c = getActiveConversation();
    if (!c) return;
    renderEmptyPlaceholder();
    for (const m of c.messages) {
        if (m.role === 'system') continue;
        if (m.role === 'user') {
            appendUserMessage(m.content, m.createdAt);
        } else if (m.role === 'assistant') {
            const wrap = document.createElement('div');
            wrap.className = 'msg msg--assistant';
            const meta = document.createElement('div');
            meta.className = 'msg-meta';
            meta.textContent =
                '助手 · ' + formatMsgDateTime(m.createdAt);
            const bubble = document.createElement('div');
            bubble.className = 'msg-bubble';
            bubble.textContent = stripReasoningArtifactsOnly(m.content);
            wrap.appendChild(meta);
            wrap.appendChild(bubble);
            chatEl.appendChild(wrap);
        }
    }
    scrollChatToBottom();
}

function sessionDisplayUser(s) {
    if (!s || !s.user) return '';
    const n = (s.user.nickname || '').trim();
    const u = (s.user.username || '').trim();
    const line = n || u || '用户';
    if (s.user.role === 'admin') {
        return `${line}（管理员）`;
    }
    return line;
}

function refreshNavUser() {
    if (!navUserLabel) return;
    const showNavFallback = (initial) => {
        if (!navUserAvatarImg || !navUserAvatarFallback) return;
        navUserAvatarImg.classList.remove('is-visible');
        navUserAvatarImg.removeAttribute('src');
        navUserAvatarFallback.classList.remove('is-hidden');
        navUserAvatarFallback.textContent = initial;
    };
    if (systemSession && systemSession.user) {
        const name = (
            systemSession.user.nickname ||
            systemSession.user.username ||
            '用户'
        ).trim();
        const initial = name.charAt(0) || '用';
        const url = (systemSession.user.headImg || '').trim();
        if (url && navUserAvatarImg && navUserAvatarFallback) {
            navUserAvatarImg.onerror = () => showNavFallback(initial);
            navUserAvatarImg.onload = () => {
                navUserAvatarImg.classList.add('is-visible');
                navUserAvatarFallback.classList.add('is-hidden');
            };
            navUserAvatarImg.src = headImgUrlWithAuthToken(url);
        } else {
            navUserAvatarImg.onerror = null;
            navUserAvatarImg.onload = null;
            showNavFallback(initial);
        }
        navUserLabel.textContent =
            name.length > 5 ? name.slice(0, 5) + '…' : name;
    } else {
        navUserAvatarImg.onerror = null;
        navUserAvatarImg.onload = null;
        showNavFallback('?');
        navUserLabel.textContent = '登录';
    }
}

function showAccountLoginMode() {
    if (accountModeLogin) accountModeLogin.hidden = false;
    if (accountModeProfile) accountModeProfile.hidden = true;
}

function showAccountProfileMode() {
    if (accountModeLogin) accountModeLogin.hidden = true;
    if (accountModeProfile) accountModeProfile.hidden = false;
}

/** 与 client/lib/systemAuth.js DEFAULT_BASE 一致 */
function getErpBaseUrl() {
    return 'http://127.0.0.1:9168';
}

/**
 * img 无法携带 Authorization；后端 GET /file/{code} 需在 query 中带 token（与 Header 等价）。
 * 存入资料的 headImg 仍为无 token 的 URL，仅在展示时拼接。
 */
function headImgUrlWithAuthToken(urlStr) {
    const raw = (urlStr || '').trim();
    if (!raw || !systemSession || !systemSession.token) return raw;
    try {
        const u = new URL(raw);
        const path = u.pathname.replace(/\/+$/, '');
        if (!/^\/file\/[A-Za-z0-9]+$/i.test(path)) return raw;
        if (!u.searchParams.has('token')) {
            u.searchParams.set('token', systemSession.token);
        }
        return u.href;
    } catch {
        return raw;
    }
}

const AVATAR_CROP_DISPLAY = 260;
const AVATAR_CROP_EXPORT = 400;

/** @type {null | { img: HTMLImageElement, fitScale: number, userZoom: number, panX: number, panY: number }} */
let avatarCropState = null;
/** @type {null | { lastX: number, lastY: number }} */
let avatarCropDrag = null;

function clampAvatarCropPan() {
    if (!avatarCropState) return;
    const s = avatarCropState;
    const scale = s.fitScale * s.userZoom;
    const dw = s.img.width * scale;
    const dh = s.img.height * scale;
    const r = AVATAR_CROP_DISPLAY / 2 - 2;
    let minPanX = r - dw / 2;
    let maxPanX = dw / 2 - r;
    let minPanY = r - dh / 2;
    let maxPanY = dh / 2 - r;
    if (minPanX > maxPanX) {
        const t = minPanX;
        minPanX = maxPanX;
        maxPanX = t;
    }
    if (minPanY > maxPanY) {
        const t = minPanY;
        minPanY = maxPanY;
        maxPanY = t;
    }
    s.panX = Math.min(Math.max(s.panX, minPanX), maxPanX);
    s.panY = Math.min(Math.max(s.panY, minPanY), maxPanY);
}

function drawAvatarCropCanvas() {
    if (!avatarCropCanvas || !avatarCropState) return;
    const s = avatarCropState;
    const size = AVATAR_CROP_DISPLAY;
    const cx = size / 2;
    const cy = size / 2;
    const r = size / 2 - 2;
    const dpr = Math.min(2, window.devicePixelRatio || 1);
    avatarCropCanvas.style.width = `${size}px`;
    avatarCropCanvas.style.height = `${size}px`;
    avatarCropCanvas.width = Math.floor(size * dpr);
    avatarCropCanvas.height = Math.floor(size * dpr);
    const ctx = avatarCropCanvas.getContext('2d');
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.clearRect(0, 0, size, size);
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.clip();
    const scale = s.fitScale * s.userZoom;
    const dw = s.img.width * scale;
    const dh = s.img.height * scale;
    const x = cx - dw / 2 + s.panX;
    const y = cy - dh / 2 + s.panY;
    ctx.drawImage(s.img, x, y, dw, dh);
    ctx.restore();
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(255,255,255,0.88)';
    ctx.lineWidth = 2;
    ctx.stroke();
}

function exportAvatarCropJpegBase64() {
    if (!avatarCropState) return '';
    const s = avatarCropState;
    const out = AVATAR_CROP_EXPORT;
    const canvas = document.createElement('canvas');
    canvas.width = out;
    canvas.height = out;
    const ctx = canvas.getContext('2d');
    const ratio = out / AVATAR_CROP_DISPLAY;
    const cx = out / 2;
    const cy = out / 2;
    const r = out / 2 - 2;
    ctx.beginPath();
    ctx.arc(cx, cy, r, 0, Math.PI * 2);
    ctx.clip();
    const scale = s.fitScale * s.userZoom * ratio;
    const dw = s.img.width * scale;
    const dh = s.img.height * scale;
    const x = cx - dw / 2 + s.panX * ratio;
    const y = cy - dh / 2 + s.panY * ratio;
    ctx.drawImage(s.img, x, y, dw, dh);
    const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
    const parts = dataUrl.split(',');
    return parts.length > 1 ? parts[1] : '';
}

function closeAvatarCropModal() {
    if (!avatarCropOverlay) return;
    avatarCropOverlay.classList.remove('is-open');
    avatarCropOverlay.setAttribute('aria-hidden', 'true');
    avatarCropState = null;
    avatarCropDrag = null;
    if (avatarCropZoom) avatarCropZoom.value = '100';
}

function openAvatarCropModal(dataUrl) {
    if (!avatarCropOverlay || !avatarCropCanvas || !dataUrl) return;
    const img = new Image();
    img.onload = () => {
        const size = AVATAR_CROP_DISPLAY;
        const r = size / 2 - 2;
        const fitScale = Math.max((2 * r) / img.width, (2 * r) / img.height);
        avatarCropState = {
            img,
            fitScale,
            userZoom: 1,
            panX: 0,
            panY: 0,
        };
        if (avatarCropZoom) avatarCropZoom.value = '100';
        clampAvatarCropPan();
        drawAvatarCropCanvas();
        avatarCropOverlay.classList.add('is-open');
        avatarCropOverlay.setAttribute('aria-hidden', 'false');
    };
    img.onerror = () => {
        if (profileMsg) {
            profileMsg.textContent = '图片无法加载';
            profileMsg.className = 'profile-msg is-err';
        }
    };
    img.src = dataUrl;
}

function captchaDataUrl(raw) {
    if (!raw) return '';
    const s = String(raw).trim();
    if (s.startsWith('data:')) return s;
    return `data:image/gif;base64,${s}`;
}

function clearSmsCooldown(which) {
    if (which === 'login') {
        if (smsCooldownLoginId) {
            clearInterval(smsCooldownLoginId);
            smsCooldownLoginId = null;
        }
        if (loginBtnSms) {
            loginBtnSms.disabled = false;
            loginBtnSms.textContent = '获取验证码';
        }
    }
    if (which === 'reg') {
        if (smsCooldownRegId) {
            clearInterval(smsCooldownRegId);
            smsCooldownRegId = null;
        }
        if (regBtnSms) {
            regBtnSms.disabled = false;
            regBtnSms.textContent = '获取验证码';
        }
    }
    if (which === 'pwd') {
        if (smsCooldownPwdId) {
            clearInterval(smsCooldownPwdId);
            smsCooldownPwdId = null;
        }
        if (pwdBtnSms) {
            pwdBtnSms.disabled = false;
            pwdBtnSms.textContent = '获取验证码';
        }
    }
}

function startSmsCooldown(btn, seconds) {
    if (!btn) return null;
    let left = seconds;
    const orig = btn.textContent;
    btn.disabled = true;
    btn.textContent = `${left}s`;
    const id = setInterval(() => {
        left -= 1;
        if (left <= 0) {
            clearInterval(id);
            btn.disabled = false;
            btn.textContent = orig;
        } else {
            btn.textContent = `${left}s`;
        }
    }, 1000);
    return id;
}

async function ipcLoadCaptcha(baseUrl) {
    if (!window.uClaw || !window.uClaw.systemCaptchaGet) {
        throw new Error('客户端未就绪');
    }
    const r = await window.uClaw.systemCaptchaGet({ baseUrl });
    if (!r.ok) {
        throw new Error(r.error || '获取图形验证码失败');
    }
    return r.data;
}

async function refreshLoginCaptcha() {
    const baseUrl = getErpBaseUrl();
    try {
        const data = await ipcLoadCaptcha(baseUrl);
        if (loginCaptchaKey) loginCaptchaKey.value = (data && data.key) || '';
        if (loginCaptchaImg && data && data.image) {
            loginCaptchaImg.src = captchaDataUrl(data.image);
            loginCaptchaImg.hidden = false;
        }
        if (loginCaptchaInput) loginCaptchaInput.value = '';
    } catch (e) {
        if (loginMsg) {
            loginMsg.textContent = (e && e.message) || String(e);
            loginMsg.className = 'erp-msg login-msg is-err';
        }
    }
}

async function refreshRegCaptcha() {
    const baseUrl = getErpBaseUrl();
    try {
        const data = await ipcLoadCaptcha(baseUrl);
        if (regCaptchaKey) regCaptchaKey.value = (data && data.key) || '';
        if (regCaptchaImg && data && data.image) {
            regCaptchaImg.src = captchaDataUrl(data.image);
            regCaptchaImg.hidden = false;
        }
        if (regCaptchaInput) regCaptchaInput.value = '';
    } catch (e) {
        if (registerMsg) {
            registerMsg.textContent = (e && e.message) || String(e);
            registerMsg.className = 'erp-msg is-err';
        }
    }
}

async function refreshPwdCaptcha() {
    const baseUrl = getErpBaseUrl();
    try {
        const data = await ipcLoadCaptcha(baseUrl);
        if (pwdCaptchaKey) pwdCaptchaKey.value = (data && data.key) || '';
        if (pwdCaptchaImg && data && data.image) {
            pwdCaptchaImg.src = captchaDataUrl(data.image);
            pwdCaptchaImg.hidden = false;
        }
        if (pwdCaptchaInput) pwdCaptchaInput.value = '';
    } catch (e) {
        if (pwdMsg) {
            pwdMsg.textContent = (e && e.message) || String(e);
            pwdMsg.className = 'profile-msg is-err';
        }
    }
}

function setLoginTab(mode) {
    loginTabMode = mode === 'sms' ? 'sms' : 'password';
    if (loginTabPassword && loginTabSms) {
        loginTabPassword.classList.toggle('is-active', loginTabMode === 'password');
        loginTabSms.classList.toggle('is-active', loginTabMode === 'sms');
        loginTabPassword.setAttribute(
            'aria-selected',
            loginTabMode === 'password' ? 'true' : 'false'
        );
        loginTabSms.setAttribute(
            'aria-selected',
            loginTabMode === 'sms' ? 'true' : 'false'
        );
    }
    if (erpPanePassword) {
        erpPanePassword.hidden = loginTabMode !== 'password';
    }
    if (erpPaneMobile) {
        erpPaneMobile.hidden = loginTabMode !== 'sms';
    }
    if (loginTabMode === 'sms') {
        refreshLoginCaptcha();
    }
}

function showErpLoginView() {
    if (erpLoginStack) erpLoginStack.hidden = false;
    if (erpRegisterStack) erpRegisterStack.hidden = true;
    if (erpAuthTitle) erpAuthTitle.textContent = ERP_COPY.titleLogin;
    if (erpAuthSubtitle) erpAuthSubtitle.textContent = ERP_COPY.subLogin;
}

function showErpRegisterView() {
    if (erpLoginStack) erpLoginStack.hidden = true;
    if (erpRegisterStack) erpRegisterStack.hidden = false;
    if (erpAuthTitle) erpAuthTitle.textContent = ERP_COPY.titleReg;
    if (erpAuthSubtitle) erpAuthSubtitle.textContent = ERP_COPY.subReg;
    if (registerMsg) {
        registerMsg.textContent = '';
        registerMsg.className = 'erp-msg';
    }
    refreshRegCaptcha();
}

function clearErpLoginFields() {
    if (loginPhone) loginPhone.value = '';
    if (loginCaptchaInput) loginCaptchaInput.value = '';
    if (loginSmsInput) loginSmsInput.value = '';
    if (loginCaptchaKey) loginCaptchaKey.value = '';
    if (loginCaptchaImg) {
        loginCaptchaImg.removeAttribute('src');
        loginCaptchaImg.hidden = true;
    }
}

function clearRegisterFields() {
    if (regUsername) regUsername.value = '';
    if (regPassword) regPassword.value = '';
    if (regPassword2) regPassword2.value = '';
    if (regNickname) regNickname.value = '';
    if (regMobile) regMobile.value = '';
    if (regCaptchaInput) regCaptchaInput.value = '';
    if (regSmsInput) regSmsInput.value = '';
    if (regCaptchaKey) regCaptchaKey.value = '';
    if (regCaptchaImg) {
        regCaptchaImg.removeAttribute('src');
        regCaptchaImg.hidden = true;
    }
}

function formatBirthdayInput(v) {
    if (v == null || v === '') return '';
    if (typeof v === 'string') {
        const s = v.trim();
        if (/^\d{4}-\d{2}-\d{2}/.test(s)) return s.slice(0, 10);
        return s;
    }
    if (typeof v === 'number') {
        const d = new Date(v);
        if (!Number.isNaN(d.getTime())) {
            return d.toISOString().slice(0, 10);
        }
    }
    return '';
}

function updateProfileAvatarPreview(headImgUrl, displayName) {
    if (!profileAvatarImg || !profileAvatarFallback) return;
    const name = (displayName || '用户').trim();
    const initial = name.charAt(0) || '?';
    const url = (headImgUrl || '').trim();
    if (url) {
        profileAvatarImg.onerror = () => {
            profileAvatarImg.removeAttribute('src');
            profileAvatarImg.hidden = true;
            profileAvatarFallback.hidden = false;
            profileAvatarFallback.textContent = initial;
        };
        profileAvatarImg.onload = () => {
            profileAvatarImg.hidden = false;
            profileAvatarFallback.hidden = true;
        };
        profileAvatarImg.src = headImgUrlWithAuthToken(url);
        if (profileAvatarImg.complete && profileAvatarImg.naturalWidth) {
            profileAvatarImg.hidden = false;
            profileAvatarFallback.hidden = true;
        }
    } else {
        profileAvatarImg.removeAttribute('src');
        profileAvatarImg.hidden = true;
        profileAvatarFallback.hidden = false;
        profileAvatarFallback.textContent = initial;
    }
}

function syncPasswordSectionState(mobile) {
    const ok = /^1[3-9]\d{9}$/.test((mobile || '').trim());
    for (const el of [
        profilePasswordSubmit,
        pwdSms,
        pwdCaptchaInput,
        pwdCaptchaHit,
        pwdBtnSms,
        pwdNew,
        pwdConfirm,
    ]) {
        if (el) el.disabled = !ok;
    }
    if (profileOpenPassword) {
        if (ok) {
            profileOpenPassword.removeAttribute('aria-disabled');
            profileOpenPassword.classList.remove('is-disabled');
        } else {
            profileOpenPassword.setAttribute('aria-disabled', 'true');
            profileOpenPassword.classList.add('is-disabled');
        }
    }
}

function clearPasswordFields() {
    if (pwdSms) pwdSms.value = '';
    if (pwdCaptchaInput) pwdCaptchaInput.value = '';
    if (pwdCaptchaKey) pwdCaptchaKey.value = '';
    if (pwdCaptchaImg) {
        pwdCaptchaImg.removeAttribute('src');
        pwdCaptchaImg.hidden = true;
    }
    if (pwdNew) pwdNew.value = '';
    if (pwdConfirm) pwdConfirm.value = '';
}

function validateNewPasswordLocal(p) {
    if (!p || p.length < 8) {
        return '新密码至少 8 位';
    }
    if (!/[a-zA-Z]/.test(p) || !/\d/.test(p)) {
        return '新密码须同时包含字母与数字';
    }
    return '';
}

function fillProfileForm(user) {
    if (!user) return;
    profileUserMobile = (user.mobile && String(user.mobile).trim()) || '';
    if (profileReadonlyMobile) {
        profileReadonlyMobile.textContent = profileUserMobile || '未绑定手机号';
    }
    syncPasswordSectionState(profileUserMobile);

    if (profileDisplayName) {
        profileDisplayName.textContent = (
            user.nickname ||
            user.username ||
            '—'
        ).trim();
    }
    if (profileDisplaySub) {
        const u = (user.username || '').trim();
        const roleLabel = user.role === 'admin' ? '管理员' : '普通用户';
        profileDisplaySub.textContent = u ? `${u} · ${roleLabel}` : roleLabel;
    }
    if (profileNickname) {
        profileNickname.value = user.nickname || '';
    }
    if (profileSex) {
        const s = user.sex;
        profileSex.value =
            s === 1 || s === 2 ? String(s) : '';
    }
    if (profileBirthday) {
        profileBirthday.value = formatBirthdayInput(user.birthday);
    }
    if (profileHeadImg) {
        profileHeadImg.value = user.headImg || '';
    }
    const disp =
        (user.nickname || user.username || '用户').trim();
    updateProfileAvatarPreview(user.headImg, disp);
}

async function loadProfileFromServer() {
    if (!window.uClaw || !window.uClaw.fetchUserProfile) return;
    profileMsg.textContent = '';
    profileMsg.className = 'profile-msg';
    const r = await window.uClaw.fetchUserProfile();
    if (r.sessionExpired) {
        applySessionExpiredUi(r.error, true);
        profileMsg.textContent = r.error || '登录已过期，请重新登录';
        profileMsg.className = 'profile-msg is-err';
        return;
    }
    if (r.ok && r.user) {
        systemSession = await window.uClaw.getSystemSession();
        fillProfileForm(r.user);
        refreshNavUser();
    } else {
        profileMsg.textContent = r.error || '加载资料失败';
        profileMsg.classList.add('is-err');
    }
}

function collectProfilePayload() {
    const nickname = profileNickname ? profileNickname.value.trim() : '';
    const headImg = profileHeadImg ? profileHeadImg.value.trim() : '';
    const birthday = profileBirthday ? profileBirthday.value.trim() : '';
    const sexVal = profileSex ? profileSex.value : '';
    const payload = { nickname, headImg };
    if (birthday) payload.birthday = birthday;
    if (sexVal !== '') payload.sex = sexVal;
    return payload;
}

function openLoginModal() {
    if (!loginOverlay) return;
    closePwdSheet();
    if (loginMsg) {
        loginMsg.textContent = '';
        loginMsg.className = 'erp-msg login-msg';
    }
    if (profileMsg) {
        profileMsg.textContent = '';
        profileMsg.className = 'profile-msg';
    }
    if (systemSession) {
        if (loginStatusEl) loginStatusEl.hidden = true;
        showAccountProfileMode();
        clearPasswordFields();
        loadProfileFromServer();
    } else {
        showAccountLoginMode();
        showErpLoginView();
        setLoginTab('password');
        clearSmsCooldown('login');
        clearSmsCooldown('reg');
        if (loginStatusEl) loginStatusEl.hidden = true;
    }
    loginOverlay.classList.add('is-open');
    loginOverlay.setAttribute('aria-hidden', 'false');
    if (systemSession) {
        if (profileNickname) profileNickname.focus();
    } else if (loginTabMode === 'sms' && loginPhone) {
        loginPhone.focus();
    } else if (loginUsername) {
        loginUsername.focus();
    }
}

function closePwdSheet() {
    if (!pwdOverlay || !pwdOverlay.classList.contains('is-open')) return;
    pwdOverlay.classList.remove('is-open');
    pwdOverlay.setAttribute('aria-hidden', 'true');
    clearSmsCooldown('pwd');
    if (pwdMsg) {
        pwdMsg.textContent = '';
        pwdMsg.className = 'profile-msg';
    }
    clearPasswordFields();
}

function openPwdSheet() {
    if (!pwdOverlay) return;
    if (pwdMsg) {
        pwdMsg.textContent = '';
        pwdMsg.className = 'profile-msg';
    }
    clearPasswordFields();
    pwdOverlay.classList.add('is-open');
    pwdOverlay.setAttribute('aria-hidden', 'false');
    syncPasswordSectionState(profileUserMobile);
    const ok = /^1[3-9]\d{9}$/.test((profileUserMobile || '').trim());
    if (!ok && pwdMsg) {
        pwdMsg.textContent =
            '当前账号未绑定有效手机号，无法修改密码';
        pwdMsg.className = 'profile-msg is-err';
    }
    if (ok) {
        clearSmsCooldown('pwd');
        void refreshPwdCaptcha();
        if (pwdCaptchaInput) {
            pwdCaptchaInput.focus();
        } else if (pwdSms) {
            pwdSms.focus();
        }
    } else if (pwdClose) {
        pwdClose.focus();
    }
}

function closeLoginModal() {
    closePwdSheet();
    if (!loginOverlay) return;
    loginOverlay.classList.remove('is-open');
    loginOverlay.setAttribute('aria-hidden', 'true');
    if (loginMsg) {
        loginMsg.textContent = '';
        loginMsg.className = 'erp-msg login-msg';
    }
    if (loginPassword) loginPassword.value = '';
    clearErpLoginFields();
    clearRegisterFields();
    clearSmsCooldown('login');
    clearSmsCooldown('reg');
    showErpLoginView();
    setLoginTab('password');
    if (registerMsg) {
        registerMsg.textContent = '';
        registerMsg.className = 'erp-msg';
    }
}

/** 服务端 token 失效（401）：清空本地展示状态，可选弹出登录层 */
function applySessionExpiredUi(message, openModal) {
    closePwdSheet();
    systemSession = null;
    profileUserMobile = '';
    clearPasswordFields();
    refreshNavUser();
    showAccountLoginMode();
    const msg = message || '登录已过期，请重新登录';
    if (openModal) {
        openLoginModal();
    }
    if (loginMsg) {
        loginMsg.textContent = msg;
        loginMsg.className = 'erp-msg login-msg is-err';
    }
}

async function loadSystemSessionFromMain() {
    if (!window.uClaw || !window.uClaw.getSystemSession) return;
    systemSession = await window.uClaw.getSystemSession();
    refreshNavUser();
}

function setMainView(which) {
    const map = [
        ['chat', viewChat, navChat],
        ['ai-cs', viewAiCs, navAiCs],
        ['mail', viewMail, navMail],
        ['settings', viewSettings, navSettings],
    ];
    for (const [key, vEl, nEl] of map) {
        const on = which === key;
        if (vEl) {
            vEl.classList.toggle('view--active', on);
            // 隐藏视图仍可能通过 Tab 抢到焦点，导致在非「对话」页误触发主对话 send
            vEl.inert = !on;
            if (on) {
                vEl.removeAttribute('aria-hidden');
            } else {
                vEl.setAttribute('aria-hidden', 'true');
            }
        }
        if (nEl) {
            nEl.setAttribute('aria-pressed', on ? 'true' : 'false');
        }
    }
    const ae = document.activeElement;
    if (ae instanceof HTMLElement) {
        const host = ae.closest('.view');
        if (host && host.inert) {
            ae.blur();
        }
    }
    if (which === 'settings' && saveMsg) {
        saveMsg.textContent = '';
    }
    if (workspaceSplit) {
        workspaceSplit.hidden = which === 'settings' || which === 'mail';
    }
    syncConvSidebarLabel();
    if (which === 'chat') {
        renderConvList();
        renderChatFromMessages();
        if (inputEl) {
            setTimeout(() => inputEl.focus(), 0);
        }
    } else if (which === 'ai-cs') {
        renderConvList();
        renderAiCsFromMessages();
        runAiCsKnowledgeSync({ silentSuccess: true });
        if (aiCsInput) {
            setTimeout(() => aiCsInput.focus(), 0);
        }
    } else if (which === 'mail') {
        refreshMailPanel().catch((e) => console.error(e));
    }
}

function scrollChatToBottom() {
    requestAnimationFrame(() => {
        chatEl.scrollTop = chatEl.scrollHeight;
    });
}

/** AI 客服：知识库走服务端 POST /chat/rag-context，推理走与本站「对话」相同的本地 IPC sendPrompt */

/** 与服务端 ChatController.buildPrompt 文案一致（勿随意改措辞） */
function buildAiCsPrompt(context, question) {
    const c = context != null ? String(context) : '';
    const q = question != null ? String(question) : '';
    return (
        '你是一个客服助手，只能根据以下资料回答。\n' +
            '如果资料中没有相关信息，请说「暂时没有相关信息」。\n\n' +
            '【资料】\n' +
            c +
            '\n\n' +
            '【问题】\n' +
            q
    );
}

/** @returns {{ retrievedContext: string }} */
async function apiPostRagContext(question) {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/rag-context`;
    const res = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            Authorization: systemSession.token,
        },
        body: JSON.stringify({ question }),
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        const snippet = text.length > 120 ? text.slice(0, 120) + '…' : text;
        throw new Error(`检索异常：${snippet}`);
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '检索失败');
    }
    return data.data;
}

/** @returns {HTMLElement | undefined} */
function appendAiCsBubble(role, text, createdAt) {
    if (!aiCsMsgs) return undefined;
    const wrap = document.createElement('div');
    wrap.className = `ai-cs-msg ai-cs-msg--${role}`;
    if (
        createdAt != null &&
        typeof createdAt === 'number' &&
        Number.isFinite(createdAt) &&
        (role === 'user' || role === 'assistant')
    ) {
        const meta = document.createElement('div');
        meta.className = 'ai-cs-msg__meta';
        meta.textContent =
            (role === 'user' ? '我' : '助手') + ' · ' + formatMsgDateTime(createdAt);
        wrap.appendChild(meta);
    }
    const bubble = document.createElement('div');
    bubble.className = 'ai-cs-msg__bubble';
    bubble.textContent = text;
    wrap.appendChild(bubble);
    aiCsMsgs.appendChild(wrap);
    aiCsMsgs.scrollTop = aiCsMsgs.scrollHeight;
    return wrap;
}

let aiCsLoadingEl = null;
let aiCsAssistantBuffer = '';

function clearAiCsLoadingBubble() {
    if (aiCsLoadingEl && aiCsLoadingEl.parentNode) {
        aiCsLoadingEl.remove();
    }
    aiCsLoadingEl = null;
}

/** 单行「处理中…」提示气泡 */
function showAiCsLoadingBubble(label) {
    clearAiCsLoadingBubble();
    if (!aiCsMsgs) return;
    const wrap = document.createElement('div');
    wrap.className = 'ai-cs-msg ai-cs-msg--hint ai-cs-msg--loading-msg';
    const bubble = document.createElement('div');
    bubble.className = 'ai-cs-msg__bubble';
    bubble.textContent = label;
    wrap.appendChild(bubble);
    aiCsMsgs.appendChild(wrap);
    aiCsMsgs.scrollTop = aiCsMsgs.scrollHeight;
    aiCsLoadingEl = wrap;
}

function warmupLocalProviderIfNeeded(providerId) {
    const p =
        providerId &&
        Array.isArray(appConfig.providers) &&
        appConfig.providers.find((x) => x.id === providerId);
    if (p && p.kind === 'local' && window.uClaw && window.uClaw.warmupLocal) {
        window.uClaw.warmupLocal(providerId);
    }
}

async function apiImportAiKnowledge(file) {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/knowledge/import`;
    const fd = new FormData();
    fd.append('file', file, file.name || 'scripts.xlsx');
    const res = await fetch(url, {
        method: 'POST',
        headers: {
            Authorization: systemSession.token,
            Accept: 'application/json',
        },
        body: fd,
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        const snippet = text.length > 120 ? text.slice(0, 120) + '…' : text;
        throw new Error(`上传异常：${snippet}`);
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '导入失败');
    }
    const inserted =
        data.data && typeof data.data.inserted === 'number' ? data.data.inserted : 0;
    const updated =
        data.data && typeof data.data.updated === 'number' ? data.data.updated : 0;
    return {
        inserted,
        updated,
        message: typeof data.message === 'string' ? data.message : '',
    };
}

/** 将库中话术同步到服务端 faq 文件并重载 Python FAISS（与话术保存后逻辑一致）。 */
async function apiSyncChatKnowledge() {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/knowledge/sync-faq`;
    const res = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            Authorization: systemSession.token,
        },
        body: '{}',
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        const snippet = text.length > 120 ? text.slice(0, 120) + '…' : text;
        throw new Error(`同步话术响应异常：${snippet}`);
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '同步失败');
    }
}

/** @param {{ silentSuccess?: boolean }} [opts] 进入 AI 客服页时用 silentSuccess 避免每次刷一条提示 */
async function runAiCsKnowledgeSync(opts) {
    const silentOk = !!(opts && opts.silentSuccess);
    if (!systemSession || !systemSession.token) {
        if (!silentOk) {
            appendAiCsBubble('hint', '请先登录平台账号后再同步话术。');
        }
        return;
    }
    if (aiCsSyncKnowledge) aiCsSyncKnowledge.disabled = true;
    try {
        await apiSyncChatKnowledge();
        if (!silentOk) {
            appendAiCsBubble('hint', '话术已与检索服务同步。');
        }
    } catch (e) {
        if (e && e.sessionExpired) {
            applySessionExpiredUi(e.message, true);
            return;
        }
        const msg =
            e && typeof e.message === 'string' && e.message.trim()
                ? e.message
                : '同步失败';
        appendAiCsBubble('hint', `话术同步失败：${msg}`);
    } finally {
        if (aiCsSyncKnowledge) aiCsSyncKnowledge.disabled = false;
    }
}

/** AI 话术管理弹层缓存（当前页表格行，与话术管理列表同源） */
const AI_CS_SCRIPTS_PAGE_SIZE = 15;
let aiCsScriptsPageCurrent = 1;
let aiCsScriptsCache = [];

/** @param {unknown} isoOrStr */
function formatAiCsImportTime(isoOrStr) {
    if (isoOrStr == null || isoOrStr === '') {
        return '—';
    }
    const d = new Date(typeof isoOrStr === 'number' ? isoOrStr : String(isoOrStr));
    if (Number.isNaN(d.getTime())) {
        return String(isoOrStr);
    }
    return d.toLocaleString('zh-CN', { hour12: false });
}

/** @returns {Promise<{ records: Array<{ id:number, question?:string, scriptText?:string, supplement?:string, importTime?:string, importUsername?:string }>, total: number, size: number, current: number, pages: number }>} */
async function apiGetChatScriptsPage(current, size) {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const c = Math.max(1, Number(current) || 1);
    const s = Math.min(100, Math.max(1, Number(size) || 20));
    const url = `${root}/chat/knowledge/scripts?current=${encodeURIComponent(String(c))}&size=${encodeURIComponent(String(s))}`;
    const res = await fetch(url, {
        method: 'GET',
        headers: {
            Authorization: systemSession.token,
            Accept: 'application/json',
        },
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        const snippet = text.length > 120 ? text.slice(0, 120) + '…' : text;
        throw new Error(`加载话术列表异常：${snippet}`);
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '加载失败');
    }
    const d = data.data;
    if (!d || typeof d !== 'object') {
        return {
            records: [],
            total: 0,
            size: s,
            current: c,
            pages: 0,
        };
    }
    const rec = Array.isArray(d.records) ? d.records : [];
    return {
        records: rec,
        total: typeof d.total === 'number' ? d.total : Number(d.total) || 0,
        size: typeof d.size === 'number' ? d.size : Number(d.size) || s,
        current: typeof d.current === 'number' ? d.current : Number(d.current) || c,
        pages: typeof d.pages === 'number' ? d.pages : Number(d.pages) || 0,
    };
}

async function apiDeleteChatScript(id) {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/knowledge/scripts/${encodeURIComponent(id)}`;
    const res = await fetch(url, {
        method: 'DELETE',
        headers: {
            Authorization: systemSession.token,
            Accept: 'application/json',
        },
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        throw new Error('删除响应异常');
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '删除失败');
    }
}

async function apiUpdateChatScript(id, body) {
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/knowledge/scripts/${encodeURIComponent(id)}`;
    const res = await fetch(url, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
            Authorization: systemSession.token,
        },
        body: JSON.stringify(body),
    });
    const text = await res.text();
    let data;
    try {
        data = JSON.parse(text);
    } catch {
        throw new Error('保存响应异常');
    }
    if (data.code === 401) {
        const err = new Error(data.message || '登录已过期');
        err.sessionExpired = true;
        throw err;
    }
    if (!data.success || data.code !== 200) {
        throw new Error(data.message || '保存失败');
    }
}

function showAiCsScriptsListPane() {
    if (!aiCsScriptsPaneList || !aiCsScriptsPaneEdit) return;
    aiCsScriptsPaneList.hidden = false;
    aiCsScriptsPaneEdit.hidden = true;
}

function showAiCsScriptsEditPane() {
    if (!aiCsScriptsPaneList || !aiCsScriptsPaneEdit) return;
    aiCsScriptsPaneList.hidden = true;
    aiCsScriptsPaneEdit.hidden = false;
}

/** @type {number | null} */
let editingAiCsScriptId = null;

function openAiCsScriptsEdit(id) {
    const row =
        aiCsScriptsCache &&
        aiCsScriptsCache.find((r) => String(r.id) === String(id));
    if (!row) return;
    editingAiCsScriptId = Number(row.id);
    if (aiCsScriptEditQ) aiCsScriptEditQ.value = row.question || '';
    if (aiCsScriptEditS) aiCsScriptEditS.value = row.scriptText || '';
    if (aiCsScriptEditE) aiCsScriptEditE.value = row.supplement != null ? String(row.supplement) : '';
    showAiCsScriptsEditPane();
}

/** @param {{ total: number, current: number, pages: number }} data */
function updateAiCsScriptsPaginationBar(data) {
    if (!aiCsScriptsPagination || !aiCsScriptsPageLabel || !aiCsScriptsPagePrev || !aiCsScriptsPageNext) {
        return;
    }
    const total = typeof data.total === 'number' ? data.total : Number(data.total) || 0;
    const current = typeof data.current === 'number' ? data.current : Number(data.current) || 1;
    const pages = typeof data.pages === 'number' ? data.pages : Number(data.pages) || 1;
    if (total <= 0) {
        aiCsScriptsPagination.hidden = true;
        return;
    }
    aiCsScriptsPagination.hidden = false;
    aiCsScriptsPageLabel.textContent = `共 ${total} 条 · 第 ${current} / ${Math.max(1, pages)} 页`;
    aiCsScriptsPagePrev.disabled = current <= 1;
    aiCsScriptsPageNext.disabled = current >= pages || pages <= 1;
}

async function refreshAiCsScriptTable() {
    if (!aiCsScriptsTbody) return;
    aiCsScriptsTbody.textContent = '';
    try {
        let pg = await apiGetChatScriptsPage(aiCsScriptsPageCurrent, AI_CS_SCRIPTS_PAGE_SIZE);
        const total = pg.total || 0;
        if (total > 0 && pg.records && pg.records.length === 0 && aiCsScriptsPageCurrent > 1) {
            aiCsScriptsPageCurrent -= 1;
            pg = await apiGetChatScriptsPage(aiCsScriptsPageCurrent, AI_CS_SCRIPTS_PAGE_SIZE);
        }
        const list = pg.records || [];
        aiCsScriptsCache = list;
        updateAiCsScriptsPaginationBar(pg);
        if (!list.length && aiCsScriptsEmpty) {
            aiCsScriptsEmpty.hidden = false;
        } else if (aiCsScriptsEmpty) {
            aiCsScriptsEmpty.hidden = true;
        }
        for (const row of list) {
            const tr = document.createElement('tr');

            const tdQ = document.createElement('td');
            tdQ.className = 'script-mgmt-table__cell--clip script-mgmt-table__cell--wide';
            tdQ.title = row.question || '';
            tdQ.textContent = row.question || '';
            tr.appendChild(tdQ);

            const tdS = document.createElement('td');
            tdS.className = 'script-mgmt-table__cell--clip script-mgmt-table__cell--wide';
            tdS.title = row.scriptText || '';
            tdS.textContent = row.scriptText || '';
            tr.appendChild(tdS);

            const tdE = document.createElement('td');
            tdE.className = 'script-mgmt-table__cell--clip';
            tdE.title = row.supplement || '';
            tdE.textContent = row.supplement || '—';
            tr.appendChild(tdE);

            const tdT = document.createElement('td');
            tdT.textContent = formatAiCsImportTime(row.importTime);
            tr.appendChild(tdT);

            const tdU = document.createElement('td');
            tdU.className = 'script-mgmt-table__cell--clip';
            tdU.textContent = row.importUsername || '—';
            tr.appendChild(tdU);

            const tdAct = document.createElement('td');
            const wrap = document.createElement('div');
            wrap.className = 'script-mgmt-table__actions';
            const btnEd = document.createElement('button');
            btnEd.type = 'button';
            btnEd.textContent = '编辑';
            btnEd.addEventListener('click', () => openAiCsScriptsEdit(row.id));
            const btnDel = document.createElement('button');
            btnDel.type = 'button';
            btnDel.className = 'is-danger';
            btnDel.textContent = '删除';
            btnDel.addEventListener('click', async () => {
                if (!window.confirm('确定删除这条话术吗？')) return;
                try {
                    await apiDeleteChatScript(row.id);
                    await refreshAiCsScriptTable();
                    appendAiCsBubble('hint', '已删除一条话术。');
                } catch (e) {
                    if (e.sessionExpired) {
                        applySessionExpiredUi(e.message, true);
                    }
                    appendAiCsBubble('hint', e.message || '删除失败');
                }
            });
            wrap.appendChild(btnEd);
            wrap.appendChild(btnDel);
            tdAct.appendChild(wrap);
            tr.appendChild(tdAct);

            aiCsScriptsTbody.appendChild(tr);
        }
    } catch (e) {
        if (e.sessionExpired) {
            applySessionExpiredUi(e.message, true);
        }
        appendAiCsBubble('hint', e.message || '加载话术列表失败');
    }
}

function openAiCsScriptsOverlay() {
    if (!aiCsScriptsOverlay) return;
    if (!systemSession || !systemSession.token) {
        appendAiCsBubble('hint', '请先登录后再管理话术。');
        openLoginModal();
        return;
    }
    editingAiCsScriptId = null;
    showAiCsScriptsListPane();
    aiCsScriptsPageCurrent = 1;
    aiCsScriptsOverlay.classList.add('is-open');
    aiCsScriptsOverlay.setAttribute('aria-hidden', 'false');
    refreshAiCsScriptTable();
}

function closeAiCsScriptsOverlay() {
    if (!aiCsScriptsOverlay) return;
    editingAiCsScriptId = null;
    showAiCsScriptsListPane();
    aiCsScriptsOverlay.classList.remove('is-open');
    aiCsScriptsOverlay.setAttribute('aria-hidden', 'true');
}

async function sendAiCsMessage() {
    if (!aiCsInput || !aiCsSend) return;
    const q = aiCsInput.value.trim();
    if (!q) return;
    if (!window.uClaw || !window.uClaw.sendPrompt) {
        appendAiCsBubble('hint', '客户端未就绪');
        return;
    }
    if (!systemSession || !systemSession.token) {
        appendAiCsBubble('hint', '请先登录平台账号后再使用 AI 客服。');
        openLoginModal();
        return;
    }
    if (!Array.isArray(appConfig.providers) || appConfig.providers.length === 0) {
        appendAiCsBubble('hint', '请先到「模型配置」添加并保存至少一个模型源。');
        return;
    }

    let c = getActiveAiCsConversation();
    if (!c) {
        ensureSeedAiCsConversation();
        c = getActiveAiCsConversation();
    }
    if (!c) return;

    const pid =
        (aiCsProviderEl && aiCsProviderEl.value) ||
        (selectEl && selectEl.value) ||
        appConfig.lastProviderId;

    const hadExchange = c.messages.some(
        (m) => m.role === 'user' || m.role === 'assistant'
    );

    const userAt = Date.now();
    if (!hadExchange && aiCsMsgs) {
        aiCsMsgs.innerHTML = '';
    }
    appendAiCsBubble('user', q, userAt);
    aiCsInput.value = '';
    aiCsSend.disabled = true;
    aiCsAssistantBuffer = '';
    clearAiCsLoadingBubble();
    showAiCsLoadingBubble('检索知识库…');

    c.messages.push({ role: 'user', content: q, createdAt: userAt });

    try {
        const data = await apiPostRagContext(q);
        const ctx =
            data && data.retrievedContext != null ? String(data.retrievedContext) : '';
        const lastUser = [...c.messages].reverse().find((m) => m.role === 'user');
        if (lastUser) lastUser.ragContext = ctx;

        touchActiveAiCsMeta();
        renderConvList();
        persistAiCsConversations();
        void flushAiCsSessionSnapshotRemote(c);

        clearAiCsLoadingBubble();
        showAiCsLoadingBubble('生成中…');

        window.uClaw.sendPrompt({
            messages: [{ role: 'user', content: buildAiCsPrompt(ctx, q) }],
            providerId: pid,
            target: 'ai-cs',
        });
    } catch (e) {
        c.messages.pop();
        clearAiCsLoadingBubble();
        renderAiCsFromMessages();
        aiCsAssistantBuffer = '';
        aiCsSend.disabled = false;
        if (e.sessionExpired) {
            applySessionExpiredUi(e.message, true);
        }
        appendAiCsBubble('hint', e.message || '请求失败');
        persistAiCsConversations();
    }
}

async function downloadAiCsTemplate() {
    if (!systemSession || !systemSession.token) {
        appendAiCsBubble('hint', '请先登录后再下载模版。');
        openLoginModal();
        return;
    }
    const root = getErpBaseUrl().replace(/\/$/, '');
    const url = `${root}/chat/knowledge/template`;
    try {
        const res = await fetch(url, {
            method: 'GET',
            headers: { Authorization: systemSession.token },
        });
        if (res.status === 401) {
            const text = await res.text();
            let msg = '登录已过期';
            try {
                const j = JSON.parse(text);
                msg = j.message || msg;
            } catch {
                /* ignore */
            }
            applySessionExpiredUi(msg, true);
            appendAiCsBubble('hint', msg);
            return;
        }
        if (!res.ok) {
            let detail = '';
            try {
                detail = await res.text();
            } catch {
                /* ignore */
            }
            appendAiCsBubble(
                'hint',
                detail ? `下载失败（HTTP ${res.status}）：${detail.slice(0, 80)}` : `下载失败（HTTP ${res.status}）`
            );
            return;
        }
        const blob = await res.blob();
        const cd = res.headers.get('Content-Disposition');
        let filename = '话术导入模板.xlsx';
        if (cd) {
            const mStar = cd.match(/filename\*=UTF-8''([^;\s]+)/i);
            const mQuoted = cd.match(/filename="([^"]+)"/i);
            if (mStar && mStar[1]) {
                filename = decodeURIComponent(mStar[1].trim()) || filename;
            } else if (mQuoted && mQuoted[1]) {
                filename = mQuoted[1].trim() || filename;
            }
        }
        const a = document.createElement('a');
        const href = URL.createObjectURL(blob);
        a.href = href;
        a.download = filename;
        a.rel = 'noopener';
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(href);
        appendAiCsBubble('hint', '模版已下载，可在本窗口点击下方「导入 Excel 话术」上传。');
    } catch (e) {
        appendAiCsBubble('hint', e.message || '下载失败');
    }
}

async function onAiCsScriptsFileChange(ev) {
    const input = ev.target;
    const file = input.files && input.files[0];
    if (input) input.value = '';
    if (!file) return;
    if (!systemSession || !systemSession.token) {
        appendAiCsBubble('hint', '请先登录后再导入话术。');
        openLoginModal();
        return;
    }
    try {
        const r = await apiImportAiKnowledge(file);
        const hint =
            typeof r.message === 'string' && r.message.trim()
                ? r.message
                : `新增 ${r.inserted} 条，更新 ${r.updated} 条（若已启动 Python 检索服务会自动重建索引）。`;
        appendAiCsBubble('hint', hint);
        aiCsScriptsPageCurrent = 1;
        if (aiCsScriptsOverlay && aiCsScriptsOverlay.classList.contains('is-open')) {
            showAiCsScriptsListPane();
            await refreshAiCsScriptTable();
        }
    } catch (e) {
        if (e.sessionExpired) {
            applySessionExpiredUi(e.message, true);
        }
        appendAiCsBubble('hint', e.message || '导入失败');
    }
}

function setComposerWaiting(waiting) {
    inputEl.disabled = waiting;
    sendBtn.disabled = waiting;
    btnNewChat.disabled = waiting;
    for (const el of convListEl.querySelectorAll('.conv-item')) {
        el.disabled = waiting;
    }
    if (waiting) {
        inputEl.setAttribute('aria-busy', 'true');
    } else {
        inputEl.removeAttribute('aria-busy');
    }
}

function autoresizeTextarea() {
    inputEl.style.height = 'auto';
    inputEl.style.height = Math.min(inputEl.scrollHeight, 120) + 'px';
}

/** 在主对话输入框当前光标处插入文本（或末尾），并自动增高 */
function insertIntoMainComposer(insertText) {
    if (!inputEl) {
        return;
    }
    const ins = insertText != null ? String(insertText) : '';
    if (!ins) {
        return;
    }
    const start =
        typeof inputEl.selectionStart === 'number'
            ? inputEl.selectionStart
            : inputEl.value.length;
    const end =
        typeof inputEl.selectionEnd === 'number'
            ? inputEl.selectionEnd
            : start;
    const v = inputEl.value;
    const before = v.slice(0, start);
    const after = v.slice(end);
    const needsGap =
        before.length > 0 && !/\n$/.test(before) && !/^\n/.test(ins);
    const gap = needsGap ? '\n' : '';
    inputEl.value = before + gap + ins + after;
    const pos = (before + gap + ins).length;
    try {
        inputEl.setSelectionRange(pos, pos);
    } catch (_) {
        /* 部分环境下只读等会抛错 */
    }
    inputEl.focus();
    autoresizeTextarea();
}

inputEl.addEventListener('input', autoresizeTextarea);

inputEl.addEventListener('keydown', (e) => {
    if (e.isComposing || e.keyCode === 229) {
        return;
    }
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        send();
    }
});

sendBtn.addEventListener('click', () => send());

const composerAttachBtn = document.getElementById('composer-attach-btn');
const composerAttachMenu = document.getElementById('composer-attach-menu');

function setComposerAttachOpen(open) {
    if (!composerAttachMenu || !composerAttachBtn) {
        return;
    }
    composerAttachMenu.hidden = !open;
    composerAttachBtn.setAttribute('aria-expanded', open ? 'true' : 'false');
}

if (composerAttachBtn && composerAttachMenu && inputEl) {
    composerAttachBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        setComposerAttachOpen(composerAttachMenu.hidden);
    });
    composerAttachMenu.addEventListener('click', (e) => {
        e.stopPropagation();
    });
    document.addEventListener('click', () => {
        setComposerAttachOpen(false);
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && composerAttachMenu && !composerAttachMenu.hidden) {
            setComposerAttachOpen(false);
        }
    });
    composerAttachMenu.querySelectorAll('[data-attach]').forEach((menuItem) => {
        menuItem.addEventListener('click', async () => {
            const kind = menuItem.getAttribute('data-attach');
            setComposerAttachOpen(false);
            if (kind === 'mail') {
                insertIntoMainComposer('@发邮件 ');
                return;
            }
            if (kind === 'file') {
                if (!window.uClaw || !window.uClaw.pickChatFile) {
                    appendHintLine('[提示] 当前环境不支持从本机选择文件');
                    return;
                }
                try {
                    const r = await window.uClaw.pickChatFile();
                    if (r && r.canceled) {
                        return;
                    }
                    if (!r || !r.ok) {
                        appendErrorLine((r && r.error) || '读取文件失败');
                        return;
                    }
                    const name = r.fileName || '文件';
                    const body = (r.text || '').trimEnd();
                    const block =
                        '\n【附件: ' +
                        name +
                        '】\n' +
                        body +
                        (body.endsWith('\n') ? '' : '\n');
                    insertIntoMainComposer(block);
                } catch (err) {
                    appendErrorLine((err && err.message) || '选择文件失败');
                }
            }
        });
    });
}

if (aiCsSend && aiCsInput) {
    aiCsSend.addEventListener('click', () => sendAiCsMessage());
    aiCsInput.addEventListener('keydown', (e) => {
        if (e.isComposing || e.keyCode === 229) return;
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendAiCsMessage();
        }
    });
}
if (aiCsSyncKnowledge) {
    aiCsSyncKnowledge.addEventListener('click', () => runAiCsKnowledgeSync({}));
}
if (aiCsScriptMgmt) {
    aiCsScriptMgmt.addEventListener('click', () => openAiCsScriptsOverlay());
}
if (aiCsScriptsClose) {
    aiCsScriptsClose.addEventListener('click', () => closeAiCsScriptsOverlay());
}
if (aiCsScriptsOverlay) {
    aiCsScriptsOverlay.addEventListener('click', (e) => {
        if (e.target === aiCsScriptsOverlay) {
            closeAiCsScriptsOverlay();
        }
    });
}
if (aiCsScriptsTemplate) {
    aiCsScriptsTemplate.addEventListener('click', () => downloadAiCsTemplate());
}
if (aiCsScriptsImport && aiCsScriptsFile) {
    aiCsScriptsImport.addEventListener('click', () => aiCsScriptsFile.click());
    aiCsScriptsFile.addEventListener('change', onAiCsScriptsFileChange);
}
if (aiCsScriptsPagePrev) {
    aiCsScriptsPagePrev.addEventListener('click', async () => {
        if (!systemSession || !systemSession.token || aiCsScriptsPagePrev.disabled) return;
        if (aiCsScriptsPageCurrent <= 1) return;
        aiCsScriptsPageCurrent -= 1;
        await refreshAiCsScriptTable();
    });
}
if (aiCsScriptsPageNext) {
    aiCsScriptsPageNext.addEventListener('click', async () => {
        if (!systemSession || !systemSession.token || aiCsScriptsPageNext.disabled) return;
        aiCsScriptsPageCurrent += 1;
        await refreshAiCsScriptTable();
        /* 若超出末页（例如并发删除），refresh 内会回退一页 */
    });
}
if (aiCsScriptEditCancel) {
    aiCsScriptEditCancel.addEventListener('click', () => {
        editingAiCsScriptId = null;
        showAiCsScriptsListPane();
    });
}
if (aiCsScriptEditSave) {
    aiCsScriptEditSave.addEventListener('click', async () => {
        if (editingAiCsScriptId == null || Number.isNaN(editingAiCsScriptId)) {
            showAiCsScriptsListPane();
            return;
        }
        const q = aiCsScriptEditQ ? aiCsScriptEditQ.value.trim() : '';
        const s = aiCsScriptEditS ? aiCsScriptEditS.value.trim() : '';
        const e = aiCsScriptEditE ? aiCsScriptEditE.value.trim() : '';
        if (!q) {
            appendAiCsBubble('hint', '问题不能为空');
            return;
        }
        if (!s) {
            appendAiCsBubble('hint', '话术不能为空');
            return;
        }
        try {
            await apiUpdateChatScript(editingAiCsScriptId, {
                question: q,
                scriptText: s,
                supplement: e || undefined,
            });
            editingAiCsScriptId = null;
            showAiCsScriptsListPane();
            await refreshAiCsScriptTable();
            appendAiCsBubble('hint', '话术已保存。');
        } catch (err) {
            if (err.sessionExpired) {
                applySessionExpiredUi(err.message, true);
            }
            appendAiCsBubble('hint', err.message || '保存失败');
        }
    });
}

navChat.addEventListener('click', () => setMainView('chat'));
if (navAiCs) {
    navAiCs.addEventListener('click', () => setMainView('ai-cs'));
}
if (navMail) {
    navMail.addEventListener('click', () => setMainView('mail'));
}
navSettings.addEventListener('click', () => setMainView('settings'));

if (navUserBtn) {
    navUserBtn.addEventListener('click', () => openLoginModal());
}

if (loginCancel) {
    loginCancel.addEventListener('click', () => closeLoginModal());
}

if (loginSubmit) {
    loginSubmit.addEventListener('click', async () => {
        loginMsg.textContent = '';
        loginMsg.className = 'erp-msg login-msg';
        const baseUrl = getErpBaseUrl();
        if (!window.uClaw || !window.uClaw.systemLogin) {
            loginMsg.textContent = '客户端未就绪，无法登录';
            loginMsg.className = 'erp-msg login-msg is-err';
            return;
        }
        loginSubmit.disabled = true;
        try {
            let r;
            if (loginTabMode === 'sms') {
                const mobile = loginPhone ? loginPhone.value.trim() : '';
                const smsCode = loginSmsInput ? loginSmsInput.value.trim() : '';
                if (!/^1[3-9]\d{9}$/.test(mobile)) {
                    loginMsg.textContent = '请输入正确的手机号';
                    loginMsg.className = 'erp-msg login-msg is-err';
                    return;
                }
                if (!smsCode) {
                    loginMsg.textContent = '请输入短信验证码';
                    loginMsg.className = 'erp-msg login-msg is-err';
                    return;
                }
                r = await window.uClaw.systemLogin({
                    baseUrl,
                    mode: 'sms',
                    mobile,
                    smsCode,
                });
            } else {
                const username = loginUsername ? loginUsername.value.trim() : '';
                const password = loginPassword ? loginPassword.value : '';
                if (!username || !password) {
                    loginMsg.textContent = '请输入用户名和密码';
                    loginMsg.className = 'erp-msg login-msg is-err';
                    return;
                }
                r = await window.uClaw.systemLogin({
                    baseUrl,
                    username,
                    password,
                });
            }
            if (r.ok) {
                systemSession = r.session;
                refreshNavUser();
                showAccountProfileMode();
                await loadProfileFromServer();
            } else {
                loginMsg.textContent = r.error || '登录失败';
                loginMsg.className = 'erp-msg login-msg is-err';
                if (loginTabMode === 'sms') {
                    refreshLoginCaptcha();
                }
            }
        } catch (err) {
            loginMsg.textContent = (err && err.message) || String(err);
            loginMsg.className = 'erp-msg login-msg is-err';
        } finally {
            loginSubmit.disabled = false;
        }
    });
}

if (loginTabPassword && loginTabSms) {
    loginTabPassword.addEventListener('click', () => setLoginTab('password'));
    loginTabSms.addEventListener('click', () => setLoginTab('sms'));
}

if (loginCaptchaHit) {
    loginCaptchaHit.addEventListener('click', () => refreshLoginCaptcha());
}

if (loginBtnSms) {
    loginBtnSms.addEventListener('click', async () => {
        loginMsg.textContent = '';
        loginMsg.className = 'erp-msg login-msg';
        const baseUrl = getErpBaseUrl();
        const mobile = loginPhone ? loginPhone.value.trim() : '';
        const key = loginCaptchaKey ? loginCaptchaKey.value.trim() : '';
        const pictureCode = loginCaptchaInput
            ? loginCaptchaInput.value.trim()
            : '';
        if (!/^1[3-9]\d{9}$/.test(mobile)) {
            loginMsg.textContent = '请输入正确的手机号';
            loginMsg.className = 'erp-msg login-msg is-err';
            return;
        }
        if (!pictureCode || !key) {
            loginMsg.textContent = '请填写图形验证码（点击图片可刷新）';
            loginMsg.className = 'erp-msg login-msg is-err';
            if (!key) refreshLoginCaptcha();
            return;
        }
        if (!window.uClaw || !window.uClaw.systemSmsSend) {
            loginMsg.textContent = '客户端未就绪';
            loginMsg.className = 'erp-msg login-msg is-err';
            return;
        }
        loginBtnSms.disabled = true;
        try {
            const r = await window.uClaw.systemSmsSend({
                baseUrl,
                mobile,
                key,
                pictureCode,
            });
            if (!r.ok) {
                throw new Error(r.error || '发送失败');
            }
            loginMsg.textContent = r.message || '验证码已发送';
            loginMsg.className = 'erp-msg login-msg is-ok';
            clearSmsCooldown('login');
            smsCooldownLoginId = startSmsCooldown(loginBtnSms, 60);
            refreshLoginCaptcha();
        } catch (e) {
            loginMsg.textContent = (e && e.message) || String(e);
            loginMsg.className = 'erp-msg login-msg is-err';
            refreshLoginCaptcha();
            loginBtnSms.disabled = false;
        }
    });
}

function bindRegisterSms() {
    if (!regBtnSms) return;
    regBtnSms.addEventListener('click', async () => {
        if (registerMsg) {
            registerMsg.textContent = '';
            registerMsg.className = 'erp-msg';
        }
        const baseUrl = getErpBaseUrl();
        const mobile = regMobile ? regMobile.value.trim() : '';
        const key = regCaptchaKey ? regCaptchaKey.value.trim() : '';
        const pictureCode = regCaptchaInput
            ? regCaptchaInput.value.trim()
            : '';
        if (!/^1[3-9]\d{9}$/.test(mobile)) {
            if (registerMsg) {
                registerMsg.textContent = '请输入正确的手机号';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        if (!pictureCode || !key) {
            if (registerMsg) {
                registerMsg.textContent = '请填写图形码（点击图片可刷新）';
                registerMsg.className = 'erp-msg is-err';
            }
            if (!key) refreshRegCaptcha();
            return;
        }
        if (!window.uClaw || !window.uClaw.systemSmsSend) {
            if (registerMsg) {
                registerMsg.textContent = '客户端未就绪';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        regBtnSms.disabled = true;
        try {
            const r = await window.uClaw.systemSmsSend({
                baseUrl,
                mobile,
                key,
                pictureCode,
            });
            if (!r.ok) {
                throw new Error(r.error || '发送失败');
            }
            if (registerMsg) {
                registerMsg.textContent = r.message || '验证码已发送';
                registerMsg.className = 'erp-msg is-ok';
            }
            clearSmsCooldown('reg');
            smsCooldownRegId = startSmsCooldown(regBtnSms, 60);
            refreshRegCaptcha();
        } catch (e) {
            if (registerMsg) {
                registerMsg.textContent = (e && e.message) || String(e);
                registerMsg.className = 'erp-msg is-err';
            }
            refreshRegCaptcha();
            regBtnSms.disabled = false;
        }
    });
}
bindRegisterSms();

if (regCaptchaHit) {
    regCaptchaHit.addEventListener('click', () => refreshRegCaptcha());
}

if (linkOpenRegister) {
    linkOpenRegister.addEventListener('click', () => {
        if (loginMsg) {
            loginMsg.textContent = '';
            loginMsg.className = 'erp-msg login-msg';
        }
        showErpRegisterView();
    });
}

if (registerCancel) {
    registerCancel.addEventListener('click', () => {
        if (registerMsg) {
            registerMsg.textContent = '';
            registerMsg.className = 'erp-msg';
        }
        clearRegisterFields();
        clearSmsCooldown('reg');
        showErpLoginView();
    });
}

if (registerSubmit) {
    registerSubmit.addEventListener('click', async () => {
        if (registerMsg) {
            registerMsg.textContent = '';
            registerMsg.className = 'erp-msg';
        }
        const baseUrl = getErpBaseUrl();
        const username = regUsername ? regUsername.value.trim() : '';
        const p1 = regPassword ? regPassword.value : '';
        const p2 = regPassword2 ? regPassword2.value : '';
        const mobile = regMobile ? regMobile.value.trim() : '';
        const smsCode = regSmsInput ? regSmsInput.value.trim() : '';
        const nickname = regNickname ? regNickname.value.trim() : '';

        if (!username) {
            if (registerMsg) {
                registerMsg.textContent = '请填写账号';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        const pwErr = validateNewPasswordLocal(p1);
        if (pwErr) {
            if (registerMsg) {
                registerMsg.textContent = pwErr;
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        if (p1 !== p2) {
            if (registerMsg) {
                registerMsg.textContent = '两次输入的密码不一致';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        if (!/^1[3-9]\d{9}$/.test(mobile)) {
            if (registerMsg) {
                registerMsg.textContent = '请输入正确的手机号';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        if (!smsCode) {
            if (registerMsg) {
                registerMsg.textContent = '请填写短信验证码';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        if (!window.uClaw || !window.uClaw.systemRegister) {
            if (registerMsg) {
                registerMsg.textContent = '客户端未就绪';
                registerMsg.className = 'erp-msg is-err';
            }
            return;
        }
        registerSubmit.disabled = true;
        try {
            const r = await window.uClaw.systemRegister({
                baseUrl,
                username,
                passwordPlain: p1,
                nickname: nickname || undefined,
                mobile,
                smsCode,
            });
            if (r.ok) {
                if (registerMsg) {
                    registerMsg.textContent = '注册成功，请使用账号密码或手机登录';
                    registerMsg.className = 'erp-msg is-ok';
                }
                clearRegisterFields();
                clearSmsCooldown('reg');
                showErpLoginView();
                if (loginUsername) loginUsername.value = username;
            } else {
                if (registerMsg) {
                    registerMsg.textContent = r.error || '注册失败';
                    registerMsg.className = 'erp-msg is-err';
                }
            }
        } catch (e) {
            if (registerMsg) {
                registerMsg.textContent = (e && e.message) || String(e);
                registerMsg.className = 'erp-msg is-err';
            }
        } finally {
            registerSubmit.disabled = false;
        }
    });
}

if (loginLogout) {
    loginLogout.addEventListener('click', async () => {
        closePwdSheet();
        loginMsg.textContent = '';
        if (!window.uClaw || !window.uClaw.systemLogout) return;
        await window.uClaw.systemLogout();
        systemSession = null;
        profileUserMobile = '';
        clearPasswordFields();
        refreshNavUser();
        showAccountLoginMode();
        showErpLoginView();
        setLoginTab('password');
        clearErpLoginFields();
        clearRegisterFields();
        clearSmsCooldown('login');
        clearSmsCooldown('reg');
        if (loginMsg) {
            loginMsg.className = 'erp-msg login-msg';
        }
        if (registerMsg) {
            registerMsg.textContent = '';
            registerMsg.className = 'erp-msg';
        }
        if (loginStatusEl) loginStatusEl.hidden = true;
    });
}

if (profileSave) {
    profileSave.addEventListener('click', async () => {
        if (!window.uClaw || !window.uClaw.saveUserProfile) return;
        const nick = profileNickname ? profileNickname.value.trim() : '';
        if (!nick) {
            profileMsg.textContent = '请填写昵称';
            profileMsg.className = 'profile-msg is-err';
            return;
        }
        profileMsg.textContent = '';
        profileMsg.className = 'profile-msg';
        profileSave.disabled = true;
        try {
            const r = await window.uClaw.saveUserProfile(collectProfilePayload());
            if (r.ok) {
                systemSession = await window.uClaw.getSystemSession();
                if (r.user) fillProfileForm(r.user);
                refreshNavUser();
                profileMsg.textContent = '已保存';
                profileMsg.className = 'profile-msg is-ok';
            } else if (r.sessionExpired) {
                applySessionExpiredUi(r.error, true);
                profileMsg.textContent = r.error || '登录已过期，请重新登录';
                profileMsg.className = 'profile-msg is-err';
            } else {
                profileMsg.textContent = r.error || '保存失败';
                profileMsg.className = 'profile-msg is-err';
            }
        } catch (err) {
            profileMsg.textContent = (err && err.message) || String(err);
            profileMsg.className = 'profile-msg is-err';
        } finally {
            profileSave.disabled = false;
        }
    });
}

if (profileOpenPassword) {
    profileOpenPassword.addEventListener('click', (e) => {
        e.preventDefault();
        openPwdSheet();
    });
}

if (pwdClose) {
    pwdClose.addEventListener('click', () => closePwdSheet());
}

if (pwdCaptchaHit) {
    pwdCaptchaHit.addEventListener('click', () => refreshPwdCaptcha());
}

if (pwdBtnSms) {
    pwdBtnSms.addEventListener('click', async () => {
        if (pwdMsg) {
            pwdMsg.textContent = '';
            pwdMsg.className = 'profile-msg';
        }
        const baseUrl = getErpBaseUrl();
        const mobile = (profileUserMobile || '').trim();
        const key = pwdCaptchaKey ? pwdCaptchaKey.value.trim() : '';
        const pictureCode = pwdCaptchaInput
            ? pwdCaptchaInput.value.trim()
            : '';
        if (!/^1[3-9]\d{9}$/.test(mobile)) {
            if (pwdMsg) {
                pwdMsg.textContent = '当前账号未绑定有效手机号';
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        if (!pictureCode || !key) {
            if (pwdMsg) {
                pwdMsg.textContent = '请填写图形验证码（点击图片可刷新）';
                pwdMsg.className = 'profile-msg is-err';
            }
            if (!key) void refreshPwdCaptcha();
            return;
        }
        if (!window.uClaw || !window.uClaw.systemSmsSend) {
            if (pwdMsg) {
                pwdMsg.textContent = '客户端未就绪';
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        pwdBtnSms.disabled = true;
        try {
            const r = await window.uClaw.systemSmsSend({
                baseUrl,
                mobile,
                key,
                pictureCode,
            });
            if (!r.ok) {
                throw new Error(r.error || '发送失败');
            }
            if (pwdMsg) {
                pwdMsg.textContent = r.message || '验证码已发送';
                pwdMsg.className = 'profile-msg is-ok';
            }
            clearSmsCooldown('pwd');
            smsCooldownPwdId = startSmsCooldown(pwdBtnSms, 60);
            void refreshPwdCaptcha();
        } catch (e) {
            if (pwdMsg) {
                pwdMsg.textContent = (e && e.message) || String(e);
                pwdMsg.className = 'profile-msg is-err';
            }
            void refreshPwdCaptcha();
            pwdBtnSms.disabled = false;
        }
    });
}

if (pwdOverlay) {
    pwdOverlay.addEventListener('click', (e) => {
        if (e.target === pwdOverlay) {
            closePwdSheet();
        }
    });
}
if (pwdSheet) {
    pwdSheet.addEventListener('click', (e) => {
        e.stopPropagation();
    });
}

if (profilePasswordSubmit) {
    profilePasswordSubmit.addEventListener('click', async () => {
        if (!window.uClaw || !window.uClaw.updateUserPassword) return;
        if (pwdMsg) {
            pwdMsg.textContent = '';
            pwdMsg.className = 'profile-msg';
        }
        const sms = pwdSms ? pwdSms.value.trim() : '';
        const p1 = pwdNew ? pwdNew.value : '';
        const p2 = pwdConfirm ? pwdConfirm.value : '';
        const err = validateNewPasswordLocal(p1);
        if (err) {
            if (pwdMsg) {
                pwdMsg.textContent = err;
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        if (p1 !== p2) {
            if (pwdMsg) {
                pwdMsg.textContent = '两次输入的新密码不一致';
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        if (!/^1[3-9]\d{9}$/.test(profileUserMobile)) {
            if (pwdMsg) {
                pwdMsg.textContent =
                    '当前账号未绑定有效手机号，无法修改密码';
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        if (!sms) {
            if (pwdMsg) {
                pwdMsg.textContent = '请填写短信验证码';
                pwdMsg.className = 'profile-msg is-err';
            }
            return;
        }
        profilePasswordSubmit.disabled = true;
        try {
            const r = await window.uClaw.updateUserPassword({
                mobile: profileUserMobile,
                smsCode: sms,
                newPassword: p1,
            });
            if (r.ok && r.needRelogin) {
                closePwdSheet();
                profileUserMobile = '';
                systemSession = null;
                refreshNavUser();
                clearPasswordFields();
                showAccountLoginMode();
                if (loginMsg) {
                    loginMsg.textContent =
                        '密码已更新，请使用新密码重新登录';
                    loginMsg.className = 'erp-msg login-msg is-ok';
                }
                if (profileMsg) {
                    profileMsg.textContent = '';
                    profileMsg.className = 'profile-msg';
                }
            } else if (r.sessionExpired) {
                applySessionExpiredUi(r.error, true);
                if (pwdMsg) {
                    pwdMsg.textContent = r.error || '登录已过期，请重新登录';
                    pwdMsg.className = 'profile-msg is-err';
                }
            } else if (pwdMsg) {
                pwdMsg.textContent = r.error || '修改密码失败';
                pwdMsg.className = 'profile-msg is-err';
            }
        } catch (err) {
            if (pwdMsg) {
                pwdMsg.textContent =
                    (err && err.message) || String(err);
                pwdMsg.className = 'profile-msg is-err';
            }
        } finally {
            syncPasswordSectionState(profileUserMobile);
            if (profilePasswordSubmit) profilePasswordSubmit.disabled = false;
        }
    });
}

async function persistAvatarAfterUpload(headImgUrl) {
    const disp =
        (profileNickname && profileNickname.value.trim()) ||
        (profileDisplayName &&
        profileDisplayName.textContent &&
        profileDisplayName.textContent !== '—'
            ? profileDisplayName.textContent.trim()
            : '') ||
        '用户';
    if (profileHeadImg) profileHeadImg.value = headImgUrl;
    updateProfileAvatarPreview(headImgUrl, disp);
    const saveR = await window.uClaw.saveUserProfile({
        headImg: headImgUrl,
    });
    if (saveR.ok) {
        systemSession = await window.uClaw.getSystemSession();
        refreshNavUser();
        if (profileMsg) {
            profileMsg.textContent = '头像已更新';
            profileMsg.className = 'profile-msg is-ok';
        }
    } else if (saveR.sessionExpired) {
        applySessionExpiredUi(saveR.error, true);
        if (profileMsg) {
            profileMsg.textContent = saveR.error || '登录已过期，请重新登录';
            profileMsg.className = 'profile-msg is-err';
        }
    } else if (profileMsg) {
        profileMsg.textContent = saveR.error || '头像已上传但保存失败';
        profileMsg.className = 'profile-msg is-err';
    }
}

if (profileAvatarHit) {
    profileAvatarHit.addEventListener('click', async () => {
        if (
            !window.uClaw ||
            !window.uClaw.pickAvatarFile ||
            !window.uClaw.readImageFile
        ) {
            return;
        }
        if (profileMsg) {
            profileMsg.textContent = '';
            profileMsg.className = 'profile-msg';
        }
        profileAvatarHit.disabled = true;
        try {
            const r = await window.uClaw.pickAvatarFile();
            if (r.canceled) return;
            if (!r.ok) {
                if (r.sessionExpired) {
                    applySessionExpiredUi(r.error, true);
                    if (profileMsg) {
                        profileMsg.textContent =
                            r.error || '登录已过期，请重新登录';
                        profileMsg.className = 'profile-msg is-err';
                    }
                    return;
                }
                if (profileMsg) {
                    profileMsg.textContent = r.error || '选择文件失败';
                    profileMsg.className = 'profile-msg is-err';
                }
                return;
            }
            const rr = await window.uClaw.readImageFile(r.filePath);
            if (!rr.ok) {
                if (profileMsg) {
                    profileMsg.textContent = rr.error || '读取图片失败';
                    profileMsg.className = 'profile-msg is-err';
                }
                return;
            }
            openAvatarCropModal(rr.dataUrl);
        } catch (err) {
            if (profileMsg) {
                profileMsg.textContent =
                    (err && err.message) || String(err);
                profileMsg.className = 'profile-msg is-err';
            }
        } finally {
            profileAvatarHit.disabled = false;
        }
    });
}

if (avatarCropCanvas) {
    avatarCropCanvas.addEventListener('pointerdown', (e) => {
        if (!avatarCropState) return;
        avatarCropCanvas.setPointerCapture(e.pointerId);
        avatarCropDrag = { lastX: e.clientX, lastY: e.clientY };
    });
    avatarCropCanvas.addEventListener('pointermove', (e) => {
        if (!avatarCropDrag || !avatarCropState) return;
        const dx = e.clientX - avatarCropDrag.lastX;
        const dy = e.clientY - avatarCropDrag.lastY;
        avatarCropDrag = { lastX: e.clientX, lastY: e.clientY };
        avatarCropState.panX += dx;
        avatarCropState.panY += dy;
        clampAvatarCropPan();
        drawAvatarCropCanvas();
    });
    avatarCropCanvas.addEventListener('pointerup', () => {
        avatarCropDrag = null;
    });
    avatarCropCanvas.addEventListener('pointercancel', () => {
        avatarCropDrag = null;
    });
    avatarCropCanvas.addEventListener(
        'wheel',
        (e) => {
            if (!avatarCropState) return;
            e.preventDefault();
            const delta = e.deltaY > 0 ? -0.06 : 0.06;
            let z = avatarCropState.userZoom + delta;
            z = Math.min(3, Math.max(1, z));
            avatarCropState.userZoom = z;
            if (avatarCropZoom) {
                avatarCropZoom.value = String(Math.round(z * 100));
            }
            clampAvatarCropPan();
            drawAvatarCropCanvas();
        },
        { passive: false }
    );
}

if (avatarCropZoom) {
    avatarCropZoom.addEventListener('input', () => {
        if (!avatarCropState) return;
        avatarCropState.userZoom = Number(avatarCropZoom.value) / 100;
        clampAvatarCropPan();
        drawAvatarCropCanvas();
    });
}

if (avatarCropCancel) {
    avatarCropCancel.addEventListener('click', () => closeAvatarCropModal());
}

if (avatarCropOk) {
    avatarCropOk.addEventListener('click', async () => {
        if (!window.uClaw || !window.uClaw.uploadAvatarBase64) return;
        const b64 = exportAvatarCropJpegBase64();
        if (!b64) return;
        avatarCropOk.disabled = true;
        try {
            const r = await window.uClaw.uploadAvatarBase64({ base64: b64 });
            if (!r.ok) {
                if (r.sessionExpired) {
                    closeAvatarCropModal();
                    applySessionExpiredUi(r.error, true);
                    if (profileMsg) {
                        profileMsg.textContent =
                            r.error || '登录已过期，请重新登录';
                        profileMsg.className = 'profile-msg is-err';
                    }
                    return;
                }
                if (profileMsg) {
                    profileMsg.textContent = r.error || '上传失败';
                    profileMsg.className = 'profile-msg is-err';
                }
                return;
            }
            closeAvatarCropModal();
            await persistAvatarAfterUpload(r.url);
        } catch (err) {
            if (profileMsg) {
                profileMsg.textContent =
                    (err && err.message) || String(err);
                profileMsg.className = 'profile-msg is-err';
            }
        } finally {
            avatarCropOk.disabled = false;
        }
    });
}

btnNewChat.addEventListener('click', () => {
    if (isAiCsViewActive()) {
        if (aiCsSend && aiCsSend.disabled) return;
        startNewAiCsConversation();
        return;
    }
    if (isWaiting) return;
    startNewConversation();
});

if (convSearchEl) {
    convSearchEl.addEventListener('input', () => {
        renderConvList();
    });
}

document.addEventListener('pointerdown', (e) => {
    if (!ctxMenu || ctxMenu.hidden) {
        return;
    }
    if (e.target.closest('#ctx-menu')) {
        return;
    }
    hideCtxMenu();
});

document.addEventListener('keydown', (e) => {
    if (
        e.key === 'Escape' &&
        avatarCropOverlay &&
        avatarCropOverlay.classList.contains('is-open')
    ) {
        e.preventDefault();
        closeAvatarCropModal();
        return;
    }
    if (
        e.key === 'Escape' &&
        pwdOverlay &&
        pwdOverlay.classList.contains('is-open')
    ) {
        e.preventDefault();
        closePwdSheet();
        return;
    }
    if (
        e.key === 'Escape' &&
        loginOverlay &&
        loginOverlay.classList.contains('is-open')
    ) {
        e.preventDefault();
        closeLoginModal();
        return;
    }
    if (e.key === 'Escape' && ctxMenu && !ctxMenu.hidden) {
        e.preventDefault();
        hideCtxMenu();
        return;
    }
    if (e.isComposing || e.keyCode === 229) {
        return;
    }
    if (!(e.ctrlKey || e.metaKey) || e.code !== 'KeyN') {
        return;
    }
    if (!viewChat.classList.contains('view--active') && !viewAiCs.classList.contains('view--active')) {
        return;
    }
    e.preventDefault();
    if (viewAiCs.classList.contains('view--active')) {
        if (aiCsSend && aiCsSend.disabled) return;
        startNewAiCsConversation();
        return;
    }
    if (isWaiting) return;
    startNewConversation();
});

if (ctxMenu) {
    ctxMenu.addEventListener('click', (e) => {
        const t = e.target && e.target.closest
            ? e.target.closest('button[data-action]')
            : null;
        if (!t || t.disabled) {
            return;
        }
        const id = ctxTargetId;
        const action = t.dataset.action;
        hideCtxMenu();
        if (!id) {
            return;
        }
        const c =
            conversations.find((x) => x.id === id) ||
            aiCsConversations.find((x) => x.id === id);
        if (action === 'open') {
            if (!isWaiting) {
                selectConversation(id);
            }
            return;
        }
        if (action === 'rename') {
            if (!isWaiting) {
                renameConversation(id);
            }
            return;
        }
        if (action === 'copy-title') {
            const title = (c && c.title) || '新对话';
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(title).catch(() => {});
            }
            return;
        }
        if (action === 'delete' && c) {
            deleteConversation(id);
        }
    });
}

function deleteConversation(id) {
    const cMain = conversations.find((x) => x.id === id);
    const cAi = aiCsConversations.find((x) => x.id === id);
    const c = cMain || cAi;
    if (!c) return;
    const isAi = !!cAi;
    const busy = isAi
        ? aiCsSend && aiCsSend.disabled
        : isWaiting;
    if (busy) return;
    const hasContent = c.messages.some(
        (m) => m.role === 'user' || m.role === 'assistant'
    );
    if (hasContent) {
        if (!confirm('确定删除这条会话？')) {
            return;
        }
    }
    if (isAi) {
        const wasActive = id === activeAiCsId;
        aiCsConversations = aiCsConversations.filter((x) => x.id !== id);
        if (!aiCsConversations.length) {
            const nid = generateId();
            activeAiCsId = nid;
            aiCsConversations = [
                {
                    id: nid,
                    title: '新客服会话',
                    updatedAt: Date.now(),
                    customTitle: false,
                    messages: [],
                },
            ];
        } else if (wasActive) {
            activeAiCsId = [...aiCsConversations].sort(
                (a, b) => b.updatedAt - a.updatedAt
            )[0].id;
        }
        renderConvList();
        renderAiCsFromMessages();
        persistAiCsConversations();
        if (isAiCsViewActive() && aiCsInput) aiCsInput.focus();
        return;
    }
    const wasActive = id === activeId;
    conversations = conversations.filter((x) => x.id !== id);
    if (!conversations.length) {
        const nid = generateId();
        activeId = nid;
        conversations = [
            {
                id: nid,
                title: '新对话',
                updatedAt: Date.now(),
                customTitle: false,
                messages: [{ ...SYSTEM_MESSAGE }],
            },
        ];
    } else if (wasActive) {
        activeId = [...conversations].sort((a, b) => b.updatedAt - a.updatedAt)[0]
            .id;
    }
    renderConvList();
    renderChatFromMessages();
    persistConversations();
    setMainView('chat');
    inputEl.focus();
}

function renameConversation(id) {
    const c =
        conversations.find((x) => x.id === id) ||
        aiCsConversations.find((x) => x.id === id);
    if (!c) return;
    const isAi = aiCsConversations.some((x) => x.id === id);
    const busy = isAi ? aiCsSend && aiCsSend.disabled : isWaiting;
    if (busy) return;
    const current = c.title && c.title.trim() ? c.title : isAi ? '新客服会话' : '新对话';
    const next = window.prompt('会话标题', current);
    if (next == null) {
        return;
    }
    const t = next.trim() || (isAi ? '新客服会话' : '新对话');
    c.title = t;
    c.customTitle = true;
    c.updatedAt = Date.now();
    renderConvList();
    if (isAi) persistAiCsConversations();
    else persistConversations();
}

function startNewConversation() {
    const id = generateId();
    const c = {
        id,
        title: '新对话',
        updatedAt: Date.now(),
        customTitle: false,
        messages: [{ ...SYSTEM_MESSAGE }],
    };
    conversations.unshift(c);
    trimConversations();
    activeId = id;
    renderConvList();
    renderChatFromMessages();
    persistConversations();
    inputEl.value = '';
    inputEl.style.height = 'auto';
    inputEl.focus();
}

function selectConversation(id) {
    if (isAiCsViewActive()) {
        if (aiCsSend && aiCsSend.disabled) return;
        if (id === activeAiCsId) return;
        if (!aiCsConversations.find((c) => c.id === id)) return;
        activeAiCsId = id;
        renderConvList();
        renderAiCsFromMessages();
        persistAiCsConversations();
        if (aiCsInput) aiCsInput.focus();
        return;
    }
    if (isWaiting || id === activeId) return;
    if (!conversations.find((c) => c.id === id)) return;
    activeId = id;
    renderConvList();
    renderChatFromMessages();
    persistConversations();
    inputEl.focus();
}

document.getElementById('save-cfg').addEventListener('click', () => saveSettings());

function escapeAttr(s) {
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/"/g, '&quot;')
        .replace(/</g, '&lt;');
}

function renderProviderForms(cfg) {
    providerForms.innerHTML = '';
    const ph = window.uClaw ? window.uClaw.KEY_PLACEHOLDER : '********';
    for (const p of cfg.providers) {
        const card = document.createElement('div');
        card.className = 'prov-card';
        if (p.kind === 'local') {
            const h3 = document.createElement('h3');
            h3.textContent = p.name;
            const desc = document.createElement('p');
            desc.className = 'hint';
            const file = p.modelFile || 'qwen2.5-3b-instruct-q8_0.gguf';
            desc.innerHTML = `本机 <code>bin/llama-completion</code>（及同目录 .dll）+ <code>models/${escapeAttr(
                file
            )}</code>。请使用与官方一致的「补全」可执行文件，不要用交互用的 llama-cli。`;
            card.appendChild(h3);
            card.appendChild(desc);
        } else {
            const h3 = document.createElement('h3');
            h3.textContent = p.name;
            card.appendChild(h3);

            const r1 = document.createElement('div');
            r1.className = 'row';
            const l1 = document.createElement('label');
            l1.textContent = 'Base URL';
            const i1 = document.createElement('input');
            i1.type = 'text';
            i1.dataset.f = 'baseUrl';
            i1.dataset.id = p.id;
            i1.value = p.baseUrl || '';
            r1.appendChild(l1);
            r1.appendChild(i1);

            const r2 = document.createElement('div');
            r2.className = 'row';
            const l2 = document.createElement('label');
            l2.textContent = '模型名 / 端点 ID';
            const i2 = document.createElement('input');
            i2.type = 'text';
            i2.dataset.f = 'model';
            i2.dataset.id = p.id;
            i2.value = p.model || '';
            r2.appendChild(l2);
            r2.appendChild(i2);

            const r3 = document.createElement('div');
            r3.className = 'row';
            const l3 = document.createElement('label');
            l3.textContent = 'API Key（自建可留空；已保存为占位）';
            const i3 = document.createElement('input');
            i3.type = 'password';
            i3.dataset.f = 'apiKey';
            i3.dataset.id = p.id;
            i3.value = p.hasKey ? ph : p.apiKey || '';
            i3.autocomplete = 'off';
            r3.appendChild(l3);
            r3.appendChild(i3);

            card.appendChild(r1);
            card.appendChild(r2);
            card.appendChild(r3);
        }
        providerForms.appendChild(card);
    }
}

function collectConfigFromForm() {
    const ph = window.uClaw.KEY_PLACEHOLDER;
    const next = {
        lastProviderId: selectEl.value,
        providers: appConfig.providers.map((p) => {
            if (p.kind === 'local') {
                return { ...p };
            }
            const q = (f) =>
                providerForms.querySelector(
                    `input[data-f="${f}"][data-id="${p.id}"]`
                );
            const baseUrl = (q('baseUrl') || { value: p.baseUrl }).value;
            const model = (q('model') || { value: p.model }).value;
            let apiKey = (q('apiKey') || { value: p.apiKey }).value;
            if (apiKey === ph) {
                apiKey = ph;
            }
            return { ...p, baseUrl, model, apiKey };
        }),
    };
    return next;
}

async function saveSettings() {
    saveMsg.textContent = '保存中…';
    const next = collectConfigFromForm();
    const r = await window.uClaw.saveProviders(next);
    if (r && r.ok) {
        saveMsg.textContent = '已保存';
        appConfig = await window.uClaw.getProviders();
        renderProviderForms(appConfig);
        fillSelect();
    } else {
        saveMsg.textContent = '保存失败';
    }
}

/** 邮件页：上次加载时是否已有保存的邮箱密码（用于空密码=不修改） */
let mailImapHadPassword = false;
let mailSmtpHadPassword = false;
/** 客户联系人（与 config 同步，@发邮件 解析用） */
let mailContactsCache = [];

function newMailContactId() {
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
        return crypto.randomUUID();
    }
    return 'c' + Date.now() + '-' + Math.random().toString(36).slice(2, 9);
}

function appendMailContactRow(tbody, c) {
    if (!tbody) {
        return;
    }
    const tr = document.createElement('tr');
    const id = (c && c.id) || newMailContactId();
    tr.dataset.contactId = id;
    const tdN = document.createElement('td');
    const inN = document.createElement('input');
    inN.type = 'text';
    inN.className = 'mail-contact-nick';
    inN.autocomplete = 'off';
    inN.placeholder = '如 张三';
    inN.value = (c && c.nickname) || '';
    tdN.appendChild(inN);
    const tdE = document.createElement('td');
    const inE = document.createElement('input');
    inE.type = 'email';
    inE.className = 'mail-contact-email';
    inE.autocomplete = 'off';
    inE.placeholder = 'a@b.com';
    inE.value = (c && c.email) || '';
    tdE.appendChild(inE);
    const tdB = document.createElement('td');
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'mail-contact-del';
    btn.textContent = '删除';
    btn.addEventListener('click', () => tr.remove());
    tdB.appendChild(btn);
    tr.appendChild(tdN);
    tr.appendChild(tdE);
    tr.appendChild(tdB);
    tbody.appendChild(tr);
}

function renderMailContactsTable(contacts) {
    const tbody = document.getElementById('mail-contacts-tbody');
    if (!tbody) {
        return;
    }
    tbody.innerHTML = '';
    const arr = Array.isArray(contacts) ? contacts : [];
    for (const c of arr) {
        appendMailContactRow(tbody, c);
    }
}

function collectMailContactsFromForm() {
    const tbody = document.getElementById('mail-contacts-tbody');
    if (!tbody) {
        return [];
    }
    const rows = tbody.querySelectorAll('tr[data-contact-id]');
    const list = [];
    for (const tr of rows) {
        const nickEl = tr.querySelector('.mail-contact-nick');
        const emailEl = tr.querySelector('.mail-contact-email');
        const nickname = nickEl ? nickEl.value.trim() : '';
        const email = emailEl ? emailEl.value.trim() : '';
        if (!nickname && !email) {
            continue;
        }
        list.push({
            id: tr.dataset.contactId || newMailContactId(),
            nickname,
            email,
        });
    }
    return list;
}

function inferMailPreset(cfg) {
    const p = cfg && cfg.preset;
    if (p === 'qq' || p === 'gmail' || p === 'custom') {
        return p;
    }
    const host = (cfg.imap && cfg.imap.host) || '';
    if (/qq\.com/i.test(host)) {
        return 'qq';
    }
    if (/gmail\.com/i.test(host)) {
        return 'gmail';
    }
    return 'custom';
}

/** 根据所选类型填充推荐 IMAP/SMTP（不修改账号与密码） */
function applyMailPresetFields(preset) {
    if (preset === 'custom') {
        return;
    }
    if (preset === 'qq') {
        document.getElementById('mail-imap-host').value = 'imap.qq.com';
        document.getElementById('mail-imap-port').value = '993';
        document.getElementById('mail-imap-ssl').checked = true;
        document.getElementById('mail-imap-folder').value = 'INBOX';
        document.getElementById('mail-smtp-host').value = 'smtp.qq.com';
        document.getElementById('mail-smtp-port').value = '465';
        document.getElementById('mail-smtp-ssl').checked = true;
        document.getElementById('mail-smtp-starttls').checked = false;
    } else if (preset === 'gmail') {
        document.getElementById('mail-imap-host').value = 'imap.gmail.com';
        document.getElementById('mail-imap-port').value = '993';
        document.getElementById('mail-imap-ssl').checked = true;
        document.getElementById('mail-imap-folder').value = 'INBOX';
        document.getElementById('mail-smtp-host').value = 'smtp.gmail.com';
        document.getElementById('mail-smtp-port').value = '587';
        document.getElementById('mail-smtp-ssl').checked = false;
        document.getElementById('mail-smtp-starttls').checked = true;
    }
}

function renderMailTutorial(preset) {
    const el = document.getElementById('mail-tutorial');
    if (!el) {
        return;
    }
    if (preset === 'qq') {
        el.innerHTML =
            '<h4>QQ 邮箱配置教程</h4>' +
            '<ol>' +
            '<li>浏览器打开 <strong>QQ 邮箱</strong>（<code>https://mail.qq.com</code>）并登录。</li>' +
            '<li>进入 <strong>设置 → 账户</strong>，找到「POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV 服务」。</li>' +
            '<li>按页面提示 <strong>开启 IMAP/SMTP</strong>（可能需要发验证短信等）。</li>' +
            '<li>记录系统生成的 <strong>授权码</strong>（一串独立密码，<strong>不是</strong> QQ 登录密码）。</li>' +
            '<li>在本页将 <strong>账号</strong> 填为完整邮箱，如 <code>123456@qq.com</code>；<strong>IMAP 密码</strong>与 <strong>SMTP 密码</strong>均填该 <strong>授权码</strong>（通常两处相同）。</li>' +
            '<li><strong>发件地址</strong> 一般与账号相同，填 <code>你的邮箱@qq.com</code>。</li>' +
            '</ol>' +
            '<p class="mail-tutorial-note">已为你填入 <code>imap.qq.com:993</code>、<code>smtp.qq.com:465</code>（SSL）。若发信失败，可将 SMTP 改为端口 <code>587</code>，取消勾选「SSL」、勾选「STARTTLS」再试。</p>';
    } else if (preset === 'gmail') {
        el.innerHTML =
            '<h4>谷歌邮箱 (Gmail) 配置教程</h4>' +
            '<ol>' +
            '<li>打开 <strong>Google 账户</strong> → <strong>安全性</strong>，先开启 <strong>两步验证</strong>。</li>' +
            '<li>在「两步验证」相关设置中找到 <strong>应用专用密码</strong>（App passwords），为「邮件」生成一条 16 位密码并复制。</li>' +
            '<li>在本页 <strong>IMAP 密码</strong>、<strong>SMTP 密码</strong> 中填入该 <strong>应用专用密码</strong>（不要使用普通 Gmail 登录密码）。</li>' +
            '<li>打开 Gmail 网页 → <strong>设置 → 查看所有设置 → 转发和 POP/IMAP</strong>，确认已 <strong>启用 IMAP</strong>。</li>' +
            '<li><strong>账号</strong>与<strong>发件地址</strong>填你的 Gmail 完整地址，如 <code>name@gmail.com</code>。</li>' +
            '</ol>' +
            '<p class="mail-tutorial-note">已填入 <code>imap.gmail.com:993</code>、<code>smtp.gmail.com:587</code>（STARTTLS）。访问 Google 需网络可达；若连接超时，请检查代理或网络环境。</p>';
    } else {
        el.innerHTML =
            '<h4>自定义邮箱</h4>' +
            '<p>请向邮箱服务商或企业管理员索取 <strong>IMAP</strong>（收信）与 <strong>SMTP</strong>（发信）服务器地址、端口及加密方式（SSL / STARTTLS）。</p>' +
            '<p class="mail-tutorial-note">密码栏一般为邮箱提供的「客户端专用密码」或「授权码」，而非网页登录密码（与 QQ/Gmail 类似，以服务商说明为准）。</p>';
    }
}

function collectMailConfigFromForm() {
    const ph = window.uClaw.KEY_PLACEHOLDER;
    let imapPass = document.getElementById('mail-imap-pass')
        ? document.getElementById('mail-imap-pass').value
        : '';
    if (!imapPass && mailImapHadPassword) {
        imapPass = ph;
    }
    let smtpPass = document.getElementById('mail-smtp-pass')
        ? document.getElementById('mail-smtp-pass').value
        : '';
    if (!smtpPass && mailSmtpHadPassword) {
        smtpPass = ph;
    }
    const presetEl = document.getElementById('mail-preset');
    return {
        enabled: document.getElementById('mail-enabled').checked,
        pollIntervalMs:
            Number(document.getElementById('mail-poll-ms').value) || 120000,
        preset: (presetEl && presetEl.value) || 'custom',
        providerId: document.getElementById('mail-provider').value || '',
        contacts: collectMailContactsFromForm(),
        imap: {
            host: document.getElementById('mail-imap-host').value.trim(),
            port: Number(document.getElementById('mail-imap-port').value) || 993,
            user: document.getElementById('mail-imap-user').value.trim(),
            password: imapPass,
            folder:
                document.getElementById('mail-imap-folder').value.trim() ||
                'INBOX',
            ssl: document.getElementById('mail-imap-ssl').checked,
        },
        smtp: {
            host: document.getElementById('mail-smtp-host').value.trim(),
            port: Number(document.getElementById('mail-smtp-port').value) || 587,
            user: document.getElementById('mail-smtp-user').value.trim(),
            password: smtpPass,
            fromAddress: document.getElementById('mail-smtp-from').value.trim(),
            ssl: document.getElementById('mail-smtp-ssl').checked,
            starttls: document.getElementById('mail-smtp-starttls').checked,
        },
    };
}

async function refreshMailQueueOnly() {
    if (!window.uClaw || !window.uClaw.getMailQueue) {
        return;
    }
    const mq = await window.uClaw.getMailQueue();
    renderMailQueueList((mq && mq.items) || []);
}

function renderMailQueueList(items) {
    const root = document.getElementById('mail-queue');
    if (!root) {
        return;
    }
    root.innerHTML = '';
    if (!items.length) {
        const p = document.createElement('p');
        p.className = 'hint';
        p.textContent = '暂无邮件条目。启用收信并点击「立即收信」或等待定时轮询。';
        root.appendChild(p);
        return;
    }
    for (const it of items) {
        const card = document.createElement('div');
        card.className =
            'mail-card' +
            (it.status !== 'pending' ? ' mail-card--done' : '');
        const meta = document.createElement('div');
        meta.className = 'mail-card__meta';
        const st =
            it.status === 'pending'
                ? '待发送'
                : it.status === 'sent'
                  ? '已发送'
                  : it.status === 'discarded'
                    ? '已丢弃'
                    : it.status;
        meta.textContent = `${st} · ${it.fromAddr || '?'} · ${(it.subject || '').slice(0, 80)}`;
        card.appendChild(meta);
        if (it.bodyText) {
            const pre = document.createElement('div');
            pre.className = 'hint';
            pre.style.marginBottom = '6px';
            pre.style.whiteSpace = 'pre-wrap';
            pre.textContent = '原文摘要：' + it.bodyText.slice(0, 500) + (it.bodyText.length > 500 ? '…' : '');
            card.appendChild(pre);
        }
        if (it.aiError) {
            const er = document.createElement('div');
            er.className = 'mail-card__err';
            er.textContent = it.aiError;
            card.appendChild(er);
        }
        if (it.status === 'pending') {
            const ta = document.createElement('textarea');
            ta.value = it.suggestedBody || '';
            ta.setAttribute('aria-label', '建议回复');
            card.appendChild(ta);
            const btns = document.createElement('div');
            btns.className = 'mail-card__btns';
            const bSend = document.createElement('button');
            bSend.type = 'button';
            bSend.textContent = '发送';
            const bDis = document.createElement('button');
            bDis.type = 'button';
            bDis.textContent = '丢弃';
            bSend.addEventListener('click', async () => {
                bSend.disabled = true;
                try {
                    await window.uClaw.updateMailDraft({
                        id: it.id,
                        suggestedBody: ta.value,
                    });
                    const r = await window.uClaw.sendMailReply(it.id);
                    if (r && r.ok) {
                        await refreshMailQueueOnly();
                    } else {
                        alert((r && r.error) || '发送失败');
                    }
                } catch (e) {
                    alert(e.message || '发送失败');
                }
                bSend.disabled = false;
            });
            bDis.addEventListener('click', async () => {
                await window.uClaw.discardMailItem(it.id);
                await refreshMailQueueOnly();
            });
            btns.appendChild(bSend);
            btns.appendChild(bDis);
            card.appendChild(btns);
        } else {
            const pre = document.createElement('div');
            pre.className = 'hint';
            pre.style.whiteSpace = 'pre-wrap';
            pre.textContent = it.suggestedBody || '（无正文）';
            card.appendChild(pre);
        }
        root.appendChild(card);
    }
}

async function refreshMailPanel() {
    if (!window.uClaw || !window.uClaw.getMailConfig) {
        return;
    }
    const cfg = await window.uClaw.getMailConfig();
    mailImapHadPassword = !!(cfg.imap && cfg.imap.hasPassword);
    mailSmtpHadPassword = !!(cfg.smtp && cfg.smtp.hasPassword);
    document.getElementById('mail-enabled').checked = !!cfg.enabled;
    document.getElementById('mail-poll-ms').value =
        cfg.pollIntervalMs || 120000;
    fillMailProviderSelect();
    const mp = document.getElementById('mail-provider');
    if (mp) {
        mp.value =
            cfg.providerId && cfg.providerId.trim()
                ? cfg.providerId.trim()
                : appConfig.lastProviderId || '';
    }
    if (cfg.imap) {
        document.getElementById('mail-imap-host').value = cfg.imap.host || '';
        document.getElementById('mail-imap-port').value =
            cfg.imap.port != null ? cfg.imap.port : 993;
        document.getElementById('mail-imap-user').value = cfg.imap.user || '';
        document.getElementById('mail-imap-pass').value = cfg.imap.password || '';
        document.getElementById('mail-imap-folder').value =
            cfg.imap.folder || 'INBOX';
        document.getElementById('mail-imap-ssl').checked =
            cfg.imap.ssl !== false;
    }
    if (cfg.smtp) {
        document.getElementById('mail-smtp-host').value = cfg.smtp.host || '';
        document.getElementById('mail-smtp-port').value =
            cfg.smtp.port != null ? cfg.smtp.port : 587;
        document.getElementById('mail-smtp-user').value = cfg.smtp.user || '';
        document.getElementById('mail-smtp-pass').value = cfg.smtp.password || '';
        document.getElementById('mail-smtp-from').value =
            cfg.smtp.fromAddress || '';
        document.getElementById('mail-smtp-ssl').checked = !!cfg.smtp.ssl;
        document.getElementById('mail-smtp-starttls').checked =
            cfg.smtp.starttls !== false;
    }
    const msg = document.getElementById('mail-save-msg');
    if (msg) {
        msg.textContent = '';
    }
    const presetSel = document.getElementById('mail-preset');
    if (presetSel) {
        presetSel.value = inferMailPreset(cfg);
        renderMailTutorial(presetSel.value);
    }
    const contacts = Array.isArray(cfg.contacts) ? cfg.contacts : [];
    mailContactsCache = contacts;
    renderMailContactsTable(contacts);
    await refreshMailQueueOnly();
}

/**
 * 从模型原文中拆分「推理链」与对用户可见的正文（thinking / Qwen 等标签）。
 * @returns {{ reasoning: string | null, answer: string }}
 */
function partitionModelReasoning(raw) {
    let s = String(raw || '').trim();
    const reasoningChunks = [];

    const pushReason = (chunk) => {
        const z = String(chunk || '').trim();
        if (z) {
            reasoningChunks.push(z);
        }
    };

    const fenceThink = new RegExp(
        '`'.repeat(3) +
            'think\\s*\\n([\\s\\S]*?)\\n' +
            '`'.repeat(3),
        'gi'
    );
    s = s.replace(fenceThink, (_, inner) => {
        pushReason(inner);
        return '\n';
    });

    const pairedThink = /<think\b[^>]*>([\s\S]*?)<\/think>/gi;
    s = s.replace(pairedThink, (_, inner) => {
        pushReason(inner);
        return '\n';
    });

    const pairedThought = /<thought\b[^>]*>([\s\S]*?)<\/thought>/gi;
    s = s.replace(pairedThought, (_, inner) => {
        pushReason(inner);
        return '\n';
    });

    const pipeThink = new RegExp(
        '<\\|' +
            'think' +
            '\\|>' +
            '([\\s\\S]*?)' +
            '<\\|' +
            '/' +
            'think' +
            '\\|>',
        'gi'
    );
    s = s.replace(pipeThink, (_, inner) => {
        pushReason(inner);
        return '\n';
    });

    const pipeRw = new RegExp(
        '<\\|' +
            'redacted_reasoning' +
            '\\|>' +
            '([\\s\\S]*?)' +
            '<\\|' +
            '/' +
            'redacted_reasoning' +
            '\\|>',
        'gi'
    );
    s = s.replace(pipeRw, (_, inner) => {
        pushReason(inner);
        return '\n';
    });

    const closePipeThink = '<|' + '/' + 'think' + '|>';
    let idx = s.lastIndexOf(closePipeThink);
    if (idx !== -1) {
        const after = s.slice(idx + closePipeThink.length).trim();
        const before = s.slice(0, idx).trim();
        if (after.length > 0) {
            pushReason(
                before
                    .replace(/^<think\b[^>]*>\s*/i, '')
                    .replace(
                        new RegExp(
                            '^<\\|' + 'think' + '\\|>\\s*',
                            'i'
                        ),
                        ''
                    )
                    .trim()
            );
            s = after;
        }
    } else {
        const closeXmlThink = '</think>';
        idx = s.lastIndexOf(closeXmlThink);
        if (idx !== -1) {
            const after = s.slice(idx + closeXmlThink.length).trim();
            const before = s.slice(0, idx).trim();
            if (after.length > 0) {
                pushReason(
                    before
                        .replace(/^<think\b[^>]*>\s*/i, '')
                        .replace(
                            new RegExp(
                                '^<\\|' + 'think' + '\\|>\\s*',
                                'i'
                            ),
                            ''
                        )
                        .trim()
                );
                s = after;
            }
        }
    }

    s = peelByAnswerHeader(s, pushReason);
    s = peelByMarkdownDelimiter(s, pushReason);

    /** 无 XML 标签时：首段像「先想一想」说明，其后空行才是对用户回复 */
    const peeled = peelLeadingReasoningParagraph(s, pushReason);
    s = peeled;

    const reasoning =
        reasoningChunks.length > 0 ? reasoningChunks.join('\n\n').trim() : null;
    return { reasoning: reasoning || null, answer: s.trim() };
}

/** 「解答：」「答案：」等标题后的正文 */
function peelByAnswerHeader(s, pushReason) {
    const t = String(s || '').trim();
    const re = /\n(?:解答|答案|回复|笑话(?:正文)?)[：:]\s*\n/;
    const m = re.exec(t);
    if (m && m.index >= 8) {
        const head = t.slice(0, m.index).trim();
        const tail = t.slice(m.index + m[0].length).trim();
        if (head.length >= 8 && tail.length >= 6) {
            pushReason(head);
            return tail;
        }
    }
    return t;
}

/** --- / *** 分隔：前半视为推理说明 */
function peelByMarkdownDelimiter(s, pushReason) {
    const t = String(s || '').trim();
    const reList = [/\n-{3,}\s*\n/, /\n\*{3,}\s*\n/, /\n_{3,}\s*\n/];
    for (const re of reList) {
        const m = re.exec(t);
        if (m && m.index >= 12) {
            const head = t.slice(0, m.index).trim();
            const tail = t.slice(m.index + m[0].length).trim();
            if (head.length >= 12 && tail.length >= 8) {
                pushReason(head);
                return tail;
            }
        }
    }
    return t;
}

/**
 * @param {function(string): void} pushReason
 */
function peelLeadingReasoningParagraph(s, pushReason) {
    let t = String(s || '').trim();
    const paras = t.split(/\n\s*\n+/);
    if (paras.length < 2) {
        return t;
    }
    const head = paras[0].trim();
    const tail = paras.slice(1).join('\n\n').trim();
    if (tail.length < 10) {
        return t;
    }
    const headLooksReason =
        /^(?:推理|思考|分析)(?:过程)?[：:\s]|^【(?:推理|思考)】/.test(head) ||
        /^让我(?:先)?|^我先|^好的[,，]\s*(?:我)?(?:来|想|试着)|^接下来|^首先|^Step\s*\d/i.test(
            head
        ) ||
        (/^[^\n]{4,120}$/.test(head) &&
            /(?:想一想|想一下|梳理|分析|推理|给你讲|说一个)/.test(head));
    if (headLooksReason) {
        pushReason(head);
        return tail;
    }
    return t;
}

/**
 * 去掉各类推理链标记及其内容（thinking / R1 / DeepSeek 风格等），仅保留对用户可见正文。
 * 与 partition 内剥离互补：用于新型号标签或漏网片段。
 */
function stripReasoningArtifactsOnly(text) {
    let s = String(text || '');
    /** 与 partitionModelReasoning 一致，并补充 DeepSeek-R1 常用 `<think>...</think>` */
    const paired = [
        /<think\b[^>]*>[\s\S]*?<\/think>/gi,
        /<thought\b[^>]*>[\s\S]*?<\/thought>/gi,
        /<\|think\|>[\s\S]*?<\|\/think\|>/gi,
        /<\|redacted_reasoning\|>[\s\S]*?<\|\/redacted_reasoning\|>/gi,
        new RegExp(
            '<think' +
                'ing\\b[^>]*>[\\s\\S]*?<\\/think' +
                'ing>',
            'gi'
        ),
    ];
    let prev;
    do {
        prev = s;
        for (const re of paired) {
            s = s.replace(re, '\n');
        }
    } while (s !== prev);
    s = s.replace(/```\s*thinking\s*\r?\n([\s\S]*?)\r?\n```/gi, '\n');
    s = s.replace(/```\s*think\s*\r?\n([\s\S]*?)\r?\n```/gi, '\n');
    /** 未闭合：从最早出现的起始标签截掉其后全部（流式中间态） */
    const unclosed = [
        /<think\b[^>]*>/i,
        /<\|think\|>/i,
        /<thought\b[^>]*>/i,
        new RegExp('<think' + 'ing\\b[^>]*>', 'i'),
    ];
    let cut = s.length;
    for (const re of unclosed) {
        const openAt = s.search(re);
        if (openAt !== -1 && openAt < cut) {
            cut = openAt;
        }
    }
    if (cut < s.length) {
        s = s.slice(0, cut);
    }
    return s.replace(/\n{3,}/g, '\n\n').trim();
}

function mountAssistantAnswerStack(stackEl, answerText) {
    stackEl.textContent = '';
    const ans = String(answerText || '').trim();
    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.textContent = ans;
    stackEl.appendChild(bubble);
}

/** 去掉行首尾 Markdown，便于识别 **user** / assistant窗前 等泄漏 */
function unwrapMarkdownRoleLine(line) {
    const t = String(line || '').trim();
    return t.replace(/^\*{1,2}\s*/, '').replace(/\s\*{1,2}$/, '').trim();
}

/**
 * 正文末尾追加下一轮「user + 用户原话」草稿（如笑话讲完后再出现 user / 继续），需剥掉。
 */
function stripTrailingPlainRoleEcho(s) {
    let lines = String(s || '').split(/\r?\n/);
    let changed = true;
    while (changed) {
        changed = false;
        while (lines.length && lines[lines.length - 1].trim() === '') {
            lines.pop();
        }
        if (lines.length < 2) {
            break;
        }
        const last = lines[lines.length - 1].trim();
        const prevRole = unwrapMarkdownRoleLine(
            lines[lines.length - 2]
        ).toLowerCase();
        if (prevRole !== 'user' || !last.length) {
            break;
        }
        const bodyAbove = lines.slice(0, -2).join('\n').trim();
        const shortEcho = last.length <= 160;
        const cmdEcho =
            /^(继续|好|好的|嗯|行|可以|是的|谢谢|再来|下一个|说下去|接着说|麻烦)/i.test(
                last
            );
        /** 仅剥「短回声」或常见续写口令，避免误删正文里严肃的 user/段落讨论 */
        if (bodyAbove.length >= 12 && (shortEcho || cmdEcho)) {
            lines = lines.slice(0, -2);
            changed = true;
        }
    }
    let out = lines.join('\n').trimEnd();
    /** ChatML：末尾再次出现下一轮 user 头（模型未停干净） */
    const IM_USER = /<\|im_start\|\>\s*user\b/i;
    const hit = out.search(IM_USER);
    if (hit !== -1 && hit >= 8) {
        out = out.slice(0, hit).trimEnd();
    }
    return out;
}

/** llama.cpp / 部分模型在文末输出的 `[end of text]`（可带 Markdown 加粗） */
function stripEndOfTextMarkers(text) {
    let s = String(text || '');
    s = s.replace(/\*{1,2}\s*\[\s*end\s+of\s+text\s*\]\s*\*{1,2}/gi, '');
    s = s.replace(/\[\s*end\s+of\s+text\s*\]/gi, '');
    return s.trimEnd();
}

function normalizeLooseWhitespace(s) {
    return String(s || '').replace(/\s+/g, ' ').trim();
}

/**
 * 模型在用户说「继续」时常复述上一轮助手全文；若检测到实质重复则清空，由上层决定是否提示。
 */
function dedupeVersusPreviousAssistant(reply, messages) {
    const cur = String(reply || '').trim();
    if (!cur.length || !Array.isArray(messages)) {
        return cur;
    }
    const assts = messages.filter((m) => m.role === 'assistant');
    if (!assts.length) {
        return cur;
    }
    const prev = String(assts[assts.length - 1].content || '').trim();
    if (!prev.length) {
        return cur;
    }
    const nc = normalizeLooseWhitespace(cur);
    const np = normalizeLooseWhitespace(prev);
    if (nc === np) {
        return '';
    }
    /** 规范化串几乎整段重复，仅末尾多零星标点/语气词 */
    if (nc.startsWith(np)) {
        const suf = nc.slice(np.length).trim();
        const sufCore = suf.replace(/[，。！？、\s~～…,.!?]/g, '');
        if (!sufCore.length || sufCore.length < 10) {
            return '';
        }
    }
    /** 原文级前缀重复：去掉上一轮全文后剩余为新内容则只展示剩余（否则视为未产出有效新段落） */
    if (cur.startsWith(prev)) {
        const rest = cur.slice(prev.length).trim();
        if (rest.length >= 14) {
            return rest;
        }
        return '';
    }
    return cur;
}

/**
 * 模型偶发输出：独占行的 user → 用户原文 → assistant（独占行或与正文粘连如 assistant昵称）。
 * 首轮上下文短时更易出现；也可能写成 **user** / **assistant**。
 * 只保留 assistant 段的真实回复。
 */
function stripLeadingPlainRoleConversation(s) {
    let t = String(s || '').trim();
    if (!t.length) {
        return t;
    }
    const lines = t.split(/\r?\n/);
    const headRole = unwrapMarkdownRoleLine(lines[0] || '').toLowerCase();
    if (lines.length < 2 || headRole !== 'user') {
        return t;
    }
    let contentStart = -1;
    for (let i = 1; i < lines.length; i++) {
        const trimmed = lines[i].trim();
        const bare = unwrapMarkdownRoleLine(trimmed).toLowerCase();
        if (bare === 'assistant') {
            contentStart = i + 1;
            break;
        }
        const m = trimmed.match(/^(?:\*{1,2}\s*)?assistant(?:\*{1,2})?(.*)$/i);
        if (!m) {
            continue;
        }
        const rest = m[1] || '';
        if (!rest.trim()) {
            contentStart = i + 1;
            break;
        }
        lines[i] = rest.replace(/^\s+/, '');
        contentStart = i;
        break;
    }
    if (contentStart === -1 || contentStart >= lines.length) {
        return t;
    }
    const out = lines.slice(contentStart).join('\n').trim();
    return out.length ? out : t;
}

/**
 * 本机模型有时会把整场 ChatML 用纯文本复述出来（system / user / assistant 独占行）。
 * 优先只保留最后一次 「assistant」 段之后的正文。
 */
function stripReplayedPlainTranscript(s) {
    let t = stripLeadingPlainRoleConversation(String(s || '').trim());
    if (!t.length) {
        return t;
    }
    const chunks = t.split(/\nassistant\s*\n/i);
    if (chunks.length >= 2) {
        const tail = chunks[chunks.length - 1].trim();
        if (tail.length >= 4) {
            /** 勿提前 return 裸 tail，否则会跳过末尾 user/继续 剥除 */
            return stripTrailingPlainRoleEcho(tail);
        }
    }
    /** assistant 与中文粘连，或 **assistant**窗前…（非 \\nassistant\\n） */
    const gluedChunks = t.split(
        /\n(?=(?:\*{1,2}\s*)?assistant[^\s\r\n])/i
    );
    if (gluedChunks.length >= 2) {
        const tail = gluedChunks[gluedChunks.length - 1]
            .replace(/^(?:\*{1,2}\s*)?assistant(?:\*{1,2})?/i, '')
            .trim();
        if (tail.length >= 2) {
            return stripTrailingPlainRoleEcho(tail);
        }
    }
    t = t.replace(/^system\s*\r?\n[\s\S]*?(?=\r?\nuser\s*\r?\n)/im, '').trim();
    t = stripTrailingPlainRoleEcho(t);
    return t;
}

function stripStandaloneRoleTranscriptLeak(s) {
    const lines = s.split(/\r?\n/);
    let userCnt = 0;
    let asstCnt = 0;
    let lastAsstIdx = -1;
    for (let i = 0; i < lines.length; i++) {
        const t = unwrapMarkdownRoleLine(lines[i]);
        if (/^user$/i.test(t)) {
            userCnt += 1;
        }
        if (/^assistant$/i.test(t)) {
            asstCnt += 1;
            lastAsstIdx = i;
        }
    }
    /** 至少两轮 assistant 或很长的泄漏块，才视为 transcript 复述，避免误判示例里的单行 role */
    const looksLikeMultiTurnDump =
        userCnt >= 1 &&
        asstCnt >= 2 &&
        lastAsstIdx >= 0 &&
        lastAsstIdx < lines.length - 1;
    const looksLikeLongDump =
        userCnt >= 1 &&
        asstCnt >= 1 &&
        lines.length >= 14 &&
        lastAsstIdx >= 0 &&
        lastAsstIdx < lines.length - 1;
    if (looksLikeMultiTurnDump || looksLikeLongDump) {
        const tail = lines.slice(lastAsstIdx + 1).join('\n').trim();
        if (tail.length) {
            return tail;
        }
    }
    return s;
}

/** 本机管道清洗（不含推理分段）；推理分段见 finalizeAssistantParts */
function prepareAssistantRaw(raw) {
    if (!raw) {
        return '';
    }
    const IM_END = '<|' + 'im_end' + '|>';
    const IM_END_TAIL = new RegExp(
        '\\s*' + IM_END.replace(/[|\\]/g, '\\$&') + '\\s*$',
        'gi'
    );

    let s = String(raw);
    s = stripReplayedPlainTranscript(s);

    for (const snip of [SYSTEM_MESSAGE.content, LEGACY_SYSTEM_TEXT]) {
        if (!snip || !s.includes(snip)) {
            continue;
        }
        const at = s.indexOf(snip);
        if (at >= 0 && at < 500) {
            s = (s.slice(0, at) + s.slice(at + snip.length)).replace(
                /^\s*\r?\n/,
                ''
            );
        }
    }

    s = stripEndOfTextMarkers(s);

    s = s.replace(/^\s*(<\|im_start\|>assistant\s*\n)+/gi, '');
    s = s.replace(IM_END_TAIL, '');
    s = s.replace(/<\|endoftext\|>|<\|eot_id\|>|<\/s>/gi, '');
    s = stripStandaloneRoleTranscriptLeak(s);
    let cut = s.search(/\n\s*(USER|SYSTEM|ASSISTANT)[:：]\s*/i);
    if (cut > 0) {
        s = s.slice(0, cut);
    }
    const other = s.search(/\n其他模式[:：]/);
    if (other > 0) {
        s = s.slice(0, other);
    }
    const lines = s.split('\n');
    let i = 0;
    for (; i < lines.length; i++) {
        const t = lines[i].trim();
        if (/^(SYSTEM|USER|ASSISTANT)[:：]/i.test(t)) {
            continue;
        }
        break;
    }
    return stripEndOfTextMarkers(
        stripTrailingPlainRoleEcho(lines.slice(i).join('\n').trim())
    );
}

/** 解析推理块 + 正文；持久化仅存正文 answer（不保留推理块用于 UI） */
function finalizeAssistantParts(raw) {
    const prepared = prepareAssistantRaw(raw);
    const parts = partitionModelReasoning(prepared);
    let answer = stripReasoningArtifactsOnly(parts.answer.trim());
    answer = stripTrailingPlainRoleEcho(answer);
    answer = stripEndOfTextMarkers(answer);
    return { reasoning: null, answer };
}

function sanitizeLocalReply(raw) {
    return finalizeAssistantParts(raw).answer.trim();
}

function fillMailProviderSelect() {
    const el = document.getElementById('mail-provider');
    if (!el || !appConfig.providers) {
        return;
    }
    el.innerHTML = '';
    for (const p of appConfig.providers) {
        const o = document.createElement('option');
        o.value = p.id;
        o.textContent = p.name;
        el.appendChild(o);
    }
}

function fillSelect() {
    selectEl.innerHTML = '';
    for (const p of appConfig.providers) {
        const o = document.createElement('option');
        o.value = p.id;
        o.textContent = p.name;
        selectEl.appendChild(o);
    }
    selectEl.value = appConfig.lastProviderId;
    if (aiCsProviderEl) {
        aiCsProviderEl.innerHTML = selectEl.innerHTML;
        aiCsProviderEl.value = selectEl.value;
    }
    fillMailProviderSelect();
    const mp = document.getElementById('mail-provider');
    if (mp && appConfig.lastProviderId) {
        const has = appConfig.providers.some((x) => x.id === appConfig.lastProviderId);
        if (has) {
            mp.value = appConfig.lastProviderId;
        }
    }
}

let assistantBuffer = '';
let isWaiting = false;
let roundErrored = false;
let typingNode = null;

function removeTyping() {
    if (typingNode && typingNode.parentNode) {
        typingNode.remove();
    }
    typingNode = null;
}

function showTyping() {
    removeTyping();
    const wrap = document.createElement('div');
    wrap.className = 'msg msg--assistant typing';
    wrap.setAttribute('aria-live', 'polite');
    const meta = document.createElement('div');
    meta.className = 'msg-meta';
    meta.textContent = '助手';
    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.innerHTML =
        '<span class="typing-generating">生成中</span>' +
        '<span class="typing-dots" aria-hidden="true"><span></span><span></span><span></span></span>';
    bubble.setAttribute('aria-label', '生成中');
    wrap.appendChild(meta);
    wrap.appendChild(bubble);
    chatEl.appendChild(wrap);
    typingNode = wrap;
    scrollChatToBottom();
}

function appendUserMessage(text, createdAt) {
    const wrap = document.createElement('div');
    wrap.className = 'msg msg--user';
    const meta = document.createElement('div');
    meta.className = 'msg-meta';
    meta.textContent = '我 · ' + formatMsgDateTime(createdAt);
    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.textContent = text;
    wrap.appendChild(meta);
    wrap.appendChild(bubble);
    chatEl.appendChild(wrap);
    scrollChatToBottom();
}

function appendErrorLine(message) {
    const wrap = document.createElement('div');
    wrap.className = 'msg msg--error';
    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.textContent = message;
    wrap.appendChild(bubble);
    chatEl.appendChild(wrap);
    scrollChatToBottom();
}

function appendHintLine(message) {
    const wrap = document.createElement('div');
    wrap.className = 'msg msg--hint';
    const bubble = document.createElement('div');
    bubble.className = 'msg-bubble';
    bubble.textContent = message;
    wrap.appendChild(bubble);
    chatEl.appendChild(wrap);
    scrollChatToBottom();
}

if (window.uClaw) {
    window.uClaw.onResponse((data) => {
        if (data && data.target === 'ai-cs') {
            if (data.type === 'error') {
                clearAiCsLoadingBubble();
                if (aiCsSend) aiCsSend.disabled = false;
                aiCsAssistantBuffer = '';
                appendAiCsBubble('hint', data.message || '推理失败');
                return;
            }
            if (data.type === 'chunk') {
                clearAiCsLoadingBubble();
                aiCsAssistantBuffer += data.text || '';
                return;
            }
            if (data.type === 'done') {
                clearAiCsLoadingBubble();
                if (aiCsSend) aiCsSend.disabled = false;
                const raw = aiCsAssistantBuffer;
                aiCsAssistantBuffer = '';
                let finalText = '';
                try {
                    finalText = finalizeAssistantParts(raw || '').answer.trim();
                } catch (_) {
                    finalText = String(raw || '').trim();
                }
                if (!finalText) {
                    finalText = '（无回复）';
                }
                const assistantAt = Date.now();
                const cSes = getActiveAiCsConversation();
                appendAiCsBubble('assistant', finalText, assistantAt);
                if (cSes) {
                    cSes.messages.push({
                        role: 'assistant',
                        content: finalText,
                        createdAt: assistantAt,
                    });
                    touchActiveAiCsMeta();
                    renderConvList();
                    persistAiCsConversations();
                    void flushAiCsSessionSnapshotRemote(cSes);
                }
                return;
            }
            return;
        }

        if (data.type === 'error') {
            roundErrored = true;
            isWaiting = false;
            setComposerWaiting(false);
            removeTyping();
            appendErrorLine('[错误] ' + data.message);
            assistantBuffer = '';
            if (inputEl) {
                setTimeout(() => inputEl.focus(), 0);
            }
            return;
        }
        if (data.type === 'chunk') {
            assistantBuffer += data.text;
            /** 流式阶段不把原文写入气泡，仅保留「生成中」占位，结束后再一次性展示 */
            scrollChatToBottom();
            return;
        }
        if (data.type === 'done') {
            isWaiting = false;
            setComposerWaiting(false);
            removeTyping();
            const isLocalProv =
                document.getElementById('active-provider').value === 'local';
            const c = getActiveConversation();
            let parts = {
                reasoning: null,
                answer: assistantBuffer,
            };
            let finalText = assistantBuffer;
            /** @type {number | null} */
            let assistantReplyAt = null;
            let duplicateSuppressed = false;
            if (!roundErrored) {
                parts = finalizeAssistantParts(assistantBuffer);
                const beforeDedupe = parts.answer.trim();
                finalText = beforeDedupe;
                if (c && beforeDedupe.length) {
                    finalText = dedupeVersusPreviousAssistant(
                        beforeDedupe,
                        c.messages
                    ).trim();
                    duplicateSuppressed =
                        beforeDedupe.length > 0 && finalText.length === 0;
                }
                if (finalText.length) {
                    assistantReplyAt = Date.now();
                    const wrap = document.createElement('div');
                    wrap.className = 'msg msg--assistant';
                    const meta = document.createElement('div');
                    meta.className = 'msg-meta';
                    meta.textContent =
                        '助手 · ' + formatMsgDateTime(assistantReplyAt);
                    const stack = document.createElement('div');
                    stack.className = 'msg-asst-stack';
                    wrap.appendChild(meta);
                    wrap.appendChild(stack);
                    chatEl.appendChild(wrap);
                    mountAssistantAnswerStack(stack, finalText);
                } else if (duplicateSuppressed) {
                    appendHintLine(
                        '（本轮回复与上一轮助手内容重复，已隐藏。请换一种说法或重试。）'
                    );
                }
            }
            if (c) {
                if (!roundErrored && finalText.trim().length) {
                    c.messages.push({
                        role: 'assistant',
                        content: finalText.trim(),
                        createdAt:
                            assistantReplyAt != null
                                ? assistantReplyAt
                                : Date.now(),
                    });
                } else if (
                    !roundErrored &&
                    !finalText.trim().length &&
                    isLocalProv &&
                    !duplicateSuppressed
                ) {
                    appendHintLine(
                        '（未收到本机模型输出。可重试、查看开发者工具，或换用云端源。）'
                    );
                }
                touchActiveMeta();
                renderConvList();
                persistConversations();
            }
            roundErrored = false;
            assistantBuffer = '';
            scrollChatToBottom();
            if (inputEl) {
                setTimeout(() => inputEl.focus(), 0);
            }
        }
    });
}

/**
 * 截断历史，避免超出模型 context window。
 * 始终保留 system message，再从最新一条向前取最多 MAX_CONTEXT_MESSAGES 条。
 */
function buildContextMessages(messages) {
    const system = messages.filter((m) => m.role === 'system');
    const nonSystem = messages.filter((m) => m.role !== 'system');
    if (nonSystem.length <= MAX_CONTEXT_MESSAGES) {
        return messages;
    }
    return [...system, ...nonSystem.slice(-MAX_CONTEXT_MESSAGES)];
}

async function send() {
    if (isWaiting || !window.uClaw) return;
    // 仅主「对话」视图可发主会话；隐藏时 #input 不应获焦（见 setMainView + inert）
    if (viewChat && viewChat.inert) {
        return;
    }
    const c = getActiveConversation();
    if (!c) return;
    const text = (inputEl.value || '').trim();
    if (!text) return;

    if (text.startsWith('@发邮件')) {
        const mailParse = text.match(/^@发邮件\s+(\S+)\s+([\s\S]+)$/);
        inputEl.value = '';
        inputEl.style.height = 'auto';
        autoresizeTextarea();
        removeEmptyPlaceholder();
        const userAt = Date.now();
        appendUserMessage(text, userAt);
        c.messages.push({ role: 'user', content: text, createdAt: userAt });
        touchActiveMeta();
        renderConvList();
        persistConversations();
        if (!mailParse) {
            appendHintLine(
                '格式：@发邮件 昵称 正文（昵称与「邮件」页客户列表一致，勿含空格）'
            );
            return;
        }
        const nick = mailParse[1];
        const body = mailParse[2].trim();
        let contacts = mailContactsCache;
        if (window.uClaw.getMailConfig) {
            try {
                const fresh = await window.uClaw.getMailConfig();
                contacts = Array.isArray(fresh.contacts) ? fresh.contacts : [];
                mailContactsCache = contacts;
            } catch (_) {
                /* 使用缓存 */
            }
        }
        const lower = nick.toLowerCase();
        const contact =
            contacts.find((x) => x.nickname === nick) ||
            contacts.find(
                (x) =>
                    x.nickname && x.nickname.toLowerCase() === lower
            );
        if (!contact || !contact.email || !contact.email.includes('@')) {
            appendErrorLine(
                '未找到昵称为「' +
                    nick +
                    '」的客户或未填写有效邮箱，请到「邮件」页维护「客户邮箱」列表。'
            );
            return;
        }
        if (!window.uClaw.sendMailCompose) {
            appendErrorLine('当前版本不支持对话发信，请更新客户端。');
            return;
        }
        const subj =
            'PX-Claw：' +
            (body.slice(0, 60) + (body.length > 60 ? '…' : ''));
        try {
            const r = await window.uClaw.sendMailCompose({
                to: contact.email.trim(),
                subject: subj,
                text: body,
            });
            if (r && r.ok) {
                appendHintLine(
                    '已发送至 ' +
                        contact.email +
                        '（' +
                        (contact.nickname || nick) +
                        '）'
                );
            } else {
                appendErrorLine((r && r.error) || '发送失败');
            }
        } catch (e) {
            appendErrorLine((e && e.message) || '发送失败');
        }
        return;
    }

    inputEl.value = '';
    inputEl.style.height = 'auto';
    autoresizeTextarea();

    removeEmptyPlaceholder();
    const userAt = Date.now();
    appendUserMessage(text, userAt);

    c.messages.push({ role: 'user', content: text, createdAt: userAt });
    touchActiveMeta();
    renderConvList();
    persistConversations();

    assistantBuffer = '';
    roundErrored = false;
    isWaiting = true;
    setComposerWaiting(true);
    showTyping();

    const providerId = document.getElementById('active-provider').value;
    window.uClaw.sendPrompt({
        messages: buildContextMessages(c.messages),
        providerId,
    });
}

selectEl.addEventListener('change', async () => {
    appConfig.lastProviderId = selectEl.value;
    if (aiCsProviderEl) aiCsProviderEl.value = selectEl.value;
    if (window.uClaw && window.uClaw.setLastProvider) {
        await window.uClaw.setLastProvider(selectEl.value);
    }
    warmupLocalProviderIfNeeded(selectEl.value);
});

if (aiCsProviderEl) {
    aiCsProviderEl.addEventListener('change', async () => {
        selectEl.value = aiCsProviderEl.value;
        appConfig.lastProviderId = aiCsProviderEl.value;
        if (window.uClaw && window.uClaw.setLastProvider) {
            await window.uClaw.setLastProvider(aiCsProviderEl.value);
        }
        warmupLocalProviderIfNeeded(aiCsProviderEl.value);
    });
}

async function init() {
    if (!window.uClaw) return;
    isWaiting = false;
    if (inputEl) {
        inputEl.disabled = false;
        inputEl.readOnly = false;
    }
    setComposerWaiting(false);
    const hadPersistedChats = loadConversations();
    ensureSeedConversation();
    trimConversations();
    if (hadPersistedChats) {
        startNewConversation();
    }

    const hadPersistedAiCs = loadAiCsConversationsFromStorage();
    ensureSeedAiCsConversation();
    trimAiCsConversations();
    if (hadPersistedAiCs) {
        startNewAiCsConversation({ focus: false });
    }

    renderConvList();
    renderChatFromMessages();
    persistConversations();
    persistAiCsConversations();

    appConfig = await window.uClaw.getProviders();
    await loadSystemSessionFromMain();
    if (systemSession && window.uClaw.fetchUserProfile) {
        const vr = await window.uClaw.fetchUserProfile();
        if (vr.sessionExpired) {
            applySessionExpiredUi(vr.error || '登录已过期，请重新登录', false);
        } else if (vr.ok && vr.user) {
            systemSession = await window.uClaw.getSystemSession();
            refreshNavUser();
        }
    }
    fillSelect();
    renderProviderForms(appConfig);
    setMainView('chat');
    if (inputEl) {
        inputEl.focus();
    }
    // 若当前选中的是本机模型，立即后台预热，避免首次发消息时等待冷启动
    if (window.uClaw && window.uClaw.warmupLocal) {
        const selectedId = selectEl.value;
        warmupLocalProviderIfNeeded(selectedId);
    }

    try {
        if (window.uClaw.getMailConfig) {
            const mc0 = await window.uClaw.getMailConfig();
            mailContactsCache = Array.isArray(mc0.contacts) ? mc0.contacts : [];
        }
    } catch (_) {
        /* 忽略：进入邮件页时会再拉取 */
    }

    const mailSaveBtn = document.getElementById('mail-save-btn');
    const mailPollBtn = document.getElementById('mail-poll-btn');
    if (mailSaveBtn && window.uClaw.saveMailConfig) {
        mailSaveBtn.addEventListener('click', async () => {
            const msg = document.getElementById('mail-save-msg');
            if (msg) {
                msg.textContent = '保存中…';
            }
            try {
                const r = await window.uClaw.saveMailConfig(
                    collectMailConfigFromForm()
                );
                if (msg) {
                    msg.textContent = r && r.ok ? '已保存' : '保存失败';
                }
                if (r && r.ok) {
                    await refreshMailPanel();
                }
            } catch (e) {
                if (msg) {
                    msg.textContent = e.message || '保存失败';
                }
            }
        });
    }
    if (mailPollBtn && window.uClaw.pollMailNow) {
        mailPollBtn.addEventListener('click', async () => {
            const msg = document.getElementById('mail-save-msg');
            if (msg) {
                msg.textContent = '收信中…';
            }
            try {
                await window.uClaw.pollMailNow();
                if (msg) {
                    msg.textContent = '已触发拉取';
                }
                await refreshMailQueueOnly();
            } catch (e) {
                if (msg) {
                    msg.textContent = e.message || '失败';
                }
            }
        });
    }
    if (window.uClaw.onMailQueueUpdated) {
        window.uClaw.onMailQueueUpdated(() => {
            if (viewMail && viewMail.classList.contains('view--active')) {
                refreshMailQueueOnly().catch(() => {});
            }
        });
    }

    const mailPresetSel = document.getElementById('mail-preset');
    if (mailPresetSel) {
        mailPresetSel.addEventListener('change', () => {
            const v = mailPresetSel.value;
            if (v !== 'custom') {
                applyMailPresetFields(v);
            }
            renderMailTutorial(v);
        });
        renderMailTutorial(mailPresetSel.value || 'custom');
    }

    const mailContactAdd = document.getElementById('mail-contact-add');
    if (mailContactAdd) {
        mailContactAdd.addEventListener('click', () => {
            const tbody = document.getElementById('mail-contacts-tbody');
            if (tbody) {
                appendMailContactRow(tbody, {});
            }
        });
    }
}
init().catch((e) => {
    isWaiting = false;
    if (inputEl) {
        inputEl.disabled = false;
        inputEl.readOnly = false;
    }
    if (sendBtn) sendBtn.disabled = false;
    if (btnNewChat) btnNewChat.disabled = false;
    appendErrorLine('[错误] 无法加载配置: ' + (e && e.message));
});
