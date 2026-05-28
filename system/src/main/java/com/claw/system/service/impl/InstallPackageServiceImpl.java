package com.claw.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.claw.common.exception.BusinessException;
import com.claw.common.pagination.Paging;
import com.claw.common.tool.DateUtil;
import com.claw.common.tool.LoginUtil;
import com.claw.system.entity.InstallPackage;
import com.claw.system.mapper.InstallPackageMapper;
import com.claw.system.param.InstallPackagePageParam;
import com.claw.system.service.InstallPackageService;
import com.claw.system.vo.InstallPackageVO;
import com.claw.system.vo.OpenInstallPackageLatestVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InstallPackageServiceImpl implements InstallPackageService {

    private static final String LOCAL_PACKAGE_PATH = "./resources/install-packages/";

    private static final Set<String> ALLOWED_EXT = new HashSet<>(Arrays.asList(
            "exe", "msi", "zip", "dmg", "apk", "deb", "7z", "gz", "tar", "pkg", "appimage"
    ));

    @Value("${local.host}")
    private String localHost;

    @Autowired
    private InstallPackageMapper installPackageMapper;

    private void requireAdmin() {
        if (!LoginUtil.isAdmin()) {
            throw new BusinessException(403, "仅管理员可操作安装包管理");
        }
    }

    @Override
    public Paging<InstallPackageVO> page(InstallPackagePageParam param) throws Exception {
        requireAdmin();
        Page<InstallPackage> page = new Page<>(param.getPageIndex(), param.getPageSize());
        LambdaQueryWrapper<InstallPackage> w = new LambdaQueryWrapper<InstallPackage>()
                .eq(InstallPackage::getDelFlag, false)
                .orderByDesc(InstallPackage::getCreateTime);
        if (StringUtils.isNotBlank(param.getKeyword())) {
            String k = param.getKeyword().trim();
            w.and(x -> x.like(InstallPackage::getFileName, k)
                    .or().like(InstallPackage::getVersionLabel, k)
                    .or().like(InstallPackage::getRemark, k));
        }
        IPage<InstallPackage> iPage = installPackageMapper.selectPage(page, w);
        List<InstallPackageVO> records = iPage.getRecords().stream()
                .map(this::toVo)
                .collect(Collectors.toList());
        return new Paging<>(iPage, records);
    }

    private InstallPackageVO toVo(InstallPackage e) {
        InstallPackageVO vo = new InstallPackageVO()
                .setId(e.getId())
                .setDownloadCode(e.getDownloadCode())
                .setFileName(e.getFileName())
                .setSuffix(e.getSuffix())
                .setFileSize(e.getFileSize())
                .setVersionLabel(e.getVersionLabel())
                .setRemark(e.getRemark())
                .setCreateTime(e.getCreateTime());
        String base = localHost.endsWith("/") ? localHost : localHost + "/";
        vo.setDownloadUrl(base + "open/installPackage/download/" + e.getDownloadCode());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upload(MultipartFile file, String versionLabel, String remark) throws Exception {
        requireAdmin();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(500, "上传文件为空");
        }
        String original = file.getOriginalFilename();
        if (StringUtils.isBlank(original)) {
            throw new BusinessException(500, "文件名无效");
        }
        String safeName = original.replace("\\", "/");
        int slash = safeName.lastIndexOf('/');
        if (slash >= 0) {
            safeName = safeName.substring(slash + 1);
        }
        if (StringUtils.isBlank(safeName)) {
            throw new BusinessException(500, "文件名无效");
        }
        int dot = safeName.lastIndexOf('.');
        String suffix = dot >= 0 ? safeName.substring(dot).toLowerCase() : "";
        String ext = dot >= 0 ? safeName.substring(dot + 1).toLowerCase() : "";
        if (StringUtils.isBlank(ext) || !ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(500, "不支持的安装包类型，允许：" + String.join(", ", ALLOWED_EXT));
        }

        String downloadCode = RandomStringUtils.randomAlphanumeric(32);
        String day = DateUtil.today();
        InstallPackage row = new InstallPackage();
        row.setDownloadCode(downloadCode);
        row.setFileName(safeName);
        row.setRelativePath(day);
        row.setSuffix(suffix);
        row.setFileSize(file.getSize());
        row.setVersionLabel(StringUtils.isBlank(versionLabel) ? null : versionLabel.trim());
        row.setRemark(StringUtils.isBlank(remark) ? null : remark.trim());
        row.setCreateBy(LoginUtil.getUserId());
        row.setDelFlag(false);
        installPackageMapper.insert(row);

        File target = new File(LOCAL_PACKAGE_PATH + day + "/" + downloadCode + suffix);
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw new BusinessException(500, "创建存储目录失败");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            try (InputStream raw = file.getInputStream();
                 DigestInputStream din = new DigestInputStream(raw, md);
                 FileOutputStream out = new FileOutputStream(target)) {
                IOUtils.copy(din, out);
            }
            row.setSha512(Base64.getEncoder().encodeToString(md.digest()));
        } catch (IOException e) {
            log.error("安装包写入失败", e);
            throw new BusinessException(500, "文件保存失败");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new BusinessException(500, "校验算法不可用");
        }
        installPackageMapper.updateById(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) throws Exception {
        requireAdmin();
        InstallPackage row = installPackageMapper.selectById(id);
        if (row == null || Boolean.TRUE.equals(row.getDelFlag())) {
            throw new BusinessException(500, "记录不存在");
        }
        File f = new File(LOCAL_PACKAGE_PATH + row.getRelativePath() + "/" + row.getDownloadCode() + row.getSuffix());
        if (f.exists() && !f.delete()) {
            log.warn("安装包物理文件删除失败: {}", f.getAbsolutePath());
        }
        // 全局配置了 logic-delete-field: delFlag 时，不能靠 updateById 改 del_flag，需走 deleteById 才会生成逻辑删除 SQL
        int n = installPackageMapper.deleteById(id);
        return n > 0;
    }

    @Override
    public void downloadPublic(String downloadCode, String displayFileName, HttpServletResponse response) throws Exception {
        if (StringUtils.isBlank(downloadCode)) {
            throw new BusinessException(500, "参数错误");
        }
        InstallPackage row = installPackageMapper.selectOne(new LambdaQueryWrapper<InstallPackage>()
                .eq(InstallPackage::getDownloadCode, downloadCode.trim())
                .eq(InstallPackage::getDelFlag, false)
                .last("LIMIT 1"));
        if (row == null) {
            throw new BusinessException(500, "文件不存在或已下架");
        }
        if (StringUtils.isNotBlank(displayFileName)) {
            String decoded;
            try {
                decoded = URLDecoder.decode(displayFileName.trim(), StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                throw new BusinessException(500, "参数错误");
            }
            if (!row.getFileName().equals(decoded)) {
                throw new BusinessException(500, "文件不存在或已下架");
            }
        }
        File file = new File(LOCAL_PACKAGE_PATH + row.getRelativePath() + "/" + row.getDownloadCode() + row.getSuffix());
        if (!file.exists() || !file.isFile()) {
            throw new BusinessException(500, "文件不存在或已下架");
        }
        try {
            String encoded = URLEncoder.encode(row.getFileName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            response.addHeader("Content-Length", String.valueOf(file.length()));
            try (InputStream is = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
                IOUtils.copy(is, os);
            }
        } catch (IOException e) {
            log.error("安装包下载异常", e);
            throw new BusinessException(500, "下载失败");
        }
    }

    private static final Pattern SEMVER_IN_TEXT = Pattern.compile(
            "(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)\\.(?:0|[1-9]\\d*)(?:-[0-9A-Za-z.-]+)?(?:\\+[0-9A-Za-z.-]+)?");

    private static String extractSemver(String versionLabel, String fileName) {
        if (StringUtils.isNotBlank(versionLabel)) {
            Matcher m = SEMVER_IN_TEXT.matcher(versionLabel);
            if (m.find()) {
                return m.group();
            }
        }
        if (StringUtils.isNotBlank(fileName)) {
            Matcher m = SEMVER_IN_TEXT.matcher(fileName);
            if (m.find()) {
                return m.group();
            }
        }
        return null;
    }

    private static int semverCompare(String a, String b) {
        SemverPart pa = SemverPart.parse(a);
        SemverPart pb = SemverPart.parse(b);
        if (pa == null || pb == null) {
            return a.compareTo(b);
        }
        int c = Integer.compare(pa.major, pb.major);
        if (c != 0) {
            return c;
        }
        c = Integer.compare(pa.minor, pb.minor);
        if (c != 0) {
            return c;
        }
        c = Integer.compare(pa.patch, pb.patch);
        if (c != 0) {
            return c;
        }
        boolean ea = pa.pre.isEmpty();
        boolean eb = pb.pre.isEmpty();
        if (ea && !eb) {
            return 1;
        }
        if (!ea && eb) {
            return -1;
        }
        return pa.pre.compareTo(pb.pre);
    }

    private static final class SemverPart {
        int major;
        int minor;
        int patch;
        String pre = "";

        static SemverPart parse(String full) {
            Matcher m = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-([0-9A-Za-z.-]+))?(?:\\+([0-9A-Za-z.-]+))?$").matcher(full);
            if (!m.matches()) {
                return null;
            }
            SemverPart p = new SemverPart();
            p.major = Integer.parseInt(m.group(1));
            p.minor = Integer.parseInt(m.group(2));
            p.patch = Integer.parseInt(m.group(3));
            if (m.group(4) != null) {
                p.pre = m.group(4);
            }
            return p;
        }
    }

    private static String sha512Base64File(File f) throws IOException {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
        try (InputStream in = new FileInputStream(f); DigestInputStream din = new DigestInputStream(in, md)) {
            byte[] buf = new byte[8192];
            while (din.read(buf) != -1) {
                // drain
            }
        }
        return Base64.getEncoder().encodeToString(md.digest());
    }

    private static String yamlScalar(String s) {
        if (s == null) {
            return "\"\"";
        }
        if (s.contains(":") || s.contains(" ") || s.contains("\"") || s.contains("\n") || s.contains("\\")) {
            return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        return s;
    }

    private static String formatReleaseDate(java.util.Date d) {
        Instant i = d != null ? d.toInstant() : Instant.now();
        return DateTimeFormatter.ISO_INSTANT.format(i);
    }

    private static final class LatestWindowsPick {
        final InstallPackage pkg;
        final String semver;

        LatestWindowsPick(InstallPackage pkg, String semver) {
            this.pkg = pkg;
            this.semver = semver;
        }
    }

    /**
     * 与 electron generic latest.yml 一致：未删除的 .exe 中，按语义化版本取最大；无可用 semver 时返回 null。
     */
    private LatestWindowsPick pickLatestWindowsExe() {
        List<InstallPackage> list = installPackageMapper.selectList(new LambdaQueryWrapper<InstallPackage>()
                .eq(InstallPackage::getDelFlag, false)
                .eq(InstallPackage::getSuffix, ".exe"));
        InstallPackage best = null;
        String bestVer = null;
        for (InstallPackage p : list) {
            String v = extractSemver(p.getVersionLabel(), p.getFileName());
            if (v == null) {
                continue;
            }
            if (best == null || semverCompare(v, bestVer) > 0) {
                best = p;
                bestVer = v;
            }
        }
        if (best == null) {
            return null;
        }
        return new LatestWindowsPick(best, bestVer);
    }

    @Override
    public OpenInstallPackageLatestVO getLatestWindowsPublicInfo() {
        LatestWindowsPick pick = pickLatestWindowsExe();
        if (pick == null) {
            return null;
        }
        File file = new File(LOCAL_PACKAGE_PATH + pick.pkg.getRelativePath() + "/" + pick.pkg.getDownloadCode() + pick.pkg.getSuffix());
        if (!file.isFile()) {
            log.warn("最新桌面安装包记录存在但物理文件缺失: {}", file.getAbsolutePath());
            return null;
        }
        long size = file.length();
        if (pick.pkg.getFileSize() != null && pick.pkg.getFileSize() > 0) {
            size = pick.pkg.getFileSize();
        }
        return new OpenInstallPackageLatestVO()
                .setVersion(pick.semver)
                .setVersionLabel(pick.pkg.getVersionLabel())
                .setFileName(pick.pkg.getFileName())
                .setDownloadCode(pick.pkg.getDownloadCode())
                .setFileSize(size);
    }

    @Override
    public void writeElectronLatestYml(HttpServletResponse response) throws Exception {
        LatestWindowsPick pick = pickLatestWindowsExe();
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (pick == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            try (Writer w = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
                w.write("暂无可用 Windows 安装包，或版本说明/文件名中未包含语义化版本号（如 1.0.1）。");
            }
            return;
        }
        InstallPackage best = pick.pkg;
        String bestVer = pick.semver;
        File file = new File(LOCAL_PACKAGE_PATH + best.getRelativePath() + "/" + best.getDownloadCode() + best.getSuffix());
        if (!file.isFile()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain;charset=UTF-8");
            try (Writer w = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
                w.write("安装包文件不存在或已下架");
            }
            return;
        }
        String sha512 = best.getSha512();
        if (StringUtils.isBlank(sha512)) {
            sha512 = sha512Base64File(file);
            best.setSha512(sha512);
            installPackageMapper.updateById(best);
        }
        long size = file.length();
        if (best.getFileSize() != null && best.getFileSize() > 0) {
            size = best.getFileSize();
        }
        String base = localHost.endsWith("/") ? localHost : localHost + "/";
        String nameInPath = URLEncoder.encode(best.getFileName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
        // pathname 须以 .exe 结尾，否则 electron-updater 会把整段 URL 当本地相对路径，在 Windows 下 ENOENT
        String downloadUrl = base + "open/installPackage/download/" + best.getDownloadCode() + "/" + nameInPath;
        String yaml = "version: " + yamlScalar(bestVer) + "\n"
                + "files:\n"
                + "  - url: " + yamlScalar(downloadUrl) + "\n"
                + "    sha512: " + yamlScalar(sha512) + "\n"
                + "    size: " + size + "\n"
                + "path: " + yamlScalar(downloadUrl) + "\n"
                + "sha512: " + yamlScalar(sha512) + "\n"
                + "releaseDate: " + yamlScalar(formatReleaseDate(best.getCreateTime())) + "\n";
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/yaml;charset=UTF-8");
        try (Writer w = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            w.write(yaml);
        }
    }
}
