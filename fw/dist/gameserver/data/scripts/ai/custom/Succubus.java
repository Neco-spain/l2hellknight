package ai.custom;

import l2p.commons.util.Rnd;
import l2p.gameserver.ai.Fighter;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.scripts.Functions;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.tables.SkillTable;

public class Succubus extends Fighter {

    private static final NpcString[] SAY_TEXT = new NpcString[]{
            NpcString.HAHAHA_YOU_DARE_TO_DISRUPT_ME_I_WILL_BE_YOUR_NIGHTMARE_FROM_WHICH_YOU_CAN_NEVER_AWAKEN,
            NpcString.YOU_DARE_ATTACK_ME_I_WILL_FILL_YOUR_NIGHTMARES_WHITH_BLOOD,
            NpcString.I_CANNOT_LET_YOU_STOP_THE_WRAITH_OF_SHILEN,
            NpcString.ANSUCUBUS,
            NpcString.HALT_YOURNIGHTMARES_WILL_FILL_YOU_WITH_DREAD,
            NpcString.YOU_WONT_GET_AWAY,
            NpcString.HOW_ALL__THAT_POWER_REMOED};

    public Succubus(NpcInstance actor) {
        super(actor);
    }

    protected void onEvtAttacked(Creature attacker, int damage) {
        NpcInstance actor = getActor();
        if (attacker == null || attacker.getPlayer() == null)
            return;
        if (Rnd.chance(25))
            Functions.npcSay(actor, SAY_TEXT[Rnd.get(SAY_TEXT.length)]);
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();

        SkillTable.getInstance().getInfo(14975, 1).getEffects(killer, killer, false, false);
        SkillTable.getInstance().getInfo(14976, 1).getEffects(killer, killer, false, false);
        SkillTable.getInstance().getInfo(14977, 1).getEffects(killer, killer, false, false);

        if (Rnd.chance(25))
            Functions.npcSay(actor, NpcString.TO_THINK_THAT_I_COULD_FAIL_IMPOSSIBLE);
        else
            Functions.npcSay(actor, NpcString.SHILEN_I_HAVE_FAILED);

        super.onEvtDead(killer);
        getActor().endDecayTask();
    }

}
