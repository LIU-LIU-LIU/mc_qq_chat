package cc.ahaly.mc.mc_qq_chat.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private static final Logger logger = Logger.getLogger("mc_qq_chat");

    public static void setupLogger(String logFile) {
        try {
            // 创建目录（如果不存在）
            java.io.File file = new java.io.File(logFile);
            java.io.File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL); // 确保 FileHandler 记录所有级别的日志
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // 禁止输出到控制台
            logger.info("日志文件已设置: " + logFile);
        } catch (IOException e) {
            System.err.println("无法设置日志文件: " + logFile);
            e.printStackTrace();
        }
    }
    public static void setLogLevel(Level level) {
        logger.setLevel(level);
    }

    public static void info(String message) {
        logger.info("§3"+message);
    }

    public static void warning(String message) {
        logger.warning("§e" + message);
    }

    public static void fine(String message) {
//        logger.info("§a"+message);
        logger.fine("§a"+message);
    }

    public static void finest(String message) {
        logger.finest("§a"+message);
    }
}
