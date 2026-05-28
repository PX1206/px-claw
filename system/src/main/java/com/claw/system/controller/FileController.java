package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.log.Module;
import com.claw.common.pagination.Paging;
import com.claw.system.dto.SyncFileMetaDto;
import com.claw.system.param.FilePageParam;
import com.claw.system.service.FileService;
import com.claw.system.vo.DirectoryTreeNodeVo;
import com.claw.system.vo.FileVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件表 控制器
 *
 * @author Sakura
 * @since 2022-08-22
 */
@Slf4j
@RestController
@RequestMapping("/file")
@Api(value = "文件管理", tags = {"文件管理"})
public class FileController {

    @Autowired
    private FileService fileService;


    /**
     * 删除文件表
     */
    @PostMapping("/delete/{code}")
    @ApiOperation(value = "删除文件", response = ApiResult.class)
    public ApiResult<Boolean> deleteFile(@PathVariable("code") String code) throws Exception {
        boolean flag = fileService.deleteFile(code);
        return ApiResult.result(flag);
    }

    @GetMapping("/listBySyncDir")
    @ApiOperation(value = "按同步目录列出文件元数据（桌面端 diff 用）", response = SyncFileMetaDto.class)
    public ApiResult<java.util.List<SyncFileMetaDto>> listBySyncDir(@RequestParam Long syncDirectoryId) throws Exception {
        return ApiResult.ok(fileService.listBySyncDirectory(syncDirectoryId));
    }

    /**
     * 获取目录树（同步目录+子目录）
     */
    @GetMapping("/directoryTree")
    @ApiOperation(value = "目录树", response = DirectoryTreeNodeVo.class)
    public ApiResult<java.util.List<DirectoryTreeNodeVo>> getDirectoryTree() throws Exception {
        return ApiResult.ok(fileService.getDirectoryTree());
    }

    /**
     * 文件表分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "文件分页列表", response = FileVo.class)
    public ApiResult<Paging<FileVo>> getFilePageList(@Validated @RequestBody FilePageParam filePageParam) throws Exception {
        Paging<FileVo> paging = fileService.getFilePageList(filePageParam);
        return ApiResult.ok(paging);
    }

    @PostMapping("/upload")
    @ApiOperation(value = "上传文件", response = ApiResult.class)
    public ApiResult<String> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String path = fileService.upload(file);
        return ApiResult.ok(path);
    }

    @PostMapping("/uploadForSync")
    @ApiOperation(value = "同步上传（桌面端）", response = ApiResult.class)
    public ApiResult<String> uploadForSync(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) Long syncDirectoryId,
            @RequestParam(required = false) String relativePath) throws Exception {
        String path = fileService.uploadForSync(file, syncDirectoryId, relativePath);
        return ApiResult.ok(path);
    }

    /**
     * 下载文件
     */
    @GetMapping("/{code}")
    @ApiOperation(value = "下载")
    public void download(HttpServletResponse response, @PathVariable("code") String code) throws Exception {
        fileService.download(response, code);
    }

}

