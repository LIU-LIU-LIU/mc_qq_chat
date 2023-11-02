package cc.ahaly.mc.mc_qq_chat.util.qqbot;


import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;

public class QQBot implements WebSocketCallback {

 public static void main(String[] args) {
     // 创建WebSocketApi对象，并传递回调接口
     WebSocketApi webSocketApi = new WebSocketApi();
     webSocketApi.setWebSocketCallback(); // 设置回调接口
 }

    public void onWebSocketConnected() {
     // 调用WebSocket连接方法
         LoggerUtil.fine("WebSocket连接已建立，执行其他操作...");
         LoggerUtil.fine("用户信息是" + HttpApi.getUsername());
         String guild_id = HttpApi.getGuilds(0);
         LoggerUtil.fine("guild_id是" + guild_id);
         String channel_id = HttpApi.getChannels(guild_id, 0);
         LoggerUtil.fine("channel_id是" + channel_id);
         HttpApi.postMessages(channel_id, "消息测试");
 }
}



