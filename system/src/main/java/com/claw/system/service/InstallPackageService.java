package com.claw.system.service;

import com.claw.common.pagination.Paging;
import com.claw.system.param.InstallPackagePageParam;
import com.claw.system.vo.InstallPackageVO;
import com.claw.system.vo.OpenInstallPackageLatestVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface InstallPackageService {

    Paging<InstallPackageVO> page(InstallPackagePageParam param) throws Exception;

    void upload(MultipartFile file, String versionLabel, String remark) throws Exception;

    boolean delete(Long id) throws Exception;

    /**
     * @param displayFileName 可选；若提供须与库中文件名一致。供 latest.yml 使用「…/download/{code}/{文件名}.exe」以满足 electron-updater 对 pathname 必须以 .exe 结尾的要求。
     */
    void downloadPublic(String downloadCode, String displayFileName, HttpServletResponse response) throws Exception;

    /**
     * 输出 electron-updater（generic）所需的 latest.yml，取最新一条 Windows .exe 且能解析出语义化版本的记录。
     */
    void writeElectronLatestYml(HttpServletResponse response) throws Exception;

    /**
     * 与 {@link #writeElectronLatestYml} 相同的选取规则；物理文件须存在。无可用包或文件缺失时返回 null。
     */
    OpenInstallPackageLatestVO getLatestWindowsPublicInfo();
}
