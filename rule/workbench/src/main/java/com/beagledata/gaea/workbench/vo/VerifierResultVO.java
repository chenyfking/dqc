package com.beagledata.gaea.workbench.vo;

import com.beagledata.gaea.workbench.rule.verifer.VerifierType;

/**
 * 描述: 规则验证结果
 * @author 周庚新
 * @date 2020-07-09
 */
public class VerifierResultVO {
	/**
	 * 验证的规则名称
	 */
	private String ruleName;

	/**
	* 验证结果的类型
	*/
	private VerifierType veriferType;

	/**
	 * 验证结果的内容
	 */
	private String content;

	public VerifierResultVO() {
	}

	public VerifierResultVO(VerifierType veriferType) {
		this.veriferType = veriferType;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public VerifierType getVeriferType() {
		return veriferType;
	}

	public void setVeriferType(VerifierType veriferType) {
		this.veriferType = veriferType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("VerifierResultVO{");
		sb.append("ruleName='").append(ruleName).append('\'');
		sb.append(", veriferType=").append(veriferType);
		sb.append(", content='").append(content).append('\'');
		sb.append('}');
		return sb.toString();
	}
}