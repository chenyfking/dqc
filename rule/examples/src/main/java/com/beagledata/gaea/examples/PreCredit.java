package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 信用历史
 *
 * Created by liulu on 2018/11/5.
 */
public class PreCredit implements Serializable {
    private static final long serialVersionUID = 9021096227329497301L;

    /**
     * 借款人身份证命中贷款逾期180天名单次数
     */
    private Integer idCardHitOverdue180ListTimes;
    /**
     * 借款人身份证命中贷款逾期30天名单次数
     */
    private Integer idCardHitOverdue30ListTimes;
    /**
     * 借款人身份证命中贷款逾期90天名单次数
     */
    private Integer idCardHitOverdue90ListTimes;
    /**
     * 借款人身份证两年内命中信贷逾期名单次数
     */
    private Integer idCardHitOverdueInTwoYearsTimes;
    /**
     * 借款人身份证1月内命中被查询名单次数
     */
    private Integer idCardHitSelectedTimesInOneMonth;
    /**
     * 借款人身份证1周内有命中被查询名单次数
     */
    private Integer idCardHitSelectedTimesInOneWeek;
    /**
     * 借款人身份证1年内命中被查询名单次数
     */
    private Integer idCardHitSelectedTimesInOneYear;
    /**
     * 借款人身份证命中信贷逾期后还款名单
     */
    private String idCardOverdueRepaymentList;
    /**
     * 借款人手机号命中助学贷款逾期名单
     */
    private String phoneHelpStudyOverdueList;
    /**
     * 借款人手机号两年内命中信贷逾期名单
     */
    private String phoneOverdueListInTwoYears;
    /**
     * 借款人手机号命中信贷逾期后还款名单
     */
    private String phoneOverdueRepaymentList;

    public Integer getIdCardHitOverdue180ListTimes() {
        return idCardHitOverdue180ListTimes;
    }

    public void setIdCardHitOverdue180ListTimes(Integer idCardHitOverdue180ListTimes) {
        this.idCardHitOverdue180ListTimes = idCardHitOverdue180ListTimes;
    }

    public Integer getIdCardHitOverdue30ListTimes() {
        return idCardHitOverdue30ListTimes;
    }

    public void setIdCardHitOverdue30ListTimes(Integer idCardHitOverdue30ListTimes) {
        this.idCardHitOverdue30ListTimes = idCardHitOverdue30ListTimes;
    }

    public Integer getIdCardHitOverdue90ListTimes() {
        return idCardHitOverdue90ListTimes;
    }

    public void setIdCardHitOverdue90ListTimes(Integer idCardHitOverdue90ListTimes) {
        this.idCardHitOverdue90ListTimes = idCardHitOverdue90ListTimes;
    }

    public Integer getIdCardHitOverdueInTwoYearsTimes() {
        return idCardHitOverdueInTwoYearsTimes;
    }

    public void setIdCardHitOverdueInTwoYearsTimes(Integer idCardHitOverdueInTwoYearsTimes) {
        this.idCardHitOverdueInTwoYearsTimes = idCardHitOverdueInTwoYearsTimes;
    }

    public Integer getIdCardHitSelectedTimesInOneMonth() {
        return idCardHitSelectedTimesInOneMonth;
    }

    public void setIdCardHitSelectedTimesInOneMonth(Integer idCardHitSelectedTimesInOneMonth) {
        this.idCardHitSelectedTimesInOneMonth = idCardHitSelectedTimesInOneMonth;
    }

    public Integer getIdCardHitSelectedTimesInOneWeek() {
        return idCardHitSelectedTimesInOneWeek;
    }

    public void setIdCardHitSelectedTimesInOneWeek(Integer idCardHitSelectedTimesInOneWeek) {
        this.idCardHitSelectedTimesInOneWeek = idCardHitSelectedTimesInOneWeek;
    }

    public Integer getIdCardHitSelectedTimesInOneYear() {
        return idCardHitSelectedTimesInOneYear;
    }

    public void setIdCardHitSelectedTimesInOneYear(Integer idCardHitSelectedTimesInOneYear) {
        this.idCardHitSelectedTimesInOneYear = idCardHitSelectedTimesInOneYear;
    }

    public String getIdCardOverdueRepaymentList() {
        return idCardOverdueRepaymentList;
    }

    public void setIdCardOverdueRepaymentList(String idCardOverdueRepaymentList) {
        this.idCardOverdueRepaymentList = idCardOverdueRepaymentList;
    }

    public String getPhoneHelpStudyOverdueList() {
        return phoneHelpStudyOverdueList;
    }

    public void setPhoneHelpStudyOverdueList(String phoneHelpStudyOverdueList) {
        this.phoneHelpStudyOverdueList = phoneHelpStudyOverdueList;
    }

    public String getPhoneOverdueListInTwoYears() {
        return phoneOverdueListInTwoYears;
    }

    public void setPhoneOverdueListInTwoYears(String phoneOverdueListInTwoYears) {
        this.phoneOverdueListInTwoYears = phoneOverdueListInTwoYears;
    }

    public String getPhoneOverdueRepaymentList() {
        return phoneOverdueRepaymentList;
    }

    public void setPhoneOverdueRepaymentList(String phoneOverdueRepaymentList) {
        this.phoneOverdueRepaymentList = phoneOverdueRepaymentList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreCredit{");
        sb.append("idCardHitOverdue180ListTimes='").append(idCardHitOverdue180ListTimes).append('\'');
        sb.append(", idCardHitOverdue30ListTimes='").append(idCardHitOverdue30ListTimes).append('\'');
        sb.append(", idCardHitOverdue90ListTimes='").append(idCardHitOverdue90ListTimes).append('\'');
        sb.append(", idCardHitOverdueInTwoYearsTimes='").append(idCardHitOverdueInTwoYearsTimes).append('\'');
        sb.append(", idCardHitSelectedTimesInOneMonth='").append(idCardHitSelectedTimesInOneMonth).append('\'');
        sb.append(", idCardHitSelectedTimesInOneWeek='").append(idCardHitSelectedTimesInOneWeek).append('\'');
        sb.append(", idCardHitSelectedTimesInOneYear='").append(idCardHitSelectedTimesInOneYear).append('\'');
        sb.append(", idCardOverdueRepaymentList='").append(idCardOverdueRepaymentList).append('\'');
        sb.append(", phoneHelpStudyOverdueList='").append(phoneHelpStudyOverdueList).append('\'');
        sb.append(", phoneOverdueListInTwoYears='").append(phoneOverdueListInTwoYears).append('\'');
        sb.append(", phoneOverdueRepaymentList='").append(phoneOverdueRepaymentList).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
