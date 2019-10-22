package com.atguigu.gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();

        // 私人信息
/*        map.put("userId","2");
        map.put("nickName","jerry");
        // 盐值
        String ip = "127.0.0.1";
        String gmall072566666 = encode("gmall072566666", map, ip);

        System.out.println(gmall072566666);*/

        String token = "eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6ImplcnJ5IiwidXNlcklkIjoiMiJ9.7SBwB5dk3p_uwC6nkSx9LL4UhmWQYuielf1V80aS39M";

        Map gmall072566666 = decode("gmall072566666", token, "127.0.0.1");

        System.out.println(gmall072566666.toString());

    }


    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key, Map map, String salt) {

        if (salt != null) {
            key += salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static Map decode(String key, String token, String salt) throws SignatureException {
        if (salt != null) {
            key += salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        System.out.println("map.toString() = " + map.toString());

        return map;

    }

}
