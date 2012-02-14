package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.entity.residence.ResidenceType;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.entity.siege.SiegeClan;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class TakeCastle extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(activeChar == null || !activeChar.isPlayer())
			return false;

		L2Player player = (L2Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
			return false;

		Siege siege = SiegeManager.getSiege(activeChar, true);
		if(siege == null || siege.getSiegeUnit().getType() != ResidenceType.Castle)
			return false;
		if(siege.getAttackerClan(player.getClan()) == null)
			return false;
		if(player.isMounted())
			return false;

		if(!player.isInRangeZ(target, 120))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}

		if(first)
			for(SiegeClan sc : siege.getDefenderClans().values())
			{
				L2Clan clan = sc.getClan();
				if(clan != null)
					clan.broadcastToOnlineMembers(Msg.THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER);
			}

		return true;
	}

	public TakeCastle(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				if(!target.isArtefact())
					continue;
				L2Player player = (L2Player) activeChar;
				Siege siege = SiegeManager.getSiege(activeChar, true);
				if(siege != null)
				{
					siege.announceToPlayer(new SystemMessage(SystemMessage.CLAN_S1_HAS_SUCCEEDED_IN_ENGRAVING_THE_RULER).addString(player.getClan().getName()), false, true);
					siege.Engrave(player.getClan(), target.getObjectId());
				}
			}
	}
}