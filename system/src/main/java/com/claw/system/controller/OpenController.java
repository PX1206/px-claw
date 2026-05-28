package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 公开接口（无需登录）
 */
@RestController
@RequestMapping("/open")
@Api(value = "公开接口", tags = {"公开接口"})
public class OpenController {

    @Value("${rsa.data.public-key-str}")
    private String rsaPublicKey;

    @GetMapping("/rsaPublicKey")
    @ApiOperation("获取RSA公钥（用于密码加密登录）")
    public ApiResult<Map<String, String>> getRsaPublicKey() {
        Map<String, String> map = new HashMap<>();
        map.put("publicKey", rsaPublicKey);
        return ApiResult.ok(map);
    }
}
