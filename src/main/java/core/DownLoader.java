package core;

import cn.hutool.core.util.StrUtil;
import constant.Constant;
import utils.FileUtils;
import utils.HttpUtils;
import utils.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static constant.Constant.*;


public class DownLoader {


    private final String url;
    private ExecutorService spiltThreadPool = Executors.newFixedThreadPool(THREAD_NUM);

    private CountDownLatch noDownLoadNum = new CountDownLatch(THREAD_NUM);

    public DownLoader(String url) {
        this.url = url;
    }

    public void download() {


        if (StrUtil.isBlank(url)) {
            throw new RuntimeException("URL 不能为空");
        }

        HttpURLConnection httpConnection = null;
        String filePath = DIR_PATH + "/" + HttpUtils.getUrlFileName(url);
        ScheduledExecutorService scheduledThreadPool = null;
        try {
            httpConnection = HttpUtils.getHttpConnection(url);
        } catch (IOException e) {
            throw new RuntimeException("下载失败");
        }

        Long finishDownLoadLength = FileUtils.getLocalFileSize(filePath);
        Long totalLength = httpConnection.getContentLengthLong();

        if (finishDownLoadLength >= totalLength) {
            LogUtils.error("文件 {} 已经存在,无需下载", filePath);
            return;
        }

        try (
                BufferedInputStream bufferedInputStream = new BufferedInputStream(httpConnection.getInputStream());
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        ) {
            // 记录下载的一些信息
            scheduledThreadPool = Executors.newScheduledThreadPool(2);
            DownLoadInfoJob downLoadInfoJob = new DownLoadInfoJob(totalLength, finishDownLoadLength);
            scheduledThreadPool.scheduleAtFixedRate(downLoadInfoJob, 1, 1, TimeUnit.SECONDS);


            ArrayList<Future<Boolean>> spiltDownloadResults = new ArrayList<>();

            long startTime = System.currentTimeMillis();
            spilt(url, spiltDownloadResults, downLoadInfoJob);

            noDownLoadNum.await();

            merge(filePath);


//             download
//            int len = -1;
//            byte[] data = new byte[BUFFER_SIZE];
//            while ((len = bufferedInputStream.read(data)) != -1) {
//                bufferedOutputStream.write(data, 0, len);
//                downLoadInfoJob.addDownloadSize(len);
//            }



            System.out.println("\r");
            LogUtils.info("下载成功");
            long endTime = System.currentTimeMillis();
            LogUtils.info("下载时间为{}ms", endTime - startTime);
        } catch (IOException | InterruptedException e) {
            LogUtils.error("下载失败");
            e.printStackTrace();
        } finally {
            spiltThreadPool.shutdown();
            httpConnection.disconnect();
            if (scheduledThreadPool != null) {
                scheduledThreadPool.shutdownNow();
            }
        }
    }

    // 分块下载
    public void spilt(String url, ArrayList<Future<Boolean>> spiltDownloadResults, DownLoadInfoJob downLoadInfoJob) {

        Long contentLength = FileUtils.getFileContentLength(url);
        // 每块的大小
        long chunk = contentLength / THREAD_NUM;
        for (int i = 0; i < THREAD_NUM; i++) {
            // 每块的起始位置
            long startPos = i * chunk;
            // 最后一块的endPos为-1
            long endPos = -1;

            // 中间的块
            if (i != THREAD_NUM - 1) {
                endPos = startPos + chunk - 1;
            }

            LogUtils.info("第{}块第下载区间为{}-{}", i, startPos, endPos);
            Future<Boolean> spiltDownLoadResult = spiltThreadPool.submit(new DownLoadTask(url, startPos, endPos, i, downLoadInfoJob, noDownLoadNum));
            spiltDownloadResults.add(spiltDownLoadResult);
        }
    }

    /**
     * @param fileName 最终的文件名
     */
    public void merge(String fileName) {
        RandomAccessFile outPutStream = null;
        try {
            outPutStream = new RandomAccessFile(fileName, "rw");
        } catch (FileNotFoundException e) {
            LogUtils.error("file {} not found", fileName);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int i = 0; i < THREAD_NUM; i++) {
            try (
                    BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName + ".temp" + i));
            ) {
                for (int len = -1; (len = inputStream.read(buffer)) != -1; ) {
                    outPutStream.write(buffer, 0, len);
                }
                LogUtils.info("分片{}下载已合并", i);
            } catch (FileNotFoundException e) {
                LogUtils.error("分片下载时，file {} not found", fileName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        LogUtils.info("分片合并完成,最终文件为{}", fileName);
        LogUtils.info("合并成功，开始清除临时文件");
        for (int i = 0; i < THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            if (file.delete()) {
                LogUtils.info("文件{}已清除", fileName + ".temp" + i);
            }
        }

    }
}
