package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 评分卡（无权重）
 *
 * Created by liulu on 2018/11/5.
 */
public class Score1 implements Serializable {
    private static final long serialVersionUID = -9066133210846024742L;

    /**
     * 年收入
     */
    private Long annual_earnings;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 银行账户个数
     */
    private Integer bank_account_number;
    /**
     * 住房（形式）
     */
    private String house;
    /**
     * 入职时长（年）
     */
    private Double length_of_entry;
    /**
     * 已持有信用卡数量
     */
    private Integer the_number_of_cards;
    /**
     * 失信情况（次数）
     */
    private Integer break_faith;

    public Long getAnnual_earnings() {
        return annual_earnings;
    }

    public void setAnnual_earnings(Long annual_earnings) {
        this.annual_earnings = annual_earnings;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getBank_account_number() {
        return bank_account_number;
    }

    public void setBank_account_number(Integer bank_account_number) {
        this.bank_account_number = bank_account_number;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public Double getLength_of_entry() {
        return length_of_entry;
    }

    public void setLength_of_entry(Double length_of_entry) {
        this.length_of_entry = length_of_entry;
    }

    public Integer getThe_number_of_cards() {
        return the_number_of_cards;
    }

    public void setThe_number_of_cards(Integer the_number_of_cards) {
        this.the_number_of_cards = the_number_of_cards;
    }

    public Integer getBreak_faith() {
        return break_faith;
    }

    public void setBreak_faith(Integer break_faith) {
        this.break_faith = break_faith;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Score1{");
        sb.append("annual_earnings='").append(annual_earnings).append('\'');
        sb.append(", age='").append(age).append('\'');
        sb.append(", bank_account_number='").append(bank_account_number).append('\'');
        sb.append(", house='").append(house).append('\'');
        sb.append(", length_of_entry='").append(length_of_entry).append('\'');
        sb.append(", the_number_of_cards='").append(the_number_of_cards).append('\'');
        sb.append(", break_faith='").append(break_faith).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
