package com.beagledata.gaea.workbench.rule.verifer;

import org.drools.verifier.Verifier;

/**
 * 规则不一致校验
 *
 * Created by liulu on 2020/11/11.
 */
public class IncoherentRuleVerifier extends AbstractRuleVerifier {
    @Override
    protected void doVerifier(Verifier verifier) {
    }

    @Override
    protected VerifierType getVerifierType() {
        return VerifierType.INCOHERENT;
    }
}
