package cc.ahaly.mc.mc_qq_chat.util.qqbot;

public class Example {
    public static void main(String[] args) {
        String botToken = "Bot 102057759.lzuq9La1RqoX29wX5J9FY2Hhp4ax1ETI";
        int guildsIndex = 0;
        String channelName= "服务器内部聊天(与游戏内聊天同步)";
        String intents = "MESSAGE_CREATE";
        // 建立ws链接
        WebSocketApi webSocketApi = new WebSocketApi(botToken, intents);
        // 连接到WebSocket服务器
        webSocketApi.connectWebSocket();


        HttpApi httpAPI = new HttpApi(botToken);
        String msg = "[测试服][测试人员]测试消息";
        String guild_id = httpAPI.getGuilds(guildsIndex);
        String channel_id = httpAPI.getChannels(guild_id, channelName);
        httpAPI.postMessages(channel_id, msg);
    }
}
