package com.imooc.controller;

import com.imooc.pojo.bo.AddressBO;
import com.imooc.pojo.UserAddress;
import com.imooc.service.AddressService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.MobileEmailUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "地址相关",tags = {"地址相关的api的接口"})
@RestController
@RequestMapping("address")
public class AddressController {

    @Autowired
    private AddressService addressService;


    /**
     * 用于在确认订单页面，可以针对收获地址做如下操作
     * 1.查询y用户所有的收获地址列表
     * 2.新增收获地址
     * 3.删除收获地址
     * 4.修改收获地址
     * 5.设置默认地址
     */

    @ApiOperation(value = "根据用户ID查询收获地址列表",notes = "根据用户ID查询收获地址列表",httpMethod = "POST")
    @PostMapping("/list")
    public IMOOCJSONResult list(@RequestParam String userId){
        if (StringUtils.isBlank(userId)){
            return IMOOCJSONResult.errorMsg("");
        }
        List<UserAddress> result = addressService.queryAll(userId);
        return IMOOCJSONResult.ok(result);

    }

    @ApiOperation(value = "用户新增地址",notes = "用户新增地址",httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestBody AddressBO addressBO){
        IMOOCJSONResult checkRes = this.checkAddress(addressBO);
        if(checkRes.getStatus() != 200){
            return checkRes;
        }
        addressService.addNewUserAddress(addressBO);
        return IMOOCJSONResult.ok();

    }


    @ApiOperation(value = "根据用户ID查询收获地址列表",notes = "根据用户ID查询收获地址列表",httpMethod = "POST")
    @PostMapping("/update")
    public IMOOCJSONResult update(@RequestBody AddressBO addressBO){
        if(StringUtils.isBlank(addressBO.getAddressId())){
            return IMOOCJSONResult.errorMsg("修改地址错误：addressdId不能为空");
        }
        IMOOCJSONResult checkRes = this.checkAddress(addressBO);
        if(checkRes.getStatus() != 200){
            return checkRes;
        }
       addressService.updateUserAddress(addressBO);
        return IMOOCJSONResult.ok();

    }

    @ApiOperation(value = "用户删除地址",notes = "用户删除地址",httpMethod = "POST")
    @PostMapping("/delete")
    public IMOOCJSONResult delete(@RequestParam String userId,@RequestParam String addressId){
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId) ){
            return IMOOCJSONResult.errorMsg("");
        }
        addressService.deleteUserAddress(userId,addressId);
        return IMOOCJSONResult.ok();

    }

    @ApiOperation(value = "用户设置默认地址",notes = "用户设置默认地址",httpMethod = "POST")
    @PostMapping("/setDefalut")
    public IMOOCJSONResult setDefalut(@RequestParam String userId,@RequestParam String addressId){
        if(StringUtils.isBlank(userId) || StringUtils.isBlank(addressId) ){
            return IMOOCJSONResult.errorMsg("");
        }
        addressService.updateUserAddressToBeDefault(userId,addressId);
        return IMOOCJSONResult.ok();

    }


    /**
     * 检查前台传过来的对象属性.
     * @param addressBO
     * @return
     */
    private IMOOCJSONResult checkAddress(AddressBO addressBO){
        String receiver = addressBO.getReceiver();
        if(StringUtils.isBlank(receiver)){
            return IMOOCJSONResult.errorMsg("收货人不能为空");
        }
        if(receiver.length() > 12){
            return IMOOCJSONResult.errorMsg("收货人姓名不能太长");
        }
        String mobile = addressBO.getMobile();
        if(StringUtils.isBlank(mobile)){
            return IMOOCJSONResult.errorMsg("收货人手机号不能为空");
        }
        if(mobile.length() != 11){
            return IMOOCJSONResult.errorMsg("收货人手机号长度不正确");
        }
        if(!MobileEmailUtils.checkMobileIsOk(mobile)){
            return IMOOCJSONResult.errorMsg("收货人手机号格式不正确");
        }
        String province = addressBO.getProvince();
        String city = addressBO.getCity();
        String district = addressBO.getDistrict();
        String detail = addressBO.getDetail();
        if (StringUtils.isBlank(province)||
                StringUtils.isBlank(city)||
                StringUtils.isBlank(district)||
                StringUtils.isBlank(detail)){
            return IMOOCJSONResult.errorMsg("收货人地址信息不能为空");
        }

        return IMOOCJSONResult.ok();

    }



}
