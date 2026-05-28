package com.claw.system.controller;

import com.claw.common.api.ApiResult;
import com.claw.common.base.BaseController;
import com.claw.common.log.Module;
import com.claw.common.pagination.Paging;
import com.claw.system.param.SysOperationLogPageParam;
import com.claw.system.service.SysOperationLogService;
import com.claw.system.vo.SysOperationLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 *  控制器
 *
 * @author Sakura
 * @since 2023-10-24
 */
@Slf4j
@RestController
@RequestMapping("/sysOperationLog")
@Api(value = "系统日志管理API", tags = {"系统日志管理"})
public class SysOperationLogController extends BaseController {

    @Autowired
    private SysOperationLogService sysOperationLogService;


    /**
     * 获取详情
     */
    @GetMapping("/info/{id}")
    //@OperationLog(name = "详情", type = OperationLogType.INFO)
    @ApiOperation(value = "详情", response = SysOperationLogVO.class)
    public ApiResult<SysOperationLogVO> getSysOperationLog(@PathVariable("id") Long id) throws Exception {
        SysOperationLogVO sysOperationLogVo = sysOperationLogService.getSysOperationLog(id);
        return ApiResult.ok(sysOperationLogVo);
    }

    /**
     * 分页列表
     */
    @PostMapping("/getPageList")
    //@OperationLog(name = "分页列表", type = OperationLogType.PAGE)
    @ApiOperation(value = "分页列表", response = SysOperationLogVO.class)
    public ApiResult<Paging<SysOperationLogVO>> getSysOperationLogPageList(@Validated @RequestBody SysOperationLogPageParam sysOperationLogPageParam) throws Exception {
        Paging<SysOperationLogVO> paging = sysOperationLogService.getSysOperationLogPageList(sysOperationLogPageParam);
        return ApiResult.ok(paging);
    }

}

