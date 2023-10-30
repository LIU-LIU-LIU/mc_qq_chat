package cc.ahaly.mc.mc_qq_chat.bungee;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import java.util.Collection;


public class BungeeFun implements Listener {
    // 根据 IP 地址获取服务器名称
    private String getSenderServerName(String ipAddress) {
        Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
        for (ServerInfo serverInfo : servers) {
            LoggerUtil.fine("比对的地址是:" + serverInfo.getSocketAddress().toString() + "被比对的是" + ipAddress);
            if (serverInfo.getSocketAddress().toString().equals(ipAddress)) {
                return serverInfo.getName();
            }
        }
        return null; // 未找到对应服务器
    }
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(Const.PLUGIN_CHANNEL)) {
            LoggerUtil.fine("wbc通过PluginMessageListener收到PLUGIN_CHANNEL消息: ");
            ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
            String subChannel = in.readUTF(); // 读取子频道名称
            if (subChannel.equals(Const.SUB_PLUGIN_CHANNEL)) {
                // 处理特定子频道的消息
                String receivedMessage = in.readUTF(); // 读取消息内容
                // 获取发送方服务器名称
                String senderServerName = getSenderServerName(event.getSender().getSocketAddress().toString());
                LoggerUtil.fine("senderServerName是:" + senderServerName);
                String msg = "§e[" + senderServerName + "]" + receivedMessage;
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
                out.writeUTF(msg); // 将消息内容写入输出流
                byte[] data = out.toByteArray();
                Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
                for (ServerInfo server : servers) {
                    if (!server.getName().equals(senderServerName)) {
                        server.sendData(Const.PLUGIN_CHANNEL, data);
                    }
                }
                LoggerUtil.fine("wbc通过PluginMessageListener收到PLUGIN_CHANNEL.SUB_PLUGIN_CHANNEL消息并封装server_name后广播到其他服: " + msg);
            }
        }
    }
}