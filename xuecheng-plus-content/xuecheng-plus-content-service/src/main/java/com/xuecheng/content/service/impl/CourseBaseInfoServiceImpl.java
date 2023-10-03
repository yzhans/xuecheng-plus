package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CourseTeacherService;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.EditCourseDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 13:47
 */
@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Resource
    private CourseMarketMapper courseMarketMapper;

    @Resource
    private CourseCategoryMapper courseCategoryMapper;

    @Resource
    private CourseMarketServiceImpl courseMarketService;

    @Resource
    private CourseTeacherMapper courseTeacherMapper;

    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(
            Long companyId, PageParams params, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //拼接查询条件 根据课程名称模糊查询 name like '%名称%'
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());

        //根据培训机构id拼装查询条件
        queryWrapper.eq(
                companyId == null,
                CourseBase::getCompanyId,
                companyId);

        //课程审核状态
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,
                queryCourseParamsDto.getAuditStatus());

        //课程发布状态
        queryWrapper.eq(
                StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getStatus,
                queryCourseParamsDto.getPublishStatus());

        //分页参数
        Page<CourseBase> page = new Page<>(params.getPageNo(), params.getPageSize());

        //分页查询
        Page<CourseBase> pagerResult = courseBaseMapper.selectPage(page, queryWrapper);

        //数据
        List<CourseBase> finalData = pagerResult.getRecords();
        //总记录数
        long total = pagerResult.getTotal();

        //准备返回数据 List<T> items, long counts, long page, long pageSize

        return new PageResult<>(finalData, total, params.getPageNo(), params.getPageSize());

    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("202002");
        //发布状态默认为未发布
        courseBase.setStatus("203001");
        //插入课程基本信息表
        int number1 = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long id = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        courseMarket.setId(id);
        int number2 = insertCourseMarket(courseMarket);
        //判断是否两个都插入成功
        if (number1 <= 0 || number2 <= 0) {
            XueChangException.cast("插入数据失败");
        }
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        //获取分类名称
        CourseCategory mt = courseCategoryMapper.selectById(courseBase.getMt());
        CourseCategory st = courseCategoryMapper.selectById(courseBase.getSt());
        if (mt != null) {
            String mtName = mt.getName();
            courseBaseInfoDto.setMtName(mtName);
        }
        if (st != null) {
            String stName = st.getName();
            courseBaseInfoDto.setMtName(stName);
        }
        //拷贝已添加完成的数据数据到dto并返回
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        return courseBaseInfoDto;
    }

    private int insertCourseMarket(CourseMarket courseMarket) {
        //如果为空抛出自定义异常
        if (StringUtils.isBlank(courseMarket.getCharge())) {
            XueChangException.cast("收费规则没有选择");
        }
        //设置收费 价格没有输入则抛出异常
        if ("201001".equals(courseMarket.getCharge())) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                //throw new RuntimeException("课程收费价格必须输入且大于0");
                XueChangException.cast("课程收费价格必须输入且大于0");
            }
        }
        //插入课程营销表
        boolean b = courseMarketService.saveOrUpdate(courseMarket);
        return b ? 1 : 0;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto courseBaseInfoService(Long courseId) {
        //课程基本信息
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        //课程营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        //组成要返回的数据
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase, courseBaseInfoDto);
        if (courseMarket != null) {
            BeanUtils.copyProperties(courseMarket, courseBaseInfoDto);
        }
        //向分类的名称查询出来
        CourseCategory courseCategory = courseCategoryMapper.selectById(courseBase.getMt());//一级分类
        courseBaseInfoDto.setMtName(courseCategory.getName());
        CourseCategory courseCategory2 = courseCategoryMapper.selectById(courseBase.getSt());//二级分类
        courseBaseInfoDto.setStName(courseCategory2.getName());
        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBaseById(Long companyId, EditCourseDto editCourseDto) {
        //校验
        //课程id
        Long id = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(id);
        if (courseBase == null) {
            XueChangException.cast("课程不存在");
        }

        //校验本机构只能修改本机构的课程
        if (!courseBase.getCompanyId().equals(companyId)) {
            XueChangException.cast("本机构只能修改本机构的课程");
        }

        //封装基本信息的数据
        BeanUtils.copyProperties(editCourseDto, courseBase);
        courseBase.setChangeDate(LocalDateTime.now());

        //更新课程基本信息
        int i1 = courseBaseMapper.updateById(courseBase);

        //封装营销信息的数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto, courseMarket);

        insertCourseMarket(courseMarket);

        //查询课程信息
        return this.courseBaseInfoService(id);
    }


    @Transactional
    @Override
    public void deleteCourseBaseById(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (!companyId.equals(courseBase.getCompanyId()))
            XueChangException.cast("只允许删除本机构的课程");
        //删除教师信息
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(wrapper);
        //删除教学计划
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(queryWrapper);
        //删除营销信息
        courseMarketMapper.deleteById(courseId);
        //删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }
}
