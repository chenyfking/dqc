package com.beagledata.gaea.workbench.rule.verifer;

import com.beagledata.gaea.workbench.vo.VerifierResultVO;

import java.util.List;

/**
 * Created by liulu on 2020/11/11.
 */
public interface RuleVerifier {
    /**
     * 规则校验
     *
     * @author liulu
     * 2020/11/11 9:27
     */
    List<VerifierResultVO> verifier(String ruleContent, VerifierType... types);
}
