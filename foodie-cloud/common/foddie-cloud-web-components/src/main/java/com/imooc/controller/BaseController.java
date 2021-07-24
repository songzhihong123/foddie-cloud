package com.imooc.controller;

import org.springframework.stereotype.Controller;

import java.io.File;

@Controller
public class BaseController {

    public static final String FOODIE_SHOPCART = "shopcart";

    public static final Integer COMMON_PAGE_SIZE = 10;

    public static final Integer PAGE_SIZE = 20;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    //支付中心的调用地址
    public String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";

    //微信支付成功 => 支付中心 => 天天吃货平台
    //                          | -> 回调通知的url
    public String payReturnUrl = "http://192.168.163.145:8088/foodie-dev-api/orders/notifyMerchantOrderPaid";


    //用户上传头像的位置
    public static final String IMAGE_USER_FACE_LOCATION = "E:"+ File.separator + "JavaArchitect"+ File.separator + "userface";

    // FIXME  移植到订单中心去

//    @Autowired
//    private MyOrdersService myOrdersService;
//
//    @Autowired
//    private RedisOperator redisOperator;
//
//
//    public UsersVO conventUsersVO(Users userResult){
//        //实现用户的redis会话
//        String uniqueToken = UUID.randomUUID().toString().trim();
//        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(),uniqueToken);
//
//        UsersVO usersVO = new UsersVO();
//        BeanUtils.copyProperties(userResult,usersVO);
//        usersVO.setUserUniqueToken(uniqueToken);
//        return usersVO;
//    }
//
//
//
//    /**
//     * 用于验证用户和订单是否有关联关系，避免非法用户调用.
//     * @return
//     */
//    public IMOOCJSONResult checkUserOrder(String userId , String orderId){
//        Orders orders = myOrdersService.queryMyOrder(userId, orderId);
//        if(orders == null){
//            return IMOOCJSONResult.errorMsg("订单不存在!");
//        }
//        return IMOOCJSONResult.ok(orders);
//    }



}
