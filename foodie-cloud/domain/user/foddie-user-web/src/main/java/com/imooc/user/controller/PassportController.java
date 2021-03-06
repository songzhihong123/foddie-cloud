package com.imooc.user.controller;

import com.imooc.controller.BaseController;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.ShopcartBO;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.UserBO;
import com.imooc.user.pojo.vo.UsersVO;
import com.imooc.user.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Api(value = "注册登录",tags = {"用于注册登陆的相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;


    @ApiOperation(value = "用户名是否存在",notes = "用户名是否存在",httpMethod = "GET")
    @GetMapping("/usernameIsExist")
    public IMOOCJSONResult userNameisExist(@RequestParam String username){
        //1.判断入参不能为空
        if(StringUtils.isBlank(username)){
            return IMOOCJSONResult.errorMsg("用户名不能为空");
        }
        //2.查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //3.请求成功，用户名没有重复，返回状态码200
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户注册",notes = "用户注册",httpMethod = "POST")
    @PostMapping("/regist")
    public IMOOCJSONResult regist(@RequestBody UserBO userBO,HttpServletRequest request,HttpServletResponse response){
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        String confirmPassword = userBO.getConfirmPassword();
        //0.判断用户名和密码不为空
        if(StringUtils.isBlank(username)
                || StringUtils.isBlank(password)
                || StringUtils.isBlank(confirmPassword)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //1.查询用户名是否存在.
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist){
            return IMOOCJSONResult.errorMsg("用户名已经存在");
        }
        //2.密码长度不能少于6位.
        if(password.length() < 6){
            return IMOOCJSONResult.errorMsg("密码长度不能少于6");
        }
        //3.判断两次密码是否一致.
        if(!password.equals(confirmPassword)){
            return IMOOCJSONResult.errorMsg("两次密码数据不一致");
        }
        //4.实现注册.
        Users userResult = userService.createUser(userBO);

        //为了实现把用户的信息显示在浏览器上，为了安全起见，没有必要将Users里面的所有值对浏览器可见
        //所以需要把Users里面的某些属性给屏蔽掉
        //userResult = setNullProperty(userResult);

        //实现用户的redis会话
        UsersVO usersVO = conventUsersVO(userResult);

        //把Users的信息放入到Cookie里面
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);

        //同步购物车数据
        synchShopcartData(userResult.getId(),request,response);
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户登录",notes = "用户登录",httpMethod = "POST")
    @PostMapping("/login")
    public IMOOCJSONResult login(@RequestBody UserBO userBO, HttpServletRequest request , HttpServletResponse response) throws Exception{
        String username = userBO.getUsername();
        String password = userBO.getPassword();
        //0.判断用户名和密码不为空
        if(StringUtils.isBlank(username)
                || StringUtils.isBlank(password)){
            return IMOOCJSONResult.errorMsg("用户名或密码不能为空");
        }
        //1.实现登录
        Users userResult = userService.queryUsersForLogin(username, MD5Utils.getMD5Str(password));
        if(userResult == null){
            return IMOOCJSONResult.errorMsg("用户名或者密码不正确");
        }

        //为了实现把用户的信息显示在浏览器上，为了安全起见，没有必要将Users里面的所有值对浏览器可见
        //所以需要把Users里面的某些属性给屏蔽掉
        //userResult = setNullProperty(userResult);

        //实现用户的redis会话
        UsersVO usersVO = conventUsersVO(userResult);

        //把Users的信息放入到Cookie里面
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);

        //同步购物车数据
        synchShopcartData(userResult.getId(),request,response);
        return IMOOCJSONResult.ok(userResult);
    }


    @ApiOperation(value = "用户退出登录",notes = "用户退出登录",httpMethod = "POST")
    @PostMapping("/logout")
    public IMOOCJSONResult logout(@RequestParam String userId, HttpServletRequest request, HttpServletResponse response){

        //清除用户相关信息的Cookie
        CookieUtils.deleteCookie(request,response,"user");

        // 用户退出登录，清除redis中user的会话信息
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        // 用户退出登录 需要清空购物车
        CookieUtils.deleteCookie(request,response,FOODIE_SHOPCART);


        return IMOOCJSONResult.ok();
    }

    /**
     * 注册登录成功后，同步cookie和redis中购物车的数据
     */
    // TODO 放到购物车模块
    private void synchShopcartData(String userId,HttpServletRequest request,HttpServletResponse response){
        /**
         * 1.redis无数据，cookie中的购物车为空，那么这个时候不做任何处理
         *                cookie中的购物车不为空，此时直接放入redis中
         * 2.redis中有数据，cookie中的购物车为空，那么直接把redis购物车覆盖本地cookie
         *                  cookie中的购物车不为空，如果cookie中的某个商品在redis中存在，则以cookie为主，删除redis中
         *                      把cookie中的商品直接覆盖redis中（参考京东）
         * 3.同步到redis中去了以后，覆盖本地cookie购物的数据，保证本地购物车的数据是同步
         */

        //从redis获取购物车
        String shopcartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        //从cookie中获取购物车
        String shopcartStrCookie = CookieUtils.getCookieValue(request,FOODIE_SHOPCART,true);

        if(StringUtils.isBlank(shopcartJsonRedis)){
            //redis 为空，cookie不为空，直接吧cookie中的数据放入到redis中
            if(StringUtils.isNotBlank(shopcartStrCookie)){
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,shopcartStrCookie);
            }
        }else{
            //redis 不为空，cookie不为空，合并cookie和redis购物车的商品数据（同一商品则覆盖redis）
            if(StringUtils.isNotBlank(shopcartStrCookie)){
                /**
                 * 1.已经存在的，把cookie中对应的数量，覆盖redis
                 * 2.该项商品标记为待删除，统一放入到待删除的list
                 * 3.从cookie中清理所有的待删除list
                 * 4.合并redis和cookie中的数据
                 * 5.更新到redis和cookie中
                 */
                List<ShopcartBO> shopcartListRedis  = JsonUtils.jsonToList(shopcartJsonRedis,ShopcartBO.class);
                List<ShopcartBO> shopcartListCookie  = JsonUtils.jsonToList(shopcartStrCookie,ShopcartBO.class);
                //定义一个待删除List
                List<ShopcartBO> pendingDeleteList = new ArrayList<>();

                for (ShopcartBO redisShopCart : shopcartListRedis){
                    String redisSpecId = redisShopCart.getSpecId();
                    for (ShopcartBO cookieShopCart : shopcartListCookie){
                        String cookieSpecId = cookieShopCart.getSpecId();
                        if(cookieSpecId.equals(redisSpecId)){
                            //覆盖购买数量，不累加，参考京东
                            redisShopCart.setBuyCounts(cookieShopCart.getBuyCounts());
                            //把cookieShopCart放入待删除列表，用于最后的删除与合并
                            pendingDeleteList.add(cookieShopCart);
                        }
                    }
                }
                //从现有cookie中删除对应覆盖过的商品数据
                shopcartListCookie.removeAll(pendingDeleteList);
                //合并两个List
                shopcartListRedis.addAll(shopcartListCookie);
                // 更新到redis和cookie
                CookieUtils.setCookie(request,response,FOODIE_SHOPCART,JsonUtils.objectToJson(shopcartListRedis),true);
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,JsonUtils.objectToJson(shopcartListRedis));
            }else{
                //redis 不为空，cookie为空，直接把redis覆盖cookie
                CookieUtils.setCookie(request,response,FOODIE_SHOPCART,shopcartJsonRedis,true);
            }
        }

    }


    /**
     * 把对浏览器不可见的Users里面的值置为null
     * @param userResult
     * @return
     */
    private Users setNullProperty(Users userResult){
        userResult.setPassword(null);
        userResult.setRealname(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

    public UsersVO conventUsersVO(Users userResult){
        //实现用户的redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN + ":" + userResult.getId(),uniqueToken);

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        return usersVO;
    }




}
