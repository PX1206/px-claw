package com.claw.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.claw.common.constant.CommonConstant;
import com.claw.common.exception.BusinessException;
import com.claw.common.redis.RedisUtil;
import com.claw.common.tool.AliyunSmsUtils;
import com.claw.common.tool.CommonUtil;
import com.claw.common.tool.DateUtil;
import com.claw.system.param.SmsCodeParam;
import com.claw.system.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Sakura
 * @date 2023/8/14 14:25
 */
@Service
@Slf4j
public class SmsServiceImpl implements SmsService {

    @Value("${aliyun.sms.send-max-num}")
    Integer ALIYUN_SMS_SEND_MAX_NUM;

    @Value("${aliyun.sms.register.sign-name}")
    String ALIYUN_SMS_REGISTER_SIGN_NAME;

    @Value("${aliyun.sms.register.template-code}")
    String ALIYUN_SMS_REGISTER_TEMPLATE_CODE;

    @Value("${aliyun.sms.register.template-param}")
    String ALIYUN_SMS_REGISTER_TEMPLATE_PARAM;

    @Autowired
    CommonUtil commonUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private AliyunSmsUtils aliyunSmsUtils;

    @Override
    public String getSMSCode(SmsCodeParam smsCodeParam) throws Exception {
        // 校验图片验证码是否正确，防止脚本刷短信
        if (!commonUtil.checkCode(smsCodeParam.getKey(), smsCodeParam.getPictureCode())) {
            throw new BusinessException(500, "图片验证码错误");
        }

        // 用户每天发送短信不得超过最大限制数
        long smsNum = redisUtil.incr(CommonConstant.SMS_SEND_NUM + smsCodeParam.getMobile(), 1);
        if (smsNum > ALIYUN_SMS_SEND_MAX_NUM) {
            throw new BusinessException(500, "当天短信发送数量已达最大");
        }
        // 设置当前短信记录发送有效期,当前日期到晚上23：59:59
        redisUtil.expire(CommonConstant.SMS_SEND_NUM + smsCodeParam.getMobile(), DateUtil.timeToMidnight());

        // 发送短信验证码
        String smsCode = String.valueOf((int) ((Math.random() * 9 + 1) * 100000)); // 生成一个6位数验证码
        log.info(smsCode); // 测试用，建议删除
        JSONObject jsonParam = new JSONObject();
        jsonParam.put(ALIYUN_SMS_REGISTER_TEMPLATE_PARAM, smsCode);
        boolean sendStatus = aliyunSmsUtils.sendSms(smsCodeParam.getMobile(), jsonParam.toJSONString(),
                ALIYUN_SMS_REGISTER_SIGN_NAME, ALIYUN_SMS_REGISTER_TEMPLATE_CODE);
        if (!sendStatus) {
            redisUtil.decr(CommonConstant.SMS_SEND_NUM + smsCodeParam.getMobile(), 1); // 短信发送失败不计算次数
            throw new BusinessException(500, "短信发送失败，请联系管理员");
        }
        // 将短信验证码放入Redis,有效期5分钟
        redisUtil.set(CommonConstant.SMS_CODE + smsCodeParam.getMobile(), smsCode, 60 * 5);

        return "验证码已发送至手机号：" + smsCodeParam.getMobile() + ",5分钟内有效！";
    }
}
