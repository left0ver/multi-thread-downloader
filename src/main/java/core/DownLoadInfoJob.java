package core;

import utils.LogUtils;

import java.util.concurrent.atomic.LongAdder;

public class DownLoadInfoJob implements Runnable {
    // 1MB = 1024*1024B
    private static final int UNIT = 1024 * 1024;


    // 文件总大小
    private long fileTotalSize;

    // 本地已下载文件的大小（可能中间会中断，然后可以断点续传）
    private LongAdder finishSize = new LongAdder();

    // 多个线程会操作这个变量，使用LongAdder保证线程安全
    //这次下载的文件总大小
    private volatile LongAdder downloadSize = new LongAdder();
    // 这一次下载的文件大小（1s前下载的总大小）由于我们是1s一次，因此downloadSize - prevDownloadSize 可以得到这1s之内下载的文件大小
    private long prevDownloadSize = 0;


    public DownLoadInfoJob(long fileTotalSize, long finishSize) {
        this.fileTotalSize = fileTotalSize;

        this.finishSize.add(finishSize);
    }

    @Override
    public void run() {
        //文件总大小
        String fileTotalSizeMB = String.format("%.2f", (double) fileTotalSize / UNIT);

        // 所有已下载的总大小
        String fileDownloadSizeMB = String.format("%.2f", (downloadSize.doubleValue() + finishSize.doubleValue()) / UNIT);
        //剩余的文件大小
        String remainSizeMB = String.format("%.2f", (fileTotalSize - downloadSize.doubleValue() - finishSize.doubleValue()) / UNIT);


        // 下载速度 mb/s
        String downloadRate = String.format("%.1f", (downloadSize.doubleValue() - prevDownloadSize) / 1024 * 8);

        prevDownloadSize = downloadSize.longValue();
        System.out.print("\r");
        LogUtils.print("总文件大小{}MB,已下载文件总大小{}MB,剩余文件大小{}MB,下载速度为{}kb/s", "info", fileTotalSizeMB, fileDownloadSizeMB, remainSizeMB, downloadRate);

    }


    public long getPrevDownloadSize() {
        return prevDownloadSize;
    }

    public void setPrevDownloadSize(long prevDownloadSize) {
        this.prevDownloadSize = prevDownloadSize;
    }

    public long getDownloadSize() {
        return downloadSize.longValue();
    }

    public void addDownloadSize(long addSize) {
        this.downloadSize.add(addSize);
    }
}
