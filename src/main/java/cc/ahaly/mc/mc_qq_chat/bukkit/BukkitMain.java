package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapCommonAPIListener;

import java.util.logging.Level;

public final class BukkitMain extends JavaPlugin {

    @Override
    public void onEnable() {
        // 插件启用
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        String logLevel = config.getString("log-level", "INFO");
        String logFile =  config.getString("logFile", "plugins/mc_qq_chat/run.log");
        LoggerUtil.setupLogger(logFile);
        try {
            Level level = Level.parse(logLevel.toUpperCase());
            LoggerUtil.setLogLevel(level);
        } catch (IllegalArgumentException e) {
            LoggerUtil.warning("无效的日志级别: " + logLevel + "，将使用默认的日志级别 INFO");
            LoggerUtil.setLogLevel(Level.INFO);
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, Const.PLUGIN_CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(this, Const.PLUGIN_CHANNEL, new MessageReceived());
        getServer().getPluginManager().registerEvents(new MessageSend(), this);
//        DynmapCommonAPIListener.register(new DynmapAPIListener());
        LoggerUtil.info("mc_qq_chat插件已经在Bukkit服务器上启用");
    }
    @Override
    public void onDisable() {
        // 插件禁用
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        LoggerUtil.info("mc_qq_chat插件已经已经在Bukkit服务器上禁用");
    }
}

