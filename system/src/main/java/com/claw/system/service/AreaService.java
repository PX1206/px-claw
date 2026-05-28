package com.claw.system.service;

import com.claw.system.vo.AreaTreeVO;
import com.claw.system.vo.AreaVO;

import java.util.List;

/**
 *  服务类
 *
 * @author Sakura
 * @since 2024-08-12
 */
public interface AreaService {

    List<AreaVO> getSubAreas(Integer parentId);

    List<AreaTreeVO> getAreas() throws Exception;

}
