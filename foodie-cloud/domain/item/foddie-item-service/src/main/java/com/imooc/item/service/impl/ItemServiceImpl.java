package com.imooc.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.enums.CommentLevel;
import com.imooc.item.mapper.*;
import com.imooc.item.pojo.*;
import com.imooc.item.pojo.vo.CommentLevelCountsVO;
import com.imooc.item.pojo.vo.ItemCommentVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.imooc.item.pojo.vo.ShopcartVO;
import com.imooc.item.service.ItemService;
import com.imooc.pojo.PagedGridResult;
import com.imooc.utils.DesensitizationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class ItemServiceImpl implements ItemService {

    private Logger logger = LoggerFactory.getLogger(ItemServiceImpl.class);

    @Autowired
    private ItemsMapper itemsMapper;

    @Autowired
    private ItemsImgMapper itemsImgMapper;

    @Autowired
    private ItemsSpecMapper itemsSpecMapper;

    @Autowired
    private ItemsParamMapper itemsParamMapper;

    @Autowired
    private ItemsCommentsMapper itemsCommentsMapper;

    @Autowired
    private ItemsMapperCustom itemsMapperCustom;

    @Autowired
    private RedissonClient redissonClient;


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Items queryItemById(String itemId) {
        return itemsMapper.selectByPrimaryKey(itemId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsImg> queryItemImgList(String itemId) {
        Example example = new Example(ItemsImg.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);
        List<ItemsImg> result = itemsImgMapper.selectByExample(example);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ItemsSpec> queryItemSpecList(String itemId) {
        Example example = new Example(ItemsSpec.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);
        List<ItemsSpec> result = itemsSpecMapper.selectByExample(example);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsParam queryItemParam(String itemId) {
        Example example = new Example(ItemsParam.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("itemId",itemId);
        ItemsParam result = itemsParamMapper.selectOneByExample(example);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public CommentLevelCountsVO queryCommentCounts(String itemId) {
        Integer goodCounts = getCommontCounts(itemId, CommentLevel.GOOD.type);
        Integer normalCounts = getCommontCounts(itemId, CommentLevel.NORMAL.type);
        Integer badCounts = getCommontCounts(itemId, CommentLevel.BAD.type);
        Integer totalCounts = goodCounts + normalCounts + badCounts;
        CommentLevelCountsVO result = new CommentLevelCountsVO();
        result.setBadCounts(badCounts);
        result.setTotalCounts(totalCounts);
        result.setGoodCounts(goodCounts);
        result.setNormalCounts(normalCounts);
        return result;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public Integer getCommontCounts(String itemId, Integer level){
        ItemsComments condition = new ItemsComments();
        condition.setItemId(itemId);
        if(level != null){
            condition.setCommentLevel(level);
        }
        return itemsCommentsMapper.selectCount(condition);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public PagedGridResult queryPagedCommonts(String itemId, Integer level, Integer page, Integer pageSize) {
        Map<String,Object> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("level",level);

        // mybatis-pagehelper
        /**
         * page: ????????????
         * pageSize??????????????????????????????
         */
        PageHelper.startPage(page,pageSize);
        List<ItemCommentVO> result = itemsMapperCustom.queryItemComments(map);
        result.stream().forEach(vo -> {vo.setNickname(DesensitizationUtil.commonDisplay(vo.getNickname()));});
        return setPagedGrid(result,page);
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ShopcartVO> queryItemsBySpecIds(String specIds) {
        String[] ids = specIds.split(",");
        List<String> specIdsList = new ArrayList<>();
        Collections.addAll(specIdsList,ids);
        return itemsMapperCustom.queryItemsBySpecIds(specIdsList);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public ItemsSpec queryItemSpecById(String specId) {
        return itemsSpecMapper.selectByPrimaryKey(specId);
    }


    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public String queryItemMainImgById(String itemId) {

        ItemsImg itemsImg = new ItemsImg();
        itemsImg.setItemId(itemId);
        itemsImg.setIsMain(1);
        ItemsImg result = itemsImgMapper.selectOne(itemsImg);
        return result != null ? result.getUrl() : "";
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public  void decreaseItemSpecStock(String specId, int buyCounts){

        // synchronized ????????????????????????????????????????????????
        // ??????????????????????????????????????????????????????
        // ???????????? zookeeper redis


        //??????Redisson???????????????????????????
        RLock lock = redissonClient.getLock("Item_Lock" + specId);
        lock.lock(5, TimeUnit.SECONDS);
        try{
            logger.info("???????????????");
            int result = itemsMapperCustom.decreaseItemSpecStock(specId,buyCounts);
            if(result != 1){
                logger.info("???????????????");
                throw new RuntimeException("??????????????????????????????????????????");
            }
        }catch (RuntimeException e){
            throw e;
        }finally {
            logger.info("?????????");
            lock.unlock();
        }

    }

    /**
     * ????????????????????????
     * @param result
     * @param page
     * @return
     */
    private PagedGridResult setPagedGrid(List<?> result,Integer page){
        PageInfo<?> pageList = new PageInfo<>(result);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(result);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }



}
