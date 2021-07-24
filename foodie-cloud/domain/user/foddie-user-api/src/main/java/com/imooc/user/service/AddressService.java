package com.imooc.service;

import com.imooc.pojo.bo.AddressBO;
import com.imooc.pojo.UserAddress;

import java.util.List;

public interface AddressService {


    /**
     * 根据用户Id查询用户的收获地址列表.
     * @param userId
     * @return
     */
    public List<UserAddress> queryAll(String userId);

    /**
     * 根据用户iD和地址ID查询具体的用户地址信息
     * @param userId
     * @param addressId
     * @return
     */
    public UserAddress queryUserAddress(String userId,String addressId);

    /**
     * 用户新增收获地址列表.
     * @param addressBO
     */
    public void addNewUserAddress(AddressBO addressBO);



    /**
     * 用户修改 收获地址列表.
     * @param addressBO
     */
    public void updateUserAddress(AddressBO addressBO);


    /**
     * 根据用户Id和地址Id，删除对应的用户地址信息.
     * @param userId
     * @param addressId
     */
    public void deleteUserAddress(String userId,String addressId);


    /**
     * 修改默认地址
     * @param userId
     * @param addressId
     */
    public void updateUserAddressToBeDefault(String userId,String addressId);



}
