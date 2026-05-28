const { contextBridge, ipcRenderer } = require('electron');

const KEY_PLACEHOLDER = '********';

contextBridge.exposeInMainWorld('uClaw', {
    KEY_PLACEHOLDER,

    getProviders: () => ipcRenderer.invoke('providers:get'),
    saveProviders: (data) => ipcRenderer.invoke('providers:save', data),
    setLastProvider: (id) => ipcRenderer.invoke('providers:setLast', id),

    sendPrompt(payload) {
        ipcRenderer.send('llm:send', payload);
    },

    pickChatFile: () => ipcRenderer.invoke('chat:pickFile'),

    warmupLocal(providerId) {
        ipcRenderer.invoke('llm:warmup', providerId).catch(() => {});
    },

    onResponse(callback) {
        if (typeof callback !== 'function') return;
        ipcRenderer.removeAllListeners('llm:event');
        ipcRenderer.on('llm:event', (_event, data) => {
            callback(data);
        });
    },

    getSystemSession: () => ipcRenderer.invoke('system:session:get'),
    systemLogin: (payload) => ipcRenderer.invoke('system:session:login', payload),
    systemLogout: () => ipcRenderer.invoke('system:session:logout'),
    systemCaptchaGet: (payload) =>
        ipcRenderer.invoke('system:captcha:get', payload),
    systemSmsSend: (payload) => ipcRenderer.invoke('system:sms:send', payload),
    systemRegister: (payload) =>
        ipcRenderer.invoke('system:user:register', payload),
    openExternalUrl: (url) => ipcRenderer.invoke('system:openExternal', url),

    fetchUserProfile: () => ipcRenderer.invoke('system:user:fetchProfile'),
    saveUserProfile: (payload) =>
        ipcRenderer.invoke('system:user:saveProfile', payload),
    pickUserAvatar: () => ipcRenderer.invoke('system:user:pickAvatar'),
    pickAvatarFile: () => ipcRenderer.invoke('system:user:pickAvatarFile'),
    readImageFile: (filePath) =>
        ipcRenderer.invoke('system:user:readImageFile', filePath),
    uploadAvatarBase64: (payload) =>
        ipcRenderer.invoke('system:user:uploadAvatarBase64', payload),
    updateUserPassword: (payload) =>
        ipcRenderer.invoke('system:user:updatePassword', payload),

    getMailConfig: () => ipcRenderer.invoke('mail:getConfig'),
    saveMailConfig: (data) => ipcRenderer.invoke('mail:saveConfig', data),
    getMailQueue: () => ipcRenderer.invoke('mail:getQueue'),
    updateMailDraft: (payload) =>
        ipcRenderer.invoke('mail:updateDraft', payload),
    sendMailReply: (id) => ipcRenderer.invoke('mail:sendReply', id),
    sendMailCompose: (payload) => ipcRenderer.invoke('mail:sendCompose', payload),
    discardMailItem: (id) => ipcRenderer.invoke('mail:discardMail', id),
    pollMailNow: () => ipcRenderer.invoke('mail:pollNow'),
    onMailQueueUpdated: (callback) => {
        if (typeof callback !== 'function') {
            return;
        }
        ipcRenderer.removeAllListeners('mail:queueUpdated');
        ipcRenderer.on('mail:queueUpdated', () => callback());
    },
});
