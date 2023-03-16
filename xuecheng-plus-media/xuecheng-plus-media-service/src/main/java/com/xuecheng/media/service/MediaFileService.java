package com.xuecheng.media.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description 媒资文件管理业务类
 * @date 2022/9/10 8:55
 */
public interface MediaFileService {

    /**
     * @param pageParams          分页参数
     * @param queryMediaParamsDto 查询条件
     * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
     * @description 媒资文件查询方法
     * @author Mr.M
     * @date 2022/9/10 8:57
     */
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto);

    /***
     * @description 上传文件
     * @param companyId 企业id
     * @param uploadFileParamsDto 文件的信息
     * @param localFilePath 本地文件路径
     * @return com.xuecheng.media.model.dto.UploadFileResultDto
     * @author yzhans
     * @date 2023/3/9 18:20
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath);


    /***
    * @description 判断md5是否存在数据库或minio
    * @param fileMd5 文件md5
    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    * @author yzhans
    * @date 2023/3/10 18:18
    */
    RestResponse<Boolean> checkFile(String fileMd5);

    /***
     * @description 检查minio里分块是否存在
     * @param fileMd5 分块信息
     * @param chunk 第几个分块
     * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
     * @author yzhans
     * @date 2023/3/10 18:39
     */
    RestResponse<Boolean> checkChunk(String fileMd5, int chunk);

    /***
    * @description 上传分块文件
    * @param fileMd5 文件md5
     * @param chunk 分块数
     * @param localFilePath 本地路径
    * @return com.xuecheng.base.model.RestResponse
    * @author yzhans
    * @date 2023/3/10 18:51
    */
    RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath);

    /**
     * @param companyId           机构id
     * @param fileMd5             文件md5
     * @param chunkTotal          分块总和
     * @param uploadFileParamsDto 文件信息
     * @return com.xuecheng.base.model.RestResponse
     * @description 合并分块
     * @author Mr.M
     * @date 2022/9/13 15:56
     */
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto);

    MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5, String objectName, String bucket);

    /**
     * 从minio下载文件
     *
     * @param bucket     桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket, String objectName);

    /***
     * @description 将文件上传到minio
     * @param localFilePath 本地文件路径
     * @param mimeType 媒体类型
     * @param bucket 桶
     * @param objectName 文件保存名
     * @return java.lang.Boolean
     * @author yzhans
     * @date 2023/3/9 18:40
     */
    public Boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName);
}
