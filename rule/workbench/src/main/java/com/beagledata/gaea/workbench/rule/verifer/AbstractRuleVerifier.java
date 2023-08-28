package com.beagledata.gaea.workbench.rule.verifer;

import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.beagledata.gaea.workbench.vo.VerifierResultVO;
import com.beagledata.util.StringUtils;
import org.drools.verifier.Verifier;
import org.drools.verifier.components.Field;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liulu on 2020/11/11.
 */
public abstract class AbstractRuleVerifier {
    protected List<VerifierResultVO> results = new ArrayList<>();

    public List<VerifierResultVO> verifier(Verifier verifier) {
        doVerifier(verifier);
        return results;
    }

    protected void addResult(String ruleName, String content) {
        VerifierResultVO resultVO = new VerifierResultVO();
        resultVO.setVeriferType(getVerifierType());
        resultVO.setRuleName(ruleName);
        resultVO.setContent(content);
        results.add(resultVO);
    }

    protected String getFieldLabel(Field field) {
        String factName = field.getObjectTypeName();
        String fieldName = field.getName();
        if (fieldName.startsWith("$")) {
            int dotIndex = fieldName.indexOf('.');
            factName = fieldName.substring(1, dotIndex);
            fieldName = fieldName.substring(dotIndex + 1);
        }

        String factId = factName.substring(5);
        Fact fact = AssetsCache.getFactById(factId);
        if (fact == null) {
            return fieldName;
        }

        List<String> names = new ArrayList<>();
        names.add(fact.getName());
        for (String name : fieldName.split("\\.")) {
            com.beagledata.gaea.workbench.rule.define.Field factField = fact.getFieldByName(name);
            if (factField == null) {
                return fieldName;
            }

            if (factField.isObjectType()) {
                fact = AssetsCache.getFactById(factField.getSubType());
                if (fact == null) {
                    return fieldName;
                }
            }
            names.add(factField.getLabel());
        }
        return StringUtils.join(names, ".");
    }

    protected String cleanRuleName(String ruleName) {
        int index = ruleName.indexOf('-', ruleName.indexOf('-') + 1);
        if (index != -1) {
            ruleName = ruleName.substring(index + 1);
        }
        return ruleName;
    }

    protected abstract void doVerifier(Verifier verifier);

    protected abstract VerifierType getVerifierType();
}
