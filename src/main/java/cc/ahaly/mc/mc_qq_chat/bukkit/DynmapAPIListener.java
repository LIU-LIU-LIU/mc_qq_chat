package cc.ahaly.mc.mc_qq_chat.bukkit;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;

public class DynmapAPIListener extends DynmapCommonAPIListener {
    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        // 在这里您可以访问 Dynmap 的 API
        MarkerAPI markerAPI = api.getMarkerAPI();

        // 这里可以执行您想要的操作

    }
}
