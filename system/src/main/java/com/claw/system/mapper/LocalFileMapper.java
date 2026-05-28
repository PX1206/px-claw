package com.claw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.system.entity.LocalFile;
import com.claw.system.dto.SyncDirPathDto;
import com.claw.system.dto.SyncFileMetaDto;
import com.claw.system.param.FilePageParam;
import com.claw.system.vo.FileVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LocalFileMapper extends BaseMapper<LocalFile> {

	/**
	 * 用户所有同步目录下文件大小之和（字节），与 file.size 存储一致（按字节计）
	 */
	Long sumSyncFileBytesByUserId(@Param("userId") Long userId);

	IPage<FileVo> getFileList(@Param("page") Page page, @Param("localFilePath") String localFilePath,
							  @Param("param") FilePageParam param);

	/**
	 * 查询有文件的同步目录下的 (sync_directory_id, relative_path) 对
	 * 用于构建目录树
	 */
	List<SyncDirPathDto> getDistinctSyncDirPaths(@Param("syncDirIds") List<Long> syncDirIds);

	/**
	 * 按同步目录查询文件元数据（relativePath, size, updateTime），用于桌面端 diff
	 */
	List<SyncFileMetaDto> listBySyncDirectory(@Param("syncDirectoryId") Long syncDirectoryId);

}
