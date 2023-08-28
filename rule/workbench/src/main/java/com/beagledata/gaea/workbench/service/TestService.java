package com.beagledata.gaea.workbench.service;

import com.beagledata.gaea.workbench.entity.TestData;
import java.util.List;

/**
 * Created by liulu on 2018/1/25.
 */
public interface TestService {
    /**
     * @author liulu
     * 2018/1/25 17:18
     */
    List<TestData> listDataByProjectPackage(int page, int pageNum, String packageId, Integer baselineVersion);
    
    /**
     * @author liulu
     * 2018/1/26 10:09
     */
    void addData(TestData testData);
    
    /**
     * @author liulu
     * 2018/1/26 10:15
     */
    void deleteData(String uuid);
}
