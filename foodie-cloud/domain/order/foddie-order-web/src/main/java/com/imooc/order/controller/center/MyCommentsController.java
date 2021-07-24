package com.imooc.order.controller.center;

import com.imooc.controller.BaseController;
import com.imooc.enums.YesOrNo;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.order.service.OrderService;
import com.imooc.order.service.center.MyCommentsService;
import com.imooc.order.service.center.MyOrdersService;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.pojo.PagedGridResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Api(value = "用户中心评价模块",tags = {"用户中心评价模块的相关接口"})
@RestController
@RequestMapping("mycomments")
public class MyCommentsController extends BaseController {

    @Autowired
    private MyCommentsService myCommentsService;

    @Autowired
    private MyOrdersService myOrdersService;

    @Autowired
    private LoadBalancerClient client;

    @Autowired
    private RestTemplate restTemplate;


    @ApiOperation(value = "查询订单列表",notes = "查询订单列表",httpMethod = "POST")
    @PostMapping("/pending")
    public IMOOCJSONResult pending(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId",value = "订单ID",required = true)
            @RequestParam String orderId){

        //判断用户和订单是否关联
        IMOOCJSONResult checkResult = myOrdersService.checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()){
            return checkResult;
        }
        //判断改订单已经评价过了，评价过了就不在继续
        Orders myOrder = (Orders)checkResult.getData();
        if(myOrder.getIsComment() == YesOrNo.YES.type){
            return IMOOCJSONResult.errorMsg("该订单已经评价!");
        }

        List<OrderItems> orderItems = myCommentsService.queryPendingComments(orderId);

        return IMOOCJSONResult.ok(orderItems);
    }

    @ApiOperation(value = "保存评论列表",notes = "保存评论列表",httpMethod = "POST")
    @PostMapping("/saveList")
    public IMOOCJSONResult saveList(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @ApiParam(name = "orderId",value = "订单ID",required = true)
            @RequestParam String orderId,
            @ApiParam(name = "commentList",value = "评论的详细信息",required = true)
            @RequestBody List<OrderItemsCommentBO> commentList){

        System.out.println(commentList);

        //判断用户和订单是否关联
        IMOOCJSONResult checkResult = myOrdersService.checkUserOrder(userId, orderId);
        if (checkResult.getStatus() != HttpStatus.OK.value()){
            return checkResult;
        }
        //判断评论内容不能为空
        if(commentList == null || commentList.isEmpty() || commentList.size() == 0  ){
            return IMOOCJSONResult.errorMsg("评论内容不能为空");
        }
        myCommentsService.saveComments(orderId,userId,commentList);
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "查询我的评价",notes = "查询我的评价",httpMethod = "POST")
    @PostMapping("/query")
    public IMOOCJSONResult query(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @ApiParam(name = "page",value = "查询下一页的第几页",required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "分页的每一页显示的记录数",required = false)
            @RequestParam Integer pageSize){
        if(StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg(null);
        }
        if (page == null){
            page = 1;
        }
        if (pageSize == null){
            pageSize = COMMON_PAGE_SIZE;
        }


        //TODO 前方施工，学完Fegin再来改造
//        PagedGridResult gridResult = myCommentsService.queryMyComments(userId,page,pageSize);

        ServiceInstance instance = client.choose("FODDIE-ITEM-SERVICE");
        String url = String.format("http://%s:%s/item-comments-api/myConnents" + "?userId=%s&page=%s&pageSize=%s",
                instance.getHost(),instance.getPort(),userId,page,pageSize);
        PagedGridResult gridResult = restTemplate.getForObject(url,PagedGridResult.class);

        return IMOOCJSONResult.ok(gridResult);
    }







}
