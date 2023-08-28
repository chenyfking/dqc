package com.beagledata.gaea.workbench.rule.verifer;

import org.apache.commons.collections.SetUtils;
import org.drools.core.ClassObjectFilter;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierImpl;
import org.drools.verifier.components.*;
import org.drools.verifier.report.components.Subsumption;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 重叠条件校验
 *
 * Created by liulu on 2020/11/11.
 */
public class OverlapRuleVerifier extends AbstractRuleVerifier {
    @Override
    protected void doVerifier(Verifier verifier) {
        Collection<? extends Object> subsumptionList = ((VerifierImpl) verifier).getKnowledgeSession().getObjects(
                new ClassObjectFilter(Subsumption.class)
        );
        StringBuilder buffer = new StringBuilder();
        Set<String> overlaps = new HashSet<>();
        for (Object obj : subsumptionList) {
            if (VerifierComponentType.SUB_RULE.equals((((Subsumption) obj).getLeft()).getVerifierComponentType())) {
                SubRule leftRule = (SubRule) ((Subsumption) obj).getLeft();
                SubRule rightRule = (SubRule) ((Subsumption) obj).getRight();
                Set<String> leftSet = getPatternComponents(leftRule);
                Set<String> rightSet = getPatternComponents(rightRule);
                if (SetUtils.isEqualSet(leftSet, rightSet)) {
                    // 相同校验不认为重叠
                    continue;
                }

                leftSet.retainAll(rightSet);
                if (!leftSet.isEmpty()) {
                    // 有交集认为条件重叠
                    String leftRuleName = cleanRuleName(leftRule.getRuleName());
                    String rightRuleName = cleanRuleName(rightRule.getRuleName());
                    if (leftRuleName.compareTo(rightRuleName) > 0) {
                        overlaps.add(leftRuleName + ", " + rightRuleName);
                    } else {
                        overlaps.add(rightRuleName + ", " + leftRuleName);
                    }
                }
            }
        }

        if (!overlaps.isEmpty()) {
            for (String str : overlaps) {
                buffer.append("[").append(str).append("], ");
            }
            addResult(null, buffer.substring(0, buffer.length() - 2));
        }
    }

    private Set<String> getPatternComponents(SubRule subRule) {
        Set<String> set = new HashSet<>();
        for (RuleComponent rc : subRule.getItems()) {
            SubPattern sp = (SubPattern) rc;
            for (PatternComponent pc : sp.getItems()) {
                if (pc instanceof LiteralRestriction) {
                    LiteralRestriction lr = (LiteralRestriction) pc;
                    set.add(lr.getFieldPath() + lr.getOperator().getOperatorString() + lr.getValueAsString());
                }
            }
        }
        return set;
    }

    @Override
    protected VerifierType getVerifierType() {
        return VerifierType.OVERLAP;
    }
}
