package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.SharedData;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import static org.bukkit.Bukkit.getServer;

import org.bukkit.plugin.Plugin;

import java.util.Arrays;

public class MessageSend implements Listener {

    public static void communicateBungee(String message) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("mc_qq_chat");
        if (plugin != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
            out.writeUTF(message); // 将消息内容写入输出流
            byte[] data = out.toByteArray();
            LoggerUtil.fine("发送的消息长度: " + data.length);
            LoggerUtil.fine("发送的消息内容（字节形式）: " + Arrays.toString(data));
            //寻找一个在线玩家对象
            Player vPlayer = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            vPlayer.sendPluginMessage(plugin, Const.PLUGIN_CHANNEL, data);//插件通道消息必须要有玩家为载体
            LoggerUtil.fine("服务器通过sendPluginMessage发送消息: " + message);
        }
    }

    @EventHandler
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        //判断事件产生源是否是插件本身的广播事件
        if (SharedData.getInstance().getSharedVariable()){
            //如果是插件本身产生的事件，则重置sendStatus变量
            SharedData.getInstance().setSharedVariable(false);
        } else {
            String message = event.getMessage(); // 获取广播消息
            // 将消息发送到其他服务器（包括 WBC）
            communicateBungee(message);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        //判断事件产生源是否是插件本身的广播事件
        if (SharedData.getInstance().getSharedVariable()){
            //如果是插件本身产生的事件，则重置sendStatus变量
            SharedData.getInstance().setSharedVariable(false);
        } else {
            String prefix = "§a[" + event.getPlayer().getName() + "]:§f";
            String message = prefix + event.getMessage(); // 获取玩家聊天消息
            // 将消息发送到其他服务器（包括 WBC）
            communicateBungee(message);
        }
    }
}