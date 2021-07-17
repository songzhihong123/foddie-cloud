package com.imooc.service;

import com.github.pagehelper.PageInfo;
import com.imooc.pojo.PagedGridResult;

import java.util.List;

public class BaseService {


    /**
     * 把分页方法功能化
     * @param result
     * @param page
     * @return
     */
    public PagedGridResult setPagedGrid(List<?> result, Integer page){
        PageInfo<?> pageList = new PageInfo<>(result);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(result);
        grid.setTotal(pageList.getPages());
        grid.setRecords(pageList.getTotal());
        return grid;
    }


}
