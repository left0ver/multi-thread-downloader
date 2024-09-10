package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {

    public static void info(String msg, Object... args) {
        println(msg, "info", args);
    }

    public static void error(String msg, Object... args) {
        println(msg, "error", args);
    }

    public static void print(String msg, String level, Object... args) {

        String newMsg = String.format(msg.replace("{}", "%s"), args);


        String threadName = Thread.currentThread().getName();


        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now());


        System.out.print(String.format("%s,%s %s %s", time, threadName + "线程", "--" + level + "--", newMsg));

    }

    public static void println(String msg, String level, Object... args) {

        String newMsg = String.format(msg.replace("{}", "%s"), args);


        String threadName = Thread.currentThread().getName();


        String time = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").format(LocalDateTime.now());


        System.out.println(String.format("%s,%s %s %s", time, threadName + "线程", "--" + level + "--", newMsg));

    }
}
