package com.imooc.service.center;

import com.imooc.pojo.bo.center.CenterUsersBO;
import com.imooc.pojo.Users;

public interface CenterUserService {


    /**
     * 根据用户ID查询用户信息.
     * @param userId
     * @return
     */
    public Users queryUserInfo(String userId);


    /**
     * 修改用户信息.
     * @param userId
     * @param centerUsersBO
     */
    public Users updateUserInfo(String userId, CenterUsersBO centerUsersBO);


    /**
     * 用户头像更新.
     * @param userId
     * @param centerUsersBO
     */
    public Users updateUserFace(String userId, String faceUrl);





}
