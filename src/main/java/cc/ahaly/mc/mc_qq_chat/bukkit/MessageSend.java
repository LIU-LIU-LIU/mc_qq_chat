package cc.ahaly.mc.mc_qq_chat.bukkit;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.SharedData;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.BroadcastMessageEvent;

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

    @EventHandler
    public void onBroadcastMessage(BroadcastMessageEvent event) {
        //判断事件产生源是否是插件本身的广播事件
        if (SharedData.getInstance().getSharedVariable()){
            //如果是插件本身产生的事件，则重置sendStatus变量
            SharedData.getInstance().setSharedVariable(false);
        } else {
            String message = event.getMessage(); // 获取广播消息
            // 将消息发送到其他服务器（包括 WBC）
            communicateBungee("",message);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        // 判断事件产生源是否是插件本身的广播事件
        if (SharedData.getInstance().getSharedVariable()){
            // 如果是插件本身产生的事件，则重置 sendStatus 变量
            SharedData.getInstance().setSharedVariable(false);
        } else {
            // 取消事件传递 其他监听玩家聊天事件的插件将会无法正常使用。例卫星地图看不到了
            event.setCancelled(true);

            Player player = event.getPlayer();
            String user = player.getName();//获取玩家名
            String message = event.getMessage(); // 获取玩家聊天消息
            SharedData.getInstance().setSharedVariable(true);
//            event.callEvent();
            // 创建一个 AsyncPlayerChatEvent 事件
            AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player, message, null);
            Bukkit.getServer().getPluginManager().callEvent(chatEvent);// 调用事件,使其他插件可以正常使用。

            TextComponent formattedMessage = mcMsgFormatting(player,message,getNowTime());//格式化消息，实现悬停及点击效果
            //发送消息到本服
            for (Player iplayer : Bukkit.getServer().getOnlinePlayers()) {
                iplayer.sendMessage(formattedMessage);
            }
            // 将消息发送到其他服务器（包括 WBC）
            communicateBungee(user, message);
        }
    }

    public static TextComponent mcMsgFormatting(Player player,String msg,String msgDate){
        // 创建主文本消息
        TextComponent mainText = new TextComponent("");

        // 创建用户部分并设置悬停和点击事件
        String user = player.getPlayer().getName();
        TextComponent userText = new TextComponent("§a[" + user + "]: §f");
        userText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(user + "网络延迟：\n" + player.getPing() + "毫秒\n§6点击通过ptp发送传送请求").create()));
        userText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ptp " + user));

        // 创建消息内容部分
        TextComponent msgText = new TextComponent(msg);
        msgText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("发送时间：\n" + msgDate+ "\n§6点击对此消息进行私聊回复").create()));
        msgText.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/tell " + user + " 回复'" + msg + "' "));

        // 将所有部分附加到主文本消息
        mainText.addExtra(userText);
        mainText.addExtra(msgText);
        return mainText;
    }
}