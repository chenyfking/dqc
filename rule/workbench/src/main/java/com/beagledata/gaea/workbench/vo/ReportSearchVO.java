package com.beagledata.gaea.workbench.vo;

/**
 * Created by liulu on 2020/11/11.
 */
public class ReportSearchVO extends BaseSearchVO {
    /**
     * 起始日期
     */
    private String startDate;
    /**
     * 截止日期
     */
    private String endDate;
    /**
     * 搜索关键词
     */
    private String keywords;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ReportSearchVO{");
        sb.append("startDate='").append(startDate).append('\'');
        sb.append(", endDate='").append(endDate).append('\'');
        sb.append(", keywords='").append(keywords).append('\'');
        sb.append('}');
        sb.append(" ").append(super.toString());
        return sb.toString();
    }
}
