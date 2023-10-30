package cc.ahaly.mc.mc_qq_chat.util;

public class SharedData {

    //发送状态，true插件本身产生的，不会触发发送给wbc的方法，
    // false代表用户产生的事件经过wbc转发
    private boolean sendStatus = false; 

    private SharedData() {
        // Private constructor to enforce singleton pattern
    }

    private static SharedData instance = new SharedData();

    public static SharedData getInstance() {
        return instance;
    }

    public boolean getSharedVariable() {
        return sendStatus;
    }

    public void setSharedVariable(boolean value) {
        sendStatus = value;
    }
}
