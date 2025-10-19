package cc.ahaly.mc.mc_qq_chat.util.qqbot;

public interface ProxyAdapter {
    void sendMessageToMinecraft(String channelName, String userName, String avatar, String msg, String msgDate, String message_id);
    String getChannelName();
}

