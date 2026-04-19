package com.example.ai_app_java.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.StreamSupport;

@Component // 交给Spring处理才能读取properties
public class JwtUtils {
    @Value("${jwt.secret}")
    private String secretString;
    @Value("${jwt.expiration}")
    private Long expiration;
    /*
        根据配置的字符串生成安全的密钥对象
     */
    private SecretKey getSecretKey() {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    /*
    1.生成token
     */
    public String createToken(Long userId, String username,String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .setHeaderParam("typ","JWT")
                .setSubject("user_auth")    //主题
                .setIssuedAt(now)           //签发时间
                .setExpiration(expiryDate)  //过期时间
                .claim("userId",userId)  //自定义载荷：存用户ID
                .claim("username",username)//自定义载荷：存用户名
                .claim("role",role)//自定义载荷：存用户角色
                .signWith(getSecretKey(),SignatureAlgorithm.HS256)//签名算法
                .compact();
    }
    /*
    2.解析Token并获取载荷
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    /*
    3.校验Token是否合法、是否过期
     */
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return true; //解析成功，说明没被篡改且没过期
        }catch (ExpiredJwtException e){
            System.err.println("【JWT】Token已过期: "+e.getMessage());
        }catch (UnsupportedJwtException e){
            System.err.println("【JWT】不支持的 Token: " + e.getMessage());
        }catch (MalformedJwtException e){
            System.err.println("【JWT】Token格式错误 : "+e.getMessage());
        } catch (SignatureException e) {
            System.err.println("【JWT】Token签名无效(可能被篡改): " + e.getMessage());
        }catch (IllegalArgumentException e){
            System.err.println("【JWT】Token为空：: "+e.getMessage());
        }
        return false;
    }
}
