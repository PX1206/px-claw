package com.claw.system.service;

import com.claw.system.param.SmsCodeParam;

/**
 * @author Sakura
 * @date 2023/8/14 14:25
 */
public interface SmsService {

    /**
     * @description: 获取短信验证码
     */
    String getSMSCode(SmsCodeParam smsCodeParam) throws Exception;


}
