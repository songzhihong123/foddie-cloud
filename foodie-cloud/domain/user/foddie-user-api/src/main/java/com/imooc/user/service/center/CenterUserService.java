package com.imooc.user.service.center;

import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.center.CenterUsersBO;
import org.springframework.web.bind.annotation.*;

@RequestMapping("center-user-api")
public interface CenterUserService {


    /**
     * 根据用户ID查询用户信息.
     * @param userId
     * @return
     */
    @GetMapping("profile")
    public Users queryUserInfo(@RequestParam("userId") String userId);


    /**
     * 修改用户信息.
     * @param userId
     * @param centerUsersBO
     */
    @PutMapping("profile/{userId}")
    public Users updateUserInfo(@PathVariable("userId") String userId,
                                @RequestBody CenterUsersBO centerUsersBO);


    /**
     * 用户头像更新.
     * @param userId
     * @param faceUrl
     */
    @PostMapping("updatePhoto")
    public Users updateUserFace(@RequestParam("userId") String userId,
                                @RequestParam("faceUrl") String faceUrl);





}
