package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.SharedData;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

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
            in.readUTF(); // 读取时间（不使用）
            in.readUTF(); // 读取用户（不使用）
            String rawJsonStr = in.readUTF(); // 读取消息内容（JSON格式）
            
            // 将JSON格式字符串转换为Adventure Component
            Component receivedMessage = GsonComponentSerializer.gson().deserialize(rawJsonStr);

            LoggerUtil.fine("子服通过PluginMessageListener收到PLUGIN_CHANNEL.SUB_PLUGIN_CHANNEL消息: " + rawJsonStr);
            
            // 向所有在线玩家发送消息
            for (Player tempPlayer : Bukkit.getOnlinePlayers()) {
                SharedData.getInstance().setSharedVariable(true); // 设置通过插件本身发送广播事件标志
                tempPlayer.sendMessage(receivedMessage);
            }
        }
    }
}
