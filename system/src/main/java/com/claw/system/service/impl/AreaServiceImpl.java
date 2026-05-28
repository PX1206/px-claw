package com.claw.system.service.impl;

import com.claw.common.exception.BusinessException;
import com.claw.system.mapper.AreaMapper;
import com.claw.system.service.AreaService;
import com.claw.system.vo.AreaTreeVO;
import com.claw.system.vo.AreaVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *  服务实现类
 *
 * @author Sakura
 * @since 2024-08-12
 */
@Slf4j
@Service
public class AreaServiceImpl implements AreaService {

    @Autowired
    private AreaMapper areaMapper;

    @Override
    public List<AreaVO> getSubAreas(Integer parentId) {
        return areaMapper.getSubAreas(parentId);
    }

    @Override
    public List<AreaTreeVO> getAreas() throws Exception {
        // 先获取所有的区域信息
        List<AreaVO> areaVos = areaMapper.getAreas();
        int num = 0; // 添加一个遍历层数，防止数据异常导致递归死循环
        List<AreaTreeVO> areaTreeVos = getChildAreas(0, areaVos, num);
        return areaTreeVos;
    }

    private List<AreaTreeVO> getChildAreas(Integer parentId, List<AreaVO> areas, int num) throws Exception {
        num++;// 控制遍历次数，防止因数据问题导致无限循环内存溢出，目前最大支持5层
        if (num > 5) {
            log.error("区域数据parentId：" + parentId);
            throw new BusinessException(500, "区域数据异常，请联系管理人员");
        }

        List<AreaTreeVO> resultList = new ArrayList<>();

        for (AreaVO areaVo : areas) {
            if (parentId.equals(areaVo.getParentId())) {
                AreaTreeVO areaTreeVo = new AreaTreeVO();
                BeanUtils.copyProperties(areaVo, areaTreeVo);
                // 通过当前ID获取子权限
                List<AreaTreeVO> childAreas = getChildAreas(areaVo.getId(), areas, num);
                areaTreeVo.setChildren(childAreas);

                resultList.add(areaTreeVo);
            }
        }

        return resultList;
    }

}
