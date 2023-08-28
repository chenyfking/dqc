package com.beagledata.gaea.ruleengine.util;

import com.beagledata.gaea.ruleengine.monitor.ModelResult;
import com.beagledata.gaea.ruleengine.monitor.Roc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 模型监测
 * Created by Cyf on 2020/4/13
 **/
public class MonitorUtils {
    public static final int sampleGroupNum = 10;    //计算roc坐标的分组数
    public static final int aucGroupNum = 100;      //计算auc, ks的分组数
    /**
     * 根据roc坐标计算AUC （微积分求ROC曲线下面部分的面积）
     * Created by Chenyafeng on 2019/7/8
     */
    public static double getAuc(List<ModelResult> results) {
        double result = 0.0;
        if (results == null || results.size() <= 0) {
            return result;
        }
        List<Roc> points = getRocCoordinates(results, aucGroupNum);
        if (points != null && points.size() > 0) {
            Collections.sort(points);   //升序排序，即按照x轴上横坐标大小排序
            Roc firstPoint = points.get(0);
            result += firstPoint.getFpr() * firstPoint.getTpr();
            for (int i = 1; i < points.size(); i++) {
                Roc point = points.get(i);
                //计算出该点前面点的横坐标(lastFpr),再用该点横坐标减去前点横坐标,得出该点在计算中x轴上有实际意义的距离(diffFpr)
                // 再用diffFpr乘以该店纵坐标得出该点的有效面积
                double lastFpr = points.get(i -1).getFpr();
                double diffFpr = point.getFpr() - lastFpr;
                result += diffFpr * point.getTpr();
            }
        }
        BigDecimal b_result = new BigDecimal(result);
        return b_result.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 根据roc坐标计算 ks 值
     * 求出每一个坐标点的 真正率与假正率的差值的绝对值，再返回这些值中的最大值
     * Created by Chenyafeng on 2019/7/8
     */
    public static double getKs(List<ModelResult> results) {
        double result = 0.0;
        if (results == null || results.size() <= 0) {
            return result;
        }
        List<Roc> points = getRocCoordinates(results, aucGroupNum);
        if (points != null && points.size() > 0) {
            for (Roc roc : points) {
                double tpr = roc.getTpr();
                double fpr = roc.getFpr();
                double diff = Math.abs(tpr - fpr);
                result = result >= diff ? result : diff;
            }
        }
        BigDecimal b_result = new BigDecimal(result);
        return b_result.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 根据模型预测结果计算ROC曲线坐标
     * Created by Chenyafeng on 2019/7/8
     */
    public static List<Roc> getRocCoordinates(List<ModelResult> results, int groupNum){
        if (results == null || results.size() <= 0) {
            return Collections.emptyList();
        }
        //所有数据降序排序
        Collections.sort(results);
        //将数据分组 ，组数 = sampleGroupNum
        int size = results.size();
        //数据数量较少，减少分组数量
        if (size <= groupNum) {
            groupNum = sampleGroupNum;
        }
        if (size <= sampleGroupNum) {
            groupNum = size;
        }

        int remaider = size % groupNum; // 计算出余数
        int number = size / groupNum; // 计算出商
        if (remaider > 0) { //整分后多余的数量单独分组
            groupNum += 1;
        }
        int[] groupIndex = new int[groupNum-1];
        for (int i = 1; i < groupNum; i++) {
            groupIndex[i-1] = i * number - 1;
//            if (i != groupNum) {
//                groupIndex[i-1] = i * number - 1;
//            } else {
//                groupIndex[i-1] = i * number - 1 + remaider;
//            }
        }

        List<Roc> list = new ArrayList<>(size);
        for (int i = 0; i < groupIndex.length; i++) {
            int index = groupIndex[i];
            ModelResult pointResult = results.get(index);
            double threshold = pointResult.getScore();
            double tp=0.0, fn=0.0, fp=0.0, tn=0.0;
//            int pNum = 0, nNum = 0;
            for (int j = 0; j < results.size(); j++) {
                ModelResult result = results.get(j);
                if (result.getScore() >= threshold) {   //results 已排序, 预测为正类
                    if (result.isPositive()) {  //实际为正类
                        tp += 1;
                    } else {    //实际为负类
                        fp += 1;
                    }
//                    pNum++;
                } else {    //预测为负类
                    if (result.isPositive()) {  //实际为正类
                        fn += 1;
                    } else {    //实际为负类
                        tn += 1;
                    }
//                    nNum++;
                }
            }
            double fpr =0, tpr = 0;
            //todo tp+fn 或 fp+tn 的值是0 理论上不可能存在
            if (fp > 0) {
                BigDecimal b_fp = new BigDecimal(fp);
                BigDecimal b_tn = new BigDecimal(tn);
                fpr = b_fp.divide(b_fp.add(b_tn), 3, BigDecimal.ROUND_HALF_UP).setScale(3).doubleValue();
            }
            if (tp > 0) {
                BigDecimal b_tp = new BigDecimal(tp);
                BigDecimal b_fn = new BigDecimal(fn);
                tpr = b_tp.divide(b_tp.add(b_fn), 3, BigDecimal.ROUND_HALF_UP).setScale(3).doubleValue();
            }
//            System.out.println(String.format("第%s组下标%s，阈值：%s, 真正数量：%s, 假正数量：%s, 假负数量：%s, 真负数量：%s, 坐标(%s,%s)，预测正类：%s, 预测负类：%s",
//                    i, index, threshold, tp, fp, fn, tn, fpr, tpr, pNum, nNum));
            list.add(new Roc(fpr, tpr));
        }
        return list;
    }

    public static double getPsi(List<ModelResult> results) {
        double result = 0.0;
        if (results == null || results.size() <= 0) {
            return result;
        }
        for (ModelResult modelResult : results) {

        }
        return result;
    }

    public static void main(String[] args) {
        List<ModelResult> results = new ArrayList<>(100);
        for (int i = 0; i < 2; i++) {
            ModelResult result = new ModelResult();
            result.setPositive("1");
            result.setNegative("0");
            int randomInt = (int) (1 + Math.random() * (10 - 1 + 1));
            if (randomInt % 2 == 0) {
                result.setY("0");
            } else {
                result.setY("1");
            }
            double randomDouble = new Random().nextDouble() * (1.0-0.0)+0.0;
            result.setScore(randomDouble);
            results.add(result);
        }
        List<Roc> rocCoordinates = getRocCoordinates(results, sampleGroupNum);
        System.out.println(rocCoordinates);

        System.out.println("ks = " + getKs(results));

        System.out.println("auc = " + getAuc(results));
    }
}