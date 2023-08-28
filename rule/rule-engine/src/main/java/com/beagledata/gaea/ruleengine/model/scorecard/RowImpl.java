package com.beagledata.gaea.ruleengine.model.scorecard;

import java.math.BigDecimal;

/**
 * Created by liulu on 2020/4/17.
 */
public class RowImpl implements Row {
    private int rowNumber;
    private Double score;
    private Double weight;
    private String reasonCode;
    private String reasonMsg;

    public RowImpl() {
    }

    public RowImpl(int rowNumber, Double score) {
        this.rowNumber = rowNumber;
        this.score = score;
    }
    public RowImpl(int rowNumber, Integer score) {
        this(rowNumber, score.doubleValue());
    }
    public RowImpl(int rowNumber, Long score) {
        this(rowNumber, score.doubleValue());
    }
    public RowImpl(int rowNumber, BigDecimal score) {
        this(rowNumber, score.doubleValue());
    }

    public RowImpl(int rowNumber, Double score, Double weight) {
        this(rowNumber, score);
        this.weight = weight;
    }

    public RowImpl(int rowNumber, Integer score, Double weight) {
        this(rowNumber, score.doubleValue(), weight);
    }

    public RowImpl(int rowNumber, Long score, Double weight) {
        this(rowNumber, score.doubleValue(), weight);
    }

    public RowImpl(int rowNumber, BigDecimal score, Double weight) {
        this(rowNumber, score.doubleValue(), weight);
    }

    public RowImpl(int rowNumber, Double score, String reasonCode, String reasonMsg) {
        this(rowNumber, score);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, Integer score, String reasonCode, String reasonMsg) {
        this(rowNumber, score);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, Long score, String reasonCode, String reasonMsg) {
        this(rowNumber, score);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, BigDecimal score, String reasonCode, String reasonMsg) {
        this(rowNumber, score);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, Double score, Double weight, String reasonCode, String reasonMsg) {
        this(rowNumber, score, weight);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, Integer score, Double weight, String reasonCode, String reasonMsg) {
        this(rowNumber, score, weight);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, Long score, Double weight, String reasonCode, String reasonMsg) {
        this(rowNumber, score, weight);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    public RowImpl(int rowNumber, BigDecimal score, Double weight, String reasonCode, String reasonMsg) {
        this(rowNumber, score, weight);
        this.reasonCode = reasonCode;
        this.reasonMsg = reasonMsg;
    }

    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    @Override
    public Double getScore() {
        return score;
    }

    @Override
    public Double getWeight() {
        return weight;
    }

    @Override
    public Double getActualScore() {
        double actualScore = getScore() != null ? getScore() : 0;
        return weight != null ? weight * actualScore : actualScore;
    }

    @Override
    public String getReasonCode() {
        return reasonCode;
    }

    @Override
    public String getReasonMsg() {
        return reasonMsg;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
