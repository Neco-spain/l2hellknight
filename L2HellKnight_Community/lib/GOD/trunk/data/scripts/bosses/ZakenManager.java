package bosses;

import javolution.util.FastMap;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.ExSendUIEvent;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.FuncMul;
import l2rt.util.Location;
import l2rt.util.Rnd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author : Drizzy and Ragnarok
 * @date : 15.01.11
 */

public class ZakenManager extends Functions implements ScriptFile {
    private static String text = "";
    public static HashMap<Long, ZakenInstanceInfo> instances;
    // Во избежание ошибок Z координаты закенов и Z координаты бочек на этаже НЕ должны отличаться
    public static final int[][] ROOM_CENTER_COORDS = {
            // комнаты 1-го этажа
            {54248, 220120, -3522, 0},
            {56280, 220120, -3522, 0},
            {55272, 219096, -3522, 0},
            {54232, 218072, -3522, 0},
            {56296, 218072, -3522, 0},
            // комнаты 2-го этажа
            {56280, 218072, -3250, 0},
            {54232, 218072, -3250, 0},
            {55256, 219112, -3250, 0},
            {56296, 220120, -3250, 0},
            {54232, 220136, -3250, 0},
            // комнаты 3-го этажа
            {56296, 218072, -2978, 0},
            {54232, 218072, -2978, 0},
            {55272, 219112, -2978, 0},
            {56280, 220120, -2978, 0},
            {54232, 220120, -2978, 0}
    };

    public final class ZakenInstanceInfo {
        int zakenId;
        Location zakenLoc;
        List<Integer> blueKandles;
        List<Integer> redKandles;

        public ZakenInstanceInfo(int zakenId, Location zakenLoc) {
            this.zakenId = zakenId;
            this.zakenLoc = zakenLoc;
            blueKandles = new ArrayList<Integer>();
            redKandles = new ArrayList<Integer>();
        }

        public Location getZakenLoc() {
            return zakenLoc;
        }

        public List<Integer> getBlueKandles() {
            return blueKandles;
        }

        public List<Integer> getRedKandles() {
            return redKandles;
        }

        public int getZakenId() {
            return zakenId;
        }
    }

	@SuppressWarnings( { "fallthrough" }) // Ну хз...
    public void enterInstance(String[] strings) {
        int zakenId;
        int instancedZoneId;
        L2Player player = (L2Player) getSelf();
        L2Party party = player.getParty();

        if (strings[0].equalsIgnoreCase("Night") && GameTimeController.getInstance().isNowNight()) {
            instancedZoneId = 515;
            zakenId = 29022;
        } else if (strings[0].equalsIgnoreCase("Day") && !GameTimeController.getInstance().isNowNight()) {
            instancedZoneId = 516;
            zakenId = 29176;
        } else if (strings[0].equalsIgnoreCase("DayHigh") && !GameTimeController.getInstance().isNowNight()) {
            instancedZoneId = 517;
            zakenId = 29181;
        } else {
            player.sendMessage("Не подходящее время.");
            return;
        }
        InstancedZoneManager izm = InstancedZoneManager.getInstance();
        FastMap<Integer, InstancedZoneManager.InstancedZone> izs = InstancedZoneManager.getInstance().getById(instancedZoneId);
        if (izs == null) {
            player.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        InstancedZoneManager.InstancedZone iz = izs.get(0);
        if (iz == null) {
            player.sendPacket(Msg.SYSTEM_ERROR);
            return;
        }
        String name = iz.getName();
        int timelimit = iz.getTimelimit();
        int minMembers = iz.getMinParty();
        int maxMembers = iz.getMaxParty();
        int min_level = iz.getMinLevel();
        int max_level = iz.getMaxLevel();

        //check party
        if (party == null) {
            player.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
            return;
        }

        // Если игрок тпанулся из инста(смерть, сое), возвращаем его в инстанс
        if (player.getParty().isInReflection()) {
            Reflection old_ref = player.getParty().getReflection();
            if (old_ref.getInstancedZoneId() != instancedZoneId) {
                player.sendMessage("Неправильно выбран инстанс");
                return;
            }

            if (player.getLevel() < min_level || player.getLevel() > max_level) {
                player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
                return;
            }
            if (player.isCursedWeaponEquipped() || player.isInFlyingTransform() || player.isDead()) {
                player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(player));
                return;
            }
            if (izm.getTimeToNextEnterInstance(name, player) > 0) {
                player.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
                return;
            }
            dispellBuff(player);
            player.setReflection(old_ref);
            player.teleToLocation(iz.getTeleportCoords(), old_ref.getId());
            return;
        }

        //иначе заходит новая пати, проверяем условия, создаем инстанс
        //check party leader
        switch (instancedZoneId) 
		{
            case 516:
            case 517:
                if (party.getCommandChannel() == null)
				{
                    if (!party.isLeader(player)) 
					{
                        player.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_TRY_TO_ENTER);
                        return;
                    }
                    //check min count member for party
                    if (party.getMemberCount() < minMembers) 
					{
                        player.sendMessage("The party must contains at least " + minMembers + " members.");
                        return;
                    }

                    for (L2Player member : party.getPartyMembers())
					{
                        if (member.getLevel() < min_level || member.getLevel() > max_level) 
						{
                            player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                            return;
                        }
                        if (member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead()) 
						{
                            player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                            return;
                        }
                        if (!player.isInRange(member, 500))
						{
                            member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                            player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                            return;
                        }
                        if (izm.getTimeToNextEnterInstance(name, member) > 0) 
						{
                            member.sendPacket(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(player));
                            return;
                        }
                    }
					break;
                }
            case 515:
                if (!player.getParty().isInCommandChannel()) {
                    player.sendPacket(Msg.YOU_CANNOT_ENTER_BECAUSE_YOU_ARE_NOT_IN_A_CURRENT_COMMAND_CHANNEL);
                    return;
                }
                L2CommandChannel cc = player.getParty().getCommandChannel();
                //check cc leader
                if (cc.getChannelLeader() != player) {
                    player.sendMessage("You must be leader of the command channel.");
                    return;
                }
                //check min-max member count for CC
                if (cc.getMemberCount() < minMembers) {
                    player.sendMessage("The command channel must contains at least " + minMembers + " members.");
                    return;
                }
                if (cc.getMemberCount() > maxMembers) {
                    player.sendMessage("The command channel must contains not more than " + maxMembers + " members.");
                    return;
                }

                for (L2Player member : cc.getMembers()) {
                    if (member.getLevel() < min_level || member.getLevel() > max_level) {
                        player.sendPacket(new SystemMessage(SystemMessage.C1S_LEVEL_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                        return;
                    }
                    if (member.isCursedWeaponEquipped() || member.isInFlyingTransform() || member.isDead()) {
                        player.sendPacket(new SystemMessage(SystemMessage.C1S_QUEST_REQUIREMENT_IS_NOT_SUFFICIENT_AND_CANNOT_BE_ENTERED).addName(member));
                        return;
                    }
                    if (!player.isInRange(member, 500)) {
                        member.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                        player.sendPacket(Msg.ITS_TOO_FAR_FROM_THE_NPC_TO_WORK);
                        return;
                    }
                    if (izm.getTimeToNextEnterInstance(name, member) > 0) {
                        cc.broadcastToChannelMembers(new SystemMessage(SystemMessage.C1_MAY_NOT_RE_ENTER_YET).addName(member));
                        return;
                    }
                }
                break;
        }

        Reflection r = new Reflection(name);
        r.setInstancedZoneId(instancedZoneId);
        for (InstancedZoneManager.InstancedZone i : izs.values()) {
            if (r.getTeleportLoc() == null) {
                r.setTeleportLoc(i.getTeleportCoords());
            }
            r.FillSpawns(i.getSpawnsInfo());
            r.FillDoors(i.getDoors());
        }
        r.setCoreLoc(r.getReturnLoc());
        r.setReturnLoc(player.getLoc());
        if (player.getParty().isInCommandChannel()) {
            L2CommandChannel cc = player.getParty().getCommandChannel();
            for (L2Player member : cc.getMembers()) {
                member.setVar("backCoords", r.getReturnLoc().toXYZString());
                member.teleToLocation(iz.getTeleportCoords(), r.getId());
                dispellBuff(member);
                member.sendPacket(new ExSendUIEvent(member, false, true, 0, timelimit * 60 * 1000, text));
            }
            cc.setReflection(r);
            r.setCommandChannel(cc);
        } else {
            for (L2Player member : player.getParty().getPartyMembers()) {
                member.setVar("backCoords", r.getReturnLoc().toXYZString());
                member.teleToLocation(iz.getTeleportCoords(), r.getId());
                dispellBuff(member);
                member.sendPacket(new ExSendUIEvent(member, false, true, 0, timelimit * 60 * 1000, text));
            }
            player.getParty().setReflection(r);
            r.setParty(player.getParty());
        }
        if (instances == null)
            instances = new HashMap<Long, ZakenInstanceInfo>();
        int[] coords = ROOM_CENTER_COORDS[Rnd.get(ROOM_CENTER_COORDS.length)];
        instances.put(r.getId(), new ZakenInstanceInfo(zakenId, new Location(coords)));
        if (timelimit > 0) {
            r.startCollapseTimer(timelimit * 60 * 1000);
            if (player.getParty().isInCommandChannel())
                player.getParty().getCommandChannel().broadcastToChannelMembers(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
            else
                player.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(timelimit));
        }
    }

    /**
     * Подсчитывает статы в зависимости от горящих красных свечек
     * Увеличиваем m\pDef, m\pAtk закена
     */
    public static void calcZakenStat(L2NpcInstance zaken, long rId) {
        ZakenInstanceInfo instanceInfo = instances.get(rId);
        double count = instanceInfo.getRedKandles().size();
        double stat = Math.max(0, 1 + instanceInfo.getRedKandles().size() / 40);
        //zaken.removeStatsOwner(zaken);
        if (count > 0) {
            zaken.addStatFunc(new FuncMul(Stats.POWER_DEFENCE, 0x30, zaken, stat));
            zaken.addStatFunc(new FuncMul(Stats.MAGIC_DEFENCE, 0x30, zaken, stat));
            zaken.addStatFunc(new FuncMul(Stats.POWER_ATTACK, 0x30, zaken, stat));
            zaken.addStatFunc(new FuncMul(Stats.MAGIC_ATTACK, 0x30, zaken, stat));
        }
    }

    /**
     * Снимаем бафы все кроме бафов новичков.
     */
    public static void dispellBuff(L2Player player) {
        for (L2Effect e : player.getEffectList().getAllEffects())
            if (!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
                e.exit();
        if (player.getPet() != null)
            for (L2Effect e : player.getPet().getEffectList().getAllEffects())
                if (!e.getSkill().isOffensive() && !e.getSkill().getName().startsWith("Adventurer's "))
                    e.exit();
    }

    public void onLoad() {
        System.out.println("ZakenManager: Init Zaken Manager.");
    }

    public void onReload() {
    }

    public void onShutdown() {
    }
}

