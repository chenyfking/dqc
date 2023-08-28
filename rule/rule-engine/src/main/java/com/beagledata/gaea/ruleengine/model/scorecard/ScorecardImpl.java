package com.beagledata.gaea.ruleengine.model.scorecard;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by liulu on 2020/4/17.
 */
public class ScorecardImpl implements Scorecard {
    private String name;
    private Double defaultScore;
    private List<Row> rows = new ArrayList<>();

    public ScorecardImpl() {
    }

    public ScorecardImpl(String name) {
        this.name = name;
    }

    public ScorecardImpl(String name, Double defaultScore) {
        this.name = name;
        this.defaultScore = defaultScore;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Double getDefaultScore() {
        return defaultScore;
    }

    @Override
    public List<Row> getRows() {
        return rows;
    }

    public void addRow(Row row) {
        this.rows.add(row);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public String getReasonCodes() {
        if (rows.isEmpty()) {
            return StringUtils.EMPTY;
        }
        List<String> codes = new ArrayList<>(rows.size());
        rows.forEach(row -> {
            if (StringUtils.isNotBlank(row.getReasonCode())) {
                codes.add(row.getReasonCode());
            }
        });

        if (codes.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return codes.stream().collect(Collectors.joining(","));
    }

    @Override
    public String getReasonMsgs() {
        if (rows.isEmpty()) {
            return StringUtils.EMPTY;
        }
        List<String> msgs = new ArrayList<>(rows.size());
        rows.forEach(row -> {
            if (StringUtils.isNotBlank(row.getReasonMsg())) {
                msgs.add(row.getReasonMsg());
            }
        });

        if (msgs.isEmpty()) {
            return StringUtils.EMPTY;
        }

        return msgs.stream().collect(Collectors.joining(","));
    }

}
