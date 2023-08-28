package com.beagledata.gaea.ruleengine.function;

import com.beagledata.gaea.ruleengine.annotation.FunctionProperty;
import com.beagledata.gaea.ruleengine.annotation.FunctionMethodProperty;
import com.beagledata.gaea.ruleengine.exception.RuleException;

import java.math.BigDecimal;

/**
 * Created by liulu on 2018/10/12.
 */
@FunctionProperty(name = "数学函数")
public class MathFunction {

	@FunctionMethodProperty(name = "获取指数基础E")
	public Double getE() {
		return Math.E;
	}

	@FunctionMethodProperty(name = "获取圆周率PI")
	public Double getPI() {
		return Math.PI;
	}

    @FunctionMethodProperty(name = "求绝对值", params = {"数字"})
    public Double abs(Number obj) {
        return Math.abs(toBigDecimal(obj).doubleValue());
    }

    @FunctionMethodProperty(name = "求最大值", params = {"数字1", "数字2"})
	public Double max(Number obj1, Number obj2) {
		BigDecimal v1 = toBigDecimal(obj1);
		BigDecimal v2 = toBigDecimal(obj2);
		return Math.max(v1.doubleValue(), v2.doubleValue());
	}

	@FunctionMethodProperty(name = "求最小值", params = {"数字1", "数字2"})
	public Double min(Number obj1, Number obj2) {
		BigDecimal v1 = toBigDecimal(obj1);
		BigDecimal v2 = toBigDecimal(obj2);
		return Math.min(v1.doubleValue(), v2.doubleValue());
	}

	@FunctionMethodProperty(name = "求正弦", params = {"数字"})
	public Double sin(Number obj) {
		return Math.sin(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "求余弦", params = {"数字"})
	public Double cos(Number obj) {
		return Math.cos(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "求正切", params = {"数字"})
	public Double tan(Number obj) {
    	BigDecimal[] bigDecimals = toBigDecimal(obj).subtract(toBigDecimal(90)).divideAndRemainder(toBigDecimal(180));
    	if (bigDecimals[1].equals(new BigDecimal(0))) {
    	    throw new RuleException("角度"+ obj + "没有正切值");
    	}
		return Math.tan(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "求余切", params = {"数字"})
	public Double cot(Number obj) {
		BigDecimal[] bigDecimals = toBigDecimal(obj).subtract(toBigDecimal(90)).divideAndRemainder(toBigDecimal(180));
		if (bigDecimals[1].equals(new BigDecimal(0))) {
			return 0.0;
		}
		BigDecimal[] bigDecimals1 = toBigDecimal(obj).divideAndRemainder(toBigDecimal(180));
		if (bigDecimals1[1].equals(new BigDecimal(0))) {
			throw new RuleException("角度"+ obj + "没有余切值");
		}
		return 1 / Math.tan(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "求e为底的对数", params = {"数字"})
	public Double log(Number obj) {
    	if (toBigDecimal(obj).compareTo(new BigDecimal(0)) < 1) {
			throw new RuleException("求e为底的对数的输入值不能小于0");
    	}
		return Math.log(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "求10为底的对数", params = {"数字"})
	public Number log10(Number obj) {
		if (toBigDecimal(obj).compareTo(new BigDecimal(0)) < 1) {
			throw new RuleException("求10为底的对数的输入值不能小于0");
		}
		return Math.log10(toBigDecimal(obj).doubleValue());
	}

	@FunctionMethodProperty(name = "四舍五入", params = {"数字", "位数"})
	public Number round(Number obj, Integer num) {
		return toBigDecimal(obj).setScale(num, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 描述: 兼容旧版本
	 */
	public Number round(Number obj) {
		return toBigDecimal(obj).setScale(6, BigDecimal.ROUND_HALF_UP);
	}

	@FunctionMethodProperty(name = "向上取整", params = {"数字", "取整位数"})
	public Integer upIntger(Number obj, Integer num) {
		return toBigDecimal(obj).setScale(num, BigDecimal.ROUND_UP).intValue();
	}
	/**
	* 描述: 兼容旧版本
	*/
	public Integer upIntger(Number obj) {
		return toBigDecimal(obj).setScale(0, BigDecimal.ROUND_UP).intValue();
	}

	@FunctionMethodProperty(name = "向下取整", params = {"数字", "取整位数"})
	public Integer downIntger(Number obj, Integer num) {
		return toBigDecimal(obj).setScale(num, BigDecimal.ROUND_DOWN).intValue();
	}
	/**
	 * 描述: 兼容旧版本
	 */
	public Integer downIntger(Number obj) {
		return toBigDecimal(obj).setScale(0, BigDecimal.ROUND_DOWN).intValue();
	}
	@FunctionMethodProperty(name = "数字次幂", params = {"数字", "幂次"})
	public Double pow(Double num1, Double num2) {
		if (num1 == null || num2 == null) {
		    return 0D;
		}
		return Math.pow(num1, num2);
	}
	private BigDecimal toBigDecimal(Object val) {
		if (val == null) {
			return new BigDecimal(0);
		}

		if (val instanceof BigDecimal) {
			return (BigDecimal) val;
		} else if (val instanceof String) {
			String str = (String) val;
			str = str.trim();
			if ("".equals(str)) {
				return BigDecimal.valueOf(0);
			}
			return new BigDecimal(str);
		} else if (val instanceof Number) {
			return new BigDecimal(val.toString());
		} else if (val instanceof Character) {
			int i = ((Character) val).charValue();
			return new BigDecimal(i);
		}
		return new BigDecimal(0);
    }
}
