package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.pagination.Paging;
import com.claw.system.param.InstallPackagePageParam;
import com.claw.system.service.InstallPackageService;
import com.claw.system.vo.InstallPackageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/installPackage")
@Api(value = "安装包管理", tags = {"安装包管理"})
public class InstallPackageController {

    @Autowired
    private InstallPackageService installPackageService;

    @PostMapping("/getPageList")
    @ApiOperation(value = "分页列表", response = InstallPackageVO.class)
    public ApiResult<Paging<InstallPackageVO>> getPageList(@Validated @RequestBody InstallPackagePageParam param) throws Exception {
        return ApiResult.ok(installPackageService.page(param));
    }

    @PostMapping("/upload")
    @ApiOperation(value = "上传安装包")
    public ApiResult<Boolean> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "versionLabel", required = false) String versionLabel,
            @RequestParam(value = "remark", required = false) String remark) throws Exception {
        installPackageService.upload(file, versionLabel, remark);
        return ApiResult.ok(true);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation(value = "删除（下架）")
    public ApiResult<Boolean> delete(@PathVariable("id") Long id) throws Exception {
        return ApiResult.result(installPackageService.delete(id));
    }
}
