package cc.ahaly.mc.mc_qq_chat.velocity;

import cc.ahaly.mc.mc_qq_chat.util.Const;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import cc.ahaly.mc.mc_qq_chat.util.qqbot.HttpApi;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.Collection;
import java.util.Optional;

public class VelocityFun {
    private final ProxyServer server;

    public VelocityFun(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(VelocityMain.IDENTIFIER)) {
            return;
        }

        // 只处理从后端服务器发来的消息
        if (!(event.getSource() instanceof ServerConnection)) {
            return;
        }

        ServerConnection serverConnection = (ServerConnection) event.getSource();
        RegisteredServer sourceServer = serverConnection.getServer();

        LoggerUtil.fine("Velocity通过PluginMessageListener收到PLUGIN_CHANNEL消息");

        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String subChannel = in.readUTF(); // 读取子频道名称
        
        if (subChannel.equals(Const.SUB_PLUGIN_CHANNEL)) {
            // 处理特定子频道的消息
            String msgDate = in.readUTF(); // 读取时间
            String user = in.readUTF(); // 读取用户
            String receivedMessage = in.readUTF(); // 读取消息内容

            String senderServerName = sourceServer.getServerInfo().getName();
            LoggerUtil.fine("发送服务器名称是: " + senderServerName);

            // 处理MC的消息格式
            String msg = mcMsgFormatting(senderServerName, user, receivedMessage, msgDate);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
            out.writeUTF(msgDate); // 写入原始时间
            out.writeUTF(user); // 写入原始用户
            out.writeUTF(msg); // 将消息内容写入输出流
            byte[] data = out.toByteArray();

            // 遍历所有服务器，向所有子服发送插件消息，除了事件产生方本身
            for (RegisteredServer server : server.getAllServers()) {
                if (!server.getServerInfo().getName().equals(senderServerName)) {
                    server.sendPluginMessage(VelocityMain.IDENTIFIER, data);
                }
            }

            LoggerUtil.fine("Velocity通过PluginMessageListener收到PLUGIN_CHANNEL.SUB_PLUGIN_CHANNEL消息并封装server_name后广播到其他服: " + msg);

            // 通过QQBot发送消息
            sendMsgToQQ("[" + senderServerName + "][" + user + "]: " + receivedMessage);
        }
    }

    public void sendMsgToMC(String channelName, String userName, String avatar, String msg, String msgDate, String message_id) {
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
            msg = qqMsgFormatting(channelName, userName, avatar, msg, msgDate, message_id);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(Const.SUB_PLUGIN_CHANNEL); // 将子频道名称写入输出流
            out.writeUTF(msgDate); // 写入原始时间
            out.writeUTF(userName); // 写入原始用户
            out.writeUTF(msg); // 将消息内容写入输出流
            byte[] data = out.toByteArray();

            for (RegisteredServer server : server.getAllServers()) {
                server.sendPluginMessage(VelocityMain.IDENTIFIER, data);
            }
        }
    }

    public static void sendMsgToQQ(String msg) {
        HttpApi httpAPI = new HttpApi(VelocityMain.botToken);
        msg = msg.replaceAll("§.", ""); // 去掉mc的颜色格式
        String guild_id = httpAPI.getGuilds(VelocityMain.guildsIndex);
        String channel_id = httpAPI.getChannels(guild_id, VelocityMain.channelName);
        httpAPI.postMessages(channel_id, msg);
    }

    public String glist() {
        // 初始化在线玩家数量
        int onlinePlayersCount = 0;
        // 创建一个StringBuilder来构建合并的消息
        StringBuilder messageBuilder = new StringBuilder();

        for (RegisteredServer server : server.getAllServers()) {
            // 获取在该服务器上的在线玩家
            String serverMessage = serverList(server);
            int playerCount = server.getPlayersConnected().size();
            
            if (playerCount > 0) {
                // 将服务器信息添加到StringBuilder
                messageBuilder.append(serverMessage).append("\n");
            }
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

    public String mcMsgFormatting(String serverName, String user, String msg, String msgDate) {
        // 使用 Adventure API 创建主文本消息
        Component mainText = Component.text("");

        // 创建服务器部分并设置悬停和点击事件
        RegisteredServer server = this.server.getServer(serverName).orElse(null);
        Component serverText = Component.text("[" + serverName + "]", NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(Component.text(serverName + "玩家数量：\n" + 
                        (server != null ? serverList(server) : "未知") + "\n§6点击前往该服务器")))
                .clickEvent(ClickEvent.runCommand("/server " + serverName));

        // 创建用户部分并设置悬停和点击事件
        Optional<Player> player = this.server.getPlayer(user);
        Component userText;
        if (player.isPresent()) {
            Player p = player.get();
            userText = Component.text("[" + user + "]: ", NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(user + "网络延迟：\n" + 
                            p.getPing() + "毫秒\n§6点击查看该玩家大数据")))
                    .clickEvent(ClickEvent.openUrl("http://mc.ahaly.cn:8804/player/" + user + "/overview"));
        } else {
            userText = Component.text("[" + user + "]: ", NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(user + "无法获取玩家信息")));
        }

        // 创建消息内容部分
        Component msgText = Component.text(msg, NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(Component.text("发送时间：\n" + msgDate)));

        // 将所有部分附加到主文本消息
        mainText = mainText.append(serverText).append(userText).append(msgText);

        // 将主文本消息转换为JSON字符串
        String formattedMessage = GsonComponentSerializer.gson().serialize(mainText);
        LoggerUtil.fine("原始文本:" + mainText + "格式化成字符串后 :" + formattedMessage);
        return formattedMessage;
    }

    public String qqMsgFormatting(String channelName, String userName, String avatar, String msg, String msgDate, String message_id) {
        // 使用 Adventure API 创建主文本消息
        Component mainText = Component.text("");

        // 创建服务器部分并设置悬停和点击事件
        Component serverText = Component.text("[QQ]", NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(Component.text("来源频道：\n" + channelName + "\n§6点击加入:")))
                .clickEvent(ClickEvent.openUrl(VelocityMain.channelUrl));

        // 创建用户部分并设置悬停和点击事件
        Component userText = Component.text("[" + userName + "]: ", NamedTextColor.GREEN)
                .hoverEvent(HoverEvent.showText(Component.text("昵称：\n" + userName + "\n§6点击查看头像:")))
                .clickEvent(ClickEvent.openUrl(avatar));

        // 创建消息内容部分
        Component msgText = Component.text(msg, NamedTextColor.WHITE)
                .hoverEvent(HoverEvent.showText(Component.text("发送时间：\n" + msgDate + "\n§6点击回复该消息(功能尚未开发)")));

        // 将所有部分附加到主文本消息
        mainText = mainText.append(serverText).append(userText).append(msgText);

        // 将主文本消息转换为JSON字符串
        String formattedMessage = GsonComponentSerializer.gson().serialize(mainText);
        LoggerUtil.fine("原始文本:" + mainText + "格式化成字符串后 :" + formattedMessage);
        return formattedMessage;
    }

    public String serverList(RegisteredServer server) {
        // 获取在该服务器上的在线玩家
        Collection<Player> playersOnServer = server.getPlayersConnected();
        int playerCount = playersOnServer.size();
        
        // 如果有玩家在服务器上
        if (playerCount > 0) {
            // 自定义消息格式
            StringBuilder serverMessage = new StringBuilder("[" + server.getServerInfo().getName() + "] (" + playerCount + "): ");
            for (Player player : playersOnServer) {
                serverMessage.append(player.getUsername()).append(", ");
            }
            return serverMessage.substring(0, serverMessage.length() - 2); // 去掉最后一个逗号
        }
        return "";
    }
}

