package com.imooc.order.pojo.vo;


import com.imooc.pojo.ShopcartBO;

import java.util.List;

public class OrderVO {

    private String orderId;
    private MerchantOrdersVO merchantOrdersVO;

    private List<ShopcartBO> toBeRemovedShopCartdList;


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public MerchantOrdersVO getMerchantOrdersVO() {
        return merchantOrdersVO;
    }

    public void setMerchantOrdersVO(MerchantOrdersVO merchantOrdersVO) {
        this.merchantOrdersVO = merchantOrdersVO;
    }

    public List<ShopcartBO> getToBeRemovedShopCartdList() {
        return toBeRemovedShopCartdList;
    }

    public void setToBeRemovedShopCartdList(List<ShopcartBO> toBeRemovedShopCartdList) {
        this.toBeRemovedShopCartdList = toBeRemovedShopCartdList;
    }
}
