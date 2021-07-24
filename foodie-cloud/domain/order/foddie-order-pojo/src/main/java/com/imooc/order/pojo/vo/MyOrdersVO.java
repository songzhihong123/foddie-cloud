package com.imooc.order.pojo.vo;


import java.util.Date;
import java.util.List;

/**
 * 用户中心，我的订单列表VO
 */
public class MyOrdersVO {

    /**
     *  od.id AS orderId,
     * 	od.created_time AS createTime,
     * 	od.pay_method AS payMethod,
     * 	od.real_pay_amount AS realPayAmount,
     * 	od.post_amount AS postAmount,
     * 	os.order_status AS orderStatus,
     * 	oi.item_id AS itemId,
     * 	oi.item_img AS itemName,
     * 	oi.item_img AS itemImg,
     * 	oi.item_spec_id AS itemSpecId,
     * 	oi.item_spec_name AS itemSpecName,
     * 	oi.buy_counts AS buyCounts,
     * 	oi.price AS price
     */
    private String orderId;
    private Date createTime;
    private Integer payMethod;
    private Integer realPayAmount;
    private Integer postAmount;
    private Integer isComment;
    private Integer orderStatus;
    private List<com.imooc.pojo.vo.MySubOrderItemVO> subOrderItemList;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(Integer payMethod) {
        this.payMethod = payMethod;
    }

    public Integer getRealPayAmount() {
        return realPayAmount;
    }

    public void setRealPayAmount(Integer realPayAmount) {
        this.realPayAmount = realPayAmount;
    }

    public Integer getPostAmount() {
        return postAmount;
    }

    public void setPostAmount(Integer postAmount) {
        this.postAmount = postAmount;
    }

    public Integer getIsComment() {
        return isComment;
    }

    public void setIsComment(Integer isComment) {
        this.isComment = isComment;
    }

    public Integer getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Integer orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<com.imooc.pojo.vo.MySubOrderItemVO> getSubOrderItemList() {
        return subOrderItemList;
    }

    public void setSubOrderItemList(List<com.imooc.pojo.vo.MySubOrderItemVO> subOrderItemList) {
        this.subOrderItemList = subOrderItemList;
    }
}
