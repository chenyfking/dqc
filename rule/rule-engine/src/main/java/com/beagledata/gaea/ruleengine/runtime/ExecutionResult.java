package com.beagledata.gaea.ruleengine.runtime;

import com.beagledata.gaea.ruleengine.common.RuleMetaData;
import org.apache.commons.lang.StringUtils;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.kie.api.runtime.rule.Match;

import java.util.*;

/**
 * Created by liulu on 2018/10/11.
 */
public class ExecutionResult {
    /**
     * 触发的规则条数
     */
    private int fireNum;
    /**
     * 触发的规则名称列表
     */
    private List<String> fireRules = new ArrayList<>();
    /**
     * 执行的时间
     */
    private long executionTime;
    /**
     *  决策流执行的节点key
     */
    private Map<String, List<Integer>> fireNodeMap = new HashMap<>();
    /**
     * 已执行互斥组
     */
    private Set<String> activationGroups = new HashSet<>();
    /**
    * 规则过滤集合
    */
    private Set<RuleImpl> filterRules = new HashSet<>();
    /**
     * 触发的规则元数据列表
     */
    private List<Map<String, Object>> fireRuleMetas = new ArrayList<>();
    /**
     * rete网络匹配到的节点
     */
    private Set<Match> matches = new HashSet<>();

    public boolean addRule(Match match) {
        RuleImpl rule = (RuleImpl) match.getRule();
        if (filterRules.contains(rule)) {
            //已执行过规则
            if (isLoopRule(rule)) {
                return matches.add(match);
            }
            return false;
        }
        if (StringUtils.isNotBlank(rule.getActivationGroup())) {
            String activationGroup = rule.getPackageName() + "_" + rule.getActivationGroup();
            if (!activationGroups.add(activationGroup)) {
                //互斥组有规则已执行
                return false;
            }
        }
        String ruleName = rule.getName();
        if (ruleName.endsWith("-ELSE分支")) {
            String sourceRuleName = ruleName.substring(0, ruleName.length() - "-ELSE分支".length());
            if (fireRules.contains(sourceRuleName)) {
                //else 分支的主分支已执行
                return false;
            }
        }

        filterRules.add(rule);
        if (isLoopRule(rule)) {
            matches.add(match);
        }

        if (!isImplicitRule(rule)) {
            // 记录非隐性规则
            fireRules.add(ruleName);
            fireNum++;
            Map<String, Object> ruleMeta = new HashMap<>(3);
            String assetsUuid = (String) rule.getMetaData("assetsUuid");
            if (assetsUuid != null) {
                ruleMeta.put("uuid", assetsUuid);
            }
            Integer assetsVersion = (Integer) rule.getMetaData("assetsVersion");
            if (assetsVersion != null) {
                ruleMeta.put("version", assetsVersion);
            }
            ruleMeta.put("name", ruleName);
            fireRuleMetas.add(ruleMeta);
        }
        return true;
    }

    public void appendNodeKey(String flowId, int nodeKey) {
        List<Integer> keys ;
        if (fireNodeMap.containsKey(flowId)) {
            keys = fireNodeMap.get(flowId);
        } else {
            keys = new LinkedList<>();
            fireNodeMap.put(flowId, keys);
        }
        if (!keys.contains(nodeKey)) {
            keys.add(nodeKey);
        }
    }

    private boolean isLoopRule(RuleImpl rule) {
        Boolean isLoopRule = (Boolean) rule.getMetaData(RuleMetaData.KEY_LOOP);
        return isLoopRule != null && isLoopRule;
    }

    private boolean isImplicitRule(RuleImpl rule) {
        Boolean isImplicitRule = (Boolean) rule.getMetaData(RuleMetaData.KEY_IMPLICIT);
        return isImplicitRule != null && isImplicitRule;
    }

    public int getFireNum() {
        return fireNum;
    }

    public void setFireNum(int fireNum) {
        this.fireNum = fireNum;
    }

    public List<String> getFireRules() {
        return fireRules;
    }

    public void setFireRules(List<String> fireRules) {
        this.fireRules = fireRules;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public Map<String, List<Integer>> getFireNodeMap() {
        return fireNodeMap;
    }

    public List<Map<String, Object>> getFireRuleMetas() {
        return fireRuleMetas;
    }

    public void setFireRuleMetas(List<Map<String, Object>> fireRuleMetas) {
        this.fireRuleMetas = fireRuleMetas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ExecutionResult{");
        sb.append("fireNum=").append(fireNum);
        sb.append(", fireRules=").append(fireRules);
        sb.append(", executionTime=").append(executionTime);
        sb.append(", fireNodeMap=").append(fireNodeMap);
        sb.append(", activationGroups=").append(activationGroups);
        sb.append(", fireRuleMetas=").append(fireRuleMetas);
        sb.append('}');
        return sb.toString();
    }
}
