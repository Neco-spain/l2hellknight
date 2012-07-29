package ai;

import l2p.commons.util.Rnd;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.templates.npc.NpcTemplate;
import l2p.gameserver.utils.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public class Mammons extends Functions implements ScriptFile {
    private static final Logger _log = LoggerFactory.getLogger(Mammons.class);
    private static final int MAMMON_PRIEST_ID = 33511; // Жрец мамона
    private static final int MAMMON_MERCHANT_ID = 31113; // Торговец мамона
    private static final int MAMMON_BLACKSMITH_ID = 31126; // Кузнец мамона

    private static final int PORT_TIME = 10 * 60 * 1000;        // 60 min
    private static NpcInstance PriestNpc;
    private static NpcInstance MerchantNpc;
    private static NpcInstance BlacksmithNpc;

    private static ScheduledFuture<?> _mammonTeleportTask;

    private static final NpcString[] mammonText = {
            NpcString.RULERS_OF_THE_SEAL_I_BRING_YOU_WONDROUS_GIFTS,
            NpcString.RULERS_OF_THE_SEAL_I_HAVE_SOME_EXCELLENT_WEAPONS_TO_SHOW_YOU,
            NpcString.IVE_BEEN_SO_BUSY_LATELY_IN_ADDITION_TO_PLANNING_MY_TRIP};

    // * - верные кординаты. Отстальные - примерные.
    private static final Location[] MAMMON_PRIEST_POINTS = {
            new Location(16403, 144843, -3016, 27931),      // Dion
            new Location(81284, 150155, -3528),             // Giran*
            new Location(114478, 217596, -3624, 0),         // Heine*
            new Location(79992, 56808, -1585),              // Oren
            new Location(-84744, 151688, -3154, 0),         // Gludin
            new Location(-12344, 121736, -3014, 0),         // Gludio
            new Location(120392, 76488, -2167, 0),          // Hunters
            new Location(146984, 29624, -2294, 0),          // Aden
            new Location(42856, -41432, -2212, 0),          // Rune
            new Location(144632, -54136, -3006, 0),         // Goddard
            new Location(90024, -143672, -1565, 0),         // Shutgard
    };

    private static final Location[] MAMMON_MERCHANT_POINTS = {
            new Location(16380, 144784, -3016, 27931),      // Dion
            new Location(81272, 150041, -3528),             // Giran*
            new Location(114482, 217538, -3624, 0),         // Heine*
            new Location(79992, 56856, -1585),              // Oren
            new Location(-84744, 151656, -3154, 0),         // Gludin
            new Location(-12344, 121784, -3014, 0),         // Gludio
            new Location(120344, 76520, -2167, 0),          // Hunters
            new Location(146984, 29672, -2294, 0),          // Aden
            new Location(42968, -41384, -2213, 0),          // Rune
            new Location(144552, -54104, -3006, 0),         // Goddard
            new Location(89944, -143688, -1565, 0),         // Shutgard
    };

    private static final Location[] MAMMON_BLACKSMITH_POINTS = {
            new Location(16335, 144696, -3024, 27931),      // Dion
            new Location(81266, 150091, -3528),             // Giran*
            new Location(114484, 217462, -3624, 0),         // Heine*
            new Location(79992, 56920, -1585),              // Oren
            new Location(-84744, 151608, -3154, 0),         // Gludin
            new Location(-12344, 121640, -3014, 0),         // Gludio
            new Location(120296, 76536, -2167, 0),         // Hunters
            new Location(146984, 29736, -2294, 0),         // Aden
            new Location(43032, -41336, -2214, 0),         // Rune
            new Location(144472, -54088, -3006, 0),         // Goddard
            new Location(89912, -143752, -1566, 0),         // Shutgard
    };

    public void SpawnMammons() {
        int firstTown = Rnd.get(MAMMON_PRIEST_POINTS.length);

        NpcTemplate template = NpcHolder.getInstance().getTemplate(MAMMON_PRIEST_ID);
        SimpleSpawner sp = new SimpleSpawner(template);
        sp.setLoc(MAMMON_PRIEST_POINTS[firstTown]);
        sp.setAmount(1);
        sp.setRespawnDelay(0);
        PriestNpc = sp.doSpawn(true);

        template = NpcHolder.getInstance().getTemplate(MAMMON_MERCHANT_ID);
        sp = new SimpleSpawner(template);
        sp.setLoc(MAMMON_MERCHANT_POINTS[firstTown]);
        sp.setAmount(1);
        sp.setRespawnDelay(0);
        MerchantNpc = sp.doSpawn(true);

        template = NpcHolder.getInstance().getTemplate(MAMMON_BLACKSMITH_ID);
        sp = new SimpleSpawner(template);
        sp.setLoc(MAMMON_BLACKSMITH_POINTS[firstTown]);
        sp.setAmount(1);
        sp.setRespawnDelay(0);
        BlacksmithNpc = sp.doSpawn(true);
    }

    public static class TeleportMammons implements Runnable {
        @Override
        public void run() {
            Functions.npcShout(BlacksmithNpc, mammonText[Rnd.get(mammonText.length)]);
            int nextTown = Rnd.get(MAMMON_PRIEST_POINTS.length);
            PriestNpc.teleToLocation(MAMMON_PRIEST_POINTS[nextTown]);
            MerchantNpc.teleToLocation(MAMMON_MERCHANT_POINTS[nextTown]);
            BlacksmithNpc.teleToLocation(MAMMON_BLACKSMITH_POINTS[nextTown]);
        }
    }

    @Override
    public void onLoad() {
        SpawnMammons();
        _mammonTeleportTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new TeleportMammons(), PORT_TIME, PORT_TIME);
        _log.info("Loaded AI: Mammons Teleporter");
    }

    @Override
    public void onReload() {

    }

    @Override
    public void onShutdown() {
        if (_mammonTeleportTask != null) {
            _mammonTeleportTask.cancel(true);
            _mammonTeleportTask = null;
        }
    }


}