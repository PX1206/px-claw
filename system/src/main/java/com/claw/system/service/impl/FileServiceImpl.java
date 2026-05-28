package com.claw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.exception.BusinessException;
import com.claw.common.pagination.Paging;
import com.claw.common.tool.DateUtil;
import com.claw.common.tool.FileTypeTool;
import com.claw.common.tool.LoginUtil;
import com.claw.common.vo.LoginUserInfoVO;
import com.claw.common.constant.SyncQuotaConstants;
import com.claw.system.entity.LocalFile;
import com.claw.system.entity.SyncDirectory;
import com.claw.system.entity.User;
import com.claw.system.mapper.LocalFileMapper;
import com.claw.system.mapper.SyncDirectoryMapper;
import com.claw.system.mapper.UserMapper;
import com.claw.system.param.FilePageParam;
import com.claw.system.service.FileService;
import com.claw.system.dto.SyncDirPathDto;
import com.claw.system.dto.SyncFileMetaDto;
import com.claw.system.service.SyncDirectoryService;
import com.claw.system.vo.DirectoryTreeNodeVo;
import com.claw.system.vo.FileVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件表 服务实现类
 *
 * 这里有个问题：
 * 当文件服务部署在不同的地方导致资源存放位置不一致
 * 请求http://localhost:8000/api-base/file/6FWBHNJhFyttppRPxsLruwH4rPZ5YcAS时会出提示不到文件
 * 需要自己处理跨服务器文件共享问题
 *
 * @author Sakura
 * @since 2022-08-22
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Value("${local.host}")
    String LOCAL_HOST;

    // 文件存放路径跟jar包同目录下/resources/files/
    private static final String LOCAL_FILE_PATH = "./resources/files/";

    @Autowired
    private LocalFileMapper localFileMapper;
    @Autowired
    private SyncDirectoryService syncDirectoryService;
    @Autowired
    private SyncDirectoryMapper syncDirectoryMapper;
    @Autowired
    private UserMapper userMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteFile(String code) throws Exception {
        LambdaQueryWrapper<LocalFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalFile::getCode, code);
        LocalFile localFile = localFileMapper.selectOne(wrapper);
        if (localFile == null) {
            throw new BusinessException(500, "文件不存在");
        }
        // 权限：普通用户只能删自己的文件，管理员可删全部
        if (!LoginUtil.isAdmin() && (localFile.getCreateBy() == null || !localFile.getCreateBy().equals(LoginUtil.getUserId()))) {
            throw new BusinessException(403, "无权限删除该文件");
        }

        localFileMapper.deleteById(localFile.getId());

        // 删除文件信息 目前不做物理删除
        // ZipUtil.deleteFileDirectory(new File(LOCAL_FILE_PATH + localFile.getPath() + "/" + localFile.getName() + localFile.getSuffix()));

        return true;
    }

    @Override
    public Paging<FileVo> getFilePageList(FilePageParam filePageParam) throws Exception {
        // 权限：普通用户只能看自己的文件，管理员可看全部
        if (!LoginUtil.isAdmin()) {
            filePageParam.setUserId(LoginUtil.getUserId());
        }
        Page<FileVo> page = new Page<>(filePageParam.getPageIndex(), filePageParam.getPageSize());
        IPage<FileVo> iPage = localFileMapper.getFileList(page, LOCAL_HOST + "file/", filePageParam);
        return new Paging<FileVo>(iPage);
    }

    @Override
    public String upload(MultipartFile multipartFile) throws Exception {
        if (multipartFile.isEmpty()) {
            throw new BusinessException(500, "上传文件为空");
        }
        LocalFile localFile = new LocalFile();
        // 保存原文件名称，文件列表展示需要用到
        localFile.setName(multipartFile.getOriginalFilename().substring(0, multipartFile.getOriginalFilename().lastIndexOf(".")));
        // 生成唯一编码code
        String code = RandomStringUtils.randomAlphanumeric(32);
        localFile.setCode(code);
        // 保存原文件后缀
        localFile.setSuffix(multipartFile.getOriginalFilename()
                .substring(multipartFile.getOriginalFilename().lastIndexOf(".")).toLowerCase());

        // 判断是否是图片，如果是图片则添加meta，这样点击链接就会预览而不是下载
        int fileType = FileTypeTool.fileType(multipartFile.getOriginalFilename());
        localFile.setType(fileType);
        localFile.setDomain(LOCAL_HOST);
        localFile.setPath(DateUtil.today());
        localFile.setSize((int) multipartFile.getSize());
        localFile.setCreateBy(LoginUtil.getUserId());
        localFileMapper.insert(localFile);

        // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
        File targetFile = new File(LOCAL_FILE_PATH + localFile.getPath()
                + "/" + localFile.getCode() + localFile.getSuffix());
        // 保证这个文件的父文件夹必须要存在
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        targetFile.createNewFile();

        // 将文件内容写入到这个文件中
        InputStream is = multipartFile.getInputStream();
        FileOutputStream fos = new FileOutputStream(targetFile);
        try {
            int len;
            byte[] buf = new byte[1024];
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
        } finally {
            // 关流顺序，先打开的后关闭
            fos.close();
            is.close();
        }

        return LOCAL_HOST + "file/" + localFile.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadForSync(MultipartFile multipartFile, Long syncDirectoryId, String relativePath) throws Exception {
        if (multipartFile == null) {
            throw new BusinessException(500, "上传文件为空");
        }
        String originalFilename = multipartFile.getOriginalFilename();
        String name = (originalFilename == null || !originalFilename.contains("."))
                ? (originalFilename != null ? originalFilename : "unknown")
                : originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String suffix = (originalFilename == null || !originalFilename.contains("."))
                ? ""
                : originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        int size = (int) multipartFile.getSize();

        LambdaQueryWrapper<LocalFile> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(LocalFile::getSyncDirectoryId, syncDirectoryId)
                .eq(LocalFile::getRelativePath, relativePath)
                .eq(LocalFile::getDelFlag, false);
        LocalFile existFile = localFileMapper.selectOne(existWrapper);

        long newSizeBytes = multipartFile.getSize();
        long oldSizeBytes = 0L;
        if (existFile != null && existFile.getSize() != null) {
            oldSizeBytes = Integer.toUnsignedLong(existFile.getSize());
        }
        long deltaBytes = newSizeBytes - oldSizeBytes;
        assertUserSyncQuotaAllows(LoginUtil.getUserId(), deltaBytes);

        if (existFile != null) {
            File oldPhysicalFile = new File(LOCAL_FILE_PATH + existFile.getPath() + "/" + existFile.getCode() + existFile.getSuffix());
            if (oldPhysicalFile.exists()) {
                oldPhysicalFile.delete();
            }

            existFile.setName(name);
            existFile.setSuffix(suffix);
            existFile.setType(FileTypeTool.fileType(originalFilename));
            existFile.setSize(size);
            existFile.setUpdateTime(new Date());
            localFileMapper.updateById(existFile);

            File targetFile = new File(LOCAL_FILE_PATH + existFile.getPath() + "/" + existFile.getCode() + existFile.getSuffix());
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            InputStream is = multipartFile.getInputStream();
            FileOutputStream fos = new FileOutputStream(targetFile);
            try {
                IOUtils.copy(is, fos);
            } finally {
                fos.close();
                is.close();
            }
            return LOCAL_HOST + "file/" + existFile.getCode();
        }

        LocalFile localFile = new LocalFile();
        localFile.setName(name);
        localFile.setSuffix(suffix);
        localFile.setCode(RandomStringUtils.randomAlphanumeric(32));
        localFile.setType(FileTypeTool.fileType(originalFilename));
        localFile.setDomain(LOCAL_HOST);
        localFile.setPath(DateUtil.today());
        localFile.setSize(size);
        localFile.setCreateBy(LoginUtil.getUserId());
        localFile.setSyncDirectoryId(syncDirectoryId);
        localFile.setRelativePath(relativePath);
        localFileMapper.insert(localFile);

        File targetFile = new File(LOCAL_FILE_PATH + localFile.getPath() + "/" + localFile.getCode() + localFile.getSuffix());
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs();
        }
        targetFile.createNewFile();
        InputStream is = multipartFile.getInputStream();
        FileOutputStream fos = new FileOutputStream(targetFile);
        try {
            IOUtils.copy(is, fos);
        } finally {
            fos.close();
            is.close();
        }
        return LOCAL_HOST + "file/" + localFile.getCode();
    }

    @Override
    public void download(HttpServletResponse response, String code) throws Exception {
        LambdaQueryWrapper<LocalFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LocalFile::getCode, code);
        LocalFile localFile = localFileMapper.selectOne(wrapper);
        if (localFile == null) {
            throw new BusinessException(500, "文件不存在");
        }
        // 权限：普通用户只能下载自己的文件，管理员可下载全部（需能从 Header 或 ?token= 解析登录态）
        LoginUserInfoVO loginUser = LoginUtil.getLoginUserInfoVo();
        boolean admin = "admin".equals(loginUser.getRole());
        if (!admin && (localFile.getCreateBy() == null || !localFile.getCreateBy().equals(loginUser.getId()))) {
            throw new BusinessException(403, "无权限下载该文件");
        }

        File downloadFile = new File(LOCAL_FILE_PATH + localFile.getPath() + "/" + localFile.getCode() + localFile.getSuffix());
        if (!downloadFile.exists()) {
            throw new BusinessException(500, "文件不存在");
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            String contentType = resolveDownloadContentType(localFile);
            response.setContentType(contentType);
            setContentDispositionHeader(response, localFile);
            if (localFile.getType() != null && (localFile.getType() == 3 || localFile.getType() == 4)) {
                response.setHeader("Accept-Ranges", "bytes");
            }
            response.addHeader("Content-Length", "" + downloadFile.length());
            is = new FileInputStream(downloadFile);
            os = response.getOutputStream();
            IOUtils.copy(is, os);
        } catch (Exception e) {
            log.error("下载图片发生异常", e);
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error("关闭流发生异常", e);
            }
        }
    }

    @Override
    public List<SyncFileMetaDto> listBySyncDirectory(Long syncDirectoryId) throws Exception {
        if (syncDirectoryId == null) return Collections.emptyList();
        if (!LoginUtil.isAdmin()) {
            List<SyncDirectory> userDirs = syncDirectoryService.listByCurrentUser();
            boolean allowed = userDirs.stream().anyMatch(d -> d.getId().equals(syncDirectoryId));
            if (!allowed) return Collections.emptyList();
        }
        return localFileMapper.listBySyncDirectory(syncDirectoryId);
    }

    @Override
    public List<DirectoryTreeNodeVo> getDirectoryTree() throws Exception {
        boolean admin = LoginUtil.isAdmin();
        List<SyncDirectory> syncDirs = admin
                ? syncDirectoryService.listAll(null)
                : syncDirectoryService.listByCurrentUser();

        if (syncDirs.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> syncDirIds = syncDirs.stream().map(SyncDirectory::getId).collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (admin) {
            Set<Long> userIds = syncDirs.stream().map(SyncDirectory::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!userIds.isEmpty()) {
                userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
            }
        }

        List<SyncDirPathDto> rawPaths = localFileMapper.getDistinctSyncDirPaths(syncDirIds);
        Map<Long, Set<String>> dirPathsBySyncDir = new HashMap<>();
        for (SyncDirectory sd : syncDirs) {
            dirPathsBySyncDir.put(sd.getId(), new HashSet<>());
        }
        for (SyncDirPathDto dto : rawPaths) {
            if (dto.getRelativePath() == null || dto.getRelativePath().trim().isEmpty()) continue;
            String n = dto.getRelativePath().replace('\\', '/').trim();
            int lastSlash = n.lastIndexOf('/');
            String dirPath = lastSlash >= 0 ? n.substring(0, lastSlash) : "";
            Set<String> paths = dirPathsBySyncDir.get(dto.getSyncDirectoryId());
            if (paths == null) continue;
            for (int i = 0; i <= dirPath.length(); i++) {
                if (i > 0 && (i == dirPath.length() || dirPath.charAt(i) == '/')) {
                    paths.add(dirPath.substring(0, i));
                }
            }
            if (!dirPath.isEmpty()) paths.add(dirPath);
        }

        Map<Long, List<DirectoryTreeNodeVo>> childrenBySyncDir = new HashMap<>();
        for (SyncDirectory sd : syncDirs) {
            childrenBySyncDir.put(sd.getId(), new ArrayList<>());
        }
        for (SyncDirectory sd : syncDirs) {
            Set<String> allDirPaths = dirPathsBySyncDir.get(sd.getId());
            if (allDirPaths == null) continue;
            for (String path : allDirPaths) {
                int slash = path.lastIndexOf('/');
                String parentPath = slash >= 0 ? path.substring(0, slash) : "";
                String name = slash >= 0 ? path.substring(slash + 1) : path;
                if (name == null || name.trim().isEmpty()) continue;
                boolean parentExists = parentPath.isEmpty() || allDirPaths.contains(parentPath);
                if (!parentExists) continue;
                List<DirectoryTreeNodeVo> list = childrenBySyncDir.get(sd.getId());
                if (list.stream().anyMatch(c -> path.equals(c.getRelativePath()))) continue;
                DirectoryTreeNodeVo node = new DirectoryTreeNodeVo();
                node.setSyncDirectoryId(sd.getId());
                node.setRelativePath(path);
                node.setDisplayName(name.trim());
                list.add(node);
            }
        }

        for (List<DirectoryTreeNodeVo> list : childrenBySyncDir.values()) {
            list.sort(Comparator.comparing(DirectoryTreeNodeVo::getRelativePath));
        }

        List<DirectoryTreeNodeVo> roots = new ArrayList<>();
        for (SyncDirectory sd : syncDirs) {
            DirectoryTreeNodeVo root = new DirectoryTreeNodeVo();
            root.setSyncDirectoryId(sd.getId());
            root.setRelativePath("");
            String rootName = sd.getDisplayName();
            if (rootName == null || rootName.trim().isEmpty()) {
                String path = sd.getLocalPath();
                if (path != null && !path.isEmpty()) {
                    int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
                    rootName = sep >= 0 ? path.substring(sep + 1) : path;
                } else {
                    rootName = "未命名";
                }
            }
            root.setDisplayName(rootName);
            if (sd.getCreateTime() != null) {
                root.setCreateTime(DateUtil.formatDateTime(sd.getCreateTime()));
            }
            if (sd.getUpdateTime() != null) {
                root.setLastSyncTime(DateUtil.formatDateTime(sd.getUpdateTime()));
            }
            if (admin && sd.getUserId() != null) {
                User u = userMap.get(sd.getUserId());
                root.setOwnerName(u != null ? u.getNickname() : null);
                root.setUserId(sd.getUserId());
            }
            buildTreeChildren(root, childrenBySyncDir.get(sd.getId()));
            roots.add(root);
        }
        return roots;
    }

    private void buildTreeChildren(DirectoryTreeNodeVo parent, List<DirectoryTreeNodeVo> flatList) {
        String prefix = parent.getRelativePath().isEmpty() ? "" : parent.getRelativePath() + "/";
        List<DirectoryTreeNodeVo> directChildren = flatList.stream()
                .filter(n -> n.getRelativePath().startsWith(prefix) && n.getRelativePath().indexOf('/', prefix.length()) < 0)
                .collect(Collectors.toList());
        for (DirectoryTreeNodeVo child : directChildren) {
            buildTreeChildren(child, flatList);
            parent.getChildren().add(child);
        }
    }

    /**
     * 下载时给浏览器的文件名：与上传时写入库的 name + suffix 一致（即用户原始文件名拆出的主名与扩展名）。
     * 若 name 为空（历史脏数据），则退回同步相对路径上的文件名。
     */
    private static String buildClientDownloadFileName(LocalFile f) {
        String base = f.getName();
        String suf = f.getSuffix() != null ? f.getSuffix().trim() : "";
        if (!suf.isEmpty() && !suf.startsWith(".")) {
            suf = "." + suf;
        }
        if (base != null && !base.trim().isEmpty()) {
            return base.trim() + suf;
        }
        String rp = f.getRelativePath();
        if (rp != null && !rp.trim().isEmpty()) {
            String n = rp.replace('\\', '/').trim();
            int slash = n.lastIndexOf('/');
            String leaf = slash >= 0 ? n.substring(slash + 1) : n;
            if (!leaf.isEmpty()) {
                return leaf;
            }
        }
        return "download" + suf;
    }

    private static String escapeContentDispositionFilename(String name) {
        if (name == null) {
            return "download";
        }
        return name.replaceAll("[\\r\\n\"\\\\]", "_");
    }

    /**
     * 统一使用 attachment，避免图片/文本等在新标签被内联预览而无法按「下载」预期落盘。
     * filename 为 ASCII 回退；filename*=UTF-8'' 承载中文等原始名。
     */
    private static void setContentDispositionHeader(HttpServletResponse response, LocalFile f) throws UnsupportedEncodingException {
        String fullName = escapeContentDispositionFilename(buildClientDownloadFileName(f));
        if (fullName.isEmpty()) {
            fullName = "download";
        }
        String utf8Encoded = URLEncoder.encode(fullName, "UTF-8").replace("+", "%20");
        String asciiFallback = asciiOnlyFilename(fullName);
        response.setHeader(
            "Content-Disposition",
            "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + utf8Encoded
        );
    }

    private static String asciiOnlyFilename(String fullName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fullName.length(); i++) {
            char c = fullName.charAt(i);
            if (c >= 32 && c < 127 && c != '"' && c != '\\') {
                sb.append(c);
            } else {
                sb.append('_');
            }
        }
        String s = sb.toString().trim();
        return s.isEmpty() ? "download" : s;
    }

    /** 按后缀与类型设置 Content-Type，下载后系统仍可按类型关联打开 */
    private static String resolveDownloadContentType(LocalFile f) {
        String suf = f.getSuffix() != null ? f.getSuffix().toLowerCase() : "";
        Integer t = f.getType();
        switch (suf) {
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            case ".webp":
                return "image/webp";
            case ".svg":
                return "image/svg+xml";
            case ".bmp":
                return "image/bmp";
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".pdf":
                return "application/pdf";
            case ".mp4":
                return "video/mp4";
            case ".webm":
                return "video/webm";
            case ".ogv":
                return "video/ogg";
            case ".ogg":
            case ".oga":
                return "audio/ogg";
            case ".mp3":
                return "audio/mpeg";
            case ".wav":
                return "audio/wav";
            case ".m4a":
                return "audio/mp4";
            case ".aac":
                return "audio/aac";
            case ".flac":
                return "audio/flac";
            case ".txt":
            case ".csv":
            case ".log":
                return "text/plain; charset=UTF-8";
            case ".html":
            case ".htm":
                return "text/html; charset=UTF-8";
            case ".json":
                return "application/json; charset=UTF-8";
            case ".xml":
                return "application/xml; charset=UTF-8";
            default:
                break;
        }
        if (t != null) {
            if (t == 1) {
                return "image/jpeg";
            }
            if (t == 3) {
                return "video/mp4";
            }
            if (t == 4) {
                return "audio/mpeg";
            }
        }
        return "application/octet-stream";
    }

    /** 同步上传新增占用 deltaBytes（覆盖旧文件时为 newSize-oldSize） */
    private void assertUserSyncQuotaAllows(Long userId, long deltaBytes) throws Exception {
        if (deltaBytes <= 0 || userId == null) {
            return;
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(500, "用户不存在");
        }
        long quota = user.getSyncQuotaBytes() != null ? user.getSyncQuotaBytes() : SyncQuotaConstants.DEFAULT_QUOTA_BYTES;
        Long usedSum = localFileMapper.sumSyncFileBytesByUserId(userId);
        long used = usedSum != null ? usedSum : 0L;
        if (used + deltaBytes > quota) {
            throw new BusinessException(500, String.format(
                    "同步空间不足（已用约 %s / 配额 %s）。请删除云端同步文件或联系管理员调整配额。",
                    formatBytesHuman(used), formatBytesHuman(quota)));
        }
    }

    private static String formatBytesHuman(long bytes) {
        if (bytes >= 1024L * 1024 * 1024) {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
        if (bytes >= 1024L * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
        if (bytes >= 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }

}
