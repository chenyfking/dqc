package com.beagledata.gaea.workbench.rule.define;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据模型表达式
 *
 * Created by liulu on 2020/4/8.
 */
public class FactExpression implements Dumper, RuleAware, JavaTypeGetter {
    /**
     * 数据模型uuid
     */
    protected String id;
    /**
     * 数据模型字段id
     */
    protected String fieldId;
    /**
     * 所属规则
     */
    protected Rule rule;
    /**
     * 数据模型
     */
    protected Fact fact;
    /**
     * 字段列表
     */
    protected List<Field> fields = new ArrayList<>();

    /**
     * 衍生对象表达式
     */
    private Expression expression;

    public FactExpression() {
    }

    public FactExpression(String id, String fieldId) {
        this.id = id;
        this.fieldId = fieldId;
    }

    @Override
    public String dump() {
        loadFact();
        if (fact == null) {
            return null;
        }
        if (StringUtils.isNotBlank(fieldId)) {
            if (fields.isEmpty()) {
                return null;
            }
            if (getLastField().isDeriveType()) {
                if (rule.isForVerifier()) {
                    return getLinkNames();
                }
                if (getLastField().getDeriveData() == null) {
                    return null;
                }
                expression = JSONObject.parseObject(getLastField().getDeriveData(), Expression.class);
                rule.addFact(fact);
                expression.setRule(rule);
                if (ExpressionType.FUNC.equals(expression.getType())) {
                    return String.format("%s",expression.dump());
                }
                return String.format("(%s)",expression.dump());
            }
            rule.addFact(fact);
            return String.format("$%s.%s", fact.getClassName(), getLinkNames());
        }
        rule.addFact(fact);
        return String.format("$%s", fact.getClassName());
    }

    public Field getLastField() {
        if (fields.isEmpty()) {
            return null;
        }
        return fields.get(fields.size() - 1);
    }

    public String getLinkNames() {
        return fields.stream().map(f -> f.getName()).collect(Collectors.joining("."));
    }

    public String getModifyName() {
        if (fact == null) {
            return null;
        }

        StringBuilder buffer = new StringBuilder(fact.getClassName());
        for (int i = 0; i < fields.size() - 1; i++) {
            buffer.append(".").append(fields.get(i).getName());
        }
        return buffer.toString();
    }

    @Override
    public String getJavaType() {
        if (fields.isEmpty()) {
            return Field.Type.Map.name();
        }
        return getLastField().getType().name();
    }

    protected void loadFact() {
        if (fact != null || StringUtils.isBlank(id)) {
            return;
        }

        fact = AssetsCache.getFactByUuid(id);
        if (fact != null && StringUtils.isNotBlank(fieldId)) {
            if (!fieldId.contains(",")) {
                Field field = fact.getFieldById(fieldId);
                if (field != null) {
                    fields.add(field);
                }
            } else {
                Fact tmpFact = fact;
                String[] fieldIds = fieldId.split(",");
                for (int i = 0; i < fieldIds.length; i++) {
                    Field field = tmpFact.getFieldById(fieldIds[i]);
                    if (field == null) {
                        break;
                    }
                    fields.add(field);
                    if (!field.isObjectType()) {
                        break;
                    }
                    tmpFact = AssetsCache.getFactByUuid(field.getSubType());
                }
            }
        }
    }

    @Override
    public void setRule(Rule rule) {
        this.rule = rule;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public Fact getFact() {
        return fact;
    }

    public void setFact(Fact fact) {
        this.fact = fact;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
