package com.ex.bean;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

@Data   //lombok注解
@AllArgsConstructor     //生成全参数构造方法
@NoArgsConstructor      //无参数构造方法
@ToString               //生成toString()
@JsonIgnoreProperties(ignoreUnknown = true)//生成json时是否忽略哪些字段
public class User {
    //主键
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uid;
    private String uname;
    private String upwd;
    private String role;
}
