package com.ex.service;

import com.ex.bean.User;
import com.ex.dao.UserDao;
import com.ex.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@Transactional      //事务处理（添加事件）
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserDao userDao;

    //spring security提供的一个秘密加密码的类
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Override
    public UserVO insert(UserVO user) {
        User u = new User();
        u.setUname(user.getUname());
        u.setUpwd(passwordEncoder.encode(user.getUpwd()));
        u.setRole("ROLE_ADMIN");
        u = userDao.save(u);
        user.setUid(u.getUid());
        return user;
    }

    @Override
    public boolean isUnameValid(String uname) {
        User u = new User();
        u.setUname(uname);
        //Example条件对象
        Example<User> example = Example.of(u);
        Optional<User> optional = userDao.findOne(example);
        u = optional.orElseGet(new Supplier<User>() {
            @Override
            public User get() {
                return null;
            }
        });
        if (u == null) {
            return true;        //查不到用户名，说明可以用
        }
        return false;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        User user = userDao.findByuname(name);
        if (user == null) {
            return null;
        } else {
            //创建一个权限的集合
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            //添加获取权限
            authorities.add(new SimpleGrantedAuthority(user.getRole()));
            //把对象信息（用户名，密码，权限）存入对象，返回该对象，controller层直接调用
            //如果数据库未加密需要添加以下注释的两行代码
            // org.springframework.security.core.userdetails.User user2 =new org.springframework.security.core.userdetails.User(user.getUsername(), passwordEncoder.encode(user.getPwd()), authorities);
            org.springframework.security.core.userdetails.User user2 = new org.springframework.security.core.userdetails.User(user.getUname(), user.getUpwd(), authorities);
            // System.out.println("管理员信息："+user.getUsername()+"   "+passwordEncoder.encode(user.getPwd())+"  "+user2.getAuthorities());
            return user2;
        }

    }
}
