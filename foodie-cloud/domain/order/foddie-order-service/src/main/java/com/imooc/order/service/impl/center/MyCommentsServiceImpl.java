package com.imooc.order.service.impl.center;

import com.imooc.enums.YesOrNo;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.center.OrderItemsCommentBO;
import com.imooc.order.service.center.MyCommentsService;
import com.imooc.org.n3r.idworker.Sid;
import com.imooc.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MyCommentsServiceImpl extends BaseService implements MyCommentsService {

    @Autowired
    private OrderItemsMapper orderItemsMapper;

//    @Autowired
//    private ItemsCommentsMapperCustom itemsCommentsMapperCustom;

    // TOOD fengin 章节里改成item-api
    @Autowired
    private LoadBalancerClient client;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private Sid sid;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<OrderItems> queryPendingComments(String orderId) {
        OrderItems queryOrderItems = new OrderItems();
        queryOrderItems.setOrderId(orderId);
        return orderItemsMapper.select(queryOrderItems);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void saveComments(String orderId, String userId, List<OrderItemsCommentBO> commentList) {

        //1.保存评价表
        for(OrderItemsCommentBO oic : commentList){
            oic.setCommentId(sid.nextShort());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("commentList",commentList);
//        itemsCommentsMapperCustom.saveComments(map);
        ServiceInstance instance = client.choose("FODDIE-ITEM-SERVICE");
        String url = String.format("http://%s:%s/item-comments-api/saveComments",instance.getHost(),instance.getPort());
        restTemplate.postForLocation(url,map);
        //2.修改订单表为已评价
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setIsComment(YesOrNo.YES.type);
        ordersMapper.updateByPrimaryKeySelective(orders);
        //3.修改订单状态表的留言时间
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCommentTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
    }

    //TODO 移到了itemcommontService当中
//    @Transactional(propagation = Propagation.SUPPORTS)
//    @Override
//    public PagedGridResult queryMyComments(String userId, Integer page, Integer pageSize) {
//        Map<String,Object> map = new HashMap<>();
//        map.put("userId",userId);
//        PageHelper.startPage(page,pageSize);
//        List<MyCommentVO> resultList = itemsCommentsMapperCustom.queryMyComments(map);
//        return setPagedGrid(resultList,page);
//    }

}



