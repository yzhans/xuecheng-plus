package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.service.CourseCategoryService;
import com.xuecheng.model.dto.CourseCategoryDto;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/3 0:11
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryDto> queryTreeNodes(String id) {
        //得到了根节点下的所有节点
        List<CourseCategoryDto> categoryDtos = courseCategoryMapper.selectTreeNodes(id);

        //定义一个list作为最终返回的数据
        List<CourseCategoryDto> courseCategoryDtoList = new ArrayList<>();
        //为了方便找子结点的父节点 定义一个map
        HashMap<String, CourseCategoryDto> nodeMap = new HashMap<>();
        //将数据封装到list 只包括根节点的下属节点
        categoryDtos.stream().forEach(item ->{
            nodeMap.put(item.getId(), item);
            if (item.getParentid().equals(id)) {
                courseCategoryDtoList.add(item);
            }
            //找到该节点的父节点
            String parentid = item.getParentid();
            CourseCategoryDto categoryDto = nodeMap.get(parentid);
            if (categoryDto != null) {
                List list = categoryDto.getChildrenTreeNodes();
                if (list == null) {
                    categoryDto.setChildrenTreeNodes(new ArrayList<CourseCategoryDto>());
                }
                //找到子节点 放到它的父节点的 属性中
                categoryDto.getChildrenTreeNodes().add(item);
            }
        });

        return courseCategoryDtoList;
    }
}
