package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.log.Module;
import com.claw.system.service.CaptchaService;
import com.claw.system.vo.PictureCodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sakura
 * @date 2023/8/14 14:15
 */
@Slf4j
@RestController
@RequestMapping("/captcha")
@Api(value = "图片验证码管理", tags = {"图片验证码管理"})
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 图片验证码
     */
    @GetMapping("getPictureCode")
    @ApiOperation(value = "获取图片验证码")
    public ApiResult<PictureCodeVO> getPictureCode() throws Exception {
        PictureCodeVO pictureCodeVo = captchaService.getPictureCode();
        return ApiResult.ok(pictureCodeVo);
    }
}
