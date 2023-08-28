package com.beagledata.gaea.examples;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

/**
 * Created by liulu on 2018/11/5.
 */
public class HuJinExample {
    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kc = ks.getKieClasspathContainer();
        KieSession session = kc.newKieSession("HuJinKS");

        session.startProcess("flow2");

        session.dispose();
    }
}
