package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author yzhans
 * @version 1.0
 * @description 媒资文件管理接口
 * @date 2023/3/18 2:46
 */
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
@RequestMapping("/open")
public class MediaOpenController {

    @Autowired
    MediaFileService mediaFileService;

    @Resource
    private MinioClient minioClient;

    @Resource
    private MediaFilesMapper mediaFilesMapper;

    @ApiOperation("预览文件")
    @GetMapping("/preview/{mediaId}")
    public RestResponse<String> getPlayUrlByMediaId(@PathVariable String mediaId){
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null) {
            return RestResponse.validfail("找不到视频");
        }
        //取出视频播放url
        String url = mediaFiles.getUrl();
        if (StringUtils.isEmpty(url)) {
            return RestResponse.validfail("该视频正在处理中");
        }
        return RestResponse.success(mediaFiles.getUrl());
    }


    @ApiOperation("删除文件")
    @DeleteMapping("/{mediaId}")
    public RestResponse<String> deleteFiles(@PathVariable String mediaId){
        MediaFiles mediaFiles = mediaFileService.getFileById(mediaId);
        if (mediaFiles == null) {
            return RestResponse.validfail("找不到视频");
        }
        //取出视频名字
        //取出桶id
        String bucket = mediaFiles.getBucket();
        //获取路径
        String filePath = mediaFiles.getFilePath();

        try {
            //删除数据库
            int i = mediaFilesMapper.deleteById(mediaId);
            if (i == 0) {
                return RestResponse.validfail("删除失败");
            }
            //删除文件的参数
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .build();
            //删除文件
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.success();

    }
}
