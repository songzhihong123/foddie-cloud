package com.imooc.order.controller;


import com.imooc.controller.BaseController;
import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.PayMethod;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.bo.PlaceOrderBO;
import com.imooc.order.pojo.bo.SubmitOrderBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.order.service.OrderService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopcartBO;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(value = "订单相关",tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 保证创建订单接口的幂等性，采用token的机制
     */
    @ApiOperation(value = "获取订单token",notes = "获取订单token",httpMethod = "POST")
    @PostMapping("/getOrderToken")
    public IMOOCJSONResult getOrderToken(HttpSession session){
        String token = UUID.randomUUID().toString();
        redisOperator.set("ORDER_TOKRN_" + session.getId(),token,600);
        return IMOOCJSONResult.ok(token);
    }

    @ApiOperation(value = "用户下单",notes = "用户下单",httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO,
                                  HttpServletRequest request, HttpServletResponse response){
        /**
         * token的机制 防止重复提交保证接口的幂等性
         */
        String orderTokenKey = "ORDER_TOKRN_" + request.getSession().getId();
        String lockKey = "LOCK_KEY_"+request.getSession().getId();
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(5, TimeUnit.SECONDS);
        try{
            String orderToken = redisOperator.get(orderTokenKey);
            if(StringUtils.isBlank(orderToken)){
                throw new RuntimeException("orderToken不存在");
            }
            boolean corretToken = orderToken.equals(submitOrderBO.getToken());
            if(!corretToken){
                throw new RuntimeException("orderToken不正确");
            }
            redisOperator.del(orderTokenKey);
        }finally {
            try {
                lock.unlock();
            }catch (Exception e){

            }
        }

        if(submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type){
            return IMOOCJSONResult.errorMsg("支付方式不支持");
        }

        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
        if(StringUtils.isBlank(shopcartJson)){
            return IMOOCJSONResult.errorMsg("购物车数据不正确");
        }

        List<ShopcartBO> shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

        // 1. 创建订单
        PlaceOrderBO placeOrderBO = new PlaceOrderBO(submitOrderBO,shopcartList);
        OrderVO orderVO = orderService.createOrder(placeOrderBO);
        String orderId = orderVO.getOrderId();
        //2.创建订单以后移除购物车中已结算（已提交）的商品
        /**
         * 1001
         * 1002 -> 用户购买
         * 1003 -> 用户购买
         * 1004
         */

        //清理覆盖现有的redis汇总的购物车数据
        shopcartList.removeAll(orderVO.getToBeRemovedShopCartdList());
        redisOperator.set(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(),JsonUtils.objectToJson(shopcartList));
        // 整合reidis之后 ，完善购物车中已结算商品的清除，并且同步到前端的cookie
        CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopcartList),true);



        //3.想支付中心发送当前订单，用于保存支付中心的订单数据
        MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
        merchantOrdersVO.setReturnUrl(payReturnUrl);

        //为了方便测试购买，所以所有的支付金额改为1分钱
        merchantOrdersVO.setAmount(1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("imoocUserId","4898345-1743261139");
        headers.add("password","4fg4-83jo-gjhi-ojio");
        HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO,headers);
        ResponseEntity<IMOOCJSONResult> responseEntity = restTemplate.postForEntity(paymentUrl, entity, IMOOCJSONResult.class);
        IMOOCJSONResult paymentResult = responseEntity.getBody();
        if(paymentResult.getStatus() != 200){
            return IMOOCJSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
        }

        //成功之后的测试
        // http://payment.t.mukewang.com/foodie-payment/payment/getPaymentCenterOrderInfo?merchantOrderId=200813G61TF9PFA8&merchantUserId=1908017YR51G1XWH
        return IMOOCJSONResult.ok(orderId);
    }

    @PostMapping("/notifyMerchantOrderPaid")
    public Integer notifyMerchantOrderPaid(String merchantOrderId){
        orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
        return HttpStatus.OK.value();
    }

    @PostMapping("/getPaidOrderInfo")
    public IMOOCJSONResult getPaidOrderInfo(@RequestParam String orderId){
        OrderStatus orderStatus = orderService.queryOrderstatusInfo(orderId);
        return IMOOCJSONResult.ok(orderStatus);
    }




}
