package com.jdbc.test;

import java.math.BigDecimal;
import java.util.Date;

import com.jdbc.test.mybatis.Sensitive;
import com.jdbc.test.mybatis.SensitiveStrategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author xuedui.zhao
 * @create 2018-06-04
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    private int id;
    @Sensitive(strategy = SensitiveStrategy.USERNAME)
    private String name;
    private int age;
    private boolean deleteFlag;
    private SexEnum sex;
    private BigDecimal price;
    private Date createtime;

}
