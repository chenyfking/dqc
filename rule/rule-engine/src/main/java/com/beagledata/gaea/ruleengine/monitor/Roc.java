package com.beagledata.gaea.ruleengine.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Roc曲线上点的坐标
 * Created by Cyf on 2019/7/4
 **/
public class Roc implements Comparable<Roc>{
    private Double fpr; //假正率  ，横坐标
    private Double tpr; //真正率  ，纵坐标

    public Roc(){}

    public Roc(double fpr, double tpr) {
        this.fpr = fpr;
        this.tpr = tpr;
    }

    public Double getTpr() {
        return tpr;
    }

    public void setTpr(double tpr) {
        this.tpr = tpr;
    }

    public Double getFpr() {
        return fpr;
    }

    public void setFpr(double fpr) {
        this.fpr = fpr;
    }

    @Override
    public int compareTo(Roc o) {
        if (null == o ) {   //升序
            return -1;
        }
        return ((this.fpr - o.fpr) > 0) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "Roc{" +
                "fpr=" + fpr +
                ", tpr=" + tpr +
                '}';
    }

    public static void main(String[] args) {
        List<Roc> list = new ArrayList<>();
        list.add(new Roc(0.9, 0.0));
        list.add(new Roc(0.2, 0.0));
        list.add(new Roc(0.4, 0.0));
        list.add(new Roc(0.5, 0.0));
        list.add(new Roc(0.3, 0.0));
        list.add(new Roc(0.6, 0.0));
        list.add(new Roc(0.8, 0.0));

        Collections.sort(list);
        System.out.println(list);
    }
}