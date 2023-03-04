package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.model.dto.AddCourseDto;
import com.xuecheng.model.dto.CourseBaseInfoDto;
import com.xuecheng.model.dto.QueryCourseParamsDto;
import com.xuecheng.model.po.CourseBase;
import com.xuecheng.model.po.CourseCategory;
import com.xuecheng.model.po.CourseMarket;
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

    @Override
    public PageResult<CourseBase> queryCourseBaseList(
            PageParams params, QueryCourseParamsDto queryCourseParamsDto) {

        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();

        //拼接查询条件 根据课程名称模糊查询 name like '%名称%'
        queryWrapper.like(
                StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,
                queryCourseParamsDto.getCourseName());

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
    public CourseBaseInfoDto createCourseBase(Long companyId,AddCourseDto addCourseDto) {
        //对参数进行校验
        //由注解校验
        //if (StringUtils.isBlank(addCourseDto.getName())) {
        //    XueChangException.cast("课程名称为空");
        //    //XueChangException.cast(CommonError.QUERY_NULL);
        //}
        //if (StringUtils.isBlank(addCourseDto.getMt())) {
        //    throw new RuntimeException("课程分类为空");
        //}
        //if (StringUtils.isBlank(addCourseDto.getSt())) {
        //    throw new RuntimeException("课程分类为空");
        //}
        //if (StringUtils.isBlank(addCourseDto.getGrade())) {
        //    throw new RuntimeException("课程等级为空");
        //}
        //if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
        //    throw new RuntimeException("教育模式为空");
        //}
        //if (StringUtils.isBlank(addCourseDto.getUsers())) {
        //    throw new RuntimeException("适应人群为空");
        //}
        //if (StringUtils.isBlank(addCourseDto.getCharge())) {
        //    throw new RuntimeException("收费规则为空");
        //}

        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto, courseBase);
        //设置机构id
        courseBase.setCompanyId(companyId);
        //设置创建时间
        courseBase.setCreateDate(LocalDateTime.now());
        //审核状态默认为未提交
        courseBase.setAuditStatus("20202");
        //发布状态默认为未发布
        courseBase.setStatus("20301");
        //插入课程基本信息表
        int number1 = courseBaseMapper.insert(courseBase);
        //获取课程id
        Long id = courseBase.getId();
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto, courseMarket);
        courseMarket.setId(id);
        //设置收费 价格没有输入则抛出异常
        if ("201001".equals(addCourseDto.getCharge())) {
            if (courseMarket.getPrice() == null || courseMarket.getPrice().floatValue() <= 0) {
                //throw new RuntimeException("课程收费价格必须输入且大于0");
                XueChangException.cast("课程收费价格必须输入且大于0");
            }
        }
        //插入课程营销表
        int number2 = courseMarketMapper.insert(courseMarket);
        //判断是否两个都插入成功
        if (number1 <= 0 || number2 <= 0) {
            throw new RuntimeException("插入数据失败");
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
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        return courseBaseInfoDto;
    }


}
