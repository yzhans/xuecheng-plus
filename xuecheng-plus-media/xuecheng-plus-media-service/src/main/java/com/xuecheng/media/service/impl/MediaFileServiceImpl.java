package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChangException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    private MediaFilesMapper mediaFilesMapper;

    @Resource
    private MediaFileService mediaFileService;

    @Resource
    private MediaProcessMapper mediaProcessMapper;

    @Resource
    private MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String files;

    @Value("${minio.bucket.videofiles}")
    private String videofiles;

    @Override
    public MediaFiles getFileById(String mediaId) {
        return mediaFilesMapper.selectById(mediaId);
    }

    /***
    * @description 分块文件上传接口
    * @param fileMd5 分块文件id
     * @param chunk 分块文件总数
     * @param localFilePath 分块文件本地绝对路径
    * @return com.xuecheng.base.model.RestResponse
    * @author yzhans
    * @date 2023/3/15 21:03
    */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localFilePath) {
        //分块文件路径
        String blockPath = getThePartitionFileDirectory(fileMd5) + chunk;
        //文件类型
        String mimeType = getMimeType("");
        Boolean aBoolean = addMediaFilesToMinIO(localFilePath, mimeType, videofiles, blockPath);
        if (!aBoolean) {
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunk) {
        //根据md5得到分块文件的路径
        String blockPath = getThePartitionFileDirectory(fileMd5);
        //分块文件路径
        String blockFilePath = blockPath + chunk;
        //查找参数
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(videofiles)
                .object(blockFilePath)
                .build();
        InputStream objectResponse = null;
        try {
            //查找
            objectResponse = minioClient.getObject(getObjectArgs);
            if (objectResponse != null) {
                //文件存在
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("发送错误:{}", e.getMessage());
        }
        return RestResponse.success(false);
    }

    /***
    * @description 断点上传检查文件是否在minio 有则返回true 无则false
    * @param fileMd5 文件id
    * @return com.xuecheng.base.model.RestResponse<java.lang.Boolean>
    * @author yzhans
    * @date 2023/3/15 21:02
    */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            //数据库存在则去minio查找
            //查找参数
            try {
                GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                        .bucket(mediaFiles.getBucket())
                        .object(mediaFiles.getFilePath())
                        .build();
                //查找
                FilterInputStream objectResponse = minioClient.getObject(getObjectArgs);
                if (objectResponse != null) {
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("发送错误:{}", e.getMessage());
            }
        }
        return RestResponse.success(false);
    }

    /***
    * @description 查询已上传的文件列表
    * @param companyId 企业id
     * @param pageParams 分页参数
     * @param queryMediaParamsDto 查询条件
    * @return com.xuecheng.base.model.PageResult<com.xuecheng.media.model.po.MediaFiles>
    * @author yzhans
    * @date 2023/3/15 21:00
    */
    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();
        Long i = 1232141425L;
        String auditStatus = queryMediaParamsDto.getAuditStatus();
        String type = queryMediaParamsDto.getType();
        String filename = queryMediaParamsDto.getFilename();
        queryWrapper.eq(MediaFiles::getCompanyId, i)
                .like(StringUtils.isNotEmpty(filename),MediaFiles::getFilename, filename)
                .eq(StringUtils.isNotEmpty(type),MediaFiles::getFileType, type)
                .eq(StringUtils.isNotEmpty(auditStatus),MediaFiles::getAuditStatus, auditStatus);

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        return new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
    }

    /***
    * @description 上传文件并保存文件信息到mediaFile表
    * @param companyId 企业id
     * @param uploadFileParamsDto 文件参数信息
     * @param localFilePath 上传端文件绝对路径
     * @param objectName 对象名 有则存储在指定位置
    * @return com.xuecheng.media.model.dto.UploadFileResultDto
    * @author yzhans
    * @date 2023/3/15 20:59
    */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath,String objectName) {
        //获取文件名
        String filename = uploadFileParamsDto.getFilename();
        //得到后缀名
        String suffix = filename.substring(filename.lastIndexOf("."));
        //获取媒体类型
        String mimeType = getMimeType(suffix);
        //获取文件目录
        String route = getDefaultFolderPath();
        //获取md5 拼接在路径后面
        String fileMd5 = getFileMd5(new File(localFilePath));
        //拼接文件名
        if (StringUtils.isEmpty(objectName)) {
            objectName = route + fileMd5 + suffix;
        }
        //将文件上传到minio中
        Boolean aBoolean = addMediaFilesToMinIO(localFilePath, mimeType, files, objectName);
        if (!aBoolean) {
            XueChangException.cast("上传文件失败");
        }
        //将文件信息保存到数据库中
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, objectName, files);
        if (mediaFiles == null) {
            XueChangException.cast("文件上传后保存信息失败");
        }
        //准备返回对象
        UploadFileResultDto FileParamsDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, FileParamsDto);
        return FileParamsDto;
    }

    /***
    * @description 合并文件 并且删除上传的分块文件
    * @param companyId 企业id
     * @param fileMd5 文件md5 也是表id
     * @param chunkTotal 分块总数
     * @param uploadFileParamsDto 文件参数
    * @return com.xuecheng.base.model.RestResponse
    * @author yzhans
    * @date 2023/3/15 20:57
    */
    @Override
    public RestResponse mergechunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        //分块文件目录
        String catalogue = getThePartitionFileDirectory(fileMd5);
        //合并指定分块的信息
        List<ComposeSource> list = Stream
                .iterate(0, i -> ++i)
                .limit(chunkTotal)
                .map(i -> ComposeSource
                        .builder()
                        .bucket(videofiles)
                        .object(catalogue + i)
                        .build())
                .collect(Collectors.toList());
        //获取源文件名字
        String filename = uploadFileParamsDto.getFilename();
        //获取后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        //获取合并文件路径
        String filePath = getThePathToSaveTheMergedFile(fileMd5, suffix);
        //合并后的信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket(videofiles)
                .object(filePath)
                .sources(list)//指定源文件
                .build();
        try {
            minioClient.composeObject(composeObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并失败,bucket:{},filePath:{},错误信息:{}", videofiles, filePath, e.getMessage());
            return RestResponse.validfail(false, "合并文件异常");
        }
        // ====验证md5====
        //下载合并后的文件
        //File minioFile = downloadFileFromMinIO(videofiles,filePath);
        //if(minioFile == null){
        //    log.debug("下载合并后文件失败,mergeFilePath:{}",filePath);
        //    return RestResponse.validfail(false, "下载合并后文件失败。");
        //}
        //
        //try (InputStream newFileInputStream = new FileInputStream(minioFile)) {
        //    //minio上文件的md5值
        //    String md5Hex = DigestUtils.md5Hex(newFileInputStream);
        //    //比较md5值，不一致则说明文件不完整
        //    if(!fileMd5.equals(md5Hex)){
        //        return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        //    }
        //    //文件大小
        //    uploadFileParamsDto.setFileSize(minioFile.length());
        //}catch (Exception e){
        //    log.debug("校验文件失败,fileMd5:{},异常:{}",fileMd5,e.getMessage(),e);
        //    return RestResponse.validfail(false, "文件合并校验失败，最终上传失败。");
        //}finally {
        //    if(minioFile!=null){
        //        minioFile.delete();
        //    }
        //}
        LocalDateTime s1 = LocalDateTime.now();
        //文件入库
        MediaFiles mediaFiles = mediaFileService.addMediaFilesToDb(companyId, uploadFileParamsDto, fileMd5, filePath, videofiles);
        if (mediaFiles == null) {
            return RestResponse.validfail(false, "文件入库失败");
        }
        //清理分块文件
        cleanUpBlockFiles(catalogue, chunkTotal);
        //执行完成
        LocalDateTime s2 = LocalDateTime.now();
        System.err.println(s2);
        System.err.println(s1);
        return RestResponse.success(true);
    }

    /***
     * @description 清理分块文件
     * @param blockFilePath 分块文件路径
     * @param totalNumberOfBlocks 分块的总数
     * @return void
     * @author yzhans
     * @date 2023/3/11 2:00
     */
    private void cleanUpBlockFiles(String blockFilePath, int totalNumberOfBlocks) {
        try {
            List<DeleteObject> deleteObjects = Stream.iterate(0, i -> ++i)
                    .limit(totalNumberOfBlocks)
                    .map(i -> new DeleteObject(blockFilePath.concat(Integer.toString(i))))
                    .collect(Collectors.toList());

            RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(videofiles).objects(deleteObjects).build();
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);
            results.forEach(r->{
                DeleteError deleteError = null;
                try {
                    deleteError = r.get();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("清楚分块文件失败,objectname:{}",deleteError.objectName(),e);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("清楚分块文件失败,chunkFileFolderPath:{}",blockFilePath,e);
        }
    }

    /**
     * 从minio下载文件
     * @param bucket 桶
     * @param objectName 对象名称
     * @return 下载后的文件
     */
    public File downloadFileFromMinIO(String bucket,String objectName){
        //临时文件
        File minioFile = null;
        FileOutputStream outputStream = null;
        try{
            InputStream stream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            //创建临时文件
            minioFile=File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(stream,outputStream);
            return minioFile;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /***
     * @description 向数据库保存文件信息
     * @param companyId 企业id
     * @param uploadFileParamsDto 文件信息
     * @param fileMd5 文件MD5
     * @param objectName 文件目录加名字
     * @param bucket 桶
     * @return com.xuecheng.media.model.po.MediaFiles
     * @author yzhans
     * @date 2023/3/9 19:22
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, UploadFileParamsDto uploadFileParamsDto, String fileMd5, String objectName, String bucket) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null) {
            return mediaFiles;
        }
        mediaFiles = new MediaFiles();
        BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
        //文件id
        mediaFiles.setId(fileMd5);
        //机构id
        mediaFiles.setCompanyId(companyId);
        //桶
        mediaFiles.setBucket(bucket);
        //file-path
        mediaFiles.setFilePath(objectName);
        //file_id
        mediaFiles.setFileId(fileMd5);
        //url
        mediaFiles.setUrl("/" + bucket + "/" + objectName);
        //上传时间
        mediaFiles.setCreateDate(LocalDateTime.now());
        //状态
        mediaFiles.setStatus("1");
        //审核状态
        mediaFiles.setAuditStatus("002003");
        //插入数据
        int insert = mediaFilesMapper.insert(mediaFiles);
        if (insert <= 0) {
            log.debug("向数据库保存文件信息失败,bucket:{},objectName:{}", bucket, objectName);
            return null;
        }
        //记录待处理任务
        addWaitingTask(mediaFiles);
        return mediaFiles;
    }

    /***
     * @description 添加待处理任务
     * @param mediaFiles 媒体信息
     * @return void
     * @author yzhans
     * @date 2023/3/13 19:25
     */
    private void addWaitingTask(MediaFiles mediaFiles) {
        //获取type
        //获取文件名称
        String filename = mediaFiles.getFilename();
        //文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        //文件后缀类型
        String mimeType = getMimeType(extension);
        //通过type判断是否为avi视频
        if ("video/x-msvideo".equals(mimeType)) {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles, mediaProcess);
            mediaProcess.setStatus("1");
            mediaProcess.setCreateDate(LocalDateTime.now());
            mediaProcess.setFailCount(0);
            mediaProcess.setUrl(null);
            //向MediaProcess插入记录
            mediaProcessMapper.insert(mediaProcess);
        }
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //获取文件默认存储目录路径 年/月/日
    private String getDefaultFolderPath() {
        //设置格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前时间切割拼接成 年/月/日
        return simpleDateFormat.format(new Date()).replace("-", "/") + "/";
    }

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
    public Boolean addMediaFilesToMinIO(String localFilePath, String mimeType, String bucket, String objectName) {
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)//桶
                    .filename(localFilePath)//指定本地上传文件路径
                    .object(objectName)//文件保存名
                    .contentType(mimeType)
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错,bucket:{},objectName:{},错误信息:{}", bucket, objectName, e.getMessage());
        }
        return false;
    }

    /***
     * @description 获取后缀媒体类型
     * @param suffix 文件后缀名
     * @return java.lang.String
     * @author yzhans
     * @date 2023/3/9 18:41
     */
    private String getMimeType(String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        //根据扩展名取mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(suffix);
        //通用mimeType 字节流
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null) {
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    /***
    * @description 获取保存合并文件的路径
    * @param fileMd5 文件md5值
     * @param suffix 后缀名
    * @return java.lang.String
    * @author yzhans
    * @date 2023/3/15 2:13
    */
    private String getThePathToSaveTheMergedFile(String fileMd5,String suffix) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + suffix;
    }

    /***
    * @description 获取分区文件目录
    * @param fileMd5 文件md5值
    * @return java.lang.String
    * @author yzhans
    * @date 2023/3/15 2:13
    */
    private String getThePartitionFileDirectory(String fileMd5) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + "block" + "/";
    }
}
