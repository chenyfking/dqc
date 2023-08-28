package com.beagledata.gaea.executioncore.domain;

import com.beagledata.gaea.ruleengine.runtime.RuleEngine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 接口输出
 *
 * Created by liulu on 2020/5/11.
 */
public class Output implements Serializable {
    private static final long serialVersionUID = 796541932188447301L;

    /**
    * 流水号
    */
    private String seqNo;
    /**
     * 返回的数据对象
     */
    @JsonProperty("result")
    private Object response;
    /**
     * 每次执行的规则引擎实例
     */
    @JsonIgnore
    private RuleEngine ruleEngine;

    public Output() {}

    public Output(String seqNo) {
        this.seqNo = seqNo;
    }
    public Output(Object response) {
        this.response = response;
    }
    public Output(String seqNo, Object response) {
        this.seqNo = seqNo;
        this.response = response;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public RuleEngine getRuleEngine() {
        return ruleEngine;
    }

    public void setRuleEngine(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Output{");
        sb.append("seqNo=").append(seqNo);
        sb.append("response=").append(response);
        sb.append('}');
        return sb.toString();
    }
}
