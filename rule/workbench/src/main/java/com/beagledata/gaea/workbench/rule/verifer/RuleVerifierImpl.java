package com.beagledata.gaea.workbench.rule.verifer;

import com.beagledata.gaea.workbench.vo.VerifierResultVO;
import org.drools.verifier.Verifier;
import org.drools.verifier.builder.VerifierBuilder;
import org.drools.verifier.builder.VerifierBuilderFactory;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by liulu on 2020/11/11.
 */
public class RuleVerifierImpl implements RuleVerifier {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        System.setProperty("drools.schema.validating", "false");
    }

    @Override
    public List<VerifierResultVO> verifier(String ruleContent, VerifierType... types) {
        if (types == null || types.length < 1) {
            return Collections.emptyList();
        }

        List<VerifierResultVO> results = new ArrayList<>();
        VerifierBuilder vBuilder = VerifierBuilderFactory.newVerifierBuilder();
        Verifier verifier =vBuilder.newVerifier();
        try {
            Resource resource = ResourceFactory.newReaderResource(new StringReader(ruleContent));
            verifier.addResourcesToVerify(resource, ResourceType.DRL);
            verifier.fireAnalysis();
            for (VerifierType type : types) {
                results.addAll(RuleVerifierFactory.get(type).verifier(verifier));
            }
        } catch (Exception e) {
            logger.error("执行规则校验失败. rule: {}", ruleContent, e);
        } finally {
            verifier.dispose();
        }
        return results;
    }
}
