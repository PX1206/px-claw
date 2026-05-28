package com.claw.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.base.BaseServiceImpl;
import com.claw.common.exception.BusinessException;
import com.claw.common.pagination.Paging;
import com.claw.system.entity.SysOperationLog;
import com.claw.system.mapper.SysOperationLogMapper;
import com.claw.system.param.SysOperationLogPageParam;
import com.claw.system.service.SysOperationLogService;
import com.claw.system.vo.SysOperationLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *
 * @author Sakura
 * @since 2023-10-24
 */
@Slf4j
@Service
public class SysOperationLogServiceImpl extends BaseServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {

    @Autowired
    private SysOperationLogMapper sysOperationLogMapper;


    @Override
    public SysOperationLogVO getSysOperationLog(Long id) throws Exception {
        SysOperationLog sysOperationLog = sysOperationLogMapper.selectById(id);
        if (sysOperationLog == null) {
            throw new BusinessException(500, "数据异常");
        }
        SysOperationLogVO sysOperationLogVo = new SysOperationLogVO();
        BeanUtils.copyProperties(sysOperationLog, sysOperationLogVo);

        return sysOperationLogVo;
    }

    @Override
    public Paging<SysOperationLogVO> getSysOperationLogPageList(SysOperationLogPageParam sysOperationLogPageParam) throws Exception {
        Page<SysOperationLogVO> page = new Page<>(sysOperationLogPageParam.getPageIndex(), sysOperationLogPageParam.getPageSize());
        IPage<SysOperationLogVO> iPage = sysOperationLogMapper.getSysOperationLogList(page, sysOperationLogPageParam);
        return new Paging<SysOperationLogVO>(iPage);
    }

}
