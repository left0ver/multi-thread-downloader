package core;

import utils.FileUtils;
import utils.HttpUtils;
import utils.LogUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

import static constant.Constant.BUFFER_SIZE;
import static constant.Constant.DIR_PATH;

public class DownLoadTask implements Callable<Boolean> {

    private String url;
    private long startPos;
    private long endPos;
    private int part;
    private CountDownLatch noDownLoadNum;
    private DownLoadInfoJob downLoadInfoJob;

    public DownLoadTask(String url, long startPos, long endPos, int part, DownLoadInfoJob downLoadInfoJob, CountDownLatch noDownLoadNum) {
        this.endPos = endPos;
        this.part = part;
        this.startPos = startPos;
        this.url = url;
        this.downLoadInfoJob = downLoadInfoJob;
        this.noDownLoadNum = noDownLoadNum;
    }

    @Override
    public Boolean call() throws IOException {
        String filePath = DIR_PATH + HttpUtils.getUrlFileName(url) + ".temp" + part;

        File tempFile = new File(filePath);
        long tempFileLength = 0;
        //原先下载过，因此临时文件存在，则断点续传
        if (tempFile.exists()) {
            tempFileLength = tempFile.length();
            downLoadInfoJob.addDownloadSize(tempFileLength);
        }
        // 跳过读取前面已下载的字节
        startPos += tempFileLength;
        HttpURLConnection httpConnection = HttpUtils.getHttpConnection(url, startPos, endPos);

        try (
                BufferedInputStream bufferedInputStream = new BufferedInputStream(httpConnection.getInputStream());
                RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
        ) {
            byte[] data = new byte[BUFFER_SIZE];
            // 跳过写入前面已下载的字节
            randomAccessFile.seek(tempFileLength);
            randomAccessFile.skipBytes()
            for (int len = -1; (len = bufferedInputStream.read(data)) != -1; ) {
                randomAccessFile.write(data, 0, len);
                // 下载总量累加
                downLoadInfoJob.addDownloadSize(len);
            }
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.error("file {} not found", filePath);
            return false;
        } catch (IOException e) {
            LogUtils.error("下载失败");
            return false;
        } finally {
            httpConnection.disconnect();
            noDownLoadNum.countDown();

        }
    }
}
