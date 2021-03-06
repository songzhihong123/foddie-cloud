package com.imooc.item.mapper;


import com.imooc.item.pojo.vo.ItemCommentVO;
import com.imooc.item.pojo.vo.ShopcartVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsMapperCustom{


    public List<ItemCommentVO> queryItemComments(@Param("paramsMap") Map<String,Object> map);


    //TODO 迁移到foddie-search 模块

//    List<SearchItemsVO> searchItems(@Param("paramsMap") Map<String,Object> map);

//    List<SearchItemsVO> searchItemsByThirdCat(@Param("paramsMap") Map<String,Object> map);


    public List<ShopcartVO> queryItemsBySpecIds(@Param("paramsList") List specIdsList);

    public int decreaseItemSpecStock(@Param("specId") String specId,@Param("pendingCounts") Integer pendingCounts);



}