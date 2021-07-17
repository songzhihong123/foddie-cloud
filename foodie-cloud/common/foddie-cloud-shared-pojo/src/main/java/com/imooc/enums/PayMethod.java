package com.imooc.enums;

/**
 * 支付方式 枚举
 */
public enum PayMethod {

    WEIXIN(1,"微信"),
    ALIPAY(2,"支付宝");

    public Integer type;
    public String value;

    PayMethod(Integer type,String value){
        this.type = type;
        this.value = value;
    }



}
