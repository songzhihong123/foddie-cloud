package com.imooc.order.service.impl;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.item.pojo.Items;
import com.imooc.item.pojo.ItemsSpec;
import com.imooc.item.service.ItemService;
import com.imooc.order.mapper.OrderItemsMapper;
import com.imooc.order.mapper.OrderStatusMapper;
import com.imooc.order.mapper.OrdersMapper;
import com.imooc.order.pojo.OrderItems;
import com.imooc.order.pojo.OrderStatus;
import com.imooc.order.pojo.Orders;
import com.imooc.order.pojo.bo.PlaceOrderBO;
import com.imooc.order.service.OrderService;
import com.imooc.org.n3r.idworker.Sid;
import com.imooc.pojo.ShopcartBO;
import com.imooc.order.pojo.bo.SubmitOrderBO;
import com.imooc.order.pojo.vo.MerchantOrdersVO;
import com.imooc.order.pojo.vo.OrderVO;
import com.imooc.user.pojo.UserAddress;
import com.imooc.user.service.AddressService;
import com.imooc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private Sid sid;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ItemService itemService;


    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(PlaceOrderBO orderBO) {
        SubmitOrderBO submitOrderBO = orderBO.getOrder();
        List<ShopcartBO> shopcartList = orderBO.getItems();

        String userId = submitOrderBO.getUserId();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        Integer payMethod = submitOrderBO.getPayMethod();
        String leftMsg = submitOrderBO.getLeftMsg();
        //?????????????????????0
        Integer postAmount = 0;

        String orderId = sid.nextShort();
        UserAddress userAddress = addressService.queryUserAddress(userId, addressId);

        //1.?????????????????????
        Orders newOrder = new Orders();
        newOrder.setId(orderId);
        newOrder.setUserId(userId);
        newOrder.setReceiverName(userAddress.getReceiver());
        newOrder.setReceiverMobile(userAddress.getMobile());
        newOrder.setReceiverAddress(userAddress.getProvince()
                + " "+userAddress.getCity()+" "
                +userAddress.getDistrict()+" "
                +userAddress.getDetail());
        newOrder.setPostAmount(postAmount);
        newOrder.setPayMethod(payMethod);
        newOrder.setLeftMsg(leftMsg);
        newOrder.setIsComment(YesOrNo.NO.type);
        newOrder.setIsDelete(YesOrNo.NO.type);
        newOrder.setCreatedTime(new Date());
        newOrder.setUpdatedTime(new Date());
        newOrder.setTotalAmount(0);
        newOrder.setRealPayAmount(0);
        ordersMapper.insert(newOrder);

        //2.????????????itemSpecIds???????????????????????????
        String[] itemSpecIdArr = itemSpecIds.split(",");
        Integer totalAmount = 0;  //??????????????????
        Integer realPayAmount = 0; //?????????????????????????????????????????????
        List<ShopcartBO> toBeRemovedShopCartdList = new ArrayList<>();
        for(String itemSpecId:itemSpecIdArr){
            ShopcartBO cartItem = getBuyCountsFromShopcart(shopcartList, itemSpecId);
            // ??????Reids???????????????????????????????????????redis?????????????????????
            int buyCounts = cartItem.getBuyCounts();
            toBeRemovedShopCartdList.add(cartItem);


            //2.1.????????????id????????????????????????????????????????????????
            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);

            totalAmount += itemsSpec.getPriceNormal() * buyCounts;
            realPayAmount += itemsSpec.getPriceDiscount() * buyCounts;
            //2.2.????????????ID?????????????????????????????????
            String itemId = itemsSpec.getItemId();
            Items items = itemService.queryItemById(itemId);
            String imgUrl = itemService.queryItemMainImgById(itemId);
            String subOrderId = sid.nextShort();
            //2.3???????????????????????????????????????
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemName(items.getItemName());
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemsSpec.getName());
            subOrderItem.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);
            //2.4 ?????????????????????????????????????????????????????????
            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
        }
        newOrder.setTotalAmount(totalAmount);
        newOrder.setRealPayAmount(realPayAmount);
        // ????????????????????????????????????
        newOrder.setUserId(null);
        ordersMapper.updateByPrimaryKeySelective(newOrder);

        //3.?????????????????????
        OrderStatus waitPayOrderStatus = new OrderStatus();
        waitPayOrderStatus.setOrderId(orderId);
        waitPayOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitPayOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitPayOrderStatus);

        // 4. ?????????????????????????????????????????????
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realPayAmount + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        //?????????????????????vo
        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        orderVO.setToBeRemovedShopCartdList(toBeRemovedShopCartdList);
        return orderVO;
    }

    /**
     * ???redis??????????????????????????????????????????counts
     * @param shopcartBOList
     * @param specId
     * @return
     */
    private ShopcartBO getBuyCountsFromShopcart(List<ShopcartBO> shopcartBOList,String specId){
        for(ShopcartBO cart : shopcartBOList){
            if(specId.equals(cart.getSpecId())){
                return cart;
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public OrderStatus queryOrderstatusInfo(String orderId){
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void closeOrder() {
        //??????????????????????????????????????????????????????????????????1?????????????????????????????????
        OrderStatus queryOrder = new OrderStatus();
        queryOrder.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        List<OrderStatus> list = orderStatusMapper.select(queryOrder);
        for (OrderStatus os: list) {
            //????????????????????????
            Date createdTime = os.getCreatedTime();
            //?????????????????????
            int days = DateUtil.daysBetween(createdTime, new Date());
            if(days >= 1){
                //??????1???????????????
                doCloseOrder(os.getOrderId());
            }
        }

    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void doCloseOrder(String orderId){
        OrderStatus close = new OrderStatus();
        close.setOrderId(orderId);
        close.setOrderStatus(OrderStatusEnum.CLOSE.type);
        close.setCloseTime(new Date());
        orderStatusMapper.updateByPrimaryKeySelective(close);
    }


}
