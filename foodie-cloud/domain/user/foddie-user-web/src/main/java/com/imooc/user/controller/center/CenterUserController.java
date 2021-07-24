package com.imooc.center;

import com.imooc.pojo.bo.center.CenterUsersBO;
import com.imooc.controller.BaseController;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.resource.FileUpload;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(value = "用户信息接口",tags = {"用户信息相关接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("/update")
    public IMOOCJSONResult update(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @RequestBody @Valid CenterUsersBO centerUsersBO,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response){

        //判断BindingResult中是否有错误的验证信息，如果有，则直接return
        if(result.hasErrors()){
            Map<String, String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }

        Users userResult = centerUserService.updateUserInfo(userId, centerUsersBO);
//        userResult = setNullProperty(userResult);

        //后续要改，增加令牌token,会整合redis，分布式会话
        UsersVO usersVO = conventUsersVO(userResult);

        //把Users的信息放入到Cookie里面
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);



        return IMOOCJSONResult.ok(userResult);

    }


    @ApiOperation(value = "用户头像修改",notes = "用户头像修改",httpMethod = "POST")
    @PostMapping("/uploadFace")
    public IMOOCJSONResult uploadFace(
            @ApiParam(name = "userId",value = "用户ID",required = true)
            @RequestParam String userId,
            @ApiParam(name = "file",value = "用户头像",required = true)
            MultipartFile file,
            HttpServletRequest request,
            HttpServletResponse response){

        //定义头像保存的地址
//        String fileSpace = IMAGE_USER_FACE_LOCATION;
        String fileSpace = fileUpload.getImageUserFaceLocation();
        //在路径上为每一个用户上增加一个userId，来区分不用的用户上传
        String uploadPathPerfix = File.separator + userId;
        //开始文件上传
        if(file != null){
            FileOutputStream fileOutputStream = null;
            try {
                //获得文件上传的文件路径
                String fileName = file.getOriginalFilename();
                if(StringUtils.isNotBlank(fileName)){
                    //文件重命名 imooc-face.png -> ["imooc-face","png]
                    String[] fileNameArr = fileName.split("\\.");
                    //获取文件的后缀名
                    String suffix = fileNameArr[fileNameArr.length - 1];

                    if(!suffix.equalsIgnoreCase("png")
                            && !suffix.equalsIgnoreCase("jpg")
                            && !suffix.equalsIgnoreCase("jpeg")){
                        return IMOOCJSONResult.errorMap("图片格式不正确");
                    }
                    // face-{userId}.png
                    //文件名称重组 覆盖式上传，增量式：需要额外拼接时间
                    String newFileName = "face-" + userId + "." + suffix;

                    //上传的头像最终保存的位置
                    String finalFacePath = fileSpace + uploadPathPerfix +File.separator+ newFileName;

                    //用于提供给web服务访问的地址
                    uploadPathPerfix += ("/" + newFileName);

                    File outFile = new File(finalFacePath);

                    if(outFile.getParentFile() != null){
                        //创建文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    //文件输出保存到目录
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    IOUtils.copy(inputStream,fileOutputStream);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }finally {
                try {
                    if(fileOutputStream != null){
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else {
            return IMOOCJSONResult.errorMap("文件不能为空!");
        }

        //获取图片的服务地址
        String imageServerUrl = fileUpload.getImageServerUrl();

        //由于浏览器有可能存在缓存的情况，所以在这里，我们需要加上时间戳来保证更新后的图片可以及时的刷新
        String finalUserFaceUrl = imageServerUrl + uploadPathPerfix+"?t="+ DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);

        Users userResult = centerUserService.updateUserFace(userId, finalUserFaceUrl);
        //userResult = setNullProperty(userResult);

        //后续要改，增加令牌token,会整合redis，分布式会话
        UsersVO usersVO = conventUsersVO(userResult);
        //把Users的信息放入到Cookie里面
        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(usersVO),true);


        return IMOOCJSONResult.ok();

    }





    /**
     * 获取验证中的错误.
     * @param result
     * @return
     */
    private Map<String ,String> getErrors(BindingResult result){
        Map<String ,String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError error:errorList) {
            //发生验证错误所对应的某一个属性
            String errorField = error.getField();
            //验证错误的信息
            String errorMessage = error.getDefaultMessage();
            map.put(errorField,errorMessage);
        }
        return map;
    }


    /**
     * 把对浏览器不可见的Users里面的值置为null
     * @param userResult
     * @return
     */
    private Users setNullProperty(Users userResult) {
        userResult.setPassword(null);
        userResult.setRealname(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }



    }
