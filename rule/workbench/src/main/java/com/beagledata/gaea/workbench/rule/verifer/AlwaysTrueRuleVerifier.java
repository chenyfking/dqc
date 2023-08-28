package com.beagledata.gaea.workbench.rule.verifer;

import com.beagledata.util.StringUtils;
import org.drools.core.ClassObjectFilter;
import org.drools.core.base.evaluators.Operator;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierImpl;
import org.drools.verifier.components.*;
import org.drools.verifier.data.VerifierComponent;

import java.util.*;
import java.util.regex.Matcher;

/**
 * 永真规则校验
 *
 * Created by liulu on 2020/11/11.
 */
public class AlwaysTrueRuleVerifier extends AbstractRuleVerifier {
    private static Map<Operator, Set<Operator>> oppositeOperators = new HashMap<>();

    static {
        addOppositeOperator(Operator.EQUAL, Operator.NOT_EQUAL);
        addOppositeOperator(Operator.LESS, Operator.GREATER_OR_EQUAL);
        addOppositeOperator(Operator.LESS_OR_EQUAL, Operator.GREATER_OR_EQUAL);
        addOppositeOperator(Operator.GREATER, Operator.LESS_OR_EQUAL);
        addOppositeOperator(Operator.GREATER_OR_EQUAL, Operator.LESS_OR_EQUAL);
        addOppositeOperator(Operator.CONTAINS, Operator.NOT_CONTAINS);
        addOppositeOperator(Operator.EXCLUDES, Operator.NOT_EXCLUDES);
        addOppositeOperator(Operator.MEMBEROF, Operator.NOT_MEMBEROF);
        addOppositeOperator(Operator.MATCHES, Operator.NOT_MATCHES);
    }

    private static void addOppositeOperator(Operator op1, Operator op2) {
        Set<Operator> set = oppositeOperators.get(op1);
        if (set == null) {
            set = new HashSet<>();
            oppositeOperators.put(op1, set);
        }
        set.add(op2);

        set = oppositeOperators.get(op2);
        if (set == null) {
            set = new HashSet();
            oppositeOperators.put(op2, set);
        }
        set.add(op1);
    }

    @Override
    protected void doVerifier(Verifier verifier) {
        Collection<? extends Object> objs = ((VerifierImpl) verifier).getKnowledgeSession().getObjects(
                new ClassObjectFilter(VerifierComponent.class)
        );

        Set<String> ruleNames = new HashSet<>();
        for (Object obj : objs) {
            if (!SubRule.class.equals(obj.getClass())) {
                continue;
            }

            SubRule sr = (SubRule) obj;
            Map<Pattern, List<SubPattern>> map = getSubPatterns(sr);
            if (isAlwaysTrue(map)) {
                ruleNames.add(cleanRuleName(sr.getRuleName()));
            }
        }

        if (!ruleNames.isEmpty()) {
            addResult(null, StringUtils.join(ruleNames, ","));
        }
    }

    private Map<Pattern, List<SubPattern>> getSubPatterns(SubRule sr) {
        Map<Pattern, List<SubPattern>> map = new HashMap<>();
        for (RuleComponent rc : sr.getItems()) {
            if (!SubPattern.class.equals(rc.getClass())) {
                continue;
            }

            SubPattern sp = (SubPattern) rc;
            List<SubPattern> list = map.get(sp.getPattern());
            if (list == null) {
                list = new ArrayList<>();
                map.put(sp.getPattern(), list);
            }
            list.add(sp);
        }
        return map;
    }

    private boolean isAlwaysTrue(Map<Pattern, List<SubPattern>> map) {
        for (Map.Entry<Pattern, List<SubPattern>> entry : map.entrySet()) {
            List<SubPattern> list = entry.getValue();
            if (list.size() > 1) {
                for (int i = 0, len = list.size(); i < len; i++) {
                    for (int j = i + 1; j < len; j++) {
                        List<PatternComponent> pcList1 = new ArrayList<>(list.get(i).getItems());
                        List<PatternComponent> pcList2 = new ArrayList<>(list.get(j).getItems());
                        pcList1.removeAll(list.get(j).getItems());
                        pcList2.removeAll(list.get(i).getItems());
                        List<PatternComponent> pcList3 = new ArrayList<>();
                        pcList3.addAll(pcList1);
                        pcList3.addAll(pcList2);
                        if (isRestrictionOpposite(pcList3)) {
                            return true;
                        }
                    }
                }
            } else if (list.size() == 1) {
                Set<PatternComponent> pcSet = list.get(0).getItems();
                for (PackageComponent pc : pcSet) {
                    if (isRestrictionAlwaysTrue(pc)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isRestrictionAlwaysTrue(PackageComponent pc) {
        if (!(pc instanceof LiteralRestriction)) {
            return false;
        }

        LiteralRestriction lr = (LiteralRestriction) pc;
        String reg = "objectType\\[@name='(.*)'\\]/field\\[@name='(.*)'\\]";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(reg);
        Matcher matcher = pattern.matcher(lr.getFieldPath());
        if (!matcher.find()) {
            return false;
        }

        String fieldName = matcher.group(2);
        if (fieldName == null) {
            return false;
        }

        if (Objects.equals(fieldName, lr.getValueAsString())) {
            if (Operator.EQUAL.equals(lr.getOperator())
                || Operator.LESS_OR_EQUAL.equals(lr.getOperator())
                || Operator.GREATER_OR_EQUAL.equals(lr.getOperator())
                || Operator.CONTAINS.equals(lr.getOperator())
                || Operator.MATCHES.equals(lr.getOperator())) {
                // a == a, a <= a, a >= a, a contains a, a matches a
                return true;
            }
        }
        return false;
    }

    private boolean isRestrictionOpposite(List<PatternComponent> list) {
        if (list.size() != 2) {
            return false;
        }
        if (list.stream().anyMatch(pc -> !(pc instanceof LiteralRestriction))) {
            return false;
        }

        return isRestrictionOpposite((LiteralRestriction) list.get(0), (LiteralRestriction) list.get(1));
    }

    private boolean isRestrictionOpposite(LiteralRestriction lr1, LiteralRestriction lr2) {
        if (Objects.equals(lr1.getFieldPath(), lr2.getFieldPath())
            && Objects.equals(lr1.getValueAsString(), lr2.getValueAsString())
            && isOperatorOpposite(lr1.getOperator(), lr2.getOperator())) {
            // a == b || a != b, a < b || a >= b, a > b || a <= b
            return true;
        }
        if (Objects.equals(getRestrictionFieldName(lr1), lr2.getValueAsString())
            && Objects.equals(lr1.getValueAsString(), getRestrictionFieldName(lr2))
            && isOperatorOpposite(lr1.getOperator(), reverseOperator(lr2.getOperator()))) {
            // a > b || b >= a
            return true;
        }
        return false;
    }

    private String getRestrictionFieldName(LiteralRestriction lr) {
        String fieldName = lr.getFieldPath();
        int index = fieldName.indexOf("/field[@name='");
        return fieldName.substring(index + "/field[@name='".length(), fieldName.length() - 2);
    }

    private boolean isOperatorOpposite(Operator op1, Operator op2) {
        return oppositeOperators.get(op1).contains(op2);
    }

    private Operator reverseOperator(Operator op) {
        if (Operator.GREATER.equals(op)) {
            return Operator.LESS;
        } else if (Operator.GREATER_OR_EQUAL.equals(op)) {
            return Operator.LESS_OR_EQUAL;
        } else if (Operator.LESS.equals(op)) {
            return Operator.GREATER;
        } else if (Operator.LESS_OR_EQUAL.equals(op)) {
            return Operator.GREATER_OR_EQUAL;
        } else {
            return op;
        }
    }

    @Override
    protected VerifierType getVerifierType() {
        return VerifierType.ALWAYS_TRUE;
    }
}
