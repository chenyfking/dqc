package com.beagledata.gaea.workbench.service.impl;

import com.beagledata.common.Result;
import com.beagledata.gaea.common.LogManager;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.entity.AssetsTemplate;
import com.beagledata.gaea.workbench.entity.RecycleBin;
import com.beagledata.gaea.workbench.entity.Refer;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.mapper.AssetsTemplateMapper;
import com.beagledata.gaea.workbench.mapper.RecycleBinMapper;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.service.AssetsService;
import com.beagledata.gaea.workbench.service.AssetsTemplateService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 模板管理
 * Created by Chenyafeng on 2020/6/18
 */
@Service
public class AssetsTemplateServiceImpl implements AssetsTemplateService {
    private static Logger logger = LogManager.getLogger(AssetsTemplateServiceImpl.class);

    @Autowired
    private AssetsTemplateMapper assetsTemplateMapper;
    @Autowired
    private AssetsMapper assetsMapper;
    @Autowired
    private RecycleBinMapper recycleBinMapper;
    @Autowired
    private AssetsService assetsService;
    @Autowired
    private ReferMapper referMapper;

    /**
     * 规则文件名称正则
     */
    private Pattern templateNamePattern = Pattern.compile(".*(\\d+)");

    @Override
    public Result add(String assetsUuid,  String name) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(assetsUuid)) {
            logger.error("文件转模板参数为空, assetsUuid:[{}], name:[{}]", assetsUuid, name);
            throw new IllegalArgumentException("参数不能为空");
        }
        AssetsTemplate template = new AssetsTemplate();

        try {
            Assets assets = assetsMapper.selectByUuid(assetsUuid);
            if (assets == null) {
                logger.error("资源文件不存在, assetsUuid=[{}]", assetsUuid);
                throw new IllegalStateException("资源文件不存在");
            }

            template.setUuid(IdUtils.UUID());
            template.setName(name);
            template.setContent(assets.getContent());
            template.setType(assets.getType());
            template.setProjectUuid(assets.getProjectUuid());
            template.setCreator(UserHolder.currentUser());
            assetsTemplateMapper.insert(template);

            Assets refer = new Assets();
            refer.setUuid(template.getUuid());
            refer.setContent(assets.getContent());
            refer.setType(AssetsType.TEMPLATE);
            Set<Refer> refers = assetsService.getRefers(refer);
            if (!refers.isEmpty()) {
                referMapper.insertBatch(refers);
            }

            return Result.newSuccess().withData(template);
        } catch (DuplicateKeyException e) {
            template.setName(copyName(name));
            return add(assetsUuid, template.getName());
        } catch (Exception e) {
            logger.error("文件转模板失败. assetsUuid: {}, name: {}", assetsUuid, name, e);
            throw new IllegalStateException("文件转模板失败");
        }
    }

    public String copyName(String name) {
        if (name.length() > 20) {
            name = name.substring(0, 20);
        }

        Matcher matcher = templateNamePattern.matcher(name);
        if (matcher.find()) {
            String indexStr = matcher.group(1);
            int length = indexStr.length();
            String oldName = name.substring(0, name.length() - length);
            int oldLength = oldName.length();
            int index = NumberUtils.toInt(indexStr) + 1;
            name = oldName + index;
            while (name.length() > 20) {
                name = oldName.substring(0, oldLength - 1) + index;
            }
        } else {
            if (name.length() == 20) {
                name = name.substring(0, 19) + "2";
            } else {
                name = name + "2";
            }
        }
        return name;
    }

    @Override
    public AssetsTemplate getByUuid(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            return assetsTemplateMapper.selectByUuid(uuid);
        } catch (Exception e) {
            logger.error("查询模板失败: {}", uuid, e);
            throw new IllegalStateException("查询模板失败");
        }
    }

    @Override
    public Result getTemplates(AssetsTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(template.getProjectUuid())) {
            logger.error("文件转模板参数为空, 模板:[{}]", template);
            throw new IllegalArgumentException("参数不能为空");
        }
        try {
            if (StringUtils.isNotBlank(template.getType())) {  //根据类型查询，返回结果为同一类型， 直接返回list
                return Result.newSuccess().withData(assetsTemplateMapper.selectByParams(template));
            }

            //查询整个项目的规则模板，根据类型区分
            List<AssetsTemplate> templates = assetsTemplateMapper.selectByParams(template);
            if (templates.isEmpty()) {
                return Result.newSuccess();
            }

            List<AssetsTemplate> guidedList = new ArrayList<>();
            List<AssetsTemplate> ruleTableList = new ArrayList<>();
            List<AssetsTemplate> ruleTreeList = new ArrayList<>();
            List<AssetsTemplate> scorecardList = new ArrayList<>();
            for (AssetsTemplate dbTemplate : templates) {
                if (AssetsType.GUIDED_RULE.equals(dbTemplate.getType())) {
                    guidedList.add(dbTemplate);
                }
                if (AssetsType.RULE_TABLE.equals(dbTemplate.getType())) {
                    ruleTableList.add(dbTemplate);
                }
                if (AssetsType.RULE_TREE.equals(dbTemplate.getType())) {
                    ruleTreeList.add(dbTemplate);
                }
                if (AssetsType.SCORECARD.equals(dbTemplate.getType())) {
                    scorecardList.add(dbTemplate);
                }
            }

            List<Map<String, Object>> list = new ArrayList<>(4);
            Map<String, Object> guidedMap = new HashMap<>(2);
            guidedMap.put("type", AssetsType.GUIDED_RULE);
            guidedMap.put("list", guidedList);

            Map<String, Object> ruleTableMap = new HashMap<>(2);
            ruleTableMap.put("type", AssetsType.RULE_TABLE);
            ruleTableMap.put("list", ruleTableList);

            Map<String, Object> ruleTreeMap = new HashMap<>(2);
            ruleTreeMap.put("type", AssetsType.RULE_TREE);
            ruleTreeMap.put("list", ruleTreeList);

            Map<String, Object> scorecardMap = new HashMap<>(2);
            scorecardMap.put("type", AssetsType.SCORECARD);
            scorecardMap.put("list", scorecardList);

            list.add(guidedMap);
            list.add(ruleTreeMap);
            list.add(ruleTableMap);
            list.add(scorecardMap);

            return Result.newSuccess().withData(list);
        } catch (Exception e) {
            logger.error("查询模板失败: {}", template, e);
            throw new IllegalStateException("查询模板失败");
        }
    }

    @Override
    @Transactional
    public Result updateContentByUuid(String uuid, String content) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            Assets referAssets = new Assets();
            referAssets.setUuid(uuid);
            referAssets.setContent(content);
            referAssets.setType(AssetsType.TEMPLATE);
            Set<Refer> refers = assetsService.getRefers(referAssets);
            List<String> types = new ArrayList<>();
            types.add("func");
            types.add("model");
            types.add("thirdApi");
            if (!refers.isEmpty()) {
                Set<String> uuids = refers.stream().filter(refer -> !types.contains(refer.getSubjectType()))
                        .map(Refer::getSubjectUuid).collect(Collectors.toSet());
                if (!uuid.isEmpty()) {
                    int count = assetsMapper.selectAssetsCountByUuids(uuids);
                    if (count != uuids.size()) {
                        return Result.newError().withMsg("使用的资源文件被删除");
                    }
                }
            }

            Refer deleteRefer = new Refer();
            deleteRefer.setReferType(AssetsType.TEMPLATE);
            deleteRefer.setReferUuid(uuid);
            deleteRefer.setReferVersion(-1);
            referMapper.delete(deleteRefer);

            assetsTemplateMapper.updateContentByUuid(uuid, content);

            if (!refers.isEmpty()) {
                referMapper.insertBatch(refers);
            }
            return Result.SUCCESS;
        } catch (Exception e) {
            logger.error("更新模板失败. uuid: {}, content: {}", uuid, content, e);
            throw new IllegalStateException("更新模板失败");
        }
    }

    @Override
    @Transactional
    public void delete(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new IllegalArgumentException("参数不能为空");
        }

        try {
            AssetsTemplate template = assetsTemplateMapper.selectByUuid(uuid);
            if (template == null) {
                throw new IllegalArgumentException("模板不存在");
            }

            assetsTemplateMapper.delete(uuid);

            RecycleBin recycleBin = new RecycleBin();
            recycleBin.setUuid(IdUtils.UUID());
            recycleBin.setProjectUuid(template.getProjectUuid());
            recycleBin.setAssetsUuid(template.getUuid());
            recycleBin.setAssetsName(template.getName());
            recycleBin.setAssetsType("tpl_" + template.getType());
            recycleBin.setCreator(UserHolder.currentUser());
            recycleBinMapper.insert(recycleBin);
        } catch (Exception e) {
            logger.error("模板删除失败: {}", uuid, e);
            throw new IllegalStateException("模板删除失败");
        }
    }
}
