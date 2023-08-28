package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.RecycleBin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 回收站
 * Created by Chenyafeng on 2020/6/24
 */
@Mapper
public interface RecycleBinMapper {

    RecycleBin selectByUuid(String uuid);

    List<RecycleBin> selectList(@Param("start") Integer start, @Param("end")Integer end, @Param("recycleBin") RecycleBin recycleBin);

    int count(@Param("recycleBin") RecycleBin recycleBin);

    void insert(RecycleBin recycleBin);

    int restoreFolder(Collection<String> uuids);

    /**
     * 批量删除回收站记录
     * @param uuids
     */
    void delete(@Param("uuids")String uuids);

    /**
     * 清空项目的回收站
     * @param projectUuid
     */
    void deleteByProject(@Param("projectUuid")String projectUuid);

    /**
     * 还原后根据还原的assets.uuid查询左侧树节点
     *
     * @param projectUuid
     * @param uuids
     * @return
     */
    List<Map<String, Object>> selectNodesByAssetsUuid(
            @Param("projectUuid") String projectUuid,
            @Param("uuids") Collection<String> uuids
    );

    /**
     * 还原模板
     *
     * @param uuids
     * @return
     */
    int restoreTemplate(Collection<String> uuids);

    /**
     * 获取还原的文件夹uuid
     *
     * @param folderUuids
     * @return
     */
    List<String> selectRestoreChildFolderUuids(Collection<String> folderUuids);

    /**
     * 批量插入到回收站
     *
     * @param recycleBins
     * @return
     */
    int insertBatch(List<RecycleBin> recycleBins);

    /**
     * 根据文件夹uuid查询能够还原的资源文件
     *
     * @param folderUuids
     * @return
     */
    List<String> selectRestoreAssetsUuidsByDirUuids(Collection<String> folderUuids);

    /**
     * 还原资源文件
     *
     * @param assetsUuids
     * @return
     */
    int restoreAssets(Collection<String> assetsUuids);

    /**
     * 根据assetsUuid删除回收站记录
     *
     * @param recycleAssetsUuids
     * @return
     */
    int deleteByAssetsUuids(Collection<String> recycleAssetsUuids);
}
