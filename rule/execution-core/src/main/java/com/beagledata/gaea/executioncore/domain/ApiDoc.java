package com.beagledata.gaea.executioncore.domain;

import java.io.Serializable;

/**
 * 服务调用文档
 *
 * Created by liulu on 2020/5/14.
 */
public class ApiDoc implements Serializable {
    private static final long serialVersionUID = 2116679493772238721L;

    /**
     * 请求示例数据
     */
    private Input input;
    /**
     * 返回示例数据
     */
    private Output output;

    public Input getInput() {
        return input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ApiDoc{");
        sb.append("input=").append(input);
        sb.append(", output=").append(output);
        sb.append('}');
        return sb.toString();
    }
}
