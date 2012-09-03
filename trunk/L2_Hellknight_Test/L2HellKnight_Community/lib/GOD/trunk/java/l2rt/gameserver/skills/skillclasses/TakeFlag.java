package l2rt.gameserver.skills.skillclasses;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.gameserver.model.instances.L2SiegeHeadquarterInstance;
import l2rt.gameserver.model.instances.L2TerritoryFlagInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.templates.StatsSet;
import l2rt.util.GArray;

public class TakeFlag extends L2Skill
{
	@Override
	public boolean checkCondition(L2Character activeChar, L2Character target, boolean forceUse, boolean dontMove, boolean first)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first))
			return false;

		if(activeChar == null || !activeChar.isPlayer())
			return false;

		L2Player player = (L2Player) activeChar;

		if(player.getClan() == null)
			return false;

		if(player.getTerritorySiege() < 0)
			return false;

		if(player.isMounted())
			return false;

		if(!(target instanceof L2SiegeHeadquarterInstance) || target.getNpcId() != 36590 || ((L2SiegeHeadquarterInstance) target).getClan() != player.getClan())
			return false;

		if(!player.isTerritoryFlagEquipped())
			return false;

		/* TODO сообщение
		if(first)
			for(SiegeClan sc : siege.getDefenderClans().values())
			{
				L2Clan clan = sc.getClan();
				if(clan != null)
					clan.broadcastToOnlineMembers(Msg.THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER);
			}
			*/

		return true;
	}

	public TakeFlag(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character activeChar, GArray<L2Character> targets)
	{
		for(L2Character target : targets)
			if(target != null)
			{
				L2Player player = (L2Player) activeChar;
				if(!(target instanceof L2SiegeHeadquarterInstance) || target.getNpcId() != 36590 || ((L2SiegeHeadquarterInstance) target).getClan() != player.getClan())
					continue;
				if(player.getTerritorySiege() > -1 && player.isTerritoryFlagEquipped())
				{
					L2ItemInstance flag = player.getActiveWeaponInstance();
					if(flag != null && flag.getCustomType1() != 77) // 77 это эвентовый флаг
					{
						L2TerritoryFlagInstance flagNpc = TerritorySiege.getNpcFlagByItemId(flag.getItemId());
						flagNpc.engrave(player);
					}
				}
			}
	}
}