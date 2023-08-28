package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.FolderMapper;
import com.beagledata.gaea.workbench.mapper.RecycleBinMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.FolderService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mahongfei on 2018/11/27.
 */

@Service
public class FolderSericeImpl implements FolderService {
	private static Logger logger = LogManager.getLogger(KnowledgePackageServiceImpl.class);
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private AssetsMapper assetsMapper;
    @Autowired
    private RecycleBinMapper recycleBinMapper;
    @Autowired
    private ReferMapper referMapper;

    /**
     *@Author: mahongfei
     *@description: 将文件夹信息添加到数据库
     */
    @Override
    public String addFolder(Folder folder) {
        if (StringUtils.isBlank(folder.getDirId())) {
            throw new IllegalStateException("文件夹id为空");
		}
		if (!folder.getDirId().matches("^[0-9]*$")){
		    throw new IllegalStateException("文件夹id只能是数字");
        }
		if (StringUtils.isBlank(folder.getDirName())) {
            throw new IllegalStateException("文件夹名称为空");
		}
		if (StringUtils.isBlank(folder.getParentId())) {
            folder.setParentId("0");
		}
		if (folder.getDirId().equals("1") && !folder.getParentId().equals("0")) {
		    throw new IllegalStateException("根目录父类id应该为0");
        }
		if (StringUtils.isBlank(folder.getCategoryName())) {
            throw new IllegalStateException("菜单分类名称为空");
		}
		if (StringUtils.isBlank(folder.getProjectUuid())) {
            throw new IllegalStateException("项目id为空");
		}

        try {
            folder.setUuid(IdUtils.UUID());
            folder.setCreator(UserHolder.currentUser());
            folderMapper.insert(folder);
            return folder.getUuid();
        } catch (IllegalStateException e) {
            logger.error(folder + e.getLocalizedMessage(), e);
            throw new IllegalStateException("文件夹添加失败");
        }catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("文件夹名称不能重复");
		}
    }

	/**
	 *@Author: mahongfei
	 *@description: 文件夹列表
	 */
	@Override
	public List<Object> listAll(String projectUuid) {
		if (StringUtils.isBlank(projectUuid)) {
			throw new IllegalArgumentException("项目uuid为空！");
		}

		List<Object> list = new ArrayList<>();
		List<String> types = new ArrayList<>();
        types.add(AssetsType.FACT);
        types.add(AssetsType.CONSTANT);
        types.add(AssetsType.GUIDED_RULE);
        types.add(AssetsType.RULE_TABLE);
        types.add(AssetsType.RULE_TREE);
        types.add(AssetsType.SCORECARD);
        types.add(AssetsType.RULE_FLOW);
		for (String categoryName : types) {
		    Map<String, Object> map = new HashMap<>();
            List<DirTree> dirTreeList = new ArrayList<>();
            map.put("type", categoryName);
            List<String> dirIdList = folderMapper.listDirId(projectUuid, categoryName);
            if (!dirIdList.isEmpty()) {
                String lastdirId = dirIdList.get(dirIdList.size()-1);
                dirTreeList.addAll(getchildrenDataTree(1, lastdirId, projectUuid, categoryName, "0"));
            }

            List<Assets> assetsList = assetsMapper.selectAssertByParentId("0", projectUuid, categoryName,
                     UserHolder.hasAdminPermission());
            if (!assetsList.isEmpty()) {
                DirTree dirTree = new DirTree();
                dirTree.setDataTreeType(DirTree.DataTreeType.FILE);
                dirTree.setAssetsList(assetsList);
                dirTreeList.add(dirTree);
            }
            map.put("list", dirTreeList);
            list.add(map);
        }
		return list;
    }

    /**
	 *@Author: mahongfei
	 *@description: 获取子目录
	 */
    public List<DirTree> getchildrenDataTree(int firstDirId,
                                             String dirLastId,
                                             String projectUuid,
                                             String categoryName,
                                             String dirParentId) {
        List<DirTree> result = new ArrayList<>();
        String dirId = String.valueOf(firstDirId);
        List<Folder> folderList = folderMapper.listAllByDirId(dirId, projectUuid, categoryName, dirParentId);
        for (Folder folder : folderList) {
            DirTree dirTree = new DirTree();
            dirTree.setTreeId(folder.getDirId());
            dirTree.setUuid(folder.getUuid());
            dirTree.setParentId(folder.getParentId());
            dirTree.setTreename(folder.getDirName());
            dirTree.setLocked(folder.isLocked());
            dirTree.setDataTreeType(DirTree.DataTreeType.DIR);
            dirParentId = dirTree.getUuid();
            List<DirTree> childrenDir = new ArrayList<>();
            if (firstDirId != Integer.parseInt(dirLastId)) {
                childrenDir = getchildrenDataTree(firstDirId + 1, dirLastId, projectUuid, categoryName, dirParentId);
            }

            List<DirTree> childrenDataSet = getChildrenData(dirTree, projectUuid, categoryName);
            List<DirTree> children = new ArrayList<>();
            if (!childrenDir.isEmpty()) {
                children.addAll(childrenDir);
            }
            if (!childrenDataSet.isEmpty()) {
                children.addAll(childrenDataSet);
            }
            dirTree.setChildren(children);
            result.add(dirTree);
        }

        return result;
    }

    /**
     *@Author: mahongfei
     *@description: 获取目录树下的数据集
     */
    public List<DirTree> getChildrenData(DirTree dirTree, String projectUuid, String categoryName) {
        //获取指定目录节点下所有文件
        List<Assets> assetsList = assetsMapper.selectAssertByParentId(dirTree.getUuid(), projectUuid, categoryName,
                    UserHolder.hasAdminPermission());
        List<DirTree> dirAssetsList = new ArrayList<>();
        if (!assetsList.isEmpty()) {
            DirTree child = new DirTree();
            child.setDataTreeType(DirTree.DataTreeType.FILE);
            child.setAssetsList(assetsList);
            dirAssetsList.add(child);
        }
        return dirAssetsList;
    }

    /**
     *@Author: mahongfei
     *@description: 编辑文件夹
     */
     @Override
    public void editFolder(Folder folder) {
        if (StringUtils.isBlank(folder.getUuid())) {
            throw new IllegalArgumentException("uuid不能为空");
        }
        if (StringUtils.isBlank(folder.getDirName()) || folder.getDirName().length() > 20) {
            throw new IllegalArgumentException("名称不能为空并且长度不能超过20个字符");
        }

        try {
             Folder folder1 = folderMapper.selectByUuid(folder.getUuid());
             if (null != folder1 && !folder1.isLocked()) {
                 folderMapper.update(folder);
             } else {
                 throw new IllegalArgumentException("uuid不存在或者被锁定");
             }
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("名称不能重复");
        } catch (Exception e) {
            logger.error("编辑文件夹失败. folder: {}", folder, e);
            throw new IllegalArgumentException("编辑失败");
        }
    }

    @Override
    @Transactional
    public Result deleteFolder(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("uuid不能为空");
        }

        try {
            Folder folder = folderMapper.selectByUuid(uuid);
            if (folder == null) {
                throw new IllegalArgumentException("文件夹不存在");
            }

            // 获取需要删除的所有文件夹，并判断文件夹是否有被锁定的
            List<Folder> deleteFolders = new ArrayList<>();
            List<Folder> parentFolders = new ArrayList<>();
            parentFolders.add(folder);
            getAllDeleteFolders(deleteFolders, parentFolders);

            List<RecycleBin> recycleBins = new ArrayList<>();
            List<String> deleteFolderUuids = new ArrayList<>();
            for (Folder deleteFolder : deleteFolders) {
                if (deleteFolder.isLocked()) {
                    return Result.newError().withMsg("文件夹或子文件夹被锁定，无法删除");
                }

                deleteFolderUuids.add(deleteFolder.getUuid());

                RecycleBin recycleBin = new RecycleBin();
                recycleBin.setUuid(IdUtils.UUID());
                recycleBin.setProjectUuid(folder.getProjectUuid());
                recycleBin.setAssetsUuid(deleteFolder.getUuid());
                recycleBin.setAssetsName(deleteFolder.getDirName());
                recycleBin.setAssetsType("dir_" + folder.getCategoryName());
                recycleBin.setCreator(UserHolder.currentUser());
                recycleBins.add(recycleBin);
            }

            // 获取所有文件夹以及子文件夹下的未彻底删除的资源文件并判断锁定的个数
            List<Assets> deleteAssets = assetsMapper.selectByParentIds(deleteFolderUuids);
            List<String> deleteAssetsUuids = new ArrayList<>();
            if (!deleteAssets.isEmpty()) {
                for (Assets assets : deleteAssets) {
                    if (assets.isLocked()) {
                        return Result.newError().withMsg("文件夹或子文件夹下的资源文件已被锁定，无法删除");
                    }
                    if (assets.getEditor() != null && StringUtils.isNotBlank(assets.getEditor().getUuid())) {
                        return Result.newError().withMsg("文件夹或子文件夹下的资源文件正在编辑，无法删除");
                    }
                    int referCount = referMapper.countBySubjectUuid(assets.getUuid());
                    if (referCount > 0) {
                        return Result.newError().withMsg("文件夹或子文件夹下的资源文件已被引用，无法删除");
                    }

                    deleteAssetsUuids.add(assets.getUuid());

                    RecycleBin recycleBin = new RecycleBin();
                    recycleBin.setUuid(IdUtils.UUID());
                    recycleBin.setProjectUuid(folder.getProjectUuid());
                    recycleBin.setAssetsUuid(assets.getUuid());
                    recycleBin.setAssetsName(assets.getName());
                    recycleBin.setAssetsType(assets.getType());
                    recycleBin.setCreator(UserHolder.currentUser());
                    recycleBins.add(recycleBin);
                }
            }

            // 根据文件夹，删除本文件夹以及子文件夹下的资源文件
            if (!deleteAssetsUuids.isEmpty()) {
                assetsMapper.deleteByUuids(deleteAssetsUuids);
            }
            folderMapper.deleteByUuids(deleteFolderUuids);
            recycleBinMapper.insertBatch(recycleBins);
            return Result.SUCCESS;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("删除文件夹失败: {}", uuid, e);
            throw new IllegalStateException("删除文件夹失败");
        }
    }

    /**
     * 获取所有需要删除的文件夹
     *
     * @param deleteFolders
     * @param parentFolders
     */
    private void getAllDeleteFolders(List<Folder> deleteFolders, List<Folder> parentFolders) {
        if (parentFolders.isEmpty()) {
            return;
        }

        deleteFolders.addAll(parentFolders);
        List<String> parentUuids = new ArrayList<>();
        parentFolders.forEach(pf -> parentUuids.add(pf.getUuid()));
        List<Folder> folders = folderMapper.selectByParentUuids(parentUuids);
        if (!folders.isEmpty()) {
            getAllDeleteFolders(deleteFolders, folders);
        }
    }

    @Override
    public Result lockFolder(String uuid, Boolean isLock) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("uuid不能为空");
        }
        if (isLock == null) {
            throw new IllegalArgumentException("isLock不能为空");
        }

        try {
            User user = UserHolder.currentUser();
            if (!user.isAdmin()) {
                Folder lockInfo = folderMapper.selectLockInfo(uuid);
                if (lockInfo == null) {
                    return Result.newError().withMsg("查询信息为空");
                }
                if (!lockInfo.getCreator().getUuid().equals(user.getUuid())) {
                    return Result.newError().withMsg("只有文件夹的创建人和管理员才可以锁定和解锁文件夹!");
                }
            }
            folderMapper.updateLock(uuid, isLock);
            return Result.newSuccess();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(),"文件夹:" + uuid + "锁操作出错");
            throw new IllegalStateException("操作失败");
        }
    }
}
