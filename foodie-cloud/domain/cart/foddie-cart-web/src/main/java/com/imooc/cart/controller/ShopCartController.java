package com.imooc.cart.controller;

import com.imooc.cart.service.CartService;
import com.imooc.controller.BaseController;
import com.imooc.pojo.ShopcartBO;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Api(value = "购物车接口controller",tags = {"购物车接口相关的api"})
@RestController
@RequestMapping("shopcart")
public class ShopCartController extends BaseController {

    @Autowired
    RedisOperator redisOperator;

    @Autowired
    private CartService cartService;

    @ApiOperation(value = "添加商品到购物车",notes = "添加商品到购物车",httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestParam String userId ,
                               @RequestBody ShopcartBO shopcartBO,
                               HttpServletRequest request, HttpServletResponse response){

        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("");
        }
        System.out.println(shopcartBO);

        cartService.addItemToCart(userId,shopcartBO);

        return IMOOCJSONResult.ok();
    }


    @ApiOperation(value = "从购物车中删除商品",notes = "从购物车中删除商品",httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(@RequestParam String userId ,
                               @RequestParam String itemSpecId,
                               HttpServletRequest request, HttpServletResponse response){

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)){
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }

        cartService.removeItemToCart(userId,itemSpecId);

        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "清空购物车",notes = "清空购物车",httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(@RequestParam String userId){

        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }
        cartService.clearCart(userId);
        return IMOOCJSONResult.ok();
    }

    //TODO 1.购物车清空功能
    //TODO 2.加减号 - 添加、减少商品数量



}
