package com.beagledata.gaea.workbench.rule.parser;

import com.alibaba.fastjson.JSON;
import com.beagledata.common.SpringBeanHolder;
import com.beagledata.gaea.ruleengine.runtime.flow.PercentageDecisionHandler;
import com.beagledata.gaea.workbench.common.AssetsType;
import com.beagledata.gaea.workbench.entity.Assets;
import com.beagledata.gaea.workbench.mapper.AssetsMapper;
import com.beagledata.gaea.workbench.rule.define.*;
import com.beagledata.util.IdUtils;
import com.beagledata.util.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.factory.SplitFactory;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2018/11/2.
 */
public class FlowParser implements Parser {
    private AssetsMapper assetsMapper = SpringBeanHolder.getBean(AssetsMapper.class);

    public static AtomicLong incrementNum = new AtomicLong(0);

    private Flow flow;
    private Assets assets;

    public FlowParser(Assets assets) {
        this.assets = assets;
    }

    @Override
    public Flow getDumper() {
        if (flow == null) {
            doParse();
        }
        flow.setAssets(assets);
        return flow;
    }

    /**
     * 调用jBPM Process API，生成bpmn2的XML字符串
     */
    private void doParse() {
        flow = new Flow();
        int id = assets.getId();
        Integer versionNo = assets.getVersionNo();
        if (versionNo == null) {
            versionNo = 0;
        }
        flow.setId(id);
        String uuid = assets.getUuid();
        Long number = incrementNum.getAndIncrement();
        flow.setProcessId("flow_" + uuid + "_" + versionNo + "_" + number);
        flow.setRulePackage(Drl.DEFAULT_PACKAGE + "." + flow.getProcessId());

        GoJsFlow goJsFlow = JSON.parseObject(assets.getContent(), GoJsFlow.class);
        goJsFlow.init();

        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess(flow.getProcessId());
        buildHeader(factory);
        buildNodes(goJsFlow, factory);
        buildConnections(goJsFlow, factory);
        buildVariables(factory);
        buildImports(factory);
        buildGlobals(factory);
        flow.setJbpmProcess(factory.validate().getProcess());
    }

    /**
     * 配置规则流名称和drools规则包路径
     * @param factory
     */
    private void buildHeader(RuleFlowProcessFactory factory) {
        StringBuilder buffer = new StringBuilder("决策流-");
        buffer.append(assets.getName());
        if (assets.getVersionNo() != null && assets.getVersionNo() > 0) {
            buffer.append("_V").append(assets.getVersionNo());
        }
        factory.name(buffer.toString()).packageName(flow.getRulePackage());
    }

    private void buildNodes(GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        for (Node node : goJsFlow.getNodeDataArray()) {
            if (Node.Category.Start.equals(node.getCategory())) {
                buildStartNode(node, goJsFlow, factory);
            } else if (Node.Category.Rule.equals(node.getCategory())) {
                buildRuleNode(node, goJsFlow, factory);
            } else if (Node.Category.Script.equals(node.getCategory())) {
                buildScriptNode(node, goJsFlow, factory);
            } else if (Node.Category.Decision.equals(node.getCategory())) {
                buildDecisionNode(node, goJsFlow, factory);
            } else if (Node.Category.Diverge.equals(node.getCategory())) {
                buildDivergeNode(node, goJsFlow, factory);
            } else if (Node.Category.Converge.equals(node.getCategory())) {
                buildConvergeNode(node, goJsFlow, factory);
            }

            // 自定义节点的UniqueId，配置TraceProcessEventListener
            org.kie.api.definition.process.Node kieNode = factory.getProcess().getNode(node.getKey());
            String nodeUniqueId = (String) kieNode.getMetaData().get("UniqueId");
            kieNode.getMetaData().put("UniqueId", nodeUniqueId + node.getKey());
        }
    }

    private void buildStartNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        factory.startNode(node.getKey()).name(node.getName()).done();
        supplyEndNode(node, goJsFlow, factory);
    }

    /**
     * 配置规则节点
     * 只有一个决策流文件：创建子流程节点
     * 有多个规则文件：设置ruleflow-group，规则包路径后缀ruleflow-group，决策流文件创建子流程节点
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void buildRuleNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        if (node.getRules().isEmpty()) { // 空规则节点
            createRuleSetNode(node, goJsFlow, factory);
            return;
        }

        List<Assets> rules = node.getRules();

        if (CollectionUtils.isEmpty(rules)){
            //没有规则文件
            createRuleSetNode(node, goJsFlow, factory);
            return;
        }
        rules = rules.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(()
                -> new TreeSet<>(Comparator.comparing(o -> o.getUuid() + ";" + o.getVersionNo()))), ArrayList::new));

        List<Assets> ruleAssetsList = assetsMapper.selectByUuidsAndVersion(rules)
                .stream().filter(assets -> StringUtils.isNotBlank(assets.getContent())).collect(Collectors.toList());
        if (ruleAssetsList.isEmpty()) { // 根据uuid列表没有查询到有内容的资源文件
            createRuleSetNode(node, goJsFlow, factory);
            return;
        }

        if (ruleAssetsList.size() == 1 && AssetsType.RULE_FLOW.equals(ruleAssetsList.get(0).getType())) { // 只有一个决策流文件：创建子流程节点
            FlowParser flowParser = (FlowParser) ParserFactory.getParser(ruleAssetsList.get(0));
            Flow subFlow = flowParser.getDumper();
            subFlow.setSubFlow(true);
            subFlow.setAssets(ruleAssetsList.get(0));
            flow.addFlow(subFlow);
            factory.subProcessNode(node.getKey())
                    .name(node.getName())
                    .independent(false)
                    .processId(subFlow.getProcessId()).done();
            supplyEndNode(node, goJsFlow, factory);
            return;
        }

        String ruleFlowGroup = createRuleSetNode(node, goJsFlow, factory);
        for (Assets assets : ruleAssetsList) {
            Parser parser = ParserFactory.getParser(assets);
            if (parser instanceof DrlParser) {
                Drl drl = ((DrlParser) parser).getDumper();
                drl.setPackageName(flow.getRulePackage() + "." + ruleFlowGroup);
                drl.addAttr(Attr.RULEFLOW_GROUP, ruleFlowGroup);
                drl.setAssets(assets);
                flow.addDrl(drl);
            } else {
                FlowParser flowParser = (FlowParser) parser;
                Flow subFlow = flowParser.getDumper();
                subFlow.setSubFlow(true);
                flow.addFlow(subFlow);

                Node subFlowNode = new Node(node.getKey() - goJsFlow.getNodeDataArray().size(), node.getName() + "_子决策流");
                factory.subProcessNode(subFlowNode.getKey())
                        .name(subFlowNode.getName())
                        .independent(false)
                        .processId(subFlow.getProcessId()).done();
                goJsFlow.insertNode(node, subFlowNode);
            }
        }
    }

    /**
     * 根据脚本生成规则，设置ruleflow-group
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void buildScriptNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        String ruleFlowGroup = createRuleSetNode(node, goJsFlow, factory);

        String name = factory.getProcess().getName();
        Rule rule = node.getAvailableScript();
        if (node.getKey() < 0) {
            rule.setName(name + "-" + node.getName() + node.getKey());
        } else {
            rule.setName(name + "-" + node.getName() + "-" + node.getKey());
        }
        Drl drl = new Drl(String.valueOf(Math.abs(node.getKey())));
        drl.setPackageName(flow.getRulePackage() + "." + ruleFlowGroup);
        drl.addAttr(Attr.RULEFLOW_GROUP, ruleFlowGroup);
        drl.addRule(rule);
        drl.setAssets(assets);
        flow.addDrl(drl);
    }

    private String createRuleSetNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        String ruleFlowGroup = String.format("group_%s_%s", flow.getProcessId(), Math.abs(node.getKey()));
        factory.ruleSetNode(node.getKey()).name(node.getName()).ruleFlowGroup(ruleFlowGroup).done();
        supplyEndNode(node, goJsFlow, factory);
        return ruleFlowGroup;
    }

    /**
     * 配置决策节点，保证至少两个出连接，至少一个条件为true，如果数量不够默认补充结束节点
     * 节点流出逻辑：优先选择第一条为true的连接
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void buildDecisionNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        SplitFactory splitFactory = factory.splitNode(node.getKey()).name(node.getName());
        int priority = 1;
        Decision decision = node.getAvailableDecision();
        splitFactory.type(Decision.Strategy.OR.equals(decision.getStrategy()) ? Split.TYPE_OR : Split.TYPE_XOR);

        for (Link link : goJsFlow.getToLinks(node)) {
            Node toNode = goJsFlow.getNode(link.getTo());
            Decision.Connection conn = decision.getConnection(link.getTo());
            if (Decision.Type.CONDITION.equals(decision.getType())) {
                String constraint = conn.getLhs().dump();
                if (constraint == null) {
                    splitFactory.constraint(toNode.getKey(), toNode.getName(), "code", "java", "return false;", priority++);
                } else {
                    splitFactory.constraint(toNode.getKey(), toNode.getName(), "rule", "mvel", constraint, priority++);
                    flow.addImports(conn.getLhs().getRule().getFacts());
                    flow.addGlobals(conn.getLhs().getRule().getFuncs());
                    flow.addModels(conn.getLhs().getRule().getModels());
                }
            } else {
                if (conn.getPercentage() == null) {
                    splitFactory.constraint(toNode.getKey(), toNode.getName(), "code", "java", "return false;", priority++);
                } else {
                    String constraint = String.format(
                            "return PercentageDecisionHandler.getInstance(\"%s_%s\").evaluate(%s);",
                            flow.getProcessId(), node.getKey(), conn.getPercentage() / 100
                    );
                    splitFactory.constraint(toNode.getKey(), toNode.getName(), "code", "java", constraint, priority++);
                }
            }
        }

        do {
            Node endNode = connectEndNode(node, goJsFlow, factory);
            splitFactory.constraint(endNode.getKey(), node.getName() + "_结束_" + priority, "code", "java", "return true;", priority);
        } while (priority++ < 2);
        splitFactory.done();
    }

    /**
     * 配置分支节点，保证至少两个出连接，如果数量不够默认补充结束节点
     * 节点流出逻辑：所有连接
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void buildDivergeNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        SplitFactory splitFactory = factory.splitNode(node.getKey()).name(node.getName()).type(Split.TYPE_AND);
        int toCount = goJsFlow.getToLinks(node).size();
        while (toCount++ < 2) {
            connectEndNode(node, goJsFlow, factory);
        }
        splitFactory.done();
    }

    /**
     * 配置聚合节点，保证至少一个出连接，如果数量不够默认补充结束节点
     * 节点到达逻辑：所有入连接到达
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void buildConvergeNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        factory.joinNode(node.getKey()).name(node.getName()).type(Join.TYPE_AND).done();
        supplyEndNode(node, goJsFlow, factory);
    }

    private void buildConnections(GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        for (Link link : goJsFlow.getLinkDataArray()) {
            factory.connection(link.getFrom(), link.getTo());
        }
    }

    /**
     * 配置决策百分比处理变量
     *
     * @param factory
     */
    private void buildVariables(RuleFlowProcessFactory factory) {
        factory.variable(
                "PercentageDecisionHandler",
                new ObjectDataType(PercentageDecisionHandler.class.getName()),
                "ItemSubjectRef",
                "_PercentageDecisionHandler"
        );
    }

    /**
     * 配置import对象
     *
     * @param factory
     */
    private void buildImports(RuleFlowProcessFactory factory) {
        if (!flow.getImports().isEmpty()) {
            Set<String> imports = new HashSet<>();
            flow.getImports().forEach(fact -> imports.add(fact.getImport()));
            factory.imports(imports.toArray(new String[0]));
        }
    }

    /**
     * 配置内置函数
     *
     * @param factory
     */
    private void buildGlobals(RuleFlowProcessFactory factory) {
        flow.getGlobals().forEach(func -> factory.global("$" + func.substring(func.lastIndexOf('.') + 1), func));
    }

    /**
     * 给没有后置节点的节点连接结束节点
     *
     * @param node
     * @param goJsFlow
     * @param factory
     */
    private void supplyEndNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        if (!goJsFlow.hasTo(node)) {
            connectEndNode(node, goJsFlow, factory);
        }
    }

    /**
     * 创建结束节点，连接本节点
     *
     * @param node
     * @param goJsFlow
     * @param factory
     * @return
     */
    private Node connectEndNode(Node node, GoJsFlow goJsFlow, RuleFlowProcessFactory factory) {
        Node endNode = genEndNode(factory);
        goJsFlow.addConnection(node, endNode);
        return endNode;
    }

    /**
     * @param factory
     * @return 生成结束节点，节点id随机4位数字
     */
    private Node genEndNode(RuleFlowProcessFactory factory) {
        int id = genRandomNodeKey();
        Node endNode = new Node(id, "结束_" + id);
        factory.endNode(endNode.getKey()).name(endNode.getName()).terminate(false).done();
        return endNode;
    }

    private int genRandomNodeKey() {
        return Integer.parseInt(IdUtils.RandomNum(4));
    }

    /**
     * gojs的工作流
     */
    static final class GoJsFlow {
        /**
         * 节点集合
         */
        private List<Node> nodeDataArray = new ArrayList<>();
        /**
         * 连接集合
         */
        private List<Link> linkDataArray = new ArrayList<>();

        /**
         * 清理非法节点和连接，只保留开始节点能到达的节点
         */
        public void init() {
            Node startNode = getNode(Node.Category.Start);
            if (startNode != null) {
                clear();
            } else { // 找不到开始节点，初始化工作流
                nodeDataArray.clear();
                linkDataArray.clear();
                nodeDataArray.add(new Node(Node.Category.Start, 1, "开始"));
            }
        }

        /**
         * 删除所有没有入连接的节点
         */
        private void clear() {
            Iterator<Node> it = nodeDataArray.iterator();
            while (it.hasNext()) {
                Node node = it.next();
                if (!Node.Category.Start.equals(node.getCategory()) && !hasFrom(node)) {
                    it.remove();
                    removeToConnection(node);
                    clear();
                    return;
                }
            }
        }

        /**
         * 插入节点
         *
         * @param fromNode 要插入节点的前置节点
         * @param insertNode 要插入的节点
         */
        public void insertNode(Node fromNode, Node insertNode) {
            getToLinks(fromNode).forEach(link -> link.setFrom(insertNode.getKey())); // 断开前置节点出连接，连接插入节点
            addConnection(fromNode, insertNode); // 前置节点连接插入节点
        }

        /**
         * @param node
         * @return 获取节点的出连接
         */
        public List<Link> getToLinks(Node node) {
            return linkDataArray.stream().filter(link -> node.getKey() == link.getFrom()).collect(Collectors.toList());
        }

        /**
         * @param node
         * @return 判断节点是否有入连接
         */
        public boolean hasFrom(Node node) {
            return linkDataArray.stream().anyMatch(link -> node.getKey() == link.getTo());
        }

        /**
         * @param node
         * @return 判断节点是否有出连接
         */
        public boolean hasTo(Node node) {
            return linkDataArray.stream().anyMatch(link -> node.getKey() == link.getFrom());
        }

        /**
         * @param node 清除节点的出连接
         */
        public void removeToConnection(Node node) {
            Iterator<Link> it = linkDataArray.iterator();
            while (it.hasNext()) {
                Link link = it.next();
                if (link.getFrom() == node.getKey()) {
                    it.remove();
                }
            }
        }

        /**
         * @param id
         * @return 根据节点id获取节点
         */
        public Node getNode(int id) {
            return nodeDataArray.stream().filter(node -> id == node.getKey()).findFirst().orElse(null);
        }

        /**
         * @param category
         * @return 根据节点类型获取节点
         */
        public Node getNode(Node.Category category) {
            return nodeDataArray.stream().filter(node -> category.equals(node.getCategory())).findFirst().orElse(null);
        }

        /**
         * @param category
         * @return 根据节点类型获取节点列表
         */
        public List<Node> getNodes(Node.Category category) {
            return nodeDataArray.stream().filter(node -> category.equals(node.getCategory())).collect(Collectors.toList());
        }

        /**
         * 连接2个节点
         *
         * @param fromNode
         * @param toNode
         */
        public void addConnection(Node fromNode, Node toNode) {
            this.linkDataArray.add(new Link(fromNode.getKey(), toNode.getKey()));
        }

        public List<Node> getNodeDataArray() {
            return nodeDataArray;
        }

        public void setNodeDataArray(List<Node> nodeDataArray) {
            this.nodeDataArray = nodeDataArray;
        }

        public List<Link> getLinkDataArray() {
            return linkDataArray;
        }

        public void setLinkDataArray(List<Link> linkDataArray) {
            this.linkDataArray = linkDataArray;
        }
    }

    /**
     * 节点
     */
    static final class Node {
        /**
         * 节点类型
         */
        private Category category;
        /**
         * gojs里节点的key
         */
        private int key;
        /**
         * 节点名称
         */
        private String name;
        /**
         * 脚本节点的脚本数据
         */
        private Rule script;
        /**
         * 规则节点的规则列表
         */
        private List<Assets> rules = new ArrayList<>();
        /**
         * 决策节点的决策数据
         */
        private Decision decision;

        enum Category {
            Start, // 开始节点
            End, // 结束节点
            Rule, // 规则节点
            Script, // 脚本节点
            Decision, // 决策节点
            Diverge, // 分支节点
            Converge // 聚合节点
        }

        public Node() {
        }

        public Node(int key, String name) {
            this.key = key;
            this.name = name;
        }

        public Node(Category category, int key, String name) {
            this.category = category;
            this.key = key;
            this.name = name;
        }

        public Rule getAvailableScript() {
            return script != null ? script : new Rule();
        }

        public Decision getAvailableDecision() {
            return decision != null ? decision : new Decision();
        }

        public Category getCategory() {
            return category;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public int getKey() {
            return key;
        }

        public void setKey(int key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Rule getScript() {
            return script;
        }

        public void setScript(Rule script) {
            this.script = script;
        }

        public List<Assets> getRules() {
            return rules;
        }

        public void setRules(List<Assets> rules) {
            this.rules = rules;
        }

        public Decision getDecision() {
            return decision;
        }

        public void setDecision(Decision decision) {
            this.decision = decision;
        }
    }

    /**
     * 连接
     */
    static final class Link {
        /**
         * 出节点id
         */
        private int from;
        /**
         * 入节点id
         */
        private int to;

        public Link() {
        }

        public Link(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public void setFrom(int from) {
            this.from = from;
        }

        public int getTo() {
            return to;
        }

        public void setTo(int to) {
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Link)) return false;

            Link link = (Link) o;

            if (getFrom() != link.getFrom()) return false;
            return getTo() == link.getTo();
        }

        @Override
        public int hashCode() {
            int result = getFrom();
            result = 31 * result + getTo();
            return result;
        }
    }

    /**
     * 决策
     */
    static final class Decision {
        /**
         * 决策类型，默认按照条件控制
         */
        private Type type = Type.CONDITION;
        /**
         * 判断策略
         */
        private Strategy strategy = Strategy.XOR;
        /**
         * 出连接列表
         */
        private List<Connection> connections = new ArrayList<>();

        public Connection getConnection(int key) {
            return connections.stream().filter(conn -> conn.getKey() == key).findFirst().orElse(new Connection());
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }

        public Strategy getStrategy() {
            return strategy;
        }

        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        public List<Connection> getConnections() {
            return connections;
        }

        public void setConnections(List<Connection> connections) {
            this.connections = connections;
        }

        enum Type {
            CONDITION, // 条件
            PERCENTAGE // 百分比
        }

        enum Strategy {
            XOR, OR
        }

        /**
         * 决策节点出连接
         */
        static final class Connection {
            /**
             * 出节点key
             */
            private int key;
            /**
             * 决策类型是条件的时候，具体条件内容
             */
            private Lhs lhs = new Lhs();
            /**
             * 决策类型是百分比的时候，具体百分比
             */
            private Double percentage;

            public int getKey() {
                return key;
            }

            public void setKey(int key) {
                this.key = key;
            }

            public Lhs getLhs() {
                return lhs;
            }

            public void setLhs(Lhs lhs) {
                this.lhs = lhs;
            }

            public Double getPercentage() {
                return percentage;
            }

            public void setPercentage(Double percentage) {
                this.percentage = percentage;
            }
        }
    }
}
