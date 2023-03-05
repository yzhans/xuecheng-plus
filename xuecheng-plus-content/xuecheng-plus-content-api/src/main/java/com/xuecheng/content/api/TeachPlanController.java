package com.xuecheng.content.api;

import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.model.dto.SaveTeachplanDto;
import com.xuecheng.model.dto.TeachPlanDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 课程计划编辑接口
 * @date 2023/3/5 10:25
 */
@Slf4j
@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
@RequestMapping("/teachplan")
public class TeachPlanController {

    @Resource
    private TeachPlanService teachPlanService;

    @ApiOperation("获取教学计划节点")
    @GetMapping("/{courseId}/tree-nodes")
    public List<TeachPlanDto> getTeachPlanTreeNodes(@PathVariable("courseId") Long courseId) {
        return teachPlanService.getTeachPlanTreeNodes(courseId);
    }

    @ApiOperation("修改-新增教学计划节点")
    @PostMapping()
    public void saveTeachPlan(@RequestBody SaveTeachplanDto saveTeachplanDto) {
        teachPlanService.saveTeachPlan(saveTeachplanDto);
    }

    @ApiOperation("删除教学计划节点")
    @DeleteMapping("/{id}")
    public void deleteTeachPlan(@PathVariable("id") Long id) {
        teachPlanService.deleteTeachPlan(id);
    }
}
