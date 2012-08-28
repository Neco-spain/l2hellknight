package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.StopMove;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class GetPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS = {SkillType.GET_PLAYER};

    public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
    {
        if (activeChar == null || activeChar.isAlikeDead()) return;
        for (L2Object target : targets)
        {
        	if (target instanceof L2PcInstance)
        	{
        		L2PcInstance trg = (L2PcInstance)target;
        		if (trg == null || trg.isAlikeDead()) continue;
				trg.abortAttack();
				trg.abortCast();
				trg.sendPacket(new ActionFailed());
				trg.broadcastPacket(new StopMove(trg));
				trg.setXYZ(activeChar.getX(), activeChar.getY(), activeChar.getZ());
				trg.broadcastPacket(new ValidateLocation(trg));
			}
        }
    }

    public SkillType[] getSkillIds()
    {
        return SKILL_IDS;
    }
}
