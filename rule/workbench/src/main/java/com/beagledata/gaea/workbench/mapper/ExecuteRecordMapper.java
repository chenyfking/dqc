package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.ExecuteRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
 * Created by liulu on 2020/5/19.
 */
@Mapper
public interface ExecuteRecordMapper {
    /**
     * @param record
     * @return
     */
    int insert(ExecuteRecord record);

    /**
     * 查询服务的调用记录
     *
     * @param microUuid
     * @return
     */
    List<ExecuteRecord> selectByMicro(
            String microUuid,
            Integer start,
            Integer limit,
            Date startTime,
            Date endTime
    );

    /**
     * 查询调用记录数量
     *
     * @param microUuid
     * @param startTime
     * @param endTime
     * @return
     */
    int countByMicro(String microUuid, Date startTime, Date endTime);
}
