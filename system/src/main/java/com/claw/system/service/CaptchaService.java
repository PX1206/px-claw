package com.claw.system.service;

import com.claw.system.vo.PictureCodeVO;

/**
 * @author Sakura
 * @date 2023/8/14 14:25
 */
public interface CaptchaService {

    /**
     * @description: 获取图片验证码
     */
    PictureCodeVO getPictureCode() throws Exception;

}
