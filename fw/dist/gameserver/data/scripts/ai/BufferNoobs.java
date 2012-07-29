package ai;

import l2p.commons.collections.LazyArrayList;
import l2p.gameserver.ai.DefaultAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.World;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.serverpackets.ExShowScreenMessage;
import l2p.gameserver.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.serverpackets.components.NpcString;
import l2p.gameserver.tables.SkillTable;

public class BufferNoobs extends DefaultAI {
    private static final int[] BUFFS = {4322, 4323, 4324, 4325, 4326, 4327, 4328};

    public BufferNoobs(NpcInstance actor) {
        super(actor);
        this.AI_TASK_ACTIVE_DELAY = 1000;
    }

    @Override
    protected boolean thinkActive() {
        NpcInstance actor = getActor();
        if (actor == null)
            return true;
        Skill skill;

        for (Player player : World.getAroundPlayers(actor, 200, 200)) {
            if (checkBuff(player)) {
                for (int skillId : BUFFS) {
                    skill = SkillTable.getInstance().getInfo(skillId, 1);
                    LazyArrayList<Creature> target = new LazyArrayList<Creature>();
                    target.add(player);
                    actor.broadcastPacket(new MagicSkillUse(actor, player, skillId, 1, 0, 0, 0, -1));
                    actor.callSkill(skill, target, true);
                }
                player.sendPacket(new ExShowScreenMessage(NpcString.NEWBIE_GUIDE_GIVE_YOU_THE_MAGIC_OF_SATTELITE_S1, 800, ScreenMessageAlign.TOP_CENTER, player.getName()));
            }
        }
        return true;
    }

    private boolean checkBuff(Player player) {
        for (int skillId : BUFFS) {
            if (player.getEffectList().getEffectsBySkillId(skillId) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }
}
