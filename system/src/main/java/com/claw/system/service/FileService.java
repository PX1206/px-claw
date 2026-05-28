package com.claw.system.service;

import com.claw.common.pagination.Paging;
import com.claw.system.dto.SyncFileMetaDto;
import com.claw.system.param.FilePageParam;
import com.claw.system.vo.DirectoryTreeNodeVo;
import com.claw.system.vo.FileVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 文件表 服务类
 *
 * @author Sakura
 * @since 2022-08-22
 */
public interface FileService {

    /**
     * 删除
     *
     * @param code
     * @return
     * @throws Exception
     */
    boolean deleteFile(String code) throws Exception;


    /**
     * 获取分页对象
     *
     * @param filePageParam
     * @return
     * @throws Exception
     */
    Paging<FileVo> getFilePageList(FilePageParam filePageParam) throws Exception;

    /**
     * 上传
     * @param file
     * @return
     * @throws Exception
     */
    String upload(MultipartFile file) throws Exception;

    /**
     * 同步上传（含同步目录信息，用于桌面端）
     * @param file
     * @param syncDirectoryId 同步目录ID，可为null
     * @param relativePath 在同步目录内的相对路径，可为null
     * @return 文件访问URL
     */
    String uploadForSync(MultipartFile file, Long syncDirectoryId, String relativePath) throws Exception;

    /**
     * 下载
     *
     * @param code
     * @return
     * @throws Exception
     */
    void download(HttpServletResponse response, String code) throws Exception;

    /**
     * 获取目录树（同步目录+子目录，管理员可看全部及所属用户）
     */
    List<DirectoryTreeNodeVo> getDirectoryTree() throws Exception;

    /**
     * 按同步目录列出文件元数据，用于桌面端 diff（只上传缺失/更新的文件）
     */
    List<SyncFileMetaDto> listBySyncDirectory(Long syncDirectoryId) throws Exception;

}
