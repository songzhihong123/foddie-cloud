package com.imooc.cart.service.impl;

import com.imooc.cart.service.CartService;
import com.imooc.pojo.ShopcartBO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.imooc.controller.BaseController.FOODIE_SHOPCART;

@RestController
@Slf4j
public class CartServiceImpl implements CartService {


    @Autowired
    RedisOperator redisOperator;

    @Override
    public boolean addItemToCart(String userId, ShopcartBO shopcartBO) {


        // 前端用户在登录的情况下，添加商品道购物车，会同时在后端同步购物车到redis缓存
        // 需要判断当前购物车中已经存在的商品，如果存在则累加购买数量
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> shopcartList = null;
        if(StringUtils.isNotBlank(shopcartJson)){
            //redis中已经有了购物车
            shopcartList = JsonUtils.jsonToList(shopcartJson,ShopcartBO.class);
            //判断购物车中是否已经存在商品，如果有的话counts累加
            boolean isHaving = false;
            for (ShopcartBO sc : shopcartList){
                String tmpSpecId = sc.getSpecId();
                if(tmpSpecId.equals(shopcartBO.getSpecId())){
                    sc.setBuyCounts(sc.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if(!isHaving){
                shopcartList.add(shopcartBO);
            }
        }else{
            //redis 中没有购物车
            shopcartList = new ArrayList<>();
            //直接添加到购物车中
            shopcartList.add(shopcartBO);
        }
        //覆盖现有redis中的购物车
        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));

        return true;
    }

    @Override
    public boolean removeItemToCart(String userId, String itemSpecId) {
        // 用户在页面删除购物车中的数据，如果此时用户已经登录,则需要同步删除redis购物车中的数据
        String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO>  shopcartList = null;
        if(StringUtils.isNotBlank(shopcartJson)){
            //redis中已经有了购物车
            shopcartList = JsonUtils.jsonToList(shopcartJson,ShopcartBO.class);
            //判断购物车中是否已经存在商品，如果有的话counts累加
            for (ShopcartBO sc : shopcartList){
                String tmpSpecId = sc.getSpecId();
                if(tmpSpecId.equals(itemSpecId)){
                    shopcartList.remove(sc);
                    break;
                }
            }
            //覆盖现有redis中的购物车
            redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(shopcartList));
        }
        return true;
    }

    @Override
    public boolean clearCart(String userId) {
        redisOperator.del(FOODIE_SHOPCART + ":" + userId);
        return true;
    }

}
