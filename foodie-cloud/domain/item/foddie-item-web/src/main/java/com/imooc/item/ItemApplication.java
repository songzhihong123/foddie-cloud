package com.imooc.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描 mybatis 通用mapper所在的包
@MapperScan(basePackages = "com.imooc.item.mapper")
//@EnableTransactionManagement
//扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.imooc","org.n3r.idworker"})
@EnableDiscoveryClient
// TODO  fegin组件包
public class ItemApplication {

    public static void main(String[] args){
        SpringApplication.run(ItemApplication.class,args);
    }

}
