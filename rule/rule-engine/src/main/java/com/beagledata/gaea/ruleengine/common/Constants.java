package com.beagledata.gaea.ruleengine.common;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by liulu on 2019/1/4.
 */
public class Constants {
    public static final String DEFAULT_DROOLS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat DEFAULT_GREENWICH_FORMAT = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
    public static final SimpleDateFormat DATE_LINE_FORMAT = new SimpleDateFormat(DEFAULT_DROOLS_DATE_FORMAT);
    /**
     * 数据模型字段为java关键字时 加固定后缀, AiModelHandler调用模型时去除
     */
    public static final String FIELD_SUFFIX = "_gaeakey";
    /**
     * 数据模型字段包含 - 时 固定字符串替换, AiModelHandler调用模型时还原
     */
    public static final String FIELD_LINE = "_gaealine_";

    /**
    * 描述: 小数点后保留位数
    */
    public static int BIGDECIMAL_SCALE = 6;
    /**
     *  小数舍入模式
     */
    public static int BIGDECIMAL_ROUND = BigDecimal.ROUND_HALF_UP;


}
