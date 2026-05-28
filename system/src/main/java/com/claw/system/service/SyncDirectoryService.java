package com.claw.system.service;

import com.claw.system.entity.SyncDirectory;

import java.util.List;

/**
 * 同步目录服务
 */
public interface SyncDirectoryService {

    /**
     * 添加同步目录
     */
    SyncDirectory add(String localPath, String displayName);

    /**
     * 删除同步目录（逻辑删除）
     */
    boolean remove(Long id);

    /**
     * 获取当前用户的同步目录列表
     */
    List<SyncDirectory> listByCurrentUser();

    /**
     * 管理员：获取所有同步目录（可按用户筛选）
     */
    List<SyncDirectory> listAll(Long userIdFilter);

    /**
     * 更换本机同步目录路径（换电脑、盘符变化时保留同一同步项与云端文件关联）
     */
    SyncDirectory updateLocalPath(Long id, String newLocalPath);
}
