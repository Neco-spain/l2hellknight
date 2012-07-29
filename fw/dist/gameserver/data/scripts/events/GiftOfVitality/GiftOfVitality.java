package events.GiftOfVitality;

import l2p.gameserver.Announcements;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SimpleSpawner;
import l2p.gameserver.model.Summon;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.scripts.ScriptFile;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.tables.SkillTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author n0nam3, pchayka
 * @date 12/10/2010 20:06
 * <p/>
 * http://www.lineage2.com/archive/2010/06/gift_of_vitalit.html
 */
public class GiftOfVitality extends Functions implements ScriptFile {
    private static final String EVENT_NAME = "GiftOfVitality";
    private static final int REUSE_HOURS = 24; // reuse
    private static final int EVENT_MANAGER_ID = 109; // npc id
    private static List<SimpleSpawner> _spawns = new ArrayList<SimpleSpawner>();
    private static final Logger _log = LoggerFactory.getLogger(GiftOfVitality.class);

    private final static int[][] _mageBuff = new int[][]{{5627, 1}, // windwalk
            {5628, 1}, // shield
            {5637, 1}, // Magic Barrier 1
            {5633, 1}, // blessthesoul
            {5634, 1}, // acumen
            {5635, 1}, // concentration
            {5636, 1}, // empower
    };

    private final static int[][] _warrBuff = new int[][]{{5627, 1}, // windwalk
            {5628, 1}, // shield
            {5637, 1}, // Magic Barrier 1
            {5629, 1}, // btb
            {5630, 1}, // vampirerage
            {5631, 1}, // regeneration
            {5632, 1}, // haste 2
    };

    private final static int[][] _summonBuff = new int[][]{{5627, 1}, // windwalk
            {5628, 1}, // shield
            {5637, 1}, // Magic Barrier 1
            {5629, 1}, // btb
            {5633, 1}, // vampirerage
            {5630, 1}, // regeneration
            {5634, 1}, // blessthesoul
            {5631, 1}, // acumen
            {5635, 1}, // concentration
            {5632, 1}, // empower
            {5636, 1}, // haste 2
    };

    public enum BuffType {
        PLAYER,
        SUMMON,
        VITALITY,
    }

    /**
     * Спавнит эвент менеджеров
     */
    private void spawnEventManagers() {
        final int EVENT_MANAGERS[][] = {{-119494, 44882, 360, 24576}, //Kamael Village
                {-84023, 243051, -3728, 4096}, //Talking Island Village
                {45538, 48357, -3056, 18000}, //Elven Village
                {9929, 16324, -4568, 62999}, //Dark Elven Village
                {115096, -178370, -880, 0}, //Dwarven Village
                {-45372, -114104, -240, 16384}, //Orc Village
                {-83156, 150994, -3120, 0}, //Gludin
                {-13727, 122117, -2984, 16384}, //Gludio
                {16111, 142850, -2696, 16000}, //Dion
                {111004, 218928, -3536, 16384}, //Heine
                {82145, 148609, -3464, 0}, //Giran
                {81083, 56118, -1552, 32768}, //Oren
                {117356, 76708, -2688, 49151}, //Hunters Village
                {147200, 25614, -2008, 16384}, //Aden
                {43966, -47709, -792, 49999}, //Rune
                {147421, -55435, -2728, 49151}, //Goddart
                {85584, -142490, -1336, 0}, //Schutgard
        };

        SpawnNPCs(EVENT_MANAGER_ID, EVENT_MANAGERS, _spawns);
    }

    /**
     * Удаляет спавн эвент менеджеров
     */
    private void unSpawnEventManagers() {
        deSpawnNPCs(_spawns);
    }

    /**
     * Читает статус эвента из базы.
     *
     * @return
     */
    private static boolean isActive() {
        return IsActive(EVENT_NAME);
    }

    /**
     * Запускает эвент
     */
    public void startEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm)
            return;

        if (SetActive(EVENT_NAME, true)) {
            spawnEventManagers();
            System.out.println("Event: 'Gift Of Vitality' started.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.GiftOfVitality.AnnounceEventStarted", null);
        } else
            player.sendMessage("Event 'Gift Of Vitality' already started.");

        show("admin/events.htm", player);
    }

    /**
     * Останавливает эвент
     */
    public void stopEvent() {
        Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm)
            return;
        if (SetActive(EVENT_NAME, false)) {
            unSpawnEventManagers();
            System.out.println("Event: 'Gift Of Vitality' stopped.");
            Announcements.getInstance().announceByCustomMessage("scripts.events.GiftOfVitality.AnnounceEventStoped", null);
        } else
            player.sendMessage("Event: 'Gift Of Vitality' not started.");

        show("admin/events.htm", player);
    }

    @Override
    public void onLoad() {
        if (isActive()) {
            spawnEventManagers();
            _log.info("Loaded Event: Gift Of Vitality [state: activated]");
        } else
            _log.info("Loaded Event: Gift Of Vitality [state: deactivated]");
    }

    @Override
    public void onReload() {
        unSpawnEventManagers();
    }

    @Override
    public void onShutdown() {
        unSpawnEventManagers();
    }

    private void buffMe(BuffType type) {
        if (getSelf() == null || getNpc() == null || getSelf().getPlayer() == null)
            return;

        String htmltext = null;
        Player player = getSelf().getPlayer();
        NpcInstance npc = getNpc();
        String var = player.getVar("govEventTime");

        switch (type) {
            case VITALITY:
                if (var != null && Long.parseLong(var) > System.currentTimeMillis() || player.getBaseClassId() != player.getActiveClassId())
                    htmltext = "jack-notime.htm";
                else {
                    npc.broadcastPacket(new MagicSkillUse(npc, player, 0, 23179, 1, 0, 0, -1));
                    player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(23179, 1));
                    player.setVar("govEventTime", String.valueOf(System.currentTimeMillis() + REUSE_HOURS * 60 * 60 * 1000L), -1);
                    player.setVitality(Config.MAX_VITALITY);
                    htmltext = "jack-okvitality.htm";
                }
                break;
            case SUMMON:
                if (player.getLevel() < 76)
                    htmltext = "jack-nolevel.htm";
                else if (player.getSummonList().getPet() != null)
                    htmltext = "jack-nosummon.htm";
                else {
                    for (int[] buff : _summonBuff) {
                        for (Summon summon : player.getSummonList().getServitors()) {
                            npc.broadcastPacket(new MagicSkillUse(npc, summon, 0, buff[0], buff[1], 0, 0, -1));
                            player.altOnMagicUseTimer(summon, SkillTable.getInstance().getInfo(buff[0], buff[1]));
                        }
                    }
                    htmltext = "jack-okbuff.htm";
                }
                break;
            case PLAYER:
                if (player.getLevel() < 76)
                    htmltext = "jack-nolevel.htm";
                else {
                    if (!player.isMageClass() || player.getTemplate().race == Race.orc)
                        for (int[] buff : _warrBuff) {
                            npc.broadcastPacket(new MagicSkillUse(npc, player, 0, buff[0], buff[1], 0, 0, -1));
                            player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(buff[0], buff[1]));
                        }
                    else
                        for (int[] buff : _mageBuff) {
                            npc.broadcastPacket(new MagicSkillUse(npc, player, 0, buff[0], buff[1], 0, 0, -1));
                            player.altOnMagicUseTimer(player, SkillTable.getInstance().getInfo(buff[0], buff[1]));
                        }
                    htmltext = "jack-okbuff.htm";
                }
                break;
        }
        show("scripts/events/GiftOfVitality/" + htmltext, getSelf().getPlayer());
    }

    public void buffVitality() {
        buffMe(BuffType.VITALITY);
    }

    public void buffSummon() {
        buffMe(BuffType.SUMMON);
    }

    public void buffPlayer() {
        buffMe(BuffType.PLAYER);
    }
}