package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.SharedData;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class MessageSend implements Listener {

    private static String getNowTime(){
        // 获取当前时间
        Date now = new Date();
        // 创建一个日期格式化对象
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 使用格式化对象将日期转换为字符串
        return dateFormat.format(now);
    }

    public static void communicateBungee(String user,String message) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("mc_qq_chat");
        if (plugin != null) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
            out.writeUTF(getNowTime()); //写入当前时间
            out.writeUTF(user); //写入用户
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncChatEvent event) {
        // 判断事件产生源是否是插件本身的广播事件
        if (SharedData.getInstance().getSharedVariable()){
            // 如果是插件本身产生的事件，则重置 sendStatus 变量
            SharedData.getInstance().setSharedVariable(false);
            return;
        }

        Player player = event.getPlayer();
        String user = player.getName(); // 获取玩家名
        
        // 将 Component 转换为纯文本字符串
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        
        // 格式化消息，实现悬停及点击效果
        Component formattedMessage = mcMsgFormatting(player, message, getNowTime());
        
        // 修改渲染器以使用我们的自定义格式
        event.renderer((source, sourceDisplayName, msg, viewer) -> formattedMessage);
        
        // 将消息发送到其他服务器（包括代理服务器）
        communicateBungee(user, message);
    }

    public static Component mcMsgFormatting(Player player, String msg, String msgDate){
        String user = player.getName();
        
        // 创建用户部分并设置悬停和点击事件
        Component userText = Component.text("[" + user + "]: ", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(
                    Component.text(user + "网络延迟：\n" + player.getPing() + "毫秒\n")
                        .append(Component.text("点击通过ptp发送传送请求", NamedTextColor.GOLD))
                ))
                .clickEvent(ClickEvent.runCommand("/ptp " + user));

        // 创建消息内容部分
        Component msgText = Component.text(msg, NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(
                    Component.text("发送时间：\n" + msgDate + "\n")
                        .append(Component.text("点击对此消息进行私聊回复", NamedTextColor.GOLD))
                ))
                .clickEvent(ClickEvent.suggestCommand("/tell " + user + " 回复'" + msg + "' "));

        // 将所有部分组合到一起
        return userText.append(msgText);
    }
}