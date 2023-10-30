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
            FileHandler fileHandler = new FileHandler(logFile, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setLogLevel(Level level) {
        logger.setLevel(level);
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void fine(String message) {
        logger.info(message);
//调试临时禁用        logger.fine(message);
    }

    public static void finest(String message) {
        logger.finest(message);
    }
}
