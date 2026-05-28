package com.claw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.claw.common.exception.BusinessException;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.SyncDirectory;
import com.claw.system.mapper.SyncDirectoryMapper;
import com.claw.system.service.SyncDirectoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class SyncDirectoryServiceImpl implements SyncDirectoryService {

    @Autowired
    private SyncDirectoryMapper syncDirectoryMapper;

    @Override
    public SyncDirectory add(String localPath, String displayName) {
        if (localPath == null || localPath.trim().isEmpty()) {
            throw new BusinessException(500, "本地路径不能为空");
        }
        Long userId = LoginUtil.getUserId();

        LambdaQueryWrapper<SyncDirectory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncDirectory::getUserId, userId)
                .eq(SyncDirectory::getLocalPath, localPath)
                .eq(SyncDirectory::getDelFlag, false);
        SyncDirectory exist = syncDirectoryMapper.selectOne(wrapper);
        if (exist != null) {
            throw new BusinessException(500, "该目录已添加");
        }

        SyncDirectory dir = new SyncDirectory();
        dir.setUserId(userId);
        dir.setLocalPath(localPath.trim());
        dir.setDisplayName(displayName != null ? displayName.trim() : extractDirName(localPath));
        dir.setCreateTime(new Date());
        dir.setUpdateTime(new Date());
        dir.setDelFlag(false);
        syncDirectoryMapper.insert(dir);
        return dir;
    }

    private String extractDirName(String path) {
        if (path == null) return "未命名";
        String p = path.replace('\\', '/').trim();
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
        int idx = p.lastIndexOf('/');
        return idx >= 0 ? p.substring(idx + 1) : p;
    }

    @Override
    public boolean remove(Long id) {
        SyncDirectory dir = syncDirectoryMapper.selectById(id);
        if (dir == null) {
            throw new BusinessException(500, "同步目录不存在");
        }
        if (!LoginUtil.isAdmin() && !dir.getUserId().equals(LoginUtil.getUserId())) {
            throw new BusinessException(403, "无权限删除");
        }
        dir.setDelFlag(true);
        dir.setUpdateTime(new Date());
        return syncDirectoryMapper.updateById(dir) > 0;
    }

    @Override
    public List<SyncDirectory> listByCurrentUser() {
        Long userId = LoginUtil.getUserId();
        LambdaQueryWrapper<SyncDirectory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncDirectory::getUserId, userId)
                .eq(SyncDirectory::getDelFlag, false)
                .orderByDesc(SyncDirectory::getCreateTime);
        return syncDirectoryMapper.selectList(wrapper);
    }

    @Override
    public SyncDirectory updateLocalPath(Long id, String newLocalPath) {
        if (newLocalPath == null || newLocalPath.trim().isEmpty()) {
            throw new BusinessException(500, "本地路径不能为空");
        }
        String path = newLocalPath.trim();
        SyncDirectory dir = syncDirectoryMapper.selectById(id);
        if (dir == null || Boolean.TRUE.equals(dir.getDelFlag())) {
            throw new BusinessException(500, "同步目录不存在");
        }
        Long userId = LoginUtil.getUserId();
        if (!dir.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权限修改");
        }
        LambdaQueryWrapper<SyncDirectory> dup = new LambdaQueryWrapper<>();
        dup.eq(SyncDirectory::getUserId, userId)
                .eq(SyncDirectory::getLocalPath, path)
                .eq(SyncDirectory::getDelFlag, false)
                .ne(SyncDirectory::getId, id);
        if (syncDirectoryMapper.selectCount(dup) > 0) {
            throw new BusinessException(500, "该本地路径已被其他同步项使用");
        }
        dir.setLocalPath(path);
        dir.setUpdateTime(new Date());
        syncDirectoryMapper.updateById(dir);
        return dir;
    }

    @Override
    public List<SyncDirectory> listAll(Long userIdFilter) {
        LambdaQueryWrapper<SyncDirectory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SyncDirectory::getDelFlag, false);
        if (userIdFilter != null) {
            wrapper.eq(SyncDirectory::getUserId, userIdFilter);
        }
        wrapper.orderByDesc(SyncDirectory::getCreateTime);
        return syncDirectoryMapper.selectList(wrapper);
    }
}
