package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 互金机构
 *
 * Created by liulu on 2018/11/5.
 */
public class PreMultiDict implements Serializable {
    private static final long serialVersionUID = -1758649675523126857L;

    /**
     * 近24个月借款人申请平台次数
     */
    private Integer applyPlatformTimesIn24Month;
    /**
     * 近12个月贷款被拒次数
     */
    private Integer loanRejectedTimesIn12Month;
    /**
     * 有无互金机构贷款行为
     */
    private Boolean mutualGoldLending;
    /**
     * 近12个月内已借款机构数
     */
    private Integer loanUnitNum12Months;
    /**
     * 近6个月新增借贷机构数
     */
    private Integer newLoanUnitNum6Months;
    /**
     * 借款人贷款逾期180天次数
     */
    private Integer hitOverdue180ListTimes;
    /**
     * 借款人贷款逾期90天次数
     */
    private Integer hitOverdue90ListTimes;
    /**
     * 借款人贷款逾期30天次数
     */
    private Integer hitOverdue30ListTimes;
    /**
     * 借款人1年内被查询次数
     */
    private Integer hitSelectedTimesInOneyear;
    /**
     * 借款人1月内被查询次数
     */
    private Integer hitSelectedTimesInOneMonth;
    /**
     * 借款人1周内被查询次数
     */
    private Integer hitSelectedTimesInaweek;

    public Integer getApplyPlatformTimesIn24Month() {
        return applyPlatformTimesIn24Month;
    }

    public void setApplyPlatformTimesIn24Month(Integer applyPlatformTimesIn24Month) {
        this.applyPlatformTimesIn24Month = applyPlatformTimesIn24Month;
    }

    public Integer getLoanRejectedTimesIn12Month() {
        return loanRejectedTimesIn12Month;
    }

    public void setLoanRejectedTimesIn12Month(Integer loanRejectedTimesIn12Month) {
        this.loanRejectedTimesIn12Month = loanRejectedTimesIn12Month;
    }

    public Boolean getMutualGoldLending() {
        return mutualGoldLending;
    }

    public void setMutualGoldLending(Boolean mutualGoldLending) {
        this.mutualGoldLending = mutualGoldLending;
    }

    public Integer getLoanUnitNum12Months() {
        return loanUnitNum12Months;
    }

    public void setLoanUnitNum12Months(Integer loanUnitNum12Months) {
        this.loanUnitNum12Months = loanUnitNum12Months;
    }

    public Integer getNewLoanUnitNum6Months() {
        return newLoanUnitNum6Months;
    }

    public void setNewLoanUnitNum6Months(Integer newLoanUnitNum6Months) {
        this.newLoanUnitNum6Months = newLoanUnitNum6Months;
    }

    public Integer getHitOverdue180ListTimes() {
        return hitOverdue180ListTimes;
    }

    public void setHitOverdue180ListTimes(Integer hitOverdue180ListTimes) {
        this.hitOverdue180ListTimes = hitOverdue180ListTimes;
    }

    public Integer getHitOverdue90ListTimes() {
        return hitOverdue90ListTimes;
    }

    public void setHitOverdue90ListTimes(Integer hitOverdue90ListTimes) {
        this.hitOverdue90ListTimes = hitOverdue90ListTimes;
    }

    public Integer getHitOverdue30ListTimes() {
        return hitOverdue30ListTimes;
    }

    public void setHitOverdue30ListTimes(Integer hitOverdue30ListTimes) {
        this.hitOverdue30ListTimes = hitOverdue30ListTimes;
    }

    public Integer getHitSelectedTimesInOneyear() {
        return hitSelectedTimesInOneyear;
    }

    public void setHitSelectedTimesInOneyear(Integer hitSelectedTimesInOneyear) {
        this.hitSelectedTimesInOneyear = hitSelectedTimesInOneyear;
    }

    public Integer getHitSelectedTimesInOneMonth() {
        return hitSelectedTimesInOneMonth;
    }

    public void setHitSelectedTimesInOneMonth(Integer hitSelectedTimesInOneMonth) {
        this.hitSelectedTimesInOneMonth = hitSelectedTimesInOneMonth;
    }

    public Integer getHitSelectedTimesInaweek() {
        return hitSelectedTimesInaweek;
    }

    public void setHitSelectedTimesInaweek(Integer hitSelectedTimesInaweek) {
        this.hitSelectedTimesInaweek = hitSelectedTimesInaweek;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreMultiDict{");
        sb.append("applyPlatformTimesIn24Month='").append(applyPlatformTimesIn24Month).append('\'');
        sb.append(", loanRejectedTimesIn12Month='").append(loanRejectedTimesIn12Month).append('\'');
        sb.append(", mutualGoldLending='").append(mutualGoldLending).append('\'');
        sb.append(", loanUnitNum12Months='").append(loanUnitNum12Months).append('\'');
        sb.append(", newLoanUnitNum6Months='").append(newLoanUnitNum6Months).append('\'');
        sb.append(", hitOverdue180ListTimes='").append(hitOverdue180ListTimes).append('\'');
        sb.append(", hitOverdue90ListTimes='").append(hitOverdue90ListTimes).append('\'');
        sb.append(", hitOverdue30ListTimes='").append(hitOverdue30ListTimes).append('\'');
        sb.append(", hitSelectedTimesInOneyear='").append(hitSelectedTimesInOneyear).append('\'');
        sb.append(", hitSelectedTimesInOneMonth='").append(hitSelectedTimesInOneMonth).append('\'');
        sb.append(", hitSelectedTimesInaweek='").append(hitSelectedTimesInaweek).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
