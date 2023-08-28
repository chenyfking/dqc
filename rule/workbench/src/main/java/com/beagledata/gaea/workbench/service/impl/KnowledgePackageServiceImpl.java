package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.ruleengine.runtime.ExecutionResult;
import com.beagledata.gaea.ruleengine.runtime.RuleContainer;
import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.beagledata.gaea.ruleengine.runtime.RuleEngineFactory;
import com.beagledata.gaea.ruleengine.util.PackageUtils;
import com.beagledata.gaea.ruleengine.util.PropertyUtils;
import com.beagledata.gaea.ruleengine.util.SafelyFiles;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.common.Constants;
import com.beagledata.gaea.workbench.common.ResourceResolver;
import com.beagledata.gaea.workbench.entity.*;
import com.beagledata.gaea.workbench.mapper.*;
import com.beagledata.gaea.workbench.rule.builder.KnowledgePackageRuleBuilder;
import com.beagledata.gaea.workbench.rule.define.Drl;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.define.Field;
import com.beagledata.gaea.workbench.rule.define.Flow;
import com.beagledata.gaea.workbench.rule.parser.*;
import com.beagledata.gaea.workbench.rule.util.BomUtils;
import com.beagledata.gaea.workbench.rule.util.FactUtils;
import com.beagledata.gaea.workbench.service.KnowledgePackageService;
import com.beagledata.gaea.workbench.util.StringHelper;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.EncodeUtils;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.kie.api.builder.Message;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by mahongfei on 2018/10/8.
 */
@Service
public class KnowledgePackageServiceImpl implements KnowledgePackageService {
    private static Logger logger = LogManager.getLogger(KnowledgePackageServiceImpl.class);

    @Autowired
    private KnowledgePackageMapper knowledgePackageMapper;
    @Autowired
    private AssetsMapper assetsMapper;
    @Autowired
    private MicroMapper microMapper;
    @Autowired
    private ReferMapper referMapper;
    @Autowired
    private ResourceResolver resourceResolver;
    @Autowired
    private KnowledgePackageBaselineMapper knowledgePackageBaselineMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private FunctionDefinitionMapper functionDefinitionMapper;
    @Autowired
    private AiModelMapper aiModelMapper;
    @Autowired
    private ThirdApiDefinitionMapper thirdApiDefinitionMapper;

    private TestRuleContainer testRuleContainer = new TestRuleContainer();
    private Cache<String, KnowledgePackageRuleBuilder> ruleBuilderCache = CacheBuilder.newBuilder()
            .maximumSize(100).expireAfterAccess(8, TimeUnit.HOURS).build();

    /**
     *@Author:mahongfei
     *@description:添加知识包
     */
    @Override
    @Transactional
    public void addKnowledgePackage(KnowledgePackage knowledgePackage) {
        if (StringUtils.isBlank(knowledgePackage.getName()) || knowledgePackage.getName().length() > 20) {
            throw new IllegalArgumentException("知识包名称不能为空并且长度不能超过20个字符");
        }
        if (knowledgePackage.getDescription() != null && knowledgePackage.getDescription().length() > 100) {
            throw new IllegalArgumentException("知识包描述不能超过100个字符");
        }
        if (StringUtils.isBlank(knowledgePackage.getProjectUuid())) {
            throw new IllegalArgumentException("项目uuid不能为空");
        }

        try {
            knowledgePackage.setCreator(UserHolder.currentUser());
            knowledgePackage.setUuid(IdUtils.UUID());
            knowledgePackageMapper.insert(knowledgePackage);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("知识包名称不能重复");
        } catch (Exception e) {
            logger.error("添加知识包失败: {}", knowledgePackage, e);
            throw new IllegalStateException("添加失败");
        }
    }

    /**
     *@Author:mahongfei
     *@description:编辑知识包
     */
    @Override
    public void editKnowledgePackage(KnowledgePackage knowledgePackage) {
        if (StringUtils.isBlank(knowledgePackage.getUuid())) {
            throw new IllegalArgumentException("uuid不能为空");
        }
        if (StringUtils.isBlank(knowledgePackage.getName()) || knowledgePackage.getName().length() > 20) {
            throw new IllegalArgumentException("知识包名称不能为空并且长度不能超过20个字符");
        }
        if (knowledgePackage.getDescription() != null && knowledgePackage.getDescription().length() > 100) {
            throw new IllegalArgumentException("知识包描述长度不能超过100个字符");
        }

        try {
            knowledgePackageMapper.update(knowledgePackage);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("知识包名称不能重复");
        } catch (Exception e) {
            logger.error("编辑知识包失败: {}", knowledgePackage, e);
            throw new IllegalArgumentException("编辑失败");
        }
    }

    /**
     *@Author:mahongfei
     *@description:知识包列表
     */
    @Override
    public List<KnowledgePackage> listAll(String projectUuid) {
        if (StringUtils.isBlank(projectUuid)) {
            throw new IllegalArgumentException("projectUuid不能为空");
        }
        try {
            return knowledgePackageMapper.selectAll(projectUuid, UserHolder.hasAdminPermission());
        } catch (Exception e) {
            logger.error("查询知识包失败: {}", projectUuid, e);
            throw new IllegalArgumentException("查询知识包失败");
        }
    }

    @Override
    public List<KnowledgePackage> getPkgList(String projectUuid) {
        if (StringUtils.isBlank(projectUuid)) {
            throw new IllegalArgumentException("projectUuid不能为空");
        }
        try {
            return knowledgePackageMapper.selectForPrj(projectUuid);
        } catch (Exception e) {
            logger.error("查询知识包失败: {}", projectUuid, e);
            throw new IllegalArgumentException("查询知识包失败");
        }
    }

    @Override
    @Transactional
    public Result deleteknowledgePackage(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("uuid不能为空");
        }

        try {
            int count = knowledgePackageMapper.countPkgEffectiveBaseline(uuid);
            if (count > 0) {
                return Result.newError().withMsg("知识包有对应已生效的服务，无法删除");
            }

            knowledgePackageMapper.delete(new KnowledgePackage(uuid));

            Refer refer = new Refer();
            refer.setReferType(AssetsType.KNOWLEDGE_PACKAGE);
            refer.setReferUuid(uuid);
            referMapper.delete(refer);

            microMapper.deleteByPackageUuid(uuid);

            return Result.SUCCESS;
        } catch (Exception e) {
            logger.error("删除知识包失败: {}", uuid, e);
            throw new IllegalStateException("删除失败");
        }
    }

    /**
     * 添加知识资源
     */
    @Override
    public void addKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        if (StringUtils.isBlank(knowledgePackage.getAssetsUuid())) {
            throw new IllegalStateException("资源uuid为空");
        }
        if (StringUtils.isBlank(knowledgePackage.getPackageUuid())) {
            throw new IllegalStateException("知识包uuid为空");
        }

        try {
            knowledgePackageMapper.addKnowledgePackageAssets(knowledgePackage);
        } catch (IllegalStateException e) {
            logger.error(knowledgePackage + e.getLocalizedMessage(), e);
            throw new IllegalStateException("新增知识包失败失败");
        }catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("知识包资源不能重复");
        }
    }

    /**
     *@auto: yangyongqiang
     *@Description: 查询列表
     *@Date: 2018-10-08 16:19
     **/
    @Override
    public List<Assets> selectKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        try {
            return knowledgePackageMapper.selectKnowledgePackageAssets(knowledgePackage);
        } catch (Exception e) {
            logger.error(knowledgePackage + e.getLocalizedMessage(), e);
        }
        throw new IllegalStateException("查询资源文件详情失败");
    }

    /**
     *@auto: yangyongqiang
     *@Description: 删除知识包资源
     *@Date: 2018-10-08 16:19
     **/
    @Override
    public void deleteKnowledgePackageAssets(KnowledgePackage knowledgePackage) {
        if (StringUtils.isBlank(knowledgePackage.getAssetsUuid())) {
            throw new IllegalStateException("资源uuid为空");
        }
        if (StringUtils.isBlank(knowledgePackage.getPackageUuid())) {
            throw new IllegalStateException("知识包uuid为空");
        }

        try {
            knowledgePackageMapper.deleteKnowledgePackageAssets(knowledgePackage);
        } catch (Exception e) {
            logger.error("删除列表异常："+e.getLocalizedMessage(),e);
            throw e;
        }
    }

    @Override
    public List<Fact> getTestData(String uuid, Integer baselineVersion) {
        if (StringUtils.isBlank(uuid)) {
            logger.warn("知识包不存在: {}", uuid);
            throw new IllegalArgumentException("知识包不存在");
        }

        try {
            KnowledgePackage pkg = selectByUuidWithAssets(uuid, baselineVersion);
            if (pkg == null) {
                logger.warn("知识包不存在: {}", uuid);
                throw new IllegalArgumentException("知识包不存在");
            }

            KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAssets();
            Set<Fact> facts = ruleBuilder.getFacts();
            for (Fact fact : facts) {
                fact.clearOriginal();
            }
            return new ArrayList<>(facts);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取测试数据失败. uuid: {}, baseline: {}", uuid, baselineVersion, e);
        }
        return Collections.emptyList();
    }

    /**
     * 调用知识包执行测试
     * @param testFacts 测试数据
     * @param result    执行时间，触发规则记录
     * @param ruleBuilder
     * @throws IllegalAccessException
     * @throws IntrospectionException
     * @throws InvocationTargetException
     */
    private void executeTest(List<Fact> testFacts, Map<String, Object> result, KnowledgePackageRuleBuilder ruleBuilder) throws Exception {
        RuleEngine ruleEngine = RuleEngineFactory.getRuleEngine(ruleBuilder.getId());
        testFacts.forEach(fact -> fact.getFields().forEach(field -> {
            if (field.getOriginal() != null) {
                ruleEngine.put(fact.getIdentity(), field.getName(), field.getOriginal());
            }
        }));
        ExecutionResult executionResult  = ruleEngine.execute();
        if (executionResult == null) {
            throw new IllegalStateException("执行失败");
        }

        result.put("fireTime", executionResult.getExecutionTime());
        result.put("fireNum", executionResult.getFireNum());
        result.put("fireRules", executionResult.getFireRuleMetas());

        Map<String, List<Integer>> fireNodeMap = executionResult.getFireNodeMap();
        if (!fireNodeMap.isEmpty()) {
            JSONArray fireNodes = new JSONArray(fireNodeMap.size());
            for (Map.Entry<String, List<Integer>> entry : fireNodeMap.entrySet()) {
                String[] keys = entry.getKey().split("_");
                String uuid = keys[1];
                String versionNo = keys[2];

                Assets flow = assetsMapper.selectByUuid(uuid);
                if (flow == null) {
                    continue;
                }

                String flowName = flow.getName();
                if (!"0".equals(versionNo)) {
                    flowName = flowName + "_V" + versionNo;
                }
                JSONObject flowNode = new JSONObject();
                flowNode.put("flowUuid", uuid);
                flowNode.put("flowName", flowName);
                flowNode.put("processId", entry.getKey());
                flowNode.put("versionNo", NumberUtils.toInt(versionNo));
                if (null != entry.getValue()) {
                    flowNode.put("nodes", entry.getValue());
                }
                fireNodes.add(flowNode);
            }
            result.put("fireNodes", fireNodes);
        }

        for (Fact fact : testFacts) {
            Object obj = ruleEngine.getObject(fact.getIdentity());
            if (obj == null) {
                continue;
            }

            Map<String, Object> properties = PropertyUtils.getProperties(obj);
            for (Field field : fact.getFields()) {
                Object value = properties.get(field.getName());
                if (value == null) {
                    field.setValue(null);
                } else if (Date.class.equals(value.getClass())) {
                    field.setValue(DateFormatUtils.format((Date)value, Constants.DEFAULT_DATE_FORMAT));
                } else {
                    field.setValue(String.valueOf(value));
                }
            }
        }
    }

    @Override
    public Map test(String uuid, String json, Integer baselineVersion) {
        if (StringUtils.isBlank(uuid) || StringUtils.isBlank(json)) {
            logger.warn("仿真测试参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }


        Map<String, Object> map = new HashMap<>();
        try {
            List<Fact> testFacts = JSON.parseArray(json).toJavaList(Fact.class);
            map.put("facts", testFacts);
            Map<String, Object> result = new HashMap<>();
            map.put("result", result);

            KnowledgePackageRuleBuilder ruleBuilder = getRuleBuilder(uuid, baselineVersion);
            executeTest(testFacts, result, ruleBuilder);
            testRuleContainer.unactivate();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuleException e) {
            if (e.getResults() != null) {
                StringBuilder builder = new StringBuilder(e.getMessage());
                for (Message msg : e.getResults().getMessages(Message.Level.ERROR)) {
                    String msgText = msg.getText();
                    if (msgText.startsWith("Rule Compilation error")) {
                        builder.append(": ").append(msgText);
                    }
                }
                throw new IllegalStateException(builder.toString());
            }
            throw  e;
        } catch (Exception e) {
            logger.error("仿真测试失败. uuid: {}, baseline: {}, json: {}", uuid, baselineVersion, json, e);
            throw new IllegalStateException(StringHelper.translateMessage(e));
        }
        return map;
    }

    private KnowledgePackage selectByUuidWithAssets(String uuid, Integer baseline) {
        if (baseline != null) {
            KnowledgePackage pkg = knowledgePackageMapper.selectByUuid(uuid);
            if (pkg != null) {
                pkg.setBaselineVersion(baseline);
                setAssets(pkg, baseline);
            }
            return pkg;
        }

        KnowledgePackage pkg = knowledgePackageMapper.selectForRecentBlByUuid(uuid);
        if (pkg != null && pkg.getBaselineVersion() != null) {
            setAssets(pkg, pkg.getBaselineVersion());
        }
        return pkg;


    }

    private void setAssets(KnowledgePackage pkg, Integer baseline) {
        if (baseline == null || baseline == 0) {
            pkg.setAssetsList(knowledgePackageMapper.selectAssetsWithContent(pkg.getUuid()));
        } else {
            pkg.setAssetsList(knowledgePackageMapper.selectAssetsVersionWithContent(pkg.getUuid(), baseline));
        }
    }

    @Override
    public void download(String uuid, Integer baselineVersion,  HttpServletResponse response) {
        OutputStream os = null;
        try {
            KnowledgePackage pkg = selectByUuidWithAssets(uuid, baselineVersion);
            if (pkg == null) {
                throw new IllegalArgumentException("下载失败，知识包不存在");
            }

            KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAll();
            byte[] bytes = ruleBuilder.getBytes();
            bytes = PackageUtils.encrypt(bytes);

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=pkg" + pkg.getId() + "_V" + baselineVersion + ".zip");
            os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (Exception e) {
            logger.error("下载基线失败. uuid: {}, baseline: {}", uuid, baselineVersion, e);
            throw new IllegalArgumentException("下载失败");
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    @Override
    public void download(String uuid, HttpServletResponse response) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("知识包uuid不能为空");
        }
        OutputStream os = null;
        try {
            KnowledgePackage pkg = selectByUuidWithAssets(uuid, null);
            if (pkg == null) {
                throw new IllegalArgumentException("下载失败，知识包不存在");
            }

            KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAll();
            byte[] bytes = ruleBuilder.getBytes();
            bytes = PackageUtils.encrypt(bytes);

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=pkg" + pkg.getId() + ".zip");
            os = response.getOutputStream();
            os.write(bytes);
            os.flush();
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (Exception e) {
            logger.error("下载失败. uuid: {}", uuid, e);
            throw new IllegalArgumentException("下载失败");
        } finally {
            IOUtils.closeQuietly(os);
        }

    }

    private void writeProject(ZipOutputStream zip, KnowledgePackage pkg) throws IOException {
        Project project = projectMapper.selectByUuid(pkg.getProjectUuid());
        if (project == null) {
            throw new IllegalArgumentException("下载失败，项目不存在");
        }

        ZipEntry entry = new ZipEntry("src/project.json");
        zip.putNextEntry(entry);
        JSONObject json = new JSONObject();
        json.put("id", project.getId());
        json.put("uuid", project.getUuid());
        json.put("name", project.getName());
        zip.write(json.toJSONString().getBytes());
    }

    private void writePkg(ZipOutputStream zip, KnowledgePackage pkg) throws IOException {
        ZipEntry entry = new ZipEntry("src/pkg.json");
        zip.putNextEntry(entry);
        JSONObject json = new JSONObject();
        json.put("id", pkg.getId());
        json.put("uuid", pkg.getUuid());
        json.put("name", pkg.getName());
        json.put("projectUuid", pkg.getProjectUuid());
        json.put("microUuid", pkg.getMicroUuid());
        zip.write(json.toJSONString().getBytes());
    }

    private void writeBaseline(ZipOutputStream zip, KnowledgePackage pkg) throws IOException {
        KnowledgePackageBaseline baseline = knowledgePackageBaselineMapper.selectByPackageUuidAndBaselineVersion(
                pkg.getUuid(), pkg.getBaselineVersion()
        );
        if (baseline == null) {
            throw new IllegalArgumentException("下载失败，基线不存在");
        }

        ZipEntry entry = new ZipEntry("src/baseline.json");
        zip.putNextEntry(entry);
        JSONObject json = new JSONObject();
        json.put("id", baseline.getId());
        json.put("uuid", baseline.getUuid());
        json.put("packageUuid", pkg.getUuid());
        json.put("versionNo", pkg.getBaselineVersion());
        zip.write(json.toJSONString().getBytes());
    }

    private void writeMicro(ZipOutputStream zip, KnowledgePackage pkg) throws IOException {
        Micro micro = microMapper.selectMicroByPackageUuid(pkg.getUuid());
        if (micro == null) {
            throw new IllegalArgumentException("下载失败，服务不存在");
        }

        ZipEntry entry = new ZipEntry("src/micro.json");
        zip.putNextEntry(entry);
        JSONObject json = new JSONObject();
        json.put("id", micro.getId());
        json.put("uuid", micro.getUuid());
        json.put("name", micro.getName());
        json.put("packageUuid", pkg.getUuid());
        zip.write(json.toJSONString().getBytes());
    }

    private KnowledgePackageRuleBuilder writeRuleBuilder(ZipOutputStream zip, KnowledgePackage pkg) throws Exception {
        KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
        ruleBuilder.setId(pkg.getUuid() + "_" + pkg.getBaselineVersion());
        ruleBuilder.buildAll();
        byte[] bytes = ruleBuilder.getBytes();
        String fileName = String.format("%s_%s.zip", pkg.getUuid(), pkg.getBaselineVersion());
        ZipEntry entry = new ZipEntry(fileName);
        zip.putNextEntry(entry);
        zip.write(bytes);
        return ruleBuilder;
    }

    private void writeAssets(ZipOutputStream zip, KnowledgePackage pkg, KnowledgePackageRuleBuilder ruleBuilder) throws IOException {
        List<String> assetsJsons = new ArrayList<>();
        List<String> assetsVersionJsons = new ArrayList<>();
        List<Assets> assetsList = assetsMapper.selectBomByProjectUuid(pkg.getProjectUuid());
        for (Assets assets : assetsList) {
            if (StringUtils.isBlank(assets.getContent())) {
                continue;
            }

            JSONObject json = new JSONObject();
            json.put("id", assets.getId());
            json.put("uuid", assets.getUuid());
            json.put("name", assets.getName());
            json.put("content", assets.getContent());
            json.put("type", assets.getType());
            json.put("projectUuid", pkg.getProjectUuid());
            assetsJsons.add(json.toJSONString());
        }
        for (Assets assets : ruleBuilder.getAssetsSet()) {
            JSONObject json = new JSONObject();
            json.put("id", assets.getId());
            json.put("uuid", assets.getUuid());
            json.put("name", assets.getName());
            json.put("type", assets.getType());
            json.put("projectUuid", pkg.getProjectUuid());
            assetsJsons.add(json.toJSONString());

            json = new JSONObject();
            json.put("id", assets.getVersionId());
            json.put("assetsUuid", assets.getUuid());
            json.put("content", assets.getContent());
            json.put("versionNo", assets.getVersionNo());
            assetsVersionJsons.add(json.toJSONString());
        }


        ZipEntry entry = new ZipEntry("src/assets.json");
        zip.putNextEntry(entry);
        zip.write(assetsJsons.stream().collect(Collectors.joining("\n")).getBytes());
        entry = new ZipEntry("src/assets_version.json");
        zip.putNextEntry(entry);
        zip.write(assetsVersionJsons.stream().collect(Collectors.joining("\n")).getBytes());
    }

    private void writeFunc(ZipOutputStream zip, KnowledgePackageRuleBuilder ruleBuilder) throws IOException {
        List<String> funcJsons = new ArrayList<>();
        for (FunctionDefinition fd : ruleBuilder.getFuncSet()) {
            JSONObject json = new JSONObject();
            json.put("id", fd.getId());
            json.put("uuid", fd.getUuid());
            json.put("name", fd.getName());
            json.put("className", fd.getClassName());
            json.put("methodsJson", fd.getMethodsJson());
            funcJsons.add(json.toJSONString());
        }

        ZipEntry entry = new ZipEntry("src/func.json");
        zip.putNextEntry(entry);
        zip.write(funcJsons.stream().collect(Collectors.joining("\n")).getBytes());
    }

    private void writeAiModel(ZipOutputStream zip, KnowledgePackageRuleBuilder ruleBuilder) throws IOException {
        List<String> aiModelJsons = new ArrayList<>();
        for (AiModel aiModel : ruleBuilder.getAiModelSet()) {
            JSONObject json = new JSONObject();
            json.put("id", aiModel.getId());
            json.put("uuid", aiModel.getUuid());
            json.put("modelName", aiModel.getModelName());
            json.put("jarName", aiModel.getJarName());
            aiModelJsons.add(json.toJSONString());
        }

        ZipEntry entry = new ZipEntry("src/aimodel.json");
        zip.putNextEntry(entry);
        zip.write(aiModelJsons.stream().collect(Collectors.joining("\n")).getBytes());
    }

    private void writeThirdApi(ZipOutputStream zip, KnowledgePackageRuleBuilder ruleBuilder) throws IOException {
        List<String> thirdApiJsons = new ArrayList<>();
        for (ThirdApiDefinition apiDef : ruleBuilder.getThirdApiDefinitions()) {
            JSONObject json = new JSONObject();
            json.put("id", apiDef.getId());
            json.put("uuid", apiDef.getUuid());
            json.put("name", apiDef.getName());
            json.put("url", apiDef.getUrl());
            json.put("method", apiDef.getMethod());
            json.put("reqContentType", apiDef.getRequestContentType());
            json.put("resJsonPath", apiDef.getResponseJsonPath());
            json.put("headersJson", apiDef.getHeadersJson());
            thirdApiJsons.add(json.toJSONString());
        }

        ZipEntry entry = new ZipEntry("src/thirdapi.json");
        zip.putNextEntry(entry);
        zip.write(thirdApiJsons.stream().collect(Collectors.joining("\n")).getBytes());
    }

    @Override
    public void downBatchModel(String uuid, Integer baselineVersion, HttpServletResponse response) {
        XSSFWorkbook wb = null;
        try {
            KnowledgePackage pkg = selectByUuidWithAssets(uuid, baselineVersion);
            if (pkg == null) {
                logger.warn("下载批量测试模板失败，知识包不存在：{}", uuid);
                throw new IllegalArgumentException("下载失败，知识包不存在");
            }

            KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAssets();
            Set<Fact> facts = ruleBuilder.getFacts();
            if (facts.isEmpty()) {
                logger.warn("下载批量测试模板, 解析fact 为空, 知识包uuid=[{}]", uuid);
                throw new IllegalArgumentException("下载失败，规则没有输入字段");
            }

            wb = new XSSFWorkbook();
            XSSFCellStyle style = wb.createCellStyle();
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setFillForegroundColor(new XSSFColor(new java.awt.Color(147, 208, 15)));
            for (Fact fact : facts) {
                XSSFSheet sheet = wb.createSheet(fact.getName());
                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(wb.createDataFormat().getFormat("@"));
                XSSFRow row = sheet.createRow(0);
                int i = 0;
                for (Field field : fact.getFields()) {
                    if (field.isDeriveType()) {
                        continue;
                    }

                    sheet.setColumnWidth(i,4000);
                    sheet.setDefaultColumnStyle(i, cellStyle);
                    XSSFCell cell = row.createCell(i);
                    cell.setCellValue(field.getLabel());
                    cell.setCellStyle(style);
                    i++;
                }
            }

            response.setContentType("application/x-xls");
            response.setHeader("Content-Disposition","attachment; filename=batchmodel.xlsx");
            OutputStream outputStream = response.getOutputStream();
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("下载批量测试模板失败. uuid: {}, baseline: {}", uuid, baselineVersion, e);
            throw new IllegalStateException("下载批量测试模板失败");
        } finally {
            IOUtils.closeQuietly(wb);
        }
    }

    @Override
    public Result importBatchData(String uuid, MultipartFile file) {
        if (StringUtils.isBlank(uuid) || file == null) {
            throw new IllegalArgumentException("导入批量测试数据失败,参数不能为空");
        }
        try {
            String fileName = file.getOriginalFilename();
            InputStream stream = file.getInputStream();
            Workbook wb;
            if (fileName.endsWith(".xls")) {
                wb = new HSSFWorkbook(stream);
            } else if (fileName.endsWith(".xlsx")) {
                wb = new XSSFWorkbook(stream);
            } else {
                return Result.newError().withMsg("文件必需以 xls 或 xlsx结尾");
            }
            List<Map<String,Object>> dataList = new ArrayList<>();
            int totalRow = 0;   //得出所有sheet页最多的行数
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }
                totalRow = sheet.getLastRowNum() > totalRow ? sheet.getLastRowNum() : totalRow;//sheet页的总行数
            }
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sheet = wb.getSheetAt(i);
                if (sheet == null) {
                    continue;
                }
                String name = sheet.getSheetName();
                Map<String,Object> sheetMaps = new HashMap<>(2);
                sheetMaps.put("name", name);
                List<Map<String,String>> rowList = new ArrayList<>(totalRow);
                Row headRow =  sheet.getRow(0);//头部字段行
                if (null == headRow) {
                    continue;
                }
                int totalColumn = headRow.getLastCellNum();//该sheet页的总列数
                //存储表头字段和列的对应关系
                Map<Integer,String> colHeadMap = new HashMap<>(totalColumn);
                //存储表头
                Map<String,String> headerMap = new HashMap<>(totalColumn);
                for (int col=0; col<totalColumn; col++) {
                    Cell headCell = headRow.getCell(col);
                    if(headCell==null){
                        continue;
                    }else {
                        String headValue = headCell.getStringCellValue();
                        colHeadMap.put(col, headValue);
                        headerMap.put(headValue, null);
                    }
                }
                for (int rowIndex=1; rowIndex<=totalRow; rowIndex++) {// 列表的每一行是要进入规则引擎的参数
                    Map<String,String> rowMap = new HashMap<>(totalRow);
                    Row row=sheet.getRow(rowIndex);//获得行数据
                    for(int colIndex=0; colIndex<totalColumn; colIndex++){
                        String headerName = colHeadMap.get(colIndex);
                        if(row==null){  //若该sheet页在该行没有值，则put空值
                            rowMap.put(headerName, "");
                        } else {
                            Cell cell = row.getCell(colIndex);
                            if(cell==null){
                                rowMap.put(headerName, "");
                            }else{
                                cell.setCellType(1);
                                String cellValue = cell.getStringCellValue();
                                if (StringUtils.isNotBlank(cellValue)) {
                                    cellValue = cellValue.trim();
                                    if (cellValue.equals("是") || cellValue.equals("否")) {
                                        cellValue = cellValue.equals("是") ? "true" : "false";
                                    }
                                }
                                rowMap.put(headerName, cellValue);
                            }
                        }
                    }
                    rowList.add(rowMap);
                }
                if (rowList.size() > 0) {
                    sheetMaps.put("data", rowList);
                } else {    //表中没有数据时，添加表头
                    sheetMaps.put("data", headerMap);
                }
                dataList.add(sheetMaps);
            }
            return Result.newSuccess().withData(dataList);
        } catch (IOException e){
            e.printStackTrace();
            throw new IllegalArgumentException("");
        }catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    @Override
    public void batchTest(String uuid, Integer baselineVersion, MultipartFile file, HttpServletResponse response) {
        Workbook wb = null;
        try {
            wb = getWorkbook(file);
            int maxRowNum = getMaxRowNum(wb);

            KnowledgePackageRuleBuilder ruleBuilder = getRuleBuilder(uuid, baselineVersion);

            Set<Fact> facts = ruleBuilder.getFacts();
            if (facts.isEmpty()) {
                logger.warn("批量测试失败, 规则没有输入字段. uuid: {}, baseline: {}", uuid, baselineVersion);
                throw new IllegalArgumentException("批量测试失败[规则没有输入字段]");
            }

            // 获取对象字段对应的坐标
            Map<String, Integer> headerIndexMap = new HashMap<>();
            for (Fact fact : facts) {
                String factName = fact.getName();
                Sheet sheet = wb.getSheet(factName);
                if (sheet == null) {
                    throw new IllegalArgumentException("批量测试失败[缺少\"" + factName + "\"sheet]");
                }
                Row headerRow = sheet.getRow(0);
                if (headerRow == null) {
                    throw new IllegalArgumentException("批量测试模板错误，请重新下载");
                }

                String factId = fact.getIdentity();
                for (Field field : fact.getFields()) {
                    if (Field.Type.Derive.equals(field.getType())) {
                        continue;
                    }

                    String fieldName = field.getName();
                    String fieldLabel = field.getLabel();
                    findFieldCord(factId, factName, fieldName, fieldLabel, headerRow, headerIndexMap);
                }
            }

            // 一行一行的执行规则，并且把执行结果写回文件，每个单元格对应每个Field
            for (int i = 1; i <= maxRowNum; i++) {
                RuleEngine ruleEngine = RuleEngineFactory.getRuleEngine(ruleBuilder.getId());
                for (Fact fact : facts) {
                    String factName = fact.getName();
                    Sheet sheet = wb.getSheet(factName);
                    if (sheet.getLastRowNum() < i) {
                        continue;
                    }
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        continue;
                    }

                    Row headerRow = sheet.getRow(0);
                    String factId = fact.getIdentity();
                    for (Field field : fact.getFields()) {
                        if (Field.Type.Derive.equals(field.getType())) {
                            continue;
                        }

                        String fieldName = field.getName();
                        String fieldLabel = field.getLabel();
                        int index = findFieldCord(factId, factName, fieldName, fieldLabel, headerRow, headerIndexMap);
                        Cell valueCell = sheet.getRow(i).getCell(index);
                        setFieldValue(i + 1, valueCell, factId, factName, fieldName, fieldLabel, field.getType(), ruleEngine);
                    }
                }

                ruleEngine.execute();

                for (Map.Entry<String, Integer> entry : headerIndexMap.entrySet()) {
                    String headerCord = entry.getKey();
                    int firstIndex = headerCord.indexOf('-');
                    int lastIndex = headerCord.lastIndexOf('-');
                    String factId = headerCord.substring(0, firstIndex);
                    String name = headerCord.substring(lastIndex + 1);
                    Object value = ruleEngine.getObject(factId, name);
                    if (value == null) {
                        continue;
                    }

                    String sheetName = headerCord.substring(firstIndex + 1, lastIndex);
                    int cellNum = entry.getValue();
                    Sheet sheet = wb.getSheet(sheetName);
                    Row row = sheet.getRow(i);
                    if (row == null) {
                        row = sheet.createRow(i);
                    }
                    Cell cell = row.getCell(cellNum);
                    if (cell == null) {
                        cell = row.createCell(cellNum);
                    }

                    if (Date.class.equals(value.getClass())) {
                        cell.setCellValue(DateFormatUtils.format((Date)value, Constants.DEFAULT_DATE_FORMAT));
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            }

            testRuleContainer.unactivate();

            String fileName = URLEncoder.encode(file.getOriginalFilename(), "UTF-8");
            response.setContentType("application/x-xls");
            response.setHeader("Content-Disposition","attachment; filename=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error(String.format("批量测试失败, uuid: %s, baseline: %s\n%s", uuid, baselineVersion, e.getLocalizedMessage()), e);
            throw new IllegalStateException("批量测试失败");
        }
    }

    private void setFieldValue(
            int rowNumber,
            Cell valueCell,
            String factId,
            String factName,
            String fieldName,
            String fieldLabel,
            Field.Type fieldType,
            RuleEngine ruleEngine) {
        if (valueCell == null) {
            return;
        }

        valueCell.setCellType(Cell.CELL_TYPE_STRING);
        String value = valueCell.getStringCellValue();
        if (StringUtils.isBlank(value)) {
            return;
        }
        value = value.trim();

        Object setObj = value;
        try {
            if (Field.Type.Integer.equals(fieldType)) {
                setObj = Integer.parseInt(value);
            } else if (Field.Type.Long.equals(fieldType)) {
                setObj = Long.parseLong(value);
            } else if (Field.Type.Double.equals(fieldType)) {
                setObj = Double.parseDouble(value);
            } else if (Field.Type.BigDecimal.equals(fieldType)) {
                setObj = new BigDecimal(value);
            } else if (Field.Type.Boolean.equals(fieldType)) {
                if (!"是".equals(value) && !"否".equals(value)) {
                    throw new IllegalArgumentException();
                }
                setObj = "是".equals(value) ? true : false;
            } else if (Field.Type.Date.equals(fieldType)) {
                if (value.length() > 10) {
                    setObj = DateUtils.parseDateStrictly(value, "yyyy-MM-dd HH:mm:ss");
                } else {
                    setObj = DateUtils.parseDateStrictly(value, "yyyy-MM-dd");
                }
            } else if (Field.Type.List.equals(fieldType)) {
                if (!value.startsWith("[") || !JSONValidator.from(value).validate()) {
                    throw new IllegalArgumentException();
                }
            } else if (Field.Type.Map.equals(fieldType) || Field.Type.Object.equals(fieldType)) {
                if (!JSONValidator.from(value).validate()) {
                    throw new IllegalArgumentException();
                }
            }
            ruleEngine.put(factId, fieldName, setObj);
        } catch (Exception e) {
            String msg = String.format("批量测试失败[\"%s\"sheet第%s行的\"%s\"字段输入不正确]", factName, rowNumber, fieldLabel);
            throw new IllegalArgumentException(msg);
        }
    }

    private Workbook getWorkbook(MultipartFile file) {
        try {
            if (file.getOriginalFilename().endsWith(".xls") || file.getOriginalFilename().endsWith(".xlsx")) {
                return WorkbookFactory.create(file.getInputStream());
            }
        } catch (Exception e) {
            logger.error("读取批量测试文件失败: {}", file, e);
        }
        throw new IllegalArgumentException("批量测试失败[不支持的文件类型]");
    }

    private int findFieldCord(
            String factId,
            String factName,
            String fieldName,
            String fieldLabel,
            Row headerRow,
            Map<String, Integer> headerIndexMap) {
        String headerCord = factId + "-" +factName + "-" + fieldName;
        Integer index = headerIndexMap.get(headerCord);
        if (index == null) {
            for (int i = 0; i < headerRow.getPhysicalNumberOfCells(); i++) {
                Cell headerCell = headerRow.getCell(i);
                if (headerCell == null) {
                    continue;
                }

                String headerLabel;
                try {
                    headerLabel = headerCell.getStringCellValue();
                } catch (Exception e) {
                    throw new IllegalArgumentException("批量测试失败[\"" + factName + "\"sheet的标题行错误]");
                }
                if (fieldLabel.equals(headerLabel)) {
                    headerIndexMap.put(headerCord, i);
                    index = i;
                    break;
                }
            }
            if (index == null) {
                throw new IllegalArgumentException("批量测试失败[\"" + factName + "\"sheet缺少\"" + fieldLabel + "\"列]");
            }
        }
        return index;
    }

    private int getMaxRowNum(Workbook wb) {
        int maxRowNum = 0;
        Iterator<Sheet> it = wb.sheetIterator();
        while (it.hasNext()) {
            int lastRowNum = it.next().getLastRowNum();
            if (lastRowNum > maxRowNum) {
                maxRowNum = lastRowNum;
            }
        }

        if (maxRowNum < 1) {
            throw new IllegalArgumentException("批量测试失败[文件没有数据]");
        }

        return maxRowNum;
    }

    private KnowledgePackageRuleBuilder getRuleBuilder(String uuid, Integer baseline) throws Exception {
        KnowledgePackage pkg = selectByUuidWithAssets(uuid, baseline);
        if (pkg == null) {
            logger.error("编译失败，知识包不存在. uuid: {}, baseline: {}", uuid, baseline);
            throw new IllegalArgumentException("知识包不存在");
        }

        String ruleBuilderKey = EncodeUtils.encodeMD5(JSON.toJSONString(pkg));
        KnowledgePackageRuleBuilder ruleBuilder = ruleBuilderCache.getIfPresent(ruleBuilderKey);
        if (ruleBuilder == null) {
            ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAll();
            ruleBuilderCache.put(ruleBuilderKey, ruleBuilder);
        }
        testRuleContainer.activate(ruleBuilder);
        return ruleBuilder;
    }

    /**
     * 解析知识包关联的文件，得到文件使用的Fact对象的id集合
     */
    @Override
    public Set<Integer> getknowledgePackageFactIds(List<Assets> assetsList) {
        Set<Integer> factIds = new HashSet<>();
        if (assetsList == null || assetsList.isEmpty()) {
            return factIds;
        }
        for (Assets assets : assetsList) {
            Parser parser = ParserFactory.getParser(assets);
            if (parser == null) {
                continue;
            }
            if (parser instanceof DrlParser) {
                DrlParser drlParser = (DrlParser) parser;
                Drl drl = drlParser.getDumper();
                drl.dump();
                for (Fact fact : drl.getFacts()) {
                    factIds.add(fact.getId());
                }
            } else {
                FlowParser flowParser = (FlowParser) parser;
                Flow flow = flowParser.getDumper();
                for (Fact fact : flow.getImports()) {
                    factIds.add(fact.getId());
                }
                Set<String> ruleAssetsIds = new HashSet<>();
                if (!ruleAssetsIds.isEmpty()) {
                    List<Assets> ruleAssetsList = assetsMapper.selectByUuids(ruleAssetsIds);
                    for (Assets ruleAssets : ruleAssetsList) {
                        DrlParser drlParser = (DrlParser) ParserFactory.getParser(ruleAssets);
                        for (Fact fact : drlParser.getDumper().getFacts()) {
                            factIds.add(fact.getId());
                        }
                    }
                }
            }
        }
        return factIds;
    }

    @Override
    public Result submitAudit(String packageUuid) {
        if (StringUtils.isBlank(packageUuid)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            KnowledgePackage knowledgePackage = new KnowledgePackage();
            knowledgePackage.setUuid(packageUuid);
            knowledgePackageMapper.update(knowledgePackage);
            return Result.newSuccess();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public KnowledgePackage addTempPkg(String assetsUuid, Integer assetsVersion) {
        try {
            KnowledgePackage pkg = knowledgePackageMapper.selectTempByAssets(assetsUuid,assetsVersion);
            if (pkg == null) {
                pkg = new KnowledgePackage();
                pkg.setUuid(IdUtils.UUID());
                if (assetsVersion != 0) {
                    pkg.setBaselineVersion(1 );
                } else {
                    pkg.setBaselineVersion(0);
                }
                knowledgePackageMapper.insert(pkg);
                Integer id =pkg.getId();
                pkg.setPackageUuid(pkg.getUuid());
                pkg.setAssetsUuid(assetsUuid);
                pkg.setAssetsVersion(assetsVersion);
                knowledgePackageMapper.addKnowledgePackageAssets(pkg);
                pkg.setId(id);
                pkg.setPackageUuid(null);
                pkg.setAssetsUuid(null);
            }
            return pkg;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalStateException("操作失败");
        }
    }

    @Override
    public void exportBatchResult(HttpServletResponse response, String json) {
        if (StringUtils.isBlank(json)) {
            logger.warn("下载批量测试数据 参数json为空");
            throw new IllegalArgumentException("请检查参数");
        }
        try {
            JSONObject facts = JSONObject.parseObject(json);
            if (facts == null || facts.size() < 1) {
                logger.warn("下载批量测试数据 参数没有fact数据, json = [{}]", json);
                throw new IllegalArgumentException("请检查参数");
            }

            Workbook wb = new SXSSFWorkbook();
            for (String key : facts.keySet()) {
                JSONObject data = facts.getJSONObject(key);
                Sheet sheet = wb.createSheet(key);
                //写表头

                JSONObject headData = data.getJSONObject("0");
                JSONArray headFields = headData.getJSONArray("fields");
                if (headFields.size() < 1) {
                    logger.info("data[0]无数据,无法写入表头, key=[{}]", key);
                    continue;
                }
                Map<String, Integer> heads = new HashMap<>(headFields.size());
                Row headRow = sheet.createRow(0);
                for (int i = 0; i < headFields.size(); i++) {
                    JSONObject headField = headFields.getJSONObject(i);
                    String fieldLable = headField.getString("label");
                    heads.put(fieldLable, i);
                    Cell cell = headRow.createCell(i);
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(fieldLable);
                }

                for (String rowIndex : data.keySet()) { //数据从0开始，行号从1开始
                    JSONObject rowData = data.getJSONObject(rowIndex);
                    Row row = sheet.createRow(Integer.parseInt(rowIndex) + 1);
                    JSONArray fields = rowData.getJSONArray("fields");
                    if (fields != null) {
                        for (int i = 0; i < fields.size(); i++) {
                            JSONObject fieldJson = fields.getJSONObject(i);
                            String fieldLabel = fieldJson.getString("label");
                            Integer cellIndex = heads.get(fieldLabel);
                            Cell cell = row.createCell(cellIndex);
                            cell.setCellType(Cell.CELL_TYPE_STRING);
                            cell.setCellValue(fieldJson.getString("value"));
                        }
                    }
                }
            }
            response.setHeader("Content-disposition", "attachment;filename=result.xls");
            response.setContentType("application/octet-stream");
            OutputStream outputStream=response.getOutputStream();
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            logger.info("批量测试结果下载出错");
            logger.error(e.getLocalizedMessage(), e);
            throw new IllegalArgumentException("下载批量测试结果数据出错");
        }
    }

    @Override
    public void downPkgFacts(String pkguuid, HttpServletResponse response) {
        if (StringUtils.isBlank(pkguuid)) {
            logger.info("下载知识包相关数据模型,参数为空");
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            KnowledgePackage pkg = selectByUuidWithAssets(pkguuid, null);
            if (pkg == null) {
                logger.info("下载知识包相关数据模型,查询知识包为空,pkguuid=[{}]", pkguuid);
                throw new IllegalArgumentException("数据异常");
            }
            KnowledgePackageRuleBuilder ruleBuilder = KnowledgePackageRuleBuilder.newBuilder(pkg);
            ruleBuilder.buildAll();
            Set<Fact> facts = ruleBuilder.getFacts();
            if (facts.isEmpty()) {
                logger.info("下载知识包相关数据模型,查询Facts为空,pkguuid=[{}]", pkguuid);
                throw new IllegalArgumentException("数据模型为空");
            }
            //执行下载
            JSONArray array = new JSONArray(facts.size());
            Set<String> identifys = new HashSet<>(array.size());    //用来标记Fact
            for (Fact fact : facts) {
                if (null == fact) {
                    continue;
                }
                addFactJson(array, fact, identifys);
            }

            byte[] bytes = array.toJSONString().getBytes();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-type", "text/html;charset=UTF-8");
            response.setHeader("Content-Disposition",String.format("attachment; filename=package_%s.json", pkg.getId()));
            OutputStream outputStream=response.getOutputStream();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            logger.error("下载知识包相关数据模型出错, pkguuid = [{}]", pkguuid, e);
            throw new IllegalArgumentException("下载失败");
        }
    }

    private void addFactJson(JSONArray array, Fact fact, Set<String> identifys) {
        JSONObject json = new JSONObject();
        array.add(json);
        String enName = fact.getEnName();
        String identify = StringUtils.isBlank(enName) ? String.valueOf(fact.getId()) : enName;
        identifys.add(identify);
        json.put("identify", identify);
        json.put("enName", enName);
        json.put("id", fact.getId());
        json.put("name", fact.getName());
        if (fact.getFields() != null) {
            JSONArray fields = new JSONArray(fact.getFields().size());
            json.put("fields", fields);
            for (Field field : fact.getFields()) {
                if (null == field) {
                    continue;
                }
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("fieldName", field.getName());
                fieldJson.put("fieldLabel", field.getLabel());
                String typeName = field.getType().name();
                fieldJson.put("fieldType", typeName);
                if ("Derive".equals(typeName)) {        //衍生类型不需要设置值
                    continue;
                }
                if ("List".equals(typeName) || "Set".equals(typeName) || "Map".equals(typeName)) {
                    String subFactUuid = field.getSubType();
                    Fact subFact = BomUtils.getFactByUuid(subFactUuid);
                    if (null != subFact) {
                        String subFactName = subFact.getName();
                        String subEnName = subFact.getEnName();
                        String subIdentify;
                        if (StringUtils.isBlank(subEnName)) {
                            fieldJson.put("subType", String.format("%s", subFactName));
                            subIdentify = String.valueOf(subFact.getId());
                        } else {
                            fieldJson.put("subType", String.format("%s_%s", subFactName, subEnName));
                            subIdentify = subEnName;
                        }

                        if (!identifys.contains(subIdentify)) {
                            addFactJson(array, subFact, identifys);
                        }
                    }
                }
                fields.add(fieldJson);
            }
        }
    }

    class TestRuleContainer extends RuleContainer {
        public void activate(KnowledgePackageRuleBuilder builder) {
            putBuilder(builder);
            threadLocal.set(this);
        }

        public void unactivate() {
            threadLocal.remove();
        }
    }

    @Override
    @Transactional
    public Result upload(MultipartFile file) {
        if (!file.getOriginalFilename().endsWith(".baseline")) {
            throw new IllegalArgumentException("基线导入失败，不支持的文件类型");
        }

        ZipInputStream zis = null;
        try {
            byte[] bytes = PackageUtils.decrypt(file.getBytes());
            zis = new ZipInputStream(new ByteArrayInputStream(bytes));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }

                String entryName = zipEntry.getName();
                if ("src/project.json".equals(entryName)) {
                    insertProject(IOUtils.readLines(zis));
                } else if ("src/assets.json".equals(entryName)) {
                    insertAssets(IOUtils.readLines(zis));
                } else if ("src/assets_version.json".equals(entryName)) {
                    insertAssetsVersion(IOUtils.readLines(zis));
                } else if ("src/pkg.json".equals(entryName)) {
                    insertPkg(IOUtils.readLines(zis));
                } else if ("src/baseline.json".equals(entryName)) {
                    insertBaseline(IOUtils.readLines(zis));
                } else if ("src/func.json".equals(entryName)) {
                    insertFunc(IOUtils.readLines(zis));
                } else if ("src/aimodel.json".equals(entryName)) {
                    insertModel(IOUtils.readLines(zis));
                } else if ("src/micro.json".equals(entryName)) {
                    insertMicro(IOUtils.readLines(zis));
                } else if ("src/thirdapi.json".equals(entryName)) {
                    insertThirdApi(IOUtils.readLines(zis));
                } else if (entryName.endsWith(".zip")) {
                    File saveFile = SafelyFiles.newFile(resourceResolver.getPkgBaselinePath(), entryName);
                    FileUtils.writeByteArrayToFile(saveFile, PackageUtils.encrypt(IOUtils.toByteArray(zis)));
                }
            }
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.error("上传基线失败: {}", file, e);
            throw new IllegalStateException("上传失败");
        } finally {
            IOUtils.closeQuietly(zis);
        }
    }

    private void insertAssets(List<String> datas) {
        for (String data : datas) {
            Assets assets = JSON.parseObject(data, Assets.class);
            assets.setCreator(UserHolder.currentUser());
            Assets exist = assetsMapper.selectOnUploadBaseline(assets);
            if (exist == null) {
                assetsMapper.insert(assets);
            } else {
                assetsMapper.updateOnUploadBaseline(assets);
            }
        }
    }

    private void insertAssetsVersion(List<String> datas) {
        for (String data : datas) {
            AssetsVersion assetsVersion = JSON.parseObject(data, AssetsVersion.class);
            assetsVersion.setCreator(UserHolder.currentUser());
            AssetsVersion exist = assetsMapper.selectVersionOnUploadBaseline(assetsVersion);
            if (exist == null) {
                assetsMapper.insertVersion(assetsVersion);
            } else {
                assetsMapper.updateVersionOnUploadBaseline(assetsVersion);
            }
        }
    }

    private void insertModel(List<String> datas) {
        for (String data : datas) {
            AiModel aiModel = JSON.parseObject(data, AiModel.class);
            AiModel exist = aiModelMapper.selectOnUploadBaseline(aiModel);
            if (exist == null) {
                aiModel.setEnable(true);
                aiModelMapper.insert(aiModel);
            } else {
                aiModelMapper.updateOnUploadBaseline(aiModel);
            }
        }
    }

    private void insertFunc(List<String> datas) {
        for (String data : datas) {
            FunctionDefinition func = JSON.parseObject(data, FunctionDefinition.class);
            func.setCreator(UserHolder.currentUser());
            FunctionDefinition exist = functionDefinitionMapper.selectOnUploadBaseline(func);
            if (exist == null) {
                functionDefinitionMapper.insert(func);
            } else {
                functionDefinitionMapper.updateOnUploadBaseline(func);
            }
        }
    }

    private void insertThirdApi(List<String> datas) {
        for (String data : datas) {
            ThirdApiDefinition apiDef = JSON.parseObject(data, ThirdApiDefinition.class);
            apiDef.setCreator(UserHolder.currentUser());
            ThirdApiDefinition exist = thirdApiDefinitionMapper.selectOnUploadBaseline(apiDef);
            if (exist == null) {
                thirdApiDefinitionMapper.insert(apiDef);
            } else {
                thirdApiDefinitionMapper.updateOnUploadBaseline(apiDef);
            }
        }
    }

    private void insertMicro(List<String> datas) {
        for (String data : datas) {
            Micro micro = JSON.parseObject(data, Micro.class);
            Micro exist = microMapper.selectOnUploadBaseline(micro);
            if (exist == null) {
                microMapper.insert(micro);
            } else {
                microMapper.updateOnUploadBaseline(micro);
            }
        }
    }

    private void insertBaseline(List<String> datas) {
        for (String data : datas) {
            KnowledgePackageBaseline baseline = JSON.parseObject(data, KnowledgePackageBaseline.class);
            baseline.setCreator(UserHolder.currentUser());
            KnowledgePackageBaseline exist = knowledgePackageBaselineMapper.selectOnUploadBaseline(baseline);
            if (exist == null) {
                baseline.setState(2);
                knowledgePackageBaselineMapper.insert(baseline);
            } else {
                knowledgePackageBaselineMapper.updateOnUploadBaseline(baseline);
            }
        }
    }

    private void insertPkg(List<String> datas) {
        for (String data : datas) {
            KnowledgePackage pkg = JSON.parseObject(data, KnowledgePackage.class);
            pkg.setCreator(UserHolder.currentUser());
            KnowledgePackage exist = knowledgePackageMapper.selectOnUploadBaseline(pkg);
            if (exist == null) {
                knowledgePackageMapper.insert(pkg);
            } else {
                knowledgePackageMapper.updateOnUploadBaseline(pkg);
            }
        }
    }

    private void insertProject(List<String> datas) {
        for (String data : datas) {
            Project project = JSON.parseObject(data, Project.class);
            project.setCreator(UserHolder.currentUser());
            Project exist = projectMapper.selectOnUploadBaseline(project);
            if (exist == null) {
                projectMapper.insert(project);
            } else {
                projectMapper.updateOnUploadBaseline(project);
            }
        }
    }
}
