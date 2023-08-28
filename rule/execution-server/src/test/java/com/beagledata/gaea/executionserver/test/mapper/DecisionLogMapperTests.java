package com.beagledata.gaea.executionserver.test.mapper;

import com.beagledata.gaea.executioncore.entity.DecisionLog;
import com.beagledata.gaea.executionserver.TestApplication;
import com.beagledata.gaea.executionserver.mapper.DecisionLogMapper;
import com.beagledata.util.IdUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @Author yangyongqiang
 * @Date 5:09 下午 2020/7/14
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class DecisionLogMapperTests {
	@Autowired
	private DecisionLogMapper decisionLogMapper;

	@Test
	public void insertBatch() {
		Date date = new Date();//取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);//把日期往后增加一天.正数往后推,负数往前推
		date = calendar.getTime();   //这个时间就是日期往后推一天的结果

		List<DecisionLog> list = new ArrayList<>();
		DecisionLog d1 = new DecisionLog();
		d1.setUuid(IdUtils.UUID());
		d1.setCreateTime(date);
		d1.setDeployUuid("B111");
		d1.setMicroUuid("B222");
		d1.setOrgName("B333");
		d1.setOrgUuid("B444");
		d1.setPkgBaseline(5);
		d1.setPkgUuid("B666");
		d1.setState(7);
		d1.setReqType(8);
		d1.setUserUuid("B888");
		d1.setUserName("B999");
		d1.setReqText("B000");
		d1.setResText("B001");
		list.add(d1);

		DecisionLog d2 = new DecisionLog();
		d2.setUuid(IdUtils.UUID());
		d2.setCreateTime(date);
		d2.setDeployUuid("A111");
		d2.setMicroUuid("A222");
		d2.setOrgName("A333");
		d2.setOrgUuid("A444");
		d2.setPkgBaseline(5);
		d2.setPkgUuid("A666");
		d2.setState(7);
		d2.setReqType(8);
		d2.setUserUuid("A888");
		d2.setUserName("A999");
		d2.setReqText("A000");
		d2.setResText("A001");
		list.add(d2);
		decisionLogMapper.insertBatch(list);
	}

	@Test
	public void insert() {
		Date date = new Date();//取时间
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, 1);//把日期往后增加一天.正数往后推,负数往前推
		date = calendar.getTime();   //这个时间就是日期往后推一天的结果

		DecisionLog d = new DecisionLog();
		d.setUuid(IdUtils.UUID());
		d.setCreateTime(date);
		d.setDeployUuid("111");
		d.setMicroUuid("222");
		d.setOrgName("333");
		d.setOrgUuid("444");
		d.setPkgBaseline(5);
		d.setPkgUuid("666");
		d.setState(7);
		d.setReqType(8);
		d.setUserUuid("888");
		d.setUserName("999");
		d.setReqText("000");
		d.setResText("001");
		decisionLogMapper.insert(d);
	}
}
