package com.ex.service;


import com.ex.vo.UserVO;

public interface UserService {
    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    public UserVO insert(UserVO user);

    /**
     * 判断用户名是否有效
     *
     * @param uname
     * @return
     */
    public boolean isUnameValid(String uname);
}
