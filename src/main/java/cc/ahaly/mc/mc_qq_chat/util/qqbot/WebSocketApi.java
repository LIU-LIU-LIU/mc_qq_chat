package cc.ahaly.mc.mc_qq_chat.util.qqbot;

import cc.ahaly.mc.mc_qq_chat.bungee.BungeeFun;
import cc.ahaly.mc.mc_qq_chat.bungee.BungeeMain;
import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketApi {

    private static WebSocketClient client; // 将 WebSocketClient 声明为类的成员变量
    private static String botToken = null;
    private static String intents = "MESSAGE_CREATE";         // 发送消息事件，
    private static Timer heartbeatTimer = null;//心跳定时任务
    private static String seq = null;//心跳d的值
    private static String session_id = null;

    public static boolean isWebSocketConnected = false;

    public WebSocketApi(String botToken, String intents) {
        this.botToken = botToken;
        this.intents = intents;
    }

    public static void connectWebSocket() {
        try {
            HttpApi httpApi = new HttpApi(botToken);
            client = new WebSocketClient(new URI(httpApi.gateway())) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    LoggerUtil.info("WebSocket连接已打开");
                }
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LoggerUtil.info("WebSocket连接关闭");
                }
                @Override
                public void onError(Exception ex) {
                    ex.printStackTrace();
                }
            };
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startHeartbeatTask(long heartbeatInterval) {
        if (heartbeatTimer != null) {
            return; // 如果定时任务已存在，不再创建新的
        }
        LoggerUtil.info("ws连接检查及心跳维持中...");
        heartbeatTimer = new Timer();
        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //如果链接存在则正常发送心跳，如果不存在则建立ws链接。
                if (client != null && client.isOpen()) {
                    client.send("{\"op\": 1, \"d\": " + seq + "}");
                }else {
                    //此定时任务只在第一次正确链接ws后才会启动，所以此处是为了其他未处理而意外导致ws链接中断的情况。
                    connectWebSocket();
                }
            }
        }, heartbeatInterval, heartbeatInterval);
    }

    public static void stopHeartbeatTask() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }

    public static void handleMessage(String message) {
        LoggerUtil.fine("收到消息: " + message);
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message);
            int op = Integer.parseInt(json.get("op").toString());
            switch (op) {
                case 10:   //需要发送鉴权
                    authenticate();
                    // 提取出心跳间隔
                    long heartbeatInterval = (Long) ((JSONObject) json.get("d")).get("heartbeat_interval");
                    // 创建一个定时任务来发送心跳消息
                    startHeartbeatTask(heartbeatInterval);
                    break;
                case 0://0代表收到消息
                    handleReceiveMessage(json);
                    break;
                case 6://短时间重连 恢复链接
                    resume();
                    break;
                case 7://服务器要求客户端重连
                    LoggerUtil.info("正在重新建立ws链接...");
                    reconnectWebSocket(); // 调用重连方法
                 break;
                case 9://当identify或resume的时候，如果参数有错，服务端会返回该消息
                    LoggerUtil.warning("参数有错，无法建立ws链接。");
                    break;
                case 11://当发送心跳成功之后，就会收到该消息
                    LoggerUtil.fine("心跳回执 OpCode 11 Heartbeat ACK 消息");
                    break;
                default:
                    LoggerUtil.warning("未知的 op 类型: " + op);
                    break;
            }
        } catch (ParseException | NumberFormatException e) {
            LoggerUtil.warning("解析消息失败: " + e.getMessage());
        }
    }

    public static void handleReceiveMessage(JSONObject json) {
        LoggerUtil.fine("收到op:0消息: " + json);
        try {
            //先设置seq值
            seq = json.get("s").toString();
            String t = json.get("t").toString();
            switch (t) {
                case "READY":   //准备就绪
                    // 从 json 对象中获取 d 字段
                    JSONObject d = (JSONObject) json.get("d");
                    // 从 d 对象中获取 session_id 字段的值
                    session_id = (String) d.get("session_id");
                    isWebSocketConnected = true;
                    break;
                // 添加其他 op 的处理逻辑
                case "MESSAGE_CREATE":
                case "AT_MESSAGE_CREATE":
                    // 消息创建或@消息创建的处理逻辑
                    // 代表收到订阅消息
                    // 来源频道
                    String channel_id = (String) ((JSONObject) json.get("d")).get("channel_id");
                    String ChannelName = HttpApi.getChannel(channel_id);
                    //只接收指定频道的消息
                    LoggerUtil.fine("|" +ChannelName + "==" + BungeeMain.channelName + "|");
                    if (ChannelName.equals(BungeeMain.channelName)){
                        //消息ID
                        String message_id = (String) ((JSONObject) json.get("d")).get("id");
                        // 提取头像
                        String avatar = (String) ((JSONObject) ((JSONObject) json.get("d")).get("author")).get("avatar");
                        // 提取昵称
                        String nick = (String) ((JSONObject) ((JSONObject) json.get("d")).get("member")).get("nick");
                        // 提取发送时间
                        String joinedAt = (String) ((JSONObject) json.get("d")).get("timestamp");
                        // 提取消息内容
                        String content = (String) ((JSONObject) json.get("d")).get("content");
                        LoggerUtil.fine("收到订阅事件:头像: " + avatar +" 昵称: " + nick+" 发送时间: " + joinedAt+" 消息内容: " + content);
                        BungeeFun.sendMsgToMC(ChannelName, nick,avatar, content,joinedAt,message_id);
                    }
                    break;
                default:
                    LoggerUtil.warning("未知的事件类型: " + t);
                    break;
            }
        } catch (NumberFormatException e) {
            LoggerUtil.warning("解析消息失败: " + e.getMessage());
        }
    }

    private static void authenticate() {
        int intent = (1 << 9) + 1;
        switch (intents){
            case "MESSAGE_CREATE":
                intent = (1 << 9) + 1 ;
                break;
            case "AT_MESSAGE_CREATE":
                intent = (1 << 30) + 1;
                break;
            default:
                LoggerUtil.warning("未知的intents类型: " + intents);
                break;
        }
        LoggerUtil.fine("intent事件ID是:" + intent);

        JSONObject payload = new JSONObject();
        payload.put("op", 2);

        JSONObject data = new JSONObject();
        data.put("token", botToken);
        data.put("intents", intent);
        data.put("shard", new JSONArray());

        JSONObject properties = new JSONObject();
        properties.put("$os", "linux");
        properties.put("$browser", "my_library");
        properties.put("$device", "my_library");
        data.put("properties", properties);

        payload.put("d", data);
        String msg = payload.toJSONString();
        LoggerUtil.fine("发送消息:"+ msg);
        client.send(msg);
    }
    private static void resume() {
        JSONObject payload = new JSONObject();
        payload.put("op", 6);

        JSONObject data = new JSONObject();
        data.put("token", botToken);
        data.put("session_id", session_id);
        data.put("seq", seq);

        payload.put("d", data);

        String msg = payload.toJSONString();
        LoggerUtil.fine("发送消息:"+ msg);
        client.send(msg);
    }

    // 创建一个ScheduledExecutorService
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void reconnectWebSocket() {
        if (client != null && client.isOpen()) {
            client.close(); // 关闭当前连接
        }

        // 使用ScheduledExecutorService执行重新连接任务
        executor.schedule(() -> connectWebSocket(), 3, TimeUnit.SECONDS);
    }

    public static void stopWebSocket() {
        if (client != null && client.isOpen()) {
            client.close(); // 关闭当前连接
            stopHeartbeatTask();
        }
    }
}