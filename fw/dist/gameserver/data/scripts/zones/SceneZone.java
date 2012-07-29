package zones;

import l2p.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Zone;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.utils.ReflectionUtils;

import java.util.List;

/**
 * @author : Ragnarok
 * @date : 23.05.12  16:28
 */
public class SceneZone implements ScriptFile {
    private static ZoneListener _zoneListener;

    @Override
    public void onLoad() {
        _zoneListener = new ZoneListener();
        List<Zone> zones = ReflectionUtils.getZonesByType(Zone.ZoneType.dummy);
        for (Zone zone : zones) {
            if (zone.getParams().containsKey("scene_id")) {
                zone.addListener(_zoneListener);
            }
        }
    }

    @Override
    public void onReload() {
        List<Zone> zones = ReflectionUtils.getZonesByType(Zone.ZoneType.dummy);
        for (Zone zone : zones) {
            if (zone.getParams().containsKey("scene_id")) {
                zone.removeListener(_zoneListener);
            }
        }
    }

    @Override
    public void onShutdown() {
    }

    private class ZoneListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(Zone zone, Creature cha) {
            int sceneId = zone.getParams().getInteger("scene_id", -1);
            boolean only_first_scene = zone.getParams().getBool("only_first_scene", false);
            if (cha != null && cha.isPlayer() && sceneId != -1) {
                boolean showed_previously = cha.getPlayer().getVarB("scene_" + sceneId + "_showed", false);
                if (showed_previously && only_first_scene)
                    return;

                if (only_first_scene)
                    cha.getPlayer().setVar("scene_" + sceneId + "_showed", "true", -1);
                cha.getPlayer().showQuestMovie(sceneId);
            }
        }

        @Override
        public void onZoneLeave(Zone zone, Creature cha) {
        }
    }
}
