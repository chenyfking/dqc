package com.beagledata.gaea.workbench.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.service.MicroDeploymentService;
import com.beagledata.gaea.workbench.service.MicroRouteService;
import com.beagledata.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liulu on 2021/1/4.
 */
@WebListener
public class UpgradeListener implements ServletContextListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private UpgradeMapper upgradeMapper;
    @Autowired
    private ClientMapper clientMapper;
    @Autowired
    private MicroMapper microMapper;
    @Autowired
    private MicroRelationMapper microRelationMapper;
    @Autowired
	private MicroDeploymentService microDeploymentService;
    @Autowired
	private MicroRouteService microRouteService;
	@Autowired
	AssetsService assetsService;
	@Autowired
	ReferMapper referMapper;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (shouldUpgrade()) {
            logger.info("开始升级新版本...");
            doUpgrade();
            logger.info("新版本升级完成");
        }
    }

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {

	}

	/**
	 * @return 是否需要运行升级代码（判断当前数据库是否旧版本）
	 */
	private boolean shouldUpgrade() {
		List list = upgradeMapper.selectSql("describe t_assets tag");
		if (CollectionUtils.isEmpty(list) || list.size() != 1) {
			return false;
		}
		return true;
	}

	private void doUpgrade() {
		try {
			doUpgrade1();
			doUpgrade2();
			doUpgrade3();
			doUpgrade4();
			doUpgrade5();
			doUpgrade6();
			doUpgrade7();
			doUpgrade8();
			doUpgrade9();
			doUpgrade10();
			doUpgrade11();
			doUpgrade12();
			doUpgrade13();
			doUpgrade14();
			doUpgrade21();
			saveRuleNewVersion(false);
			doUpgrade15();
			saveRuleNewVersion(true);
			doUpgrade19();
			doUpgrade16();
			doUpgrade20();
			doUpgrade22();
			doUpgradeRefer();
		} catch (Exception e) {
			logger.error("系统升级异常", e);
			System.exit(-1);
		}
	}

	/**
	 * 创建新增的表
	 * t_approval_report、t_audit_record、t_deployment_report、
	 * t_detail_report、t_micro_deployment、t_micro_deployment_model、t_micro_route、
	 * t_refer、t_summary_report、t_testcase
	 * @author yinrj
	 * @date 0005 2021/1/5 10:51
	 */
	private void doUpgrade1() throws Exception {
		try {
			upgradeMapper.createTables();
		} catch (Exception e) {
			throw new Exception("创建新增的表出错:", e);
		}
	}

	/**
	 * 描述: 数据模型支持模型英文名称
	 * 升级 t_assets 表 t_assets添加列en_name，删除tag列
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void doUpgrade2() {
		logger.info("开始升级t_assets表");
		//删除列tag
		deleteColumn("ALTER TABLE `t_assets` DROP COLUMN `tag`;");

		//删除索引 NewIndex1
		deleteIndex("ALTER TABLE `t_assets` DROP INDEX `NewIndex1`;");
		//删除索引 uuid
		deleteIndex("ALTER TABLE `t_assets` DROP INDEX `uuid`;");

		//新增列 en_name
		addColumn("ALTER TABLE `t_assets` ADD COLUMN `en_name` varchar(50) NULL DEFAULT NULL COMMENT '英文名称' AFTER `name`;");

		//新增索引  uk_uuid
		addIndex("ALTER TABLE `t_assets` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");
		//新增索引  uk_name
		addIndex("ALTER TABLE `t_assets` ADD UNIQUE INDEX `uk_name`(`name`, `project_uuid`, `type`, `delete_version`) USING BTREE;");
		//新增索引  uk_en_name
		addIndex("ALTER TABLE `t_assets` ADD UNIQUE INDEX `uk_en_name`(`en_name`, `project_uuid`, `delete_version`) USING BTREE;");

		logger.info("t_assets表升级完成");
	}

	/**
	 * @Author yangyongqiang
	 * @Description 知识包添加创建者
	 * @Date 2:52 下午 2021/1/5
	 **/
	private void doUpgrade3() {
		logger.info("开始升级t_knowledge_package表");
		//插入creator_uuid字段
		addColumn("ALTER TABLE `t_knowledge_package` ADD COLUMN `creator_uuid` char(32)  NULL DEFAULT NULL COMMENT '创建人uuid' AFTER `description`;");
		//更新t_knowledge_package，creator_uuid
		upgradeMapper.updateKnowledgePackageCreatorUuid();
		logger.info("t_knowledge_package表升级完成");
	}

	/**
	 * 升级t_logs表
	 * @author yinrj
	 * @date 2021/1/5
	 */
	private void doUpgrade4() {
		//升级 t_logs: isSuccess修改为is_success
		logger.info("开始升级t_logs表");
		//修改列 request_params
		modifyColumn("ALTER TABLE `t_logs` MODIFY COLUMN `request_params` longtext  NULL COMMENT '请求参数' AFTER `request_method`;");
		//修改列 isSuccess 改为is_success
		modifyColumn("ALTER TABLE `t_logs` CHANGE `isSuccess` `is_success` tinyint(1) NULL DEFAULT 1 COMMENT '是否请求成功' AFTER `back_value`;");
		//新增索引 idx_search
		addIndex("ALTER TABLE `t_logs` ADD INDEX `idx_search`(`opt_name`, `user`, `client_ip`, `is_success`, `begin_time`) USING BTREE;");
		logger.info("t_logs表升级完成");
	}

	/**
	 * 描述: t_micro升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void doUpgrade5() {
		logger.info("开始升级t_micro表");
		//删除列type_name
		deleteColumn("ALTER TABLE `t_micro` DROP COLUMN `type_name`;");
		//删除列invoke_times
		deleteColumn("ALTER TABLE `t_micro` DROP COLUMN `invoke_times`;");

		//删除索引 uuid_UNIQUE
		deleteIndex("ALTER TABLE `t_micro` DROP INDEX `uuid_UNIQUE`;");
		//删除索引 un_package_del
		deleteIndex("ALTER TABLE `t_micro` DROP INDEX `un_package_del`;");

		//新增列 approval_label
		addColumn("ALTER TABLE `t_micro` ADD COLUMN `approval_label` varchar(200)  NULL DEFAULT NULL COMMENT '设置审批字段' AFTER `delete_version`;");
		//新增列 project_uuid
		addColumn("ALTER TABLE `t_micro` ADD COLUMN `project_uuid` char(32)  NULL DEFAULT NULL COMMENT '项目uuid' AFTER `approval_label`;");

		//新增索引  uk_uuid
		addIndex("ALTER TABLE `t_micro` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");
		//新增索引  uk_package
		addIndex("ALTER TABLE `t_micro` ADD UNIQUE INDEX `uk_package`(`package_uuid`, `delete_version`) USING BTREE;\n");

		// 修改 micro 服务所属项目uuid
		upgradeMapper.updateMicroProjectUuid();

		logger.info("t_micro表升级完成");
	}


	/**
	 * @Author yangyongqiang
	 * @Description t_project升级
	 * @Date 2:53 下午 2021/1/5
	 **/
	private void doUpgrade6() {
		logger.info("开始升级t_project表");
		//删除file_num列
		deleteColumn("ALTER TABLE `t_project` DROP COLUMN `file_num`;");
		logger.info("t_project表升级完成");
	}

	/**
	 * 升级t_project_user表
	 * @author yinrj
	 * @date 2021/1/5
	 */
	private void doUpgrade7() {
		//升级t_project_user表
		logger.info("开始升级t_project_user表");
		//新增列 project_uuid
		addColumn("ALTER TABLE `t_project_user` ADD COLUMN `project_uuid` varchar(32)  NOT NULL DEFAULT '0' COMMENT '项目uuid' AFTER `id`;");
		//新增列 user_uuid
		addColumn("ALTER TABLE `t_project_user` ADD COLUMN `user_uuid` varchar(36)  NOT NULL DEFAULT '0' COMMENT '用户uuid' AFTER `project_uuid`;");
		//同步project_uuid字段数据
		upgradeMapper.updateProjectUserProjectUuid();
		//同步user_uuid字段数据
		upgradeMapper.updateProjectUserUserUuid();
		//删除索引
		deleteIndex("ALTER TABLE `t_project_user` DROP INDEX `un_pu`;");
		//删除project_id字段
		deleteColumn("ALTER TABLE `t_project_user` DROP COLUMN `project_id`;");
		//删除user_id字段
		deleteColumn("ALTER TABLE `t_project_user` DROP COLUMN `user_id`;");
		//添加索引
		addIndex("ALTER TABLE `t_project_user` ADD UNIQUE INDEX `uk_project_user`(`project_uuid`, `user_uuid`) USING BTREE;");
		logger.info("t_project_user表升级完成");
	}

	/**
	 * 描述: t_thirdapi_definition 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void doUpgrade8() {
		logger.info("开始升级t_thirdapi_definition表");

		//删除索引 uuid_UNIQUE
		deleteIndex("ALTER TABLE `t_micro` DROP INDEX `uuid_UNIQUE`;");

		//新增列 req_content_type
		addColumn("ALTER TABLE `t_thirdapi_definition` ADD COLUMN `req_content_type` varchar(30)  NULL DEFAULT NULL COMMENT 'post请求的content-type' AFTER `method`;");
		//新增列 res_json_path
		addColumn("ALTER TABLE `t_thirdapi_definition` ADD COLUMN `res_json_path` varchar(255)  NULL DEFAULT NULL COMMENT '相应json查询path' AFTER `headers_json`;");

		//新增索引  uk_uuid
		addIndex("ALTER TABLE `t_micro` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");

		logger.info("t_thirdapi_definition表升级完成");
	}

	/**
	 * @Author yangyongqiang
	 * @Description t_token升级
	 * @Date 2:53 下午 2021/1/5
	 **/
	private void doUpgrade9() {
		logger.info("开始升级t_token表");
		//新增is_all列
		addColumn("ALTER TABLE `t_token` ADD COLUMN `is_all` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否是全部服务token 0 不是，1 是' AFTER `uuid`;");
		logger.info("t_token表升级完成");
	}

	/**
	 * 升级t_user表
	 * @author yinrj
	 * @date 2021/1/6
	 */
	private void doUpgrade10() {
		//升级t_user表
		logger.info("开始升级t_user表");
		//新增列 is_force_logout
		addColumn("ALTER TABLE `t_user` ADD COLUMN `is_force_logout` tinyint(1) NULL DEFAULT 0 COMMENT '是否强制退出 0 没有强制退出 1 强制退出' AFTER `delete_version`;");
		//新增列 org_uuid
		addColumn("ALTER TABLE `t_user` ADD COLUMN `org_uuid` char(32)  NULL DEFAULT NULL COMMENT '所属机构uuid' AFTER `is_force_logout`;");
		//新增列 last_reset_pwd
		addColumn("ALTER TABLE `t_user` ADD COLUMN `last_reset_pwd` datetime(0) NULL DEFAULT NULL COMMENT '最后一次修改密码时间' AFTER `org_uuid`;");
		//同步org_uuid字段数据
		upgradeMapper.updateUserOrgUuid();
		//删除索引
		deleteIndex("ALTER TABLE `t_user` DROP INDEX `t_user_uuid_uindex`;");
		//删除索引
		deleteIndex("ALTER TABLE `t_user` DROP INDEX `t_user_username_delete_version_uindex`;");
		//删除is_admin字段
		deleteColumn("ALTER TABLE `t_user` DROP COLUMN `is_admin`;");
		//添加索引
		addIndex("ALTER TABLE `t_user` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");
		//添加索引
		addIndex("ALTER TABLE `t_user` ADD UNIQUE INDEX `uk_username`(`username`, `delete_version`) USING BTREE;");
		logger.info("t_user表升级完成");
	}

	/**
	 * 描述: t_user_permission 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade11() {
		logger.info("开始升级t_user_permission表");

		//删除索引 uk_user_permission
		deleteIndex("ALTER TABLE `t_user_permission` DROP INDEX `uk_user_permission`;");

		//新增列 user_uuid
		addColumn("ALTER TABLE `t_user_permission` ADD COLUMN `user_uuid` char(32)  NULL DEFAULT NULL COMMENT '用户UUID' AFTER `id`;");

		//根据user_id 同步 user_uuid
		upgradeMapper.updateUserUuidInUserPermission();

		//删除列 user_id
		deleteColumn("ALTER TABLE `t_user_permission` DROP COLUMN `user_id`;");
		//新增索引  uk_user_permission
		addIndex("ALTER TABLE `t_user_permission` ADD UNIQUE INDEX `uk_user_permission`(`user_uuid`, `permission_id`) USING BTREE;\n");

		logger.info("t_user_permission表升级完成");
	}

	/**
	 * @Author yangyongqiang
	 * @Description t_user_role升级
	 * @Date 2:53 下午 2021/1/5
	 **/
	private void doUpgrade12() {
		logger.info("开始升级t_user_role表");
		//新增user_uuid列
		addColumn("ALTER TABLE `t_user_role` ADD COLUMN `user_uuid` char(32)  NULL DEFAULT NULL COMMENT '用户UUID' AFTER `id`;");
		//根据user_id列查询userUuid设置user_uuid值
		upgradeMapper.updateUserRoleUserUuid();
		//删除索引
		deleteIndex("ALTER TABLE `t_user_role` DROP INDEX `uk_user_role`;");
		//删除user_id列
		deleteColumn("ALTER TABLE `t_user_role` DROP COLUMN `user_id`;");
		//新增索引  uk_user_role
		addIndex("ALTER TABLE `t_user_role` ADD UNIQUE INDEX `uk_user_role`(`user_uuid`, `role_id`) USING BTREE;");
		logger.info("t_user_role表升级完成");
	}

	/**
	 * 升级评分卡数据
	 * @author yinrj
	 * @date 2021/1/6
	 */
	private void doUpgrade13() {
		logger.info("开始升级评分卡数据");
		Assets assets = new Assets();
		assets.setType(AssetsType.SCORECARD);
		List<Assets> scoreCardList = upgradeMapper.selectAssets(assets, 0, 0);
		List<AssetsVersion> scoreCardVersionList = upgradeMapper.selectAssetsVersion(assets, 0, 0);
		for (Assets assets1 : scoreCardList) {
			if (StringUtils.isNotBlank(assets1.getContent())) {
				String contentNew = upgradeScoreCardContent(assets1.getContent());
				assets1.setContent(contentNew);
				upgradeMapper.updateAssetsById(assets1);
			}
		}
		for (AssetsVersion version : scoreCardVersionList) {
			if (StringUtils.isNotBlank(version.getContent())) {
				String contentNew = upgradeScoreCardContent(version.getContent());
				version.setContent(contentNew);
				upgradeMapper.updateAssetsVersionById(version);
			}
		}
		logger.info("评分卡数据升级完成");
	}

	private String upgradeScoreCardContent(String assetsContent) {
		JSONObject content = JSONObject.parseObject(assetsContent);
		String factId = content.getString("factId");
		if (StringUtils.isBlank(factId)) {
			return assetsContent;
		}
		JSONArray rows = content.getJSONArray("rows");
		rows.stream().forEach(row ->
				upgradeScoreCardRow((JSONObject) row, factId)
		);
		content.remove("factId");
		content.replace("rows", rows);
		content.put("defaultScore", 0);
		content.put("reasonCodes", new JSONObject());
		content.put("reasonMsgs", new JSONObject());
		return content.toJSONString();
	}

	private JSONObject upgradeScoreCardRow(JSONObject row, String id) {
		String fieldId = row.getString("fieldId");
		row.remove("fieldId");
		JSONObject fact = new JSONObject();
		fact.put("id", id);
		fact.put("fieldId", fieldId);
		row.put("fact", fact);
		return row;
	}

	/**
	 * 描述: 决策表升级  原先属性是针对全部行的，现在需要拆分到每一行
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade14() {
		logger.info("开始升级决策表content");
		Assets assetsSearch = new Assets();
		assetsSearch.setType(AssetsType.RULE_TABLE);
		int start = 0;
		int size = 0;
		List<Assets> assetsList = upgradeMapper.selectAssets(assetsSearch, start, size);
		for (Assets assets : assetsList) {
			String content = assets.getContent();
			if (StringUtils.isNotBlank(assets.getContent())) {
				content = upgradeRuleTableContent(content);
			}
			assets.setContent(content);
			upgradeMapper.updateAssetsById(assets);
		}
		List<AssetsVersion> assetsVersionsList = upgradeMapper.selectAssetsVersion(assetsSearch, start, size);
		for (AssetsVersion assetsVersion : assetsVersionsList) {
			String content = assetsVersion.getContent();
			if (StringUtils.isNotBlank(content)) {
				content = upgradeRuleTableContent(content);
			}
			assetsVersion.setContent(content);
			upgradeMapper.updateAssetsVersionById(assetsVersion);
		}
		logger.info("决策表content升级完成");
	}

	/**
	 * 描述: 升级 决策表 内容
	 * @param: [assets]
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return:
	 */
	private String upgradeRuleTableContent(String content) {
		JSONObject contentObj = JSONObject.parseObject(content);
		JSONArray attrs = contentObj.getJSONArray("attrs");
		if (attrs != null && attrs.size() > 0) {
			JSONArray headers = contentObj.getJSONArray("headers");
			JSONArray rows = contentObj.getJSONArray("rows");
			for (int i = 0; i < attrs.size(); i++) {
				JSONObject attrObj = attrs.getJSONObject(i);
				String name = attrObj.getString("name");
				Object value = attrObj.get("value");
				JSONObject headerObj = new JSONObject();
				headerObj.put("name", name);
				headerObj.put("type", "ATTR");
				headers.add(0, headerObj);
				if (rows != null && rows.size() > 0) {
					for (int j = 0; j < rows.size(); j++) {
						JSONObject rowObj = new JSONObject();
						rowObj.put("value", value);
						JSONArray array = rows.getJSONArray(j);
						array.add(0, rowObj);
					}
				}

			}
		}
		contentObj.remove("attrs");
		return JSONObject.toJSONString(contentObj);
	}

	/**
	 * 描述: 所有规则文件保存最新版本
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void saveRuleNewVersion(Boolean ruleFlow) {
		if (ruleFlow) {
			logger.info("开始保存决策流最新版本");
		} else {
			logger.info("开始保存向导式、决策表、决策树、评分卡最新版本");
		}
		upgradeMapper.saveRuleNewVersion(ruleFlow);
		if (ruleFlow) {
			logger.info("决策流最新版本保存完成");
		} else {
			logger.info("向导式、决策表、决策树、评分卡最新版本保存完成");
		}
	}

	/**
	 * @Author yangyongqiang
	 * @Description 决策流升级
	 * @Date 2:53 下午 2021/1/5
	 **/
	private void doUpgrade15() {
		Assets assets = new Assets();
		assets.setType(AssetsType.RULE_FLOW);
		int start = 0;
		int size = 0;
		List<Assets> assetsList = upgradeMapper.selectAssets(assets, start, size);
		for (Assets a : assetsList) {
			String content = a.getContent();
			//存储的Content不能为空
			if (StringUtils.isNotBlank(a.getContent())) {
				a.setContent(saveRuleFlowNewVersion(content));
				upgradeMapper.updateAssetsById(a);
			}
		}

		List<AssetsVersion> assetsVersionsList = upgradeMapper.selectAssetsVersion(assets, start, size);
		for (AssetsVersion a : assetsVersionsList) {
			String content = a.getContent();
			//存储的Content不能为空
			if (StringUtils.isNotBlank(a.getContent())) {
				a.setContent(saveRuleFlowNewVersion(content));
				upgradeMapper.updateAssetsVersionById(a);
			}
		}
	}

	private String saveRuleFlowNewVersion(String content) {
		JSONObject contentObj = JSONObject.parseObject(content);
		JSONArray newNdaJsonArray = new JSONArray();
		JSONArray ndaJsonArray = contentObj.getJSONArray("nodeDataArray");
		for (int i = 0; i < ndaJsonArray.size(); i++) {
			JSONObject nda = ndaJsonArray.getJSONObject(i);

			Object dataObject = nda.get("data");
			//判断是否有data节点，有的话处理
			if (dataObject != null) {
				JSONObject data = new JSONObject((JSONObject) dataObject);
				JSONArray rulesArray = data.getJSONArray("rules");
				nda.remove("data");
				nda.put("rules", rulesArray);
			}

			//判断是否是Rule
			if ("Rule".equals((String) nda.get("category")) && nda.get("rules") != null) {
				JSONArray rulesJsonArray = nda.getJSONArray("rules");
				//rules节点不能为空
				if (rulesJsonArray != null && !rulesJsonArray.isEmpty()) {
					for (Object rjas : rulesJsonArray) {
						JSONObject rja = new JSONObject((JSONObject) rjas);
						Assets assetsSearch = new Assets();
						assetsSearch.setUuid((String) rja.get("uuid"));

						List<AssetsVersion> assetsObject = upgradeMapper.selectAssetsVersion(assetsSearch, 0, 0);
						if (assetsObject != null && !assetsObject.isEmpty()) {
							int index = assetsObject.size() -1 ;
							AssetsVersion assetsVersion = assetsObject.get(index);
							Integer version = assetsVersion.getVersionNo();
							if (AssetsType.RULE_FLOW.equals(assetsVersion.getType())) {
								if (version == null) {
									version = 0;
								}
								version = version + 1;
							}
							rja.remove("id");
							rja.put("versionNo", version);
							rja.put("versionDes", assetsObject.get(index).getVersionDes());
							rja.put("createTime", assetsObject.get(index).getCreateTime());
						}
					}
				}
			}
			newNdaJsonArray.add(nda);
		}
		contentObj.remove(ndaJsonArray);
		contentObj.put("nodeDataArray", newNdaJsonArray);
		return JSONObject.toJSONString(contentObj);
	}

	/**
	 * 升级知识包
	 * @author yinrj
	 * @date 2021/1/7
	 */
	private void doUpgrade16() {
		//升级t_knowledge_package_assets表数据
        doUpgrade18();
        //处理旧版t_knowledge_package没有micro_uuid的数据
		/*1)、(旧版有服务，但是服务对应的知识包被删除过，新生成的知识包没有micro_uuid的，
		 	将t_micro和t_knowledge_package表根据name和project_uuid关联，在t_knowledge_package表补充micro_uuid,
		 	在t_micro表更新package_uuid)*/
		//2)、( 1)步骤后，还存在旧版生成了知识包，没发布服务导致t_knowledge_package没有micro_uuid的，处理为新版待发布状态)
		upgradeKnowledgePackageWithoutMicroUuid();
        //升级t_knowledge_package_baseline表数据
		upgradeKnowledgePackageBaseline();
		//更新t_micro表，将所属项目删除的t_micro的is_deleted设置为true
		upgradeMicro();
		//升级t_micro_deployment表数据
		upgradeMicroDeployment();
		//升级t_micro_deployment_model表数据
		upgradeMicroDeploymentModel();
	}

	/**
	 * 升级t_knowledge_package_assets表
	 * @author yinrj
	 * @date 2021/1/7
	 */
	private void doUpgrade18() {
		//升级t_knowledge_package_assets表
		logger.info("开始升级t_knowledge_package_assets表");
		deleteIndex("ALTER TABLE `t_knowledge_package_assets` DROP INDEX `package_uuid`;");
		addColumn("ALTER TABLE `t_knowledge_package_assets` ADD COLUMN `assets_version` int(11) NULL DEFAULT NULL COMMENT '资源文件版本号' AFTER `assets_uuid`;");
		addColumn("ALTER TABLE `t_knowledge_package_assets` ADD COLUMN `baseline_version` int(11) NULL DEFAULT NULL COMMENT '基线版本号' AFTER `assets_version`;");
		addIndex("ALTER TABLE `t_knowledge_package_assets` ADD UNIQUE INDEX `uk_package_assets`(`package_uuid`, `assets_uuid`, `assets_version`, `baseline_version`) USING BTREE;");
		//更新assets_version和baseline_version字段
		upgradeMapper.updateKnowledgePackageAssetsVersions();
		//更新baseline_version
		logger.info("t_knowledge_package_assets表升级完成");
	}

	/**
	 * 描述: t_knowledge_package_baseline 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade19() {
		logger.info("开始升级t_knowledge_package_baseline表");

		//删除索引 uuid
		deleteIndex("ALTER TABLE `t_knowledge_package_baseline` DROP INDEX `uuid`;");

		//修改列 delete_version 为 int（id）
		modifyColumn("ALTER TABLE `t_knowledge_package_baseline` MODIFY COLUMN `id` int(11);");
		//修改列 delete_version 为 int（11）
		modifyColumn("ALTER TABLE `t_knowledge_package_baseline` MODIFY COLUMN `delete_version` int(11) ;");

		//新增列 audit_reason
		addColumn("ALTER TABLE `t_knowledge_package_baseline` ADD COLUMN `audit_reason` varchar(100) NULL DEFAULT NULL COMMENT '审核理由' AFTER `delete_version`;");

		//新增索引  uk_uuid
		addIndex("ALTER TABLE `t_knowledge_package_baseline` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");

		logger.info("t_knowledge_package_baseline表升级完成");
	}

	/**
	 * 描述: t_testdata 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade20() {

		logger.info("开始升级t_testdata表");

		//删除索引 uuid
		deleteIndex("ALTER TABLE `t_testdata` DROP INDEX `uuid_UNIQUE`;");

		//新增列 audit_reason
		addColumn("ALTER TABLE `t_testdata` ADD COLUMN `baseline_version` int(11) NULL DEFAULT NULL COMMENT '基线版本号' AFTER `data`;");

		//新增索引  uk_uuid
		addIndex("ALTER TABLE `t_testdata` ADD UNIQUE INDEX `uk_uuid`(`uuid`) USING BTREE;");

		logger.info("t_testdata表升级完成");
	}

	/**
	 * 描述: t_assets_version 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade21() {

		logger.info("开始升级t_assets_version表");

		//删除索引 un_assetuuid_vno_delver
		deleteIndex("ALTER TABLE `t_assets_version` DROP INDEX `un_assetuuid_vno_delver`;");

		//version_no 加一
		Integer maxId = upgradeMapper.maxVersionId();
		int page = maxId / 1000 + 1;
		for (int i = 0; i < page; i++) {
			int start = i * 1000;
			int end = (i + 1) * 1000;
			upgradeMapper.updateVersionNo(start, end);
		}

		//新增索引  uk_assetuuid_vn
		addIndex("ALTER TABLE `t_assets_version` ADD UNIQUE INDEX `uk_assetuuid_vn`(`assets_uuid`, `version_no`, `delete_version`) USING BTREE;");

		logger.info("t_assets_version表升级完成");
	}

	/**
	 * 升级t_knowledge_package表没有micro_uuid的数据
	 * @author yinrj
	 * @date 2021/1/7
	 */
	private void upgradeKnowledgePackageWithoutMicroUuid() {
		/*1)、(旧版有服务，但是服务对应的知识包被删除过，新生成的知识包没有micro_uuid的，
		 	将t_micro和t_knowledge_package表根据name和project_uuid关联，在t_knowledge_package表补充micro_uuid,
		 	在t_micro表更新package_uuid)*/

		logger.info("开始批量处理服务对应的知识包没有micro_uuid的数据");
		upgradeMapper.updateKnowledgePackageWithoutMicroUuid();
		logger.info("批量处理服务对应的知识包没有micro_uuid的数据完成");
	}

	/**
	 * 升级t_knowledge_package_baseline表
	 * @author yinrj
	 * @date 2021/1/7
	 */
	private void upgradeKnowledgePackageBaseline() {
		logger.info("开始批量生成t_knowledge_package_baseline表数据");
		//主键加自增
		modifyColumn("ALTER TABLE `t_knowledge_package_baseline` MODIFY COLUMN `id` int(10) unsigned NOT NULL AUTO_INCREMENT;");
		//修改uuid字段的长度
		modifyColumn("ALTER TABLE `t_knowledge_package_baseline` MODIFY COLUMN `uuid` char(36);");
		upgradeMapper.insertKnowledgePackageBaseline();
		upgradeMapper.updateKnowledgePackageBaseline();
		//修改uuid字段的长度
		modifyColumn("ALTER TABLE `t_knowledge_package_baseline` MODIFY COLUMN `uuid` char(32);");
		logger.info("批量生成t_knowledge_package_baseline表数据完成");
	}

	/**
	 * 描述: 升级fact 数据模型内容 type:Object  替换为 type:Map 升级
	 * @param: []
	 * @author: 周庚新
	 * @date: 2021/1/6
	 * @return: void
	 */
	private void doUpgrade22() {

		logger.info("开始升级fact内容");

		Assets assetsSearch = new Assets();
		assetsSearch.setType(AssetsType.FACT);
		assetsSearch.setContent("\"Object\"");
		int start = 0;
		int size = 0;
		List<Assets> assetsList = upgradeMapper.selectAssets(assetsSearch, start, size);
		if (assetsList != null && assetsList.size() > 0) {
			for (Assets assets : assetsList) {
				String content = assets.getContent();
				if (StringUtils.isNotBlank(assets.getContent())) {
					content = content.replace("\"Object\"", "\"Map\"");
				}
				assets.setContent(content);
				upgradeMapper.updateAssetsById(assets);
			}
		}
		logger.info("fact内容升级完成");
	}

	/**
     * 升级t_micro表
     * @author yinrj
     * @date 2021/1/13
     */
	private void upgradeMicro() {
		logger.info("开始根据t_project的删除状态批量更新t_micro数据的删除标志字段");
		upgradeMapper.updateMicroDeleteTag();
		logger.info("根据t_project的删除状态批量更新t_micro数据的删除标志字段完成");
	}

	/**
     * 升级t_micro_deployment表
     * @author yinrj
     * @date 2021/1/11
     */
	private void upgradeMicroDeployment() {
		logger.info("开始批量生成t_micro_deployment表数据");
		//修改uuid字段的长度
		modifyColumn("ALTER TABLE `t_micro_deployment` MODIFY COLUMN `uuid` char(36);");
		upgradeMapper.insertMicroDeployment();
		upgradeMapper.updateTableBySql("UPDATE t_micro_deployment SET uuid = REPLACE(uuid, \"-\", '');");
		//修改uuid字段的长度
		modifyColumn("ALTER TABLE `t_micro_deployment` MODIFY COLUMN `uuid` char(32);");
		logger.info("批量生成t_micro_deployment表数据完成");
	}

	/**
     * 升级t_micro_deployment_model表
     * @author yinrj
     * @date 2021/1/11
     */
	private void upgradeMicroDeploymentModel() {
		logger.info("开始批量生成t_micro_deployment_model表数据");
		upgradeMapper.insertMicroDeploymentModel();
		logger.info("批量生成t_micro_deployment_model表数据完成");
	}

	/**
	* 描述: 保存引用关系
	* @param: []
	* @author: 周庚新
	* @date: 2021/1/11
	* @return: void
	*/
	private void doUpgradeRefer() {
		int start = 0;
		int size = 1000;
		int resSize = 0;
		do {
			resSize = 0;
			List<Assets> referAssets = upgradeMapper.selectReferAssets(start, size);
			if (referAssets != null && referAssets.size() > 0) {
				resSize = referAssets.size();
				start = start + size;
				Set<Refer> insertRefers = new HashSet<>();
				for (Assets assets : referAssets) {
					try {
						Set<Refer> refers = assetsService.getRefers(assets);
						insertRefers.addAll(refers);
						if (insertRefers.size() > 1000) {
							referMapper.insertBatch(insertRefers);
							insertRefers = new HashSet<>();
						}
					} catch (Exception e) {
						logger.warn("获取引用关系出错，uuid ：{}， version： {}", assets.getUuid(), assets.getVersionNo());
					}
				}
				if (insertRefers.size()>0) {
					referMapper.insertBatch(insertRefers);
				}
			}
		} while (resSize == size);

		start = 0;
		do {
			resSize = 0;
			List<Assets> referAssets = upgradeMapper.selectVersionReferAssets(start, size);
			if (referAssets != null && referAssets.size() > 0) {
				resSize = referAssets.size();
				start = start + size;
				Set<Refer> insertRefers = new HashSet<>();
				for (Assets assets : referAssets) {
					try {
						Set<Refer> refers = assetsService.getRefers(assets);
						insertRefers.addAll(refers);
						if (insertRefers.size() > 1000) {
							referMapper.insertBatch(insertRefers);
							insertRefers = new HashSet<>();
						}
					} catch (Exception e) {
						logger.warn("获取引用关系出错，uuid ：{}， version： {}", assets.getUuid(), assets.getVersionNo());
					}
				}
				if (insertRefers.size()>0) {
					referMapper.insertBatch(insertRefers);
				}
			}
		} while (resSize == size);
	}

	/**
	 * 描述: 根据sql 删除列
	 * @param: [sql]
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void deleteColumn(String sql) {
		try {
			upgradeMapper.updateTableBySql(sql);
		} catch (Exception e) {
			//不存在 不算报错，有可能是已经删除
			if (!e.getMessage().contains("check that column/key exists")) {
				throw e;
			}
		}
	}

	/**
	 * 描述: 根据sql 新增列
	 * @param: [sql]
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void addColumn(String sql) {
		try {
			upgradeMapper.updateTableBySql(sql);
		} catch (Exception e) {
			//已存在 不算报错，有可能是已经删除
			if (!e.getMessage().contains("Duplicate column name")) {
				throw e;
			}

		}
	}

	/**
	 * 描述: 根据sql 修改列
	 * @param: [sql]
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void modifyColumn(String sql) {
		try {
			upgradeMapper.updateTableBySql(sql);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 描述: 根据sql 删除索引
	 * @param: [sql]
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void deleteIndex(String sql) {
		try {
			upgradeMapper.updateTableBySql(sql);
		} catch (Exception e) {
			//不存在 不算报错，有可能是已经删除
			if (!e.getMessage().contains("check that column/key exists")) {
				throw e;
			}
		}
	}

	/**
	 * 描述: 根据sql 新增索引
	 * @param: [sql]
	 * @author: 周庚新
	 * @date: 2021/1/5
	 * @return: void
	 */
	private void addIndex(String sql) {
		try {
			upgradeMapper.updateTableBySql(sql);
		} catch (Exception e) {
			//已存在 不算报错，有可能是已经删除
			if (!e.getMessage().contains("Duplicate key name")) {
				throw e;
			}
		}
	}
}
