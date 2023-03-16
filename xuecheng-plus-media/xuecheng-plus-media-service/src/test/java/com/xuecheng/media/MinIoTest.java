package com.xuecheng.media;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.po.MediaFiles;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/9 15:55
 */
public class MinIoTest {


    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://110.41.137.10:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    void testUpload() throws Exception {
        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testsc")//桶
                .filename("D:\\download\\psbw.mp3")//指定本地上传文件路径
                .object("psbw.mp3")//对象名
                .build();


        //上传文件
        minioClient.uploadObject(uploadObjectArgs);

    }

    @Test
    void testDelete() throws Exception {

        //删除文件的参数
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testsc")
                .object("psbw.mp3")
                .build();
        //删除文件
        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    void testLookup() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        //查找参数
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket("testsc")
                .object("psbw.mp3")
                .build();

        //查找
        FilterInputStream objectResponse = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\download\\c\\1.mp3"));

        IOUtils.copy(objectResponse, outputStream);
    }

    //将分块上传

    @Test
    void uploadBlocks() throws Exception {
        for (int i = 0; i < 9; i++) {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testsc")//桶
                    .filename("D:\\download\\fk\\" + i)//指定本地上传文件路径
                    .object("block/" + i)//对象名
                    .build();
            minioClient.uploadObject(uploadObjectArgs);

            System.out.println("上传分块" + i + "成功");
        }
    }

    //合并


    @Test
    void mergeFiles() throws Exception {

        //合并指定分块的信息
        List<ComposeSource> list = Stream
                .iterate(0, i -> ++i)
                .limit(9)
                .map(i -> ComposeSource.builder()
                        .bucket("testsc")
                        .object("block/" + i)
                        .build())
                .collect(Collectors.toList());

        //合并后的信息
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs
                .builder()
                .bucket("testsc")
                .object("merge.mp4")
                .sources(list)//指定源文件
                .build();
        minioClient.composeObject(composeObjectArgs);
    }
}
