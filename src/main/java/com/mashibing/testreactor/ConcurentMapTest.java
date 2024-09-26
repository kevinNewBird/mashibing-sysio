package com.mashibing.testreactor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * description  ConcurentMapTest <BR>
 * <p>
 * author: zhao.song
 * date: created in 17:17  2021/7/22
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class ConcurentMapTest {

    static ConcurrentHashMap<Person, PersonPool> mapping = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        for (int i = 0; i < 20; i++) {
            new Thread(() -> handle(new Person("kevin"))).start();
        }

    }

    public static void handle(Person p1) {
        if (mapping.get(p1) == null) {
            mapping.putIfAbsent(p1, new PersonPool());
            System.out.println(mapping.get(p1).hashCode());
            return;
        }
        System.out.println(mapping.get(p1).hashCode());
    }
}
