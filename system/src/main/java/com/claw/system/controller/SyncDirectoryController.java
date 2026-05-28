package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.exception.BusinessException;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.SyncDirectory;
import com.claw.system.service.SyncDirectoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 同步目录控制器
 */
@Slf4j
@RestController
@RequestMapping("/syncDirectory")
@Api(value = "同步目录", tags = {"同步目录"})
public class SyncDirectoryController {

    @Autowired
    private SyncDirectoryService syncDirectoryService;

    @PostMapping("/add")
    @ApiOperation("添加同步目录")
    public ApiResult<SyncDirectory> add(@RequestParam String localPath,
                                        @RequestParam(required = false) String displayName) {
        SyncDirectory dir = syncDirectoryService.add(localPath, displayName);
        return ApiResult.ok(dir);
    }

    @PostMapping("/remove/{id}")
    @ApiOperation("删除同步目录")
    public ApiResult<Boolean> remove(@PathVariable Long id) {
        boolean ok = syncDirectoryService.remove(id);
        return ApiResult.ok(ok);
    }

    @PostMapping("/updatePath")
    @ApiOperation("更换本机同步路径（换电脑时重新关联本地文件夹）")
    public ApiResult<SyncDirectory> updatePath(@RequestParam Long id, @RequestParam String localPath) {
        SyncDirectory dir = syncDirectoryService.updateLocalPath(id, localPath);
        return ApiResult.ok(dir);
    }

    @GetMapping("/list")
    @ApiOperation("获取当前用户的同步目录列表")
    public ApiResult<List<SyncDirectory>> list() {
        List<SyncDirectory> list = syncDirectoryService.listByCurrentUser();
        return ApiResult.ok(list);
    }

    @GetMapping("/listAll")
    @ApiOperation("管理员：获取所有同步目录")
    public ApiResult<List<SyncDirectory>> listAll(@RequestParam(required = false) Long userId) {
        if (!LoginUtil.isAdmin()) {
            throw new BusinessException(403, "无权限");
        }
        List<SyncDirectory> list = syncDirectoryService.listAll(userId);
        return ApiResult.ok(list);
    }
}
