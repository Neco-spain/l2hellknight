package services;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.tables.SkillTable;

import java.util.ArrayList;
import java.util.List;

public class KukuruInstance extends Functions {

    public void getgokukuru() {
        Player player = getSelf();
        NpcInstance npc = getNpc();

        gokukuru(npc, player, false);
    }


    public static void gokukuru(NpcInstance npc, Player player, boolean servitor) {
        List<Creature> target = new ArrayList<Creature>();
        target.add(player);
        if (player.isCursedWeaponEquipped())
            return;

        npc.broadcastPacket(new MagicSkillUse(npc, player, 0, 9209, 1, 0, 0, -1));
        npc.callSkill(SkillTable.getInstance().getInfo(9209, 1), target, true);
    }
}
