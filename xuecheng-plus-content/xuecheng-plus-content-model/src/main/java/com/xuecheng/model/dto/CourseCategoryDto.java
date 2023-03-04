package com.xuecheng.model.dto;

import com.xuecheng.model.po.CourseCategory;
import lombok.Data;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/2 23:45
 */
@Data
public class CourseCategoryDto extends CourseCategory {

    List childrenTreeNodes;

}
