package com.beagledata.gaea.workbench.service.impl;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.Result;
import com.beagledata.gaea.workbench.entity.ThirdApiDefinition;
import com.beagledata.gaea.workbench.mapper.ReferMapper;
import com.beagledata.gaea.workbench.mapper.ThirdApiDefinitionMapper;
import com.beagledata.gaea.workbench.service.ThirdApiDefinitionService;
import com.beagledata.gaea.workbench.util.UserHolder;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created by liulu on 2020/6/14.
 */
@Service
public class ThirdApiDefinitionServiceImpl implements ThirdApiDefinitionService {
    private static final String REG_URL = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ThirdApiDefinitionMapper thirdApiDefinitionMapper;
    @Autowired
    private ReferMapper referMapper;

    @Override
    public void add(ThirdApiDefinition definition) {
        check(definition);

        try {
            definition.setUuid(IdUtils.UUID());
            definition.setHeadersJson(JSON.toJSONString((definition.getHeaders())));
            definition.setCreator(UserHolder.currentUser());
            thirdApiDefinitionMapper.insert(definition);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("接口名称不能重复");
        } catch (Exception e) {
            logger.error("添加外部接口失败. definition: {}", definition, e);
            throw new IllegalStateException("添加失败");
        }
    }

    @Override
    public List<ThirdApiDefinition> list() {
        try {
            List<ThirdApiDefinition> list = thirdApiDefinitionMapper.selectAll();
            list.forEach(definition -> {
                if (StringUtils.isNotBlank(definition.getHeadersJson())) {
                    definition.getHeaders().addAll(JSON.parseArray(definition.getHeadersJson(), ThirdApiDefinition.Header.class));
                }
            });
            return list;
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<ThirdApiDefinition> listForModeling() {
        try {
            return thirdApiDefinitionMapper.selectForModeling();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return Collections.emptyList();
    }

    @Override
    public Result delete(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            logger.warn("删除外部接口失败, uuid为空: {}", uuid);
            throw new IllegalArgumentException("除外部接口失败， uuid为空失败");
        }

        try {
            Integer countNum = referMapper.countBySubjectUuid(uuid);
            if (countNum != null && countNum > 0) {
                return Result.newError().withMsg("删除失败");
            }
            thirdApiDefinitionMapper.delete(uuid);
            return Result.newSuccess();
        } catch (Exception e) {
            throw new IllegalStateException("删除失败");
        }
    }

    @Override
    public void edit(ThirdApiDefinition definition) {
        if (StringUtils.isBlank(definition.getUuid())) {
            logger.warn("uuid不能为空: {}", definition);
            throw new IllegalArgumentException("uuid不能为空");
        }
        check(definition);

        try {
           definition.setHeadersJson(JSON.toJSONString(definition.getHeaders()));
            thirdApiDefinitionMapper.update(definition);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("接口名称不能重复");
        } catch (Exception e) {
            logger.error("编辑外部接口失败. definition: {}", definition, e);
            throw new IllegalStateException("编辑失败");
        }
    }

    @Override
    public ThirdApiDefinition getByUuid(String uuid) {
        try {
            return thirdApiDefinitionMapper.selectByUuid(uuid);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private void check(ThirdApiDefinition definition) {
        if (StringUtils.isBlank(definition.getName()) || definition.getName().length() > 30) {
            logger.warn("外部接口名称不合法: {}", definition);
            throw new IllegalArgumentException("接口名称不能为空并且长度不能超过30个字符");
        }
        if (StringUtils.isBlank(definition.getUrl()) || definition.getUrl().length() > 250) {
            logger.warn("外部接口请求地址不合法: {}", definition);
            throw new IllegalArgumentException("请求地址不能为空并且长度不能超过250个字符");
        }
        if (!definition.getUrl().matches(REG_URL)) {
            logger.warn("外部接口请求地址不合法: {}", definition);
            throw new IllegalArgumentException("请求地址格式不正确");
        }
        if (definition.getResponseJsonPath() != null && definition.getResponseJsonPath().length() > 30) {
            logger.warn("外部接口响应JSON查询PATH不合法: {}", definition);
            throw new IllegalArgumentException("响应JSON查询PATH长度不能超过30个字符");
        }
    }
}
