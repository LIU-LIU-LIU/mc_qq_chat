package cc.ahaly.mc.mc_qq_chat.bungee;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.qqbot.WebSocketApi;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class BungeeMain extends Plugin implements Listener {
    private WebSocketApi webSocketApi;

    public static int guildsIndex;
    public static String channelName;
    public static String channelUrl;
    public static String botToken;

    private Configuration loadConfig(){
        Configuration config = null;
        // 获取插件的数据文件夹
        File dataFolder = getDataFolder();

        // 如果数据文件夹不存在，BungeeCord会自动创建它
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        // 定义配置文件的路径
        File configFile = new File(dataFolder, "config.yml");

        // 如果配置文件不存在，从JAR文件中复制默认配置文件
        if (!configFile.exists()) {
            try {
                try (InputStream inputStream = getResourceAsStream("config.yml");
                     FileOutputStream outputStream = new FileOutputStream(configFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 加载配置文件
        try {
            config = YamlConfiguration.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return config;
    }
    @Override
    public void onEnable() {
        Configuration config = loadConfig();

        String logLevel = config.getString("log-level", "INFO");
        String logFile =  config.getString("logFile", "plugins/mc_qq_chat/run.log");
        botToken = config.getString("botToken", "Bot 123456.abcefg");
        guildsIndex = config.getInt("guildsIndex", 0);
        channelName = config.getString("channelName", "QQ频道聊天室");
        channelUrl = config.getString("channelUrl", "https://pd.qq.com/s/8tjjog2zh");
        String intents = config.getString("intents", "MESSAGE_CREATE");

        LoggerUtil.setupLogger(logFile);
        try {
            Level level = Level.parse(logLevel.toUpperCase());
            LoggerUtil.setLogLevel(level);
        } catch (IllegalArgumentException e) {
            LoggerUtil.warning("无效的日志级别: " + logLevel + "，将使用默认的日志级别 INFO");
            LoggerUtil.setLogLevel(Level.INFO);
        }

        // 注册插件消息通道
        getProxy().registerChannel(Const.PLUGIN_CHANNEL);
        getProxy().getPluginManager().registerListener(this, new BungeeFun());

        // 建立ws链接
        webSocketApi = new WebSocketApi(botToken, intents);
        // 连接到WebSocket服务器
        webSocketApi.connectWebSocket();
        LoggerUtil.info("mc_qq_chat插件已经在Bungee服务器上启用");
    }

    @Override
    public void onDisable() {
        // 在 BungeeCord 中的禁用
        getProxy().unregisterChannel(Const.PLUGIN_CHANNEL);
        getProxy().getPluginManager().unregisterListeners(this);
        webSocketApi.stopWebSocket();
        LoggerUtil.info("mc_qq_chat插件已经在Bungee服务器上禁用");
    }
}
