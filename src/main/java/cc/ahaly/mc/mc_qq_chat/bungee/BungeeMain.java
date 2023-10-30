package cc.ahaly.mc.mc_qq_chat.bungee;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeMain extends Plugin implements Listener {

    @Override
    public void onEnable() {
        // 注册插件消息通道
        getProxy().registerChannel(Const.PLUGIN_CHANNEL);
        getProxy().getPluginManager().registerListener(this, new bungeeFun());
        LoggerUtil.info("mc_qq_chat插件已经在Bungee服务器上启用");
    }

    @Override
    public void onDisable() {
        // 在 BungeeCord 中的禁用
        getProxy().unregisterChannel(Const.PLUGIN_CHANNEL);
        getProxy().getPluginManager().unregisterListeners(this);
        LoggerUtil.info("mc_qq_chat插件已经在Bungee服务器上禁用");
    }
}
