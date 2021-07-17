package com.imooc.enums;

/**
 * @Desc:是否枚举
 */
public enum YesOrNo {

    NO(0,"否"),
    YES(1,"是");

    public Integer type;
    public String value;

    YesOrNo(Integer type , String value){
        this.type = type;
        this.value = value;
    }



}
