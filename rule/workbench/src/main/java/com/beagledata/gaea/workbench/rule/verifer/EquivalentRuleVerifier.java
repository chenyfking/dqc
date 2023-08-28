package com.beagledata.gaea.workbench.rule.verifer;

import org.drools.verifier.Verifier;
import org.drools.verifier.data.VerifierReport;
import org.drools.verifier.report.components.MessageType;
import org.drools.verifier.report.components.Severity;
import org.drools.verifier.report.components.VerifierMessageBase;

import java.util.Collection;

/**
 * 重复规则校验
 *
 * Created by liulu on 2020/11/11.
 */
public class EquivalentRuleVerifier extends AbstractRuleVerifier {
    @Override
    protected void doVerifier(Verifier verifier) {
        VerifierReport result = verifier.getResult();
        Collection<VerifierMessageBase> warnings = result.getBySeverity(Severity.WARNING);
        StringBuilder buffer = new StringBuilder();
        for (VerifierMessageBase message : warnings) {
            if (MessageType.EQUIVALANCE.equals(message.getMessageType())) {
                Collection<String> ruleNames = message.getImpactedRules().values();
                if (!ruleNames.isEmpty()) {
                    buffer.append("[");
                    for (String ruleName : ruleNames) {
                        ruleName = cleanRuleName(ruleName);
                        buffer.append(ruleName).append(", ");
                    }
                    buffer.delete(buffer.length() - 2, buffer.length());
                    buffer.append("], ");
                }
            }
        }
        if (buffer.length() > 0) {
            addResult(null, buffer.substring(0, buffer.length() - 2));
        }
    }

    @Override
    protected VerifierType getVerifierType() {
        return VerifierType.EQUIVALENT;
    }
}
