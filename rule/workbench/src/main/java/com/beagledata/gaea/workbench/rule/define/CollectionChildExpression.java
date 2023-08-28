package com.beagledata.gaea.workbench.rule.define;

import org.apache.commons.lang.StringUtils;

/**
 * Created by liulu on 2020/5/6.
 */
public class CollectionChildExpression extends FactExpression {
    @Override
    public String dump() {
        if (rule.getLoopTarget() == null) {
            return null;
        }
        String dump = rule.getLoopTarget().dump();
        if (dump == null) {
            return null;
        }

        Field loopField = rule.getLoopTarget().getLastField();
        if (loopField.getSubType().length() == 32) {
            if (!loopField.getSubType().equals(id)) {
                return null;
            }
            loadFact();
            if (fields.isEmpty()) {
                if (StringUtils.isNotBlank(fieldId)) {
                    return null;
                }
                return "$" + fact.getClassName() + "_loop";
            }
            return "$" + fact.getClassName() + "_loop." + getLinkNames();
        }
        return "this";
    }
}
