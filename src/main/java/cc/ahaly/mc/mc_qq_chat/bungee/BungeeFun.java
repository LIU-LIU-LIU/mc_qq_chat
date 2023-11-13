package cc.ahaly.mc.mc_qq_chat.bungee;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.qqbot.HttpApi;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.chat.ComponentSerializer;
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
                String msgDate = in.readUTF();//读取时间
                String user = in.readUTF();//读取用户
                String receivedMessage = in.readUTF(); // 读取消息内容
                // 获取发送方服务器名称
                String senderServerName = getSenderServerName(event.getSender().getSocketAddress().toString());
                LoggerUtil.fine("senderServerName是:" + senderServerName);
                //处理MC的消息格式
                String msg = mcMsgFormatting(senderServerName,user,receivedMessage,msgDate); //附带MC的格式
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
                out.writeUTF(msgDate); //写入原始时间
                out.writeUTF(user); //写入原始用户
                out.writeUTF(msg); // 将消息内容写入输出流
                byte[] data = out.toByteArray();
                Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
                //遍历所有服务器，向所有子服发送插件消息，除了事件产生方本身。
                for (ServerInfo server : servers) {
                    if (!server.getName().equals(senderServerName)) {
                        server.sendData(Const.PLUGIN_CHANNEL, data);
                    }
                }
                LoggerUtil.fine("wbc通过PluginMessageListener收到PLUGIN_CHANNEL.SUB_PLUGIN_CHANNEL消息并封装server_name后广播到其他服: " + msg);
                //通过QQBot发送消息
                sendMsgToQQ("["+senderServerName+"]["+user+"]: "+ receivedMessage);
            }
        }
    }
    public static void sendMsgToMC(String channelName,String userName,String avatar,String msg,String msgDate,String message_id){
        // 定义正则表达式来匹配消息格式
        String regex = "<@!\\d+> /(.*)"; // 匹配<@!数字> /开头的消息，提取后面的内容

        // 使用正则表达式进行匹配
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(msg);

        // 判断消息是否符合格式
        if (matcher.matches()) {
            // 提取关键信息
            String command = matcher.group(1).trim();
            // 根据提取的命令进行逻辑判断
            switch (command) {
                case "glist":
                    // 执行与 /glist 相关的逻辑
                    sendMsgToQQ(glist());
                    break;
                // 可以添加其他命令的逻辑判断
                default:
                    // 处理未知命令的逻辑
                    LoggerUtil.warning("未知命令: " + command);
                    break;
            }
        } else {
            // 非at命令的消息，广播到其他服
            msg = qqMsgFormatting(channelName,userName,avatar,msg,msgDate,message_id); //附带MC格式

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
            out.writeUTF(msgDate); //写入原始时间
            out.writeUTF(userName); //写入原始用户
            out.writeUTF(msg); // 将消息内容写入输出流
            byte[] data = out.toByteArray();
            Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
            for (ServerInfo server : servers) {
                server.sendData(Const.PLUGIN_CHANNEL, data);
            }
        }
    }

    public static void sendMsgToQQ(String msg){
        HttpApi httpAPI = new HttpApi(BungeeMain.botToken);
        msg = msg.replaceAll("§.", "");//去掉mc的颜色格式
        String guild_id = httpAPI.getGuilds(BungeeMain.guildsIndex);
        String channel_id = httpAPI.getChannels(guild_id, BungeeMain.channelName);
        httpAPI.postMessages(channel_id, msg);
    }

    public static String glist() {
        // 初始化在线玩家数量
        int onlinePlayersCount = 0;
        // 创建一个StringBuilder来构建合并的消息
        StringBuilder messageBuilder = new StringBuilder();

        Collection<ServerInfo> servers = ProxyServer.getInstance().getServers().values();
        for (ServerInfo server : servers) {
            // 获取在该服务器上的在线玩家
            String serverMessage = serverList(server);
            ProxiedPlayer[] playersOnServer = server.getPlayers().toArray(new ProxiedPlayer[0]);
            int playerCount = playersOnServer.length;
            // 将服务器信息添加到StringBuilder
            messageBuilder.append(serverMessage).append("\n");
            // 更新在线玩家数量
            onlinePlayersCount += playerCount;
        }

        // 输出在线玩家总数
        String totalPlayersMessage = "在线玩家总数： " + onlinePlayersCount;

        // 将总玩家数消息添加到StringBuilder
        messageBuilder.append(totalPlayersMessage);

        // 将最终合并的消息字符串从StringBuilder中获取
        return messageBuilder.toString();
    }

    public static String mcMsgFormatting(String server,String user,String msg,String msgDate){
        // 创建主文本消息
        TextComponent mainText = new TextComponent("");
        // 创建服务器部分并设置悬停和点击事件
        TextComponent serverText = new TextComponent("§e[" + server + "]");
        ServerInfo serverI = ProxyServer.getInstance().getServerInfo(server);
        serverText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(server + "玩家数量：\n" + serverList(serverI) + "\n§6点击前往该服务器").create()));
        serverText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server));

        // 创建用户部分并设置悬停和点击事件
        TextComponent userText = new TextComponent("§a[" + user + "]: §f");
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(user);
        if (player != null) {
            userText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(user + "网络延迟：\n" + player.getPing() + "毫秒\n§6点击查看该玩家大数据").create()));
            userText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://mc.ahaly.cn:8804/player/" + user + "/overview"));
        }else{
            userText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(user + "无法获取玩家信息").create()));
        }

        // 创建消息内容部分
        TextComponent msgText = new TextComponent(msg);
        msgText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("发送时间：\n" + msgDate).create()));

        // 将所有部分附加到主文本消息
        mainText.addExtra(serverText);
        mainText.addExtra(userText);
        mainText.addExtra(msgText);

        // 将主文本消息转换为JSON字符串
        String formattedMessage = ComponentSerializer.toString(mainText);
        LoggerUtil.fine("原始文本:" + mainText + "格式化成字符串后 :" + formattedMessage);
        return formattedMessage;
    }

    public static String qqMsgFormatting(String channelName,String userName,String avatar,String msg,String msgDate,String message_id){
        // 创建主文本消息
        TextComponent mainText = new TextComponent("");

        // 创建服务器部分并设置悬停和点击事件
        TextComponent serverText = new TextComponent("§e[QQ]§f");
        serverText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "来源频道：\n" + channelName + "\n§6点击加入:").create()));
        serverText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, BungeeMain.channelUrl));

        // 创建用户部分并设置悬停和点击事件
        TextComponent userText = new TextComponent("§a[" + userName + "]: §f");
        userText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( "昵称：\n" +  userName + "\n§6点击查看头像:").create()));
        // 设置点击事件
        userText.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, avatar));

        // 创建消息内容部分
        TextComponent msgText = new TextComponent(msg);
        msgText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("发送时间：\n" + msgDate + "\n§6点击回复该消息(功能尚未开发)").create()));

        // 将所有部分附加到主文本消息
        mainText.addExtra(serverText);
        mainText.addExtra(userText);
        mainText.addExtra(msgText);

        // 将主文本消息转换为JSON字符串
        String formattedMessage = ComponentSerializer.toString(mainText);
        LoggerUtil.fine("原始文本:" + mainText + "格式化成字符串后 :" + formattedMessage);
        return formattedMessage;
    }

    public static String serverList(ServerInfo server) {
        // 获取在该服务器上的在线玩家
        ProxiedPlayer[] playersOnServer = server.getPlayers().toArray(new ProxiedPlayer[0]);
        int playerCount = playersOnServer.length;
        // 如果有玩家在服务器上
        if (playerCount > 0) {
            // 自定义消息格式
            String serverMessage = "[" + server.getName() + "] (" + playerCount + "): ";
            for (ProxiedPlayer player : playersOnServer) {
                serverMessage += player.getName() + ", ";
            }
            return serverMessage.substring(0, serverMessage.length() - 2); // 去掉最后一个逗号
        }
        return "";
    }
}