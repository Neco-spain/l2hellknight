package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.FortManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2ArtefactInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Fort;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Util;

public class TakeCastle implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(TakeCastle.class.getName());
	private static final SkillType[] SKILL_IDS = { SkillType.TAKECASTLE };

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar == null || !(activeChar instanceof L2PcInstance))
			return;

		L2PcInstance player = (L2PcInstance) activeChar;

		if(player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
			return;

		Castle castle = CastleManager.getInstance().getCastle(player);
		Fort fort = FortManager.getInstance().getFort(player);

		if(castle != null && fort == null){
			if(!checkIfOkToCastSealOfRule(player, castle, true))
				return;
		
		}else if(fort != null && castle == null){
			if(!checkIfOkToCastFlagDisplay(player, fort, true))
				return;
		}
		
		if(castle == null && fort == null)
			return;

		try
		{
			if((castle != null) && (targets[0] instanceof L2ArtefactInstance))
				castle.Engrave(player.getClan(), targets[0].getObjectId());
			else if(fort != null)
				fort.EndOfSiege(player.getClan());
		}
		catch(Exception e)
		{
				e.printStackTrace();
		}
		player = null;
		castle = null;
		fort = null;
	}

	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	/**
	 * Return true if character clan place a flag<BR>
	 * <BR>
	 * 
	 * @param activeChar The L2Character of the character placing the flag
	 */
	public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, boolean isCheckOnly)
	{
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		Fort fort = FortManager.getInstance().getFort(activeChar);

		if(fort != null && castle == null)
		{
			castle = null;
			return checkIfOkToCastFlagDisplay(activeChar, fort, isCheckOnly);
		}
		else
		{
			fort = null;
			return checkIfOkToCastSealOfRule(activeChar, castle, isCheckOnly);
		}
	}

	public static boolean checkIfOkToCastSealOfRule(L2Character activeChar, Castle castle, boolean isCheckOnly)
	{
		if(activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;

		if(castle == null || castle.getCastleId() <= 0)
			sm.addString("You must be on castle ground to use this skill");
		else if(player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
			sm.addString("You can only use this skill on an artifact");
		else if(!castle.getSiege().getIsInProgress())
			sm.addString("You can only use this skill during a siege.");
		else if(!Util.checkIfInRange(200, player, player.getTarget(), true))
			sm.addString("You are not in range of the artifact.");
		else if(castle.getSiege().getAttackerClan(player.getClan()) == null)
			sm.addString("You must be an attacker to use this skill");
		else
		{
			if(!isCheckOnly)
				castle.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to engrave the ruler.", true);
			sm = null;
			player = null;
			return true;
		}

		if(!isCheckOnly)
		{
			player.sendPacket(sm);
			sm = null;
			player = null;
		}

		return false;
	}

	public static boolean checkIfOkToCastFlagDisplay(L2Character activeChar, boolean isCheckOnly)
	{
		return checkIfOkToCastFlagDisplay(activeChar, FortManager.getInstance().getFort(activeChar), isCheckOnly);
	}

	public static boolean checkIfOkToCastFlagDisplay(L2Character activeChar, Fort fort, boolean isCheckOnly)
	{
		if(activeChar == null || !(activeChar instanceof L2PcInstance))
			return false;

		SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		L2PcInstance player = (L2PcInstance) activeChar;

		if(fort == null || fort.getFortId() <= 0)
			sm.addString("You must be on fort ground to use this skill");
		else if(player.getTarget() == null && !(player.getTarget() instanceof L2ArtefactInstance))
			sm.addString("You can only use this skill on an flagpole");
		else if(!fort.getSiege().getIsInProgress())
			sm.addString("You can only use this skill during a siege.");
		else if(!Util.checkIfInRange(200, player, player.getTarget(), true))
			sm.addString("You are not in range of the flagpole.");
		else if(fort.getSiege().getAttackerClan(player.getClan()) == null)
			sm.addString("You must be an attacker to use this skill");
		else
		{
			if(!isCheckOnly)
				fort.getSiege().announceToPlayer("Clan " + player.getClan().getName() + " has begun to raise flag.", true);
			sm = null;
			player = null;
			return true;
		}

		if(!isCheckOnly)
		{
			player.sendPacket(sm);
			sm = null;
			player = null;
		}

		return false;
	}
}
