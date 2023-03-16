package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description 任务处理
 * @date 2023/3/14 2:45
 */
public interface MediaFileProcessService {

    /***
    * @description 获取待处理任务
    * @param shardIndex 分片序号
     * @param shardTotal 分片总数
     * @param count 获取记录数
    * @return java.util.List<com.xuecheng.media.model.po.MediaProcess>
    * @author yzhans
    * @date 2023/3/14 2:48
    */
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);

    /***
    * @description 开启任务
    * @param id 任务id
    * @return boolean true 开启成功 false 开启失败
    * @author yzhans
    * @date 2023/3/14 16:53
    */
    public boolean startTask(long id);

    /**
     * @description 保存任务结果
     * @param taskId  任务id
     * @param status 任务状态
     * @param fileId  文件id
     * @param url url
     * @param errorMsg 错误信息
     * @return void
     * @author Mr.M
     * @date 2022/10/15 11:29
     */
    void saveProcessFinishStatus(Long taskId,String status,String fileId,String url,String errorMsg);
}
