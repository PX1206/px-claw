package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.system.service.InstallPackageService;
import com.claw.system.vo.OpenInstallPackageLatestVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * 公开下载（无需登录，凭随机码下载）
 */
@Slf4j
@RestController
@RequestMapping("/open/installPackage")
@Api(value = "安装包公开下载", tags = {"公开接口"})
public class OpenInstallPackageController {

    @Autowired
    private InstallPackageService installPackageService;

    @GetMapping("/download/{downloadCode}/{fileName:.+}")
    @ApiOperation("下载安装包（路径含展示文件名，便于 electron-updater 识别 .exe）")
    public void downloadWithFileName(
            HttpServletResponse response,
            @PathVariable("downloadCode") String downloadCode,
            @PathVariable("fileName") String fileName) throws Exception {
        installPackageService.downloadPublic(downloadCode, fileName, response);
    }

    @GetMapping("/download/{downloadCode}")
    @ApiOperation("下载安装包（仅随机码，兼容旧链接）")
    public void download(HttpServletResponse response, @PathVariable("downloadCode") String downloadCode) throws Exception {
        installPackageService.downloadPublic(downloadCode, null, response);
    }

    @GetMapping("/latest.yml")
    @ApiOperation("桌面端自动更新描述（electron-updater generic，取最新 .exe 且含语义化版本）")
    public void latestYml(HttpServletResponse response) throws Exception {
        installPackageService.writeElectronLatestYml(response);
    }

    @GetMapping("/latest-info")
    @ApiOperation("最新 Windows 桌面安装包元信息（与 latest.yml 规则一致，供 Web 登录页等展示下载链接，无需登录）")
    public ApiResult<OpenInstallPackageLatestVO> latestInfo() {
        OpenInstallPackageLatestVO vo = installPackageService.getLatestWindowsPublicInfo();
        if (vo == null) {
            return new ApiResult<OpenInstallPackageLatestVO>()
                    .setSuccess(false)
                    .setCode(404)
                    .setMessage("暂无可用 Windows 桌面安装包，或版本说明/文件名中未包含语义化版本号（如 1.0.1），或文件已下架。");
        }
        return ApiResult.ok(vo);
    }
}
