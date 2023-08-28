package com.beagledata.gaea.workbench.rule.verifer;

import org.drools.verifier.Verifier;
import org.drools.verifier.data.VerifierReport;
import org.drools.verifier.report.components.Gap;
import org.drools.verifier.report.components.MissingRange;

import java.util.*;

/**
 * 范围缺失校验
 *
 * Created by liulu on 2020/11/11.
 */
public class MissingRangeRuleVerifier extends AbstractRuleVerifier {
    @Override
    protected void doVerifier(Verifier verifier) {
        VerifierReport result = verifier.getResult();
        if (!result.getRangeCheckCauses().isEmpty()) {
            Map<String, List<Gap>> map = new HashMap<>();
            for (MissingRange mr : result.getRangeCheckCauses()) {
                if (mr instanceof Gap) {
                    Gap gap = (Gap) mr;
                    String field = gap.getField().getObjectTypeName() + gap.getField().getName();
                    List<Gap> gaps = map.get(field);
                    if (gaps == null) {
                        gaps = new ArrayList<>();
                        map.put(field, gaps);
                    }
                    gaps.add(gap);
                }
            }

            StringBuilder buffer = new StringBuilder();
            map.forEach((k, v) -> {
                if (!v.isEmpty()) {
                    Collections.sort(v, Comparator.comparing(Gap::getValueAsString));
                    buffer.append("[");
                    Set<String> set = new HashSet<>();
                    for (int i = 0, len = v.size(); i < len; i++) {
                        Gap gap =  v.get(i);
                        String gapText = getFieldLabel(gap.getField())
                                + gap.getOperator().getOperatorString()
                                + gap.getValueAsString();
                        if (set.add(gapText)) {
                            buffer.append(gapText).append(", ");
                        }
                    }
                    buffer.delete(buffer.length() - 2, buffer.length());
                    buffer.append("], ");
                }
            });
            addResult(null, buffer.substring(0, buffer.length() - 2));
        }
    }

    @Override
    protected VerifierType getVerifierType() {
        return VerifierType.MISSING_RANGE;
    }
}
