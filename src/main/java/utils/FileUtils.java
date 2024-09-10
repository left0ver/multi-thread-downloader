package utils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;

public class FileUtils {
    public static Long getLocalFileSize(String path) {
        File file = new File(path);

        if (file.exists() && file.isDirectory()) {
            throw new RuntimeException("文件" + path + "是一个目录");
        }
        // 文件不存在则说明以前没下载过，文件存在则说明下载过，需要接着下
        return file.exists() && file.isFile() ? file.length() : 0;
    }

    /**
     * 获取要下载的文件的总大小
     *
     * @param url 下载文件的地址
     * @return
     */
    public static Long getFileContentLength(String url) {
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = HttpUtils.getHttpConnection(url);
            return httpConnection.getContentLengthLong();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
