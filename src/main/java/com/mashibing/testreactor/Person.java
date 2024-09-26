package com.mashibing.testreactor;

import java.io.Serializable;
import java.util.Objects;

/**
 * description  Person <BR>
 * <p>
 * author: zhao.song
 * date: created in 17:18  2021/7/22
 * company: TRS信息技术有限公司
 * version 1.0
 */
public class Person  {

    private String name;

    public Person(String name) {
        this.name = name;
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Person person = (Person) o;
//        return Objects.equals(name, person.name);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(name);
//    }
}
