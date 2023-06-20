package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.service.TeachPlanService;
import com.xuecheng.model.dto.BindTeachPlanMediaDto;
import com.xuecheng.model.dto.SaveTeachplanDto;
import com.xuecheng.model.dto.TeachPlanDto;
import com.xuecheng.model.po.Teachplan;
import com.xuecheng.model.po.TeachplanMedia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 教学计划服务实施
 * @date 2023/3/5 11:32
 */
@Slf4j
@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Resource
    private TeachplanMapper teachplanMapper;

    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachPlanDto> getTeachPlanTreeNodes(Long courseId) {
        return teachplanMapper.getTeachPlanTreeNodes(courseId);
    }

    @Override
    public void saveTeachPlan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null) {
            //为空则新增
            teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            int count = getTeachPlanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            teachplan.setOrderby(++count);
            teachplanMapper.insert(teachplan);
        } else {
            //修改
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    public void deleteTeachPlan(Long id) {
        if (id == null) {
            XueChangException.cast("课程计划id为空");
        }
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan.getGrade() == 1) {
            LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Teachplan::getParentid, id);
            int count = teachplanMapper.selectCount(wrapper);
            if (count > 0) {
                XueChangException.cast("课程计划信息还有子级信息，无法操作");
            }
            teachplanMapper.deleteById(id);
        } else {
            teachplanMapper.deleteById(id);
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
            teachplanMediaMapper.delete(queryWrapper);
        }
    }

    @Override
    public void deleteTeachPlanVideo(String teachPlanId,String id) {
        if (id == null && teachPlanId == null) {
            XueChangException.cast("删除失败");
        }
        LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TeachplanMedia::getMediaId, id);
        queryWrapper.eq(TeachplanMedia::getTeachplanId, teachPlanId);
        int i = teachplanMediaMapper.delete(queryWrapper);
        if (i <= 0) {
            XueChangException.cast("删除失败");
        }
    }

    @Override
    public void mobileTeachingPlan(String moveType, Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        // 获取层级和当前orderby，章节移动和小节移动的处理方式不同
        Integer grade = teachplan.getGrade();
        Integer orderby = teachplan.getOrderby();
        // 章节移动是比较同一课程id下的orderby
        Long courseId = teachplan.getCourseId();
        // 小节移动是比较同一章节id下的orderby
        Long parentid = teachplan.getParentid();
        if ("moveup".equals(moveType)) {
            if (grade == 1) {
                //章节上移
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("limit 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                //交换
                exchangeSort(teachplan, tmp);
            } else if (grade == 2) {
                //小节上移
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getParentid, parentid)
                        .eq(Teachplan::getGrade, grade)
                        .lt(Teachplan::getOrderby, orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("limit 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                //交换
                exchangeSort(teachplan, tmp);
            }
        } else if ("movedown".equals(moveType)) {
            if (grade == 1) {
                //章节上移
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getCourseId, courseId)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("limit 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                //交换
                exchangeSort(teachplan, tmp);
            } else if (grade == 2) {
                //小节上移
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getParentid, parentid)
                        .eq(Teachplan::getGrade, grade)
                        .gt(Teachplan::getOrderby, orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("limit 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                //交换
                exchangeSort(teachplan, tmp);
            }
        }


    }

    //交换两个Teachplan的orderby
    private void exchangeSort(Teachplan formerTeachplan, Teachplan rearTeachplan) {
        if (rearTeachplan == null) {
            XueChangException.cast("已经到头啦，不能再移啦");
        }
        Integer front = formerTeachplan.getOrderby();
        Integer rear = rearTeachplan.getOrderby();
        formerTeachplan.setOrderby(rear);
        rearTeachplan.setOrderby(front);
        teachplanMapper.updateById(formerTeachplan);
        teachplanMapper.updateById(rearTeachplan);
    }

    //获取orderby
    private int getTeachPlanCount(Long courseId, Long parentId) {
        LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teachplan::getCourseId, courseId);
        wrapper.eq(Teachplan::getParentid, parentId);
        return teachplanMapper.selectCount(wrapper);
    }

    @Transactional
    @Override
    public void associationMedia(BindTeachPlanMediaDto bindTeachplanMediaDto) {
        //课程id
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        if (teachplan == null) {
            XueChangException.cast("课程计划不存在");
        }
        Integer grade = teachplan.getGrade();
        if (grade != 2) {
            XueChangException.cast("只允许第二级教学计划绑定媒资文件");
        }
        //课程id
        Long courseId = teachplan.getCourseId();
        //先删除原来该教学计划绑定的媒资
        teachplanMediaMapper.delete(new LambdaQueryWrapper<TeachplanMedia>()
                .eq(TeachplanMedia::getTeachplanId, teachplanId));
        //添加新记录
        TeachplanMedia teachplanMedia = new TeachplanMedia();
        BeanUtils.copyProperties(bindTeachplanMediaDto, teachplanMedia);
        teachplanMedia.setCourseId(teachplan.getCourseId());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMediaMapper.insert(teachplanMedia);
    }
}
