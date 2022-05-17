package com.ex.filter;

import com.ex.auth.DemoAuthenticationFailureHandler;
import com.ex.exception.ValidateCodeException;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ValidateCodeFilter extends OncePerRequestFilter {

    //失败处理器
    private DemoAuthenticationFailureHandler demoAuthenticationFailureHandler;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        if ("/doLogin.action".equals(httpServletRequest.getRequestURI()) && "post".equalsIgnoreCase(httpServletRequest.getMethod())) {
            try {
                validate(httpServletRequest);
            } catch (ValidateCodeException e) {
                demoAuthenticationFailureHandler.onAuthenticationFailure(httpServletRequest, httpServletResponse, e);
                return;
            }
        }
        //过滤器链，向后执行，就是登陆验证
        filterChain.doFilter(httpServletRequest, httpServletResponse);//过滤器向后执行
    }

    //具体的验证流程
    private void validate(HttpServletRequest request) throws ValidateCodeException {
        //取出session的验证码进行判断
        String validateCode = (String) request.getSession().getAttribute("validateCode");
        //取出客户端输入的验证码
        String imageCode = request.getParameter("imageCode");
        if (imageCode == null || "".equalsIgnoreCase(imageCode)) {
            throw new ValidateCodeException("验证码的值不能为空");
        }
        if (!validateCode.equalsIgnoreCase(imageCode)) {
            throw new ValidateCodeException("验证码不匹配");
        }

    }


    public DemoAuthenticationFailureHandler getDemoAuthenticationFailureHandler() {
        return demoAuthenticationFailureHandler;
    }

    public void setDemoAuthenticationFailureHandler(DemoAuthenticationFailureHandler demoAuthenticationFailureHandler) {
        this.demoAuthenticationFailureHandler = demoAuthenticationFailureHandler;
    }
}
