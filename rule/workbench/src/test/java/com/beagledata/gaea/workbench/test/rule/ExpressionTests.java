package com.beagledata.gaea.workbench.test.rule;

import com.alibaba.fastjson.JSONObject;
import com.beagledata.gaea.workbench.TestApplication;
import com.beagledata.gaea.workbench.rule.define.Expression;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Cyf on 2020/6/8
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class ExpressionTests {

    @Test
    public void json2Expression() {
        String text = "{\"type\":\"INPUT\",\"input\":{\"value\":2020,\"type\":\"Integer\"},\"arith\":{\"type\":\"SUB\",\"right\":{\"type\":\"FACT\",\"fact\":{\"id\":\"92a0d775ed504e6985fd818104448eae\",\"fieldId\":\"E4X5Jc\"}}}}";
        Expression expression = JSONObject.parseObject(text, Expression.class);
        System.out.println(expression);
        System.out.println(expression.dump());
    }
}