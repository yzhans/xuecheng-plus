package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 视频处理任务
 */
@Slf4j
@Component
public class VideoTask {
    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);

    @Resource
    private MediaFileService mediaFileService;

    @Resource
    private MediaFileProcessService mediaFileProcessService;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegPath;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler() throws Exception {

        // 分片参数
        // 执行器的序号 从0开始
        int shardIndex = XxlJobHelper.getShardIndex();
        // 执行器的总数
        int shardTotal = XxlJobHelper.getShardTotal();
        //获取cpu的核心数
        int processors = Runtime.getRuntime().availableProcessors();
        //查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        //获取任务数量 用于开启多少个线程池
        int size = mediaProcessList.size();
        log.debug("取到的任务数:{}", size);
        if (size <= 0) {
            return;
        }
        //创建线程池来处理任务
        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        //计数器 在线程池中所有的线程都结束才能结束这个方法 大小用size设置
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mp -> {
            //将任务加入线程池
            threadPool.execute(() -> {
                try {
                    Long taskId = mp.getId();              //任务id
                    String fileId = mp.getFileId();        //文件id 也就是md5
                    String bucket = mp.getBucket();        //桶
                    String objectName = mp.getFilePath();  //文件保存路径
                    //开启任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b) {
                        log.debug("抢占任务失败,任务id:{}", taskId);
                        return;
                    }
                    //执行视频转码
                    //下载minio上的视频 获取源avi视频的路径
                    File file = mediaFileService.downloadFileFromMinIO(bucket, objectName);
                    if (file == null) {
                        log.debug("下载视频出错，任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频出错");
                        return;
                    }
                    //下载后文件的绝对路径
                    String absolutePath = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //创建一个临时文件 作为转换后的文件
                    File minio = null;
                    try {
                        minio = File.createTempFile("minio", ".mp4");
                    } catch (IOException e) {
                        log.debug("创建临时文件异常:{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件异常");
                        return;
                    }
                    //转换后mp4文件的路径
                    String mp4_path = minio.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegPath, absolutePath, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if (!"success".equals(result)) {
                        log.debug("视频转换失败,任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, result);
                        return;
                    }
                    String savePath = getThePathToSaveTheMergedFile(fileId, ".mp4");
                    //上传到minio
                    Boolean uploadJudgment = mediaFileService.addMediaFilesToMinIO(mp4_path, "video/mp4", bucket, savePath);
                    if (!uploadJudgment) {
                        log.debug("视频上传失败,任务id:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频上传失败");
                        return;
                    }
                    //清除avi

                    //获取url
                    String url = "/" + bucket + "/" + getThePathToSaveTheMergedFile(fileId, ".mp4");
                    //保存到任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                } finally {
                    //计算器减一
                    countDownLatch.countDown();
                }
            });
        });
        //阻塞 指定最大超时时间 超过就自动解除阻塞
        countDownLatch.await(30, TimeUnit.MINUTES);
    }

    /***
     * @description 获取保存合并文件的路径
     * @param fileMd5 文件md5值
     * @param suffix 后缀名
     * @return java.lang.String
     * @author yzhans
     * @date 2023/3/15 2:13
     */
    private String getThePathToSaveTheMergedFile(String fileMd5, String suffix) {
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + suffix;
    }
}
