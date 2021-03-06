package com.imooc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    public CorsConfig(){

    }

    @Bean
    public CorsFilter corsFilter(){
        //1.添加cors配置信息
        CorsConfiguration config = new CorsConfiguration();
        /*config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedOrigin("http://192.168.163.145:8088");
        config.addAllowedOrigin("http://192.168.163.145:8080");
        config.addAllowedOrigin("http://192.168.163.145");
        config.addAllowedOrigin("http://192.168.163.145");
        config.addAllowedOrigin("http://192.168.163.146:90");
        config.addAllowedOrigin("http://192.168.163.146");*/
        config.addAllowedOrigin("*");
        //设置是否发送session信息
        config.setAllowCredentials(true);

        //设置允许请求的方式
        config.addAllowedMethod("*");

        //设置允许的header
        config.addAllowedHeader("*");

        //2. 为url添加映射映射路径
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/**",config);

        //3.返回重新定义好的corsSource
        return new CorsFilter(corsSource);

    }



}
