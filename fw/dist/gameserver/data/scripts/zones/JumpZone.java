package zones;

import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.ExNotifyFlyMoveStart;
import l2p.gameserver.utils.ReflectionUtils;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @author : Ragnarok
 * @date : 16.01.12  22:41
 */
public class JumpZone implements ScriptFile {
    private static ScheduledFuture<?> zoneTask;
    private static List<Zone> jumpZones;

    @Override
    public void onLoad() {
        jumpZones = ReflectionUtils.getZonesByType(Zone.ZoneType.jump);
        zoneTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ZoneTask(), 1000L, 1000L);
    }

    @Override
    public void onReload() {
        zoneTask.cancel(true);
        jumpZones.clear();
    }

    @Override
    public void onShutdown() {
    }


    private class ZoneTask implements Runnable {
        @Override
        public void run() {
            for (Zone zone : jumpZones) {
                for (Player player : zone.getInsidePlayers()) {
                    // Только для игроков с четвертой профессией
                    if (player.getClassId().getLevel() == 5) {
                        player.sendPacket(ExNotifyFlyMoveStart.EX_NOTIFY_FLY_MOVE_START);
                    }
                }
            }
        }
    }
}
