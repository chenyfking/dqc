package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.entity.RecycleBin;
import com.beagledata.gaea.workbench.mapper.RecycleBinMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.RecycleBinService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 回收站管理
 * Created by Cyf on 2020/6/24
 **/
@Service
public class RecycleBinServiceImpl implements RecycleBinService {
    private static Logger logger = LogManager.getLogger(RecycleBinServiceImpl.class);

    @Autowired
    private RecycleBinMapper recycleBinMapper;
    @Autowired
    private ReferMapper referMapper;

    @Override
    public Result getListByParam(int page, int pageNum, RecycleBin recycleBin) {
        if (recycleBin == null || StringUtils.isBlank(recycleBin.getProjectUuid())) {
            logger.error("查询项目回收站, 参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
           return Result.newSuccess()
                   .withData(recycleBinMapper.selectList(pageNum * (page - 1), pageNum, recycleBin))
                   .withTotal(recycleBinMapper.count(recycleBin));
        } catch (Exception e) {
            logger.error("查询回收站失败. page: {}, pageNum: {}, recycleBin: {}", page, pageNum, recycleBin, e);
            throw new IllegalStateException("项目回收站获取错误");
        }
    }

    @Override
    @Transactional
    public Result batchRestore(String projectUuid, String restoreData) {
        if (StringUtils.isBlank(restoreData) || StringUtils.isBlank(projectUuid)) {
            logger.error("批量还原回收站, 参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }

        JSONArray restoreDataArray;
        try {
            restoreDataArray = JSON.parseArray(restoreData);
        } catch (Exception e) {
            logger.error("还原回收站出错. projectUuid: {}, restoreData: {}", projectUuid, restoreData, e);
            throw new IllegalArgumentException("还原失败");
        }
        try {
            List<String> dirUuids = new ArrayList<>();
            List<String> tplUuids = new ArrayList<>();
            Set<String> assetsUuids = new HashSet<>();
            List<String> recycleUuids = new ArrayList<>();
            Set<String> recycleAssetsUuids = new HashSet<>();
            for (int i = 0; i < restoreDataArray.size(); i++) {
                JSONObject object = restoreDataArray.getJSONObject(i);
                String assetsUuid = object.getString("assetsUuid");
                recycleAssetsUuids.add(assetsUuid);
                recycleUuids.add(object.getString("uuid"));
                String type = object.getString("type");
                if ("dir".equals(type)) {
                    dirUuids.add(assetsUuid);
                } else if ("tpl".equals(type)) {
                    tplUuids.add(assetsUuid);
                } else {
                    assetsUuids.add(assetsUuid);
                }
            }

            // 还原模板
            if (!tplUuids.isEmpty()) {
                recycleBinMapper.restoreTemplate(tplUuids);
            }
            // 还原文件夹
            Set<String> folderUuids = new HashSet<>();
            if (!dirUuids.isEmpty()) {
                getAllRestoreFolderUuids(folderUuids, dirUuids);
                recycleAssetsUuids.addAll(folderUuids);
                if (!folderUuids.isEmpty()) {
                    recycleBinMapper.restoreFolder(folderUuids);
                }
            }
            //  根据文件夹uuid查询文件
            if (!folderUuids.isEmpty()) {
                List<String> assetsUuidList = recycleBinMapper.selectRestoreAssetsUuidsByDirUuids(folderUuids);
                if (!assetsUuidList.isEmpty()) {
                    recycleAssetsUuids.addAll(assetsUuidList);
                    assetsUuids.addAll(assetsUuidList);
                }
            }
            if (!assetsUuids.isEmpty()) {
                recycleBinMapper.restoreAssets(assetsUuids);
            }
            // 查询还原文件
            List<Map<String, Object>> list = recycleBinMapper.selectNodesByAssetsUuid(projectUuid, recycleAssetsUuids);
            // 删除回收站记录
            recycleBinMapper.deleteByAssetsUuids(recycleAssetsUuids);
            return Result.newSuccess().withData(list);
        } catch (DuplicateKeyException e) {
            String msg = e.getLocalizedMessage();
            msg = msg.replaceAll(".*Duplicate entry '", "").replaceAll("' for key.*", "");
            String name = msg.substring(0, msg.indexOf("-"));
            throw new IllegalStateException("存在重名文件或文件夹: " + name + ", 请修改已有文件名称");
        } catch (Exception e) {
            logger.error("还原回收站出错. projectUuid: {}, restoreData: {}", projectUuid, restoreData, e);
            throw new IllegalStateException("还原出错");
        }
    }

    private void getAllRestoreFolderUuids(Set<String> restoreFolderUuids, List<String> parentUuids) {
        if (parentUuids.isEmpty()) {
            return;
        }

        restoreFolderUuids.addAll(parentUuids);
        List<String> childFolderUuids = recycleBinMapper.selectRestoreChildFolderUuids(parentUuids);
        if (!childFolderUuids.isEmpty()) {
            getAllRestoreFolderUuids(restoreFolderUuids, childFolderUuids);
        }
    }

    @Override
    @Transactional
    public void batchDelete(String deleteJson) {
        if (StringUtils.isBlank(deleteJson)) {
            logger.error("回收站中批量删除, 参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }

        JSONArray deleteArray;
        try {
            deleteArray = JSON.parseArray(deleteJson);
        } catch (Exception e) {
            logger.error("回收站删除失败: {}", deleteJson, e);
            throw new IllegalArgumentException("删除失败");
        }

        if (deleteArray.isEmpty()) {
            logger.error("回收站删除失败: {}", deleteJson);
            throw new IllegalArgumentException("删除失败");
        }

        try {
            List<String> dirUuids = new ArrayList<>();
            List<String> tplUuids = new ArrayList<>();
            Set<String> assetsUuids = new HashSet<>();
            Set<String> recycleAssetsUuids = new HashSet<>();
            for (int i = 0; i < deleteArray.size(); i++) {
                JSONObject object = deleteArray.getJSONObject(i);
                String assetsUuid = object.getString("assetsUuid");
                recycleAssetsUuids.add(assetsUuid);
                String type = object.getString("type");
                if ("dir".equals(type)) {
                    dirUuids.add(assetsUuid);
                } else if ("tpl".equals(type)) {
                    tplUuids.add(assetsUuid);
                } else {
                    assetsUuids.add(assetsUuid);
                }
            }

            Set<String> folderUuids = new HashSet<>();
            if (!dirUuids.isEmpty()) {
                getAllRestoreFolderUuids(folderUuids, dirUuids);
                recycleAssetsUuids.addAll(folderUuids);
            }
            if (!folderUuids.isEmpty()) {
                List<String> assetsUuidList = recycleBinMapper.selectRestoreAssetsUuidsByDirUuids(folderUuids);
                if (!assetsUuidList.isEmpty()) {
                    recycleAssetsUuids.addAll(assetsUuidList);
                    assetsUuids.addAll(assetsUuidList);
                }
            }

            recycleBinMapper.deleteByAssetsUuids(recycleAssetsUuids);
            referMapper.deleteFromRecycle(null, assetsUuids, tplUuids);
        } catch (Exception e) {
            logger.error("回收站删除失败: {}", deleteJson, e);
            throw new IllegalStateException("删除出错");
        }
    }

    @Override
    @Transactional
    public void setEmpty(String projectUuid) {
        if (StringUtils.isBlank(projectUuid)) {
            logger.error("清空回收站, 参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            referMapper.deleteFromRecycle(projectUuid, null, null);
            recycleBinMapper.deleteByProject(projectUuid);
        } catch (Exception e) {
            logger.error("清空回收站出错: {}", projectUuid, e);
            throw new IllegalStateException("清空回收站出错");
        }
    }
}