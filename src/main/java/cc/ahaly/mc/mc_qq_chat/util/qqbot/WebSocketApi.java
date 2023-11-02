package cc.ahaly.mc.mc_qq_chat.util.qqbot;

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

public class WebSocketApi {

    private static String botToken = "Bot 102057759.lzuq9La1RqoX29wX5J9FY2Hhp4ax1ETI";
    private static WebSocketClient client; // 将 WebSocketClient 声明为类的成员变量
    private static final  String intents = "MESSAGE_CREATE";         // 发送消息事件，代表频道内的全部消息，而不只是 at 机器人的消息。内容与 AT_MESSAGE_CREATE 相同 AT_MESSAGE_CREATE当收到@机器人的消息时

    private static Timer heartbeatTimer = null;//心跳定时任务
    private static String seq = null;//心跳d的值
    private static String session_id = null;

    private static WebSocketCallback callback;

    public static void setWebSocketCallback() {
        WebSocketApi.callback = callback; // 设置回调接口
    }

    public static void connectWebSocket() {
        try {
            client = new WebSocketClient(new URI(HttpApi.gateway())) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    LoggerUtil.info("WebSocket连接已打开");
                    if (callback != null) {
                        callback.onWebSocketConnected();
                    }
                }
                @Override
                public void onMessage(String message) {
                    handleMessage(message);
                }
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LoggerUtil.info("WebSocket连接关闭");
                    stopHeartbeatTask();
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
        LoggerUtil.info("心跳维持中...");
        heartbeatTimer = new Timer();
        heartbeatTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (client != null && client.isOpen()) {
                    if (seq == null) {
                        // 第一次发送时，heartbeatData为null
                        client.send("{'op': 1, 'd': null}");
                    } else {
                        // 后续根据接收到的消息的s字段的值来发送心跳
                        client.send("{'op': 1, 'd': " + seq + "}");
                    }
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
                    break;
                // 添加其他 op 的处理逻辑
                case intents://代表收到订阅消息
                    // 提取头像
                    String avatar = (String) ((JSONObject) ((JSONObject) json.get("d")).get("author")).get("avatar");
                    // 提取昵称
                    String nick = (String) ((JSONObject) ((JSONObject) json.get("d")).get("member")).get("nick");
                    // 提取发送时间
                    String joinedAt = (String) ((JSONObject) ((JSONObject) json.get("d")).get("member")).get("joined_at");
                    // 提取消息内容
                    String content = (String) ((JSONObject) json.get("d")).get("content");
                    LoggerUtil.fine("收到订阅事件:头像: " + avatar +" 昵称: " + nick+" 发送时间: " + joinedAt+" 消息内容: " + content);
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
    public static void reconnectWebSocket() {
        if (client != null && client.isOpen()) {
            client.close(); // 关闭当前连接
        }

        // 等待一段时间，然后重新连接
        try {
            Thread.sleep(3000);
            connectWebSocket(); // 重新连接WebSocket
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
