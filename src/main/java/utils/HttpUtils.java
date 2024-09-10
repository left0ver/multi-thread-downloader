package utils;

import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {

    private static final String urlSeparator = "/";

    public static HttpURLConnection getHttpConnection(String url, long startPos, long endPos) throws IOException {
        HttpURLConnection httpConnection = getHttpConnection(url);

        // 使用Range来实现分块下载
        if (endPos != -1) {
            // 中间块的内容
            httpConnection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
        } else {
            // 最后一个块
            httpConnection.setRequestProperty("Range", "bytes=" + startPos + "-");
        }
        return httpConnection;

    }

    public static HttpURLConnection getHttpConnection(String url) throws IOException {
        if (StrUtil.isBlank(url)) {
            throw new RuntimeException("URL不能为空");
        }
        URL httpUrl = new URL(url);

        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows  NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");
        return httpURLConnection;

    }

    public static String getUrlFileName(String url) {
        if (StrUtil.isBlank(url)) {
            throw new RuntimeException("URL不能为空");
        }

        int idx = url.lastIndexOf(urlSeparator);
        return url.substring(idx);
    }
}
