import cn.hutool.core.util.StrUtil;
import core.DownLoader;

import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        String url = null;
        if (args.length > 0 && null != args[0]) {
            url = args[0];
        }
        Scanner sc = new Scanner(System.in);
        while (StrUtil.isBlank(url)) {
            System.out.println("请输入URL：");
            url = sc.next();
        }
        ;
        url = url.trim();
        new DownLoader(url).download();
    }


}
