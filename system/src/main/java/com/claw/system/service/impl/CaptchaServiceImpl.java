package com.claw.system.service.impl;

import com.claw.common.captcha.GifCaptcha;
import com.claw.common.redis.RedisUtil;
import com.claw.system.service.CaptchaService;
import com.claw.system.vo.PictureCodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author Sakura
 * @date 2023/8/14 14:25
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired

    @Override
    public PictureCodeVO getPictureCode() throws Exception {
        // 通过GifCaptcha生成图片验证码
        GifCaptcha gifCaptcha = new GifCaptcha(130, 48, 5);
        String verCode = gifCaptcha.text().toLowerCase();

        PictureCodeVO pictureCodeVO = new PictureCodeVO();
        String key = UUID.randomUUID().toString();
        pictureCodeVO.setKey(key);
        pictureCodeVO.setImage(gifCaptcha.toBase64());

        log.info(key + "+++++++++++++" + verCode); // 测试用的，建议删除

        // 将验证码放入Redis,有效期5分钟
        redisUtil.set(key, verCode.toLowerCase(), 60 * 5);

        return pictureCodeVO;
    }
}
