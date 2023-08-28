package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.AssetsVersion;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.ReferService;
import com.beagledata.gaea.workbench.vo.AssetsReferenceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2020/11/13.
 */
@Service
public class ReferServiceImpl implements ReferService {
    @Autowired
    private ReferMapper referMapper;

    @Override
    public Result selectReferBySubjectUuidAndVersion(String subjectUuid, Integer subjectVersion) {
        List<AssetsReferenceVO> assetsReferenceVOS = referMapper.selectReferBySubjectUuidAndVersion(subjectUuid, subjectVersion);
        if (assetsReferenceVOS.isEmpty()) {
            return Result.emptyList();
        }

        Map<String, List<AssetsReferenceVO>> mapRefer = new HashMap<>();
        for (AssetsReferenceVO vo : assetsReferenceVOS) {
            String type = vo.getType();
            Integer versionNo = vo.getVersionNo();
            boolean isTemplate = vo.isTemplate();
            List<AssetsReferenceVO> referenceVOS = mapRefer.get(type);
            if (referenceVOS == null) {
                referenceVOS = new ArrayList<>();
            }
            if (isTemplate || "testcase".equals(type) || "fact".equals(type)) {
                referenceVOS.add(vo);
                mapRefer.put(type, referenceVOS);
                continue;
            }
            if (versionNo == null) {
                versionNo = 0;
            }

            AssetsVersion assetsVersion = new AssetsVersion();
            assetsVersion.setVersionNo(versionNo);
            assetsVersion.setVersionDes(vo.getDescription());
            assetsVersion.setCreateTime(vo.getCreateTime());
            boolean contains = false;
            AssetsReferenceVO reference = null;
            for (AssetsReferenceVO referenceVO : referenceVOS) {
                if (vo.getUuid().equals(referenceVO.getUuid()) && !referenceVO.isTemplate()) {
                    contains = true;
                    reference = referenceVO;
                    break;
                }
            }

            if (!contains) {
                reference = new AssetsReferenceVO();
                reference.setName(vo.getName());
                reference.setType(vo.getType());
                reference.setUuid(vo.getUuid());
                reference.setRecycle(vo.isRecycle());
            }
            List<AssetsVersion> assetsVersions = reference.getAssetsVersions();
            if (assetsVersions == null) {
                assetsVersions = new ArrayList<>();
            }
            assetsVersions.add(assetsVersion);
            List<AssetsVersion> assetsVersionsSort = assetsVersions.stream().sorted(
                    Comparator.comparing(AssetsVersion::getVersionNo).reversed()).collect(Collectors.toList());
            reference.setAssetsVersions(assetsVersionsSort);
            if (!contains) {
                referenceVOS.add(reference);
            }
            mapRefer.put(type, referenceVOS);
        }

        if (mapRefer.isEmpty()) {
            return Result.emptyList();
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Map.Entry<String, List<AssetsReferenceVO>> entry : mapRefer.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", entry.getKey());
            map.put("list", entry.getValue());
            dataList.add(map);
        }
        return Result.newSuccess().withData(dataList);
    }
}
