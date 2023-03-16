package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author yzhans
 * @version 1.0
 * @description TODO
 * @date 2023/3/10 15:04
 */
public class BigFileTest {

    @Test
    void testBlock() throws IOException {
        //文件
        File file = new File("D:\\download\\1.mp4");
        //分块目录
        String block = "D:\\download\\fk\\";
        //分块大小
        int blockSize = 1024 * 1024 * 5;
        //分块文件个数
        int blockNum = (int) Math.ceil(file.length() * 1.0 / blockSize);
        //使用流从源文件读数据 向分块文件中写数据
        RandomAccessFile read = new RandomAccessFile(file, "r");
        //缓存区
        byte[] hcq = new byte[1024];
        for (int i = 0; i < blockNum; i++) {
            File file1 = new File(block + i);
            RandomAccessFile writeIn = new RandomAccessFile(file1, "rw");
            int len = -1;
            while ((len = read.read(hcq)) !=-1) {
                writeIn.write(hcq, 0, len);
                if (file1.length() >= blockSize) {
                    break;
                }
            }
            writeIn.close();
        }
        read.close();
    }

    @Test
    void testMerge() throws IOException {
        //分块目录
        File file = new File("D:\\download\\fk\\");
        //源文件
        File sourceFile = new File("D:\\download\\1.mp4");
        //合并的文件
        File mergedFiles = new File("D:\\download\\3.mp4");

        File[] files = file.listFiles();
        //转换成list 用工具类排序
        List<File> list = Arrays.asList(files);

        list.sort(new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        RandomAccessFile rw = new RandomAccessFile(mergedFiles, "rw");
        byte[] hcq =new byte[1024];
        for (File f : list) {
            RandomAccessFile r = new RandomAccessFile(f, "r");
            int len = -1;
            while ((len = r.read(hcq)) != -1) {
                rw.write(hcq, 0, len);
            }
            r.close();
        }
        rw.close();

        //合并进行校验
        FileInputStream inputStream1 = new FileInputStream(sourceFile);
        FileInputStream inputStream2 = new FileInputStream(mergedFiles);
        String s1 = DigestUtils.md5Hex(inputStream1);
        String s2 = DigestUtils.md5Hex(inputStream2);
        if (s1.equals(s2)) {
            System.out.println("是一致的文件");
        }
    }
}
