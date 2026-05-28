package com.claw.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.claw.system.entity.Area;
import com.claw.system.vo.AreaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *  Mapper 接口
 *
 * @author Sakura
 * @since 2024-08-12
 */
@Mapper
public interface AreaMapper extends BaseMapper<Area> {

    List<AreaVO> getSubAreas(@Param("parentId") Integer parentId);

    List<AreaVO> getAreas();

}
