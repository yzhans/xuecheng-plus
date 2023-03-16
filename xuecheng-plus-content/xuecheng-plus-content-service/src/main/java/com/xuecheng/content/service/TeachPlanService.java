package com.xuecheng.content.service;

import com.xuecheng.model.dto.SaveTeachplanDto;
import com.xuecheng.model.dto.TeachPlanDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/5 11:31
 */
public interface TeachPlanService {

    /***
    * @description 获取教学计划树节点
    * @param courseId 教学计划ID
    * @return java.util.List<com.xuecheng.model.dto.TeachPlanDto>
    * @author yzhans
    * @date 2023/3/5 13:18
    */
    List<TeachPlanDto> getTeachPlanTreeNodes(Long courseId);

    /***
    * @description 修改或新增教学计划
    * @param saveTeachplanDto 教学计划信息
    * @return void
    * @author yzhans
    * @date 2023/3/5 13:19
    */
    void saveTeachPlan(SaveTeachplanDto saveTeachplanDto);

    /***
    * @description 删除课程节点
    * @param id 节点id
    * @return void
    * @author yzhans
    * @date 2023/3/5 14:29
    */
    void deleteTeachPlan(Long id);

    /***
     * @description 删除视频信息
     * @param id 节点id
     * @return void
     * @author yzhans
     * @date 2023/3/5 14:29
     */
    void deleteTeachPlanVideo(Long id);

    /***
    * @description 上移或下节点排序
    * @param id 接点id
    * @return void
    * @author yzhans
    * @date 2023/3/6 15:27
    */
    void mobileTeachingPlan(String moveType,Long id);


}
