package cc.ahaly.mc.mc_qq_chat.util.qqbot;

import cc.ahaly.mc.mc_qq_chat.util.LoggerUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpApi {

    private static String botToken = null;

    public HttpApi(String botToken) {
        this.botToken = botToken;
    }

    public static String http(String urlS, String method, String... data) {//如果请求方法是Post，data参数将作为data Post到服务器。如果是Get，data可以不传
        String value = null;
        System.setProperty("java.util.logging.ConsoleHandler.level", "ALL");
        System.setProperty("sun.net.www.protocol.http.HttpURLConnection.level", "ALL");

        try {
            // 创建 URL 对象
            URL urlO = new URL("https://api.sgroup.qq.com" + urlS);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) urlO.openConnection();

            // 添加 Authorization 头
            connection.setRequestProperty("Authorization", botToken);

            // 添加 Content-Type 头
            connection.setRequestProperty("Content-Type", "application/json");

            // 设置请求方法为 GET
            connection.setRequestMethod(method);

            LoggerUtil.fine("Request Method: " + connection.getRequestMethod());
            LoggerUtil.fine("Request Headers: " + connection.getRequestProperties());
            if (method.equals("POST") && data.length > 0) {
                LoggerUtil.fine("Request Body: " + data[0]);
            }

            if (method.equals("POST") && data.length > 0) {
                connection.setDoOutput(true);
                connection.getOutputStream().write(data[0].getBytes());
            }


            // 获取返回的输入流
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            // 读取返回的内容
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            // 关闭输入流
            in.close();
            value = content.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggerUtil.fine("响应的消息是:"+ value);
        return value;
    }

    private static String jsonObj(String receive, String key){
        try {
            // 对返回的 JSON 字符串进行解析
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(receive.toString());
            JSONObject json = (JSONObject) obj;
            String value = (String) json.get(key);
            LoggerUtil.fine(key + "值为: " + value);
            return value;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String jsonArrayIndex(String receive, int index, String key) {
        try {
            // 对返回的 JSON 字符串进行解析
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(receive);
            // 获取指定索引位置的 JSON 对象
            JSONObject jsonObject = (JSONObject) jsonArray.get(index);
            String value = (String) jsonObject.get(key);// 获取指定键的值
            LoggerUtil.fine(key + "值为: " + value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String jsonArrayName(String receive, String name, String key) {
        try {
            // 对返回的 JSON 字符串进行解析
            JSONParser parser = new JSONParser();
            JSONArray jsonArray = (JSONArray) parser.parse(receive);

            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                String itemName = (String) jsonObject.get("name");
                if (name.equals(itemName)) {
                    String value = (String) jsonObject.get(key);// 获取指定键的值
                    LoggerUtil.fine(key + "值为: " + value);
                    return value;
                }
            }

            LoggerUtil.warning("未找到名称为 '" + name + "' 的项");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public String gateway(){
        return jsonObj(http("/gateway", "GET"), "url");
    }

    //获取用户详情
    public String getUsername(){
        return jsonObj(http("/users/@me", "GET"),"username");
    }

    //获取用户频道列表
    public String getGuilds(int index){
        return jsonArrayIndex(http("/users/@me/guilds", "GET"), index, "id");
    }

    //获取子频道列表
    public String getChannels(String guild_id,String name){
        return jsonArrayName(http("/guilds/" + guild_id + "/channels", "GET"), name, "id");
    }

    public static String getChannel(String channel_id){
        return jsonObj(http("/channels/" + channel_id, "GET"), "name");
    }

    //发送消息
    public void postMessages(String channel_id, String content){
        if (!WebSocketApi.isWebSocketConnected){
            LoggerUtil.warning("WS链接尚未建立，本次无法发送消息。");
            return;
        }
        JSONObject payload = new JSONObject();
        payload.put("content", content);
        http("/channels/"+ channel_id+ "/messages", "POST", payload.toJSONString());
    }
}
