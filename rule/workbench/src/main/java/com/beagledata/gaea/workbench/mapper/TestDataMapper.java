package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.TestData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by liulu on 2018/1/25.
 */
@Mapper
public interface TestDataMapper {
    /**
     * @author liulu
     * 2018/1/25 17:14
     */
    int insert(TestData testData);

    /**
     * @author liulu
     * 2018/1/25 17:16
     */
    List<TestData> selectByProjectPackage(@Param("start") int start, @Param("end") int end, @Param("packageId") String packageId, @Param("baselineVersion") int baselineVersion);
    
    /**
     * @author liulu
     * 2018/1/25 17:17
     */
    int delete(String uuid);
}
