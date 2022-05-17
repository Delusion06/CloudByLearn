package com.ex.controller;

import com.ex.service.UserService;
import com.ex.vo.JsonModel;
import com.ex.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/isUnameValid.action", method = {RequestMethod.GET, RequestMethod.POST})
    public JsonModel isUnameValid(JsonModel jm, String uname) {
        String regExp = "^\\w{6,10}$";
        if (!uname.matches(regExp)) {
            jm.setCode(0);
            jm.setMsg("用户名必须为6-10位以上数字、字母、下划线组成...");
            return jm;
        }
        boolean flag = userService.isUnameValid(uname);
        if (flag) {
            jm.setCode(1);
        } else {
            jm.setCode(0);
            jm.setMsg("用户名重名");
        }
        return jm;
    }

    @RequestMapping(value = "/reg.action", method = {RequestMethod.POST})
    public JsonModel reg(JsonModel jm, UserVO userVo) {
        userVo = userService.insert(userVo);
        jm.setCode(1);
        return jm;
    }

    @RequestMapping(value = "/back/checkLogin", method = {RequestMethod.GET, RequestMethod.POST})
    public JsonModel checkLoginOp(JsonModel jm, HttpSession session) {
        if (session.getAttribute("uname") == null) {
            jm.setCode(0);
            jm.setMsg("该用户没有登录");
        } else {
            jm.setCode(1);
            String uname = (String) session.getAttribute("uname");
            jm.setObj(uname);
        }
        return jm;
    }
}
