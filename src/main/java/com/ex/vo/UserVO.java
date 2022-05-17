package com.ex.vo;

import lombok.Data;

@Data
public class UserVO {

    private Integer uid;
    private String uname;
    private String upwd;
    private String role;

    private String imageCode;       //界面上才有   验证码
}
