package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.SharedData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.dynmap.DynmapAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class MessageReceived implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals(Const.PLUGIN_CHANNEL)) {
            return;
        }
        LoggerUtil.fine("子服通过onPluginMessageReceived收到PLUGIN_CHANNEL消息");
        LoggerUtil.fine("收到的消息长度: " + message.length);
        LoggerUtil.fine("收到的消息内容（字节形式）: " + Arrays.toString(message));
        String firstFewBytesAsString = new String(message, 0, Math.min(10, message.length), StandardCharsets.UTF_8);
        LoggerUtil.fine("收到的消息的前几个字节（字符串形式）: " + firstFewBytesAsString);

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF(); // 读取子频道名称
        if (subChannel.equals(Const.SUB_PLUGIN_CHANNEL)) {
            // 处理特定子频道的消息
            String receivedMessage = in.readUTF(); // 读取消息内容
            // 处理消息的逻辑
            LoggerUtil.fine("子服通过PluginMessageListener收到PLUGIN_CHANNEL.SUB_PLUGIN_CHANNEL消息: " + receivedMessage);
            SharedData.getInstance().setSharedVariable(true);//设置通过插件本身发送广播事件标志

            //寻找一个在线玩家对象
            Player vPlayer = Bukkit.getOnlinePlayers().stream().findFirst().orElse(null);
            String playerName = vPlayer.getName();
            //临时设置该玩家的显示名称
            vPlayer.setDisplayName("[跨服消息]");
            // 创建一个 AsyncPlayerChatEvent 事件
            AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(false, vPlayer, receivedMessage, null);
            Bukkit.getServer().getPluginManager().callEvent(chatEvent);// 调用事件
            //恢复该玩家的显示名称
            vPlayer.setDisplayName(playerName);
//            vPlayer.sendMessage(receivedMessage);
            SharedData.getInstance().setSharedVariable(true);//设置通过插件本身发送广播事件标志
            Bukkit.broadcastMessage(receivedMessage);
        }
    }
}
