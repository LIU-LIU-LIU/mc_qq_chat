package cc.ahaly.mc.mc_qq_chat.velocity;

import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.qqbot.ProxyAdapter;
import cc.ahaly.mc.mc_qq_chat.util.qqbot.WebSocketApi;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;

@Plugin(
        id = "mc_qq_chat",
        name = "mc_qq_chat",
        version = "2.0-SNAPSHOT",
        description = "MC QQ 频道聊天同步插件 - Velocity 版",
        authors = {"ahaly"}
)
public class VelocityMain {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private WebSocketApi webSocketApi;

    public static String channelName;
    public static String channelUrl;
    public static String botToken;
    public static int guildsIndex;

    // Velocity的插件消息通道标识符
    public static final MinecraftChannelIdentifier IDENTIFIER = 
            MinecraftChannelIdentifier.create("mc_qq_chat", "bungeecord");

    @Inject
    public VelocityMain(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // 加载配置文件
        Map<String, Object> config = loadConfig();

        String logLevel = getConfigValue(config, "log-level", "INFO");
        String logFile = getConfigValue(config, "logFile", "plugins/mc_qq_chat/run.log");
        botToken = getConfigValue(config, "botToken", "Bot 123456.abcefg");
        guildsIndex = Integer.parseInt(getConfigValue(config, "guildsIndex", "0"));
        channelName = getConfigValue(config, "channelName", "QQ频道聊天室");
        channelUrl = getConfigValue(config, "channelUrl", "https://pd.qq.com/s/8tjjog2zh");
        String intents = getConfigValue(config, "intents", "MESSAGE_CREATE");

        LoggerUtil.setupLogger(logFile);
        try {
            Level level = Level.parse(logLevel.toUpperCase());
            LoggerUtil.setLogLevel(level);
        } catch (IllegalArgumentException e) {
            LoggerUtil.warning("无效的日志级别: " + logLevel + "，将使用默认的日志级别 INFO");
            LoggerUtil.setLogLevel(Level.INFO);
        }

        // 注册插件消息通道
        server.getChannelRegistrar().register(IDENTIFIER);

        // 注册事件监听器
        VelocityFun velocityFun = new VelocityFun(server);
        server.getEventManager().register(this, velocityFun);

        // 创建代理适配器
        ProxyAdapter adapter = new ProxyAdapter() {
            @Override
            public void sendMessageToMinecraft(String channelName, String userName, String avatar, String msg, String msgDate, String message_id) {
                velocityFun.sendMsgToMC(channelName, userName, avatar, msg, msgDate, message_id);
            }

            @Override
            public String getChannelName() {
                return VelocityMain.channelName;
            }
        };

        // 建立 ws 链接（异步，避免阻塞启动）
        try {
            LoggerUtil.info("正在连接 QQ 机器人...");
            logger.info("正在连接 QQ 机器人...");
            
            webSocketApi = new WebSocketApi(botToken, intents, adapter);
            // 连接到 WebSocket 服务器
            webSocketApi.connectWebSocket();
        } catch (Exception e) {
            LoggerUtil.warning("QQ 机器人连接失败，但插件仍正常运行。错误信息: " + e.getMessage());
            logger.warn("QQ 机器人连接失败，但插件仍正常运行。错误信息: " + e.getMessage());
            logger.warn("请检查配置文件中的 botToken 是否正确");
            logger.warn("格式应为: Bot APPID.Token");
            // 不抛出异常，让插件继续运行
        }
        
        LoggerUtil.info("mc_qq_chat插件已经在Velocity服务器上启用");
        logger.info("mc_qq_chat插件已经在Velocity服务器上启用");
        logger.info("日志文件: " + logFile);
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        // 在 Velocity 中的禁用
        server.getChannelRegistrar().unregister(IDENTIFIER);
        if (webSocketApi != null) {
            webSocketApi.stopWebSocket();
        }
        LoggerUtil.info("mc_qq_chat插件已经在Velocity服务器上禁用");
        logger.info("mc_qq_chat插件已经在Velocity服务器上禁用");
    }

    private Map<String, Object> loadConfig() {
        // 确保数据文件夹存在
        File dataFolder = dataDirectory.toFile();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // 定义配置文件的路径
        File configFile = new File(dataFolder, "config.yml");

        // 如果配置文件不存在，从JAR文件中复制默认配置文件
        if (!configFile.exists()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml");
                 FileOutputStream outputStream = new FileOutputStream(configFile)) {
                if (inputStream != null) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                logger.error("无法创建默认配置文件", e);
            }
        }

        // 加载配置文件
        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            return yaml.load(inputStream);
        } catch (IOException e) {
            logger.error("无法加载配置文件", e);
            return Map.of(); // 返回空配置
        }
    }

    private String getConfigValue(Map<String, Object> config, String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public ProxyServer getServer() {
        return server;
    }
}

