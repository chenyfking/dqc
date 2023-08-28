package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Folder;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * Created by mahongfei on 2018/11/27.
 */

@Mapper
public interface FolderMapper {
	/**
	 * @Author: mahongfei
	 * @description: 添加文件夹信息
	 */
	int insert(Folder folder);

	/**
	 * @Author: mahongfei
	 * @description: 获取所有的dirid
	 */
	List<String> listDirId(String projectUuid, String categoryName);

	/**
	 * @Author: mahongfei
	 * @description: 获取子目录
	 */
	List<Folder> listAllByDirId(String dirId, String projectUuid, String categoryName, String parentId);

	/**
	 * @Author: mahongfei
	 * @description: 编辑名称
	 */
	int update(Folder folder);

	/**
	 * @Author: mahongfei
	 * @description: 删除文件夹
	 */
	int delete(Folder folder);

	/**
	 * @Author: mahongfei
	 * @description: 查找文件夹
	 */
	Folder selectByUuid(String uuid);

	/**
	 * @Author: mahongfei
	 * @description: 查找子类文件夹
	 */
	List<Folder> selectByParentId(String parentId);

	/**
	 * @Author: mahongfei
	 * @description: 查询文件夹锁定的相关属性
	 */
	Folder selectLockInfo(String uuid);

	/**
	 * @Author: mahongfei
	 * @description: 更改锁定状态
	 */
	void updateLock(String uuid, Boolean isLock);

	/**
	 * @Author: mahongfei
	 * @description: 根据项目uuid查找文件夹
	 */
	List<Folder> selectByProjectUuid(String projectUuid);

	/**
	 * @author liulu
	 * 2020/4/27 19:11
	 */
	int insertBatch(List<Folder> folders);

	/**
	 * 描述: 根据参数找文件夹
	 * @param: [folder]
	 * @author: 周庚新
	 * @date: 2020/6/23
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Folder>
	 */
	List<Folder> selectByParames(Folder folder);

	/**
	 * 描述: 根据文件夹uuid 统计改文件夹子文件夹以及文件锁定个数
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.lang.Integer
	 */
	Integer countLocked(String uuid);

	/**
	 * 描述: 根据文件夹uui统计 资源文件的uuid
	 * @param: [uuid]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<java.lang.String>
	 */
	List<String> selectAssetsUuidByFolderUuid(String uuid);

	void deleteFolders(String uuid);

	void deleteAssets(String uuid);

	/**
	 * 描述: 根据文件夹uuid 获取文件夹以及子文件夹
	 * @param: [uuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: java.util.List<com.beagledata.gaea.workbench.entity.Folder>
	 */
	List<Folder> selectByParentUuids(Collection<String> uuids);

	/**
	 * 描述: 根据文件夹uuid删除
	 * @param: [uuids]
	 * @author: 周庚新
	 * @date: 2020/11/11
	 * @return: void
	 */
	void deleteByUuids(Collection<String> uuids);

}
