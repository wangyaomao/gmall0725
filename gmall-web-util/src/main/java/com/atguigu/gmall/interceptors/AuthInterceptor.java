package com.atguigu.gmall.interceptors;

import com.atguigu.gmall.annotations.LoginRequire;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import com.atguigu.gmall.util.JwtUtil;
import javassist.util.proxy.MethodHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandle;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{

        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 判断当前请求方法的拦截类型
            HandlerMethod hm = (HandlerMethod)handler;
            LoginRequire methodAnnotation = hm.getMethodAnnotation(LoginRequire.class);

            if(null==methodAnnotation){
                // 不需要验证
                return true;
            }
            // 是否必须验证通过
            boolean neededSuccess = methodAnnotation.isNeededSuccess();

            // 获得用户的token
            String token = "";
            String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);
            String newToken = request.getParameter("newToken");

            if(StringUtils.isNotBlank(oldToken)){
                token = oldToken;
            }
            if(StringUtils.isNotBlank(newToken)){
                token = newToken;
            }


            if(StringUtils.isNotBlank(token)){
                // 验证token，http的工具
                // 通过负载均衡nginx
                String ip = request.getHeader("x-forwarded-for");
                if(StringUtils.isBlank(ip)){
                    ip = request.getRemoteAddr();
                    if(StringUtils.isBlank(ip)){
                        ip = "127.0.0.1";
                    }
                }
                String doGet = HttpClientUtil.doGet("http://passport.gmall.com:8085/verify?token="+token+"&currentIp="+ip);

                if(doGet.equals("success")){
                    // 刷新用户cookie中的token
                    CookieUtil.setCookie(request,response,"oldToken",token,60*30,true);
                    // 将用户id和昵称写入
                    Map gmall072566666 = JwtUtil.decode("gmall072566666", token, ip);
                    request.setAttribute("userId",gmall072566666.get("userId"));
                    request.setAttribute("nickName",gmall072566666.get("nickName"));
                    return true;
                }
            }

            // token为空或者验证不通过
            if(neededSuccess==true){
                String returnUrl = request.getRequestURL().toString();
                response.sendRedirect("http://passport.gmall.com:8085/index?returnUrl="+returnUrl);
                return false;
            }

            // token为空且不需要登陆
            return true;
        }
}
