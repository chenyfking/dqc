package com.beagledata.gaea.workbench.mapper;

import com.beagledata.gaea.workbench.entity.Client;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 集群节点
 * Created by Chenyafeng on 2018/11/7.
 */
@Mapper
public interface ClientMapper {
	/**
	 * 新增集群节点
	 * @author chenyafeng
	 * @date 2018/11/7
	 */
	int insert(Client client);

	/**
	 * 修改集群节点
	 * @author chenyafeng
	 * @date 2018/11/7
	 */
	int update(Client client);

	/**
	 * 删除集群节点
	 * @author chenyafeng
	 * @date 2018/11/7
	 */
	int delete(String uuid);

	/**
	 * 根据uuid查询集群节点
	 * @param uuid
	 * @return
	 */
	Client selectByUuid(String uuid);

	/**
	 * 根据url查询集群节点
	 * @author yinrj
	 * @date 2020/7/20
	 */
	Client selectByUrl(String url);

	/**
	 * 查询关联某个服务的集群节点
	 * Created by Chenyafeng on 2019/6/12
	 */
	List<Client> selectByMicro(@Param("microUuid") String microUuid);

	/**
	 * 查询最近新增的集群节点
	 */
	Client selectRecently();

	/**
	 * 统计baseUrl数量
	 * @param baseUrl
	 * @return
	 */
	int countByBaseUrl(String baseUrl);

	/**
	 * @return 查询所有集群节点
	 */
	List<Client> selectAll();

	/**
	 * @return 查询所有可用集群节点
	 */
	List<Client> selectAllAvailable();

	/**
	 * 根据多个uuid批量查询集群节点集合
	 * @param clients
	 * @return
	 */
	List<Client> selectByUuids(List<Client> clients);
}
