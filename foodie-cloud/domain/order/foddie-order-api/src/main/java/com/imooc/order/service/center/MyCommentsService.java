package com.imooc.order.service.center;


import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("foddie-order-service")
@RequestMapping("order-commonts-api")
public interface MyCommentsService {

    /**
     * 根据订单ID查询关联的商品.
     * @param orderId
     */
    @GetMapping("orderItems")
    public List<OrderItems> queryPendingComments(@RequestParam("orderId") String orderId);


    /**
     * 保存用户的评论
     * @param orderId
     * @param userId
     * @param commentList
     */
    @PostMapping("saveOrderComments")
    public void saveComments(@RequestParam("orderId") String orderId,
                             @RequestParam("userId") String userId,
                             @RequestBody List<OrderItemsCommentBO> commentList);




    //TODO 移到了itemComments里
//    /**
//     * 我的评价查询 分页
//     * @param userId
//     * @param page
//     * @param pageSize
//     * @return
//     */
//    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize);



}
