package l2rt.gameserver.model.instances;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2DropData;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.ResidenceType;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Util;

public class L2SiegeGuardInstance extends L2NpcInstance
{
	public L2SiegeGuardInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public int getAggroRange()
	{
		return 1200;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		L2Player player = attacker.getPlayer();
		if(player == null)
			return false;
		L2Clan clan = player.getClan();
		if(clan != null && SiegeManager.getSiege(this, true) == clan.getSiege() && clan.isDefender())
			return false;
		Castle castle = getCastle();
		if(player.getTerritorySiege() > -1 && castle != null && player.getTerritorySiege() == castle.getId())
			return false;
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}

	@Override
	public boolean isInvul()
	{
		return false;
	}

	private static final L2DropData EPAULETTE = new L2DropData(9912, 3, 6, 1000000, 1, 85);

	@Override
	public void doDie(L2Character killer)
	{
		Siege siege = SiegeManager.getSiege(this, true);
		if(killer != null)
		{
			L2Player player = killer.getPlayer();
			if(siege != null && player != null && siege.getSiegeUnit().getType() == ResidenceType.Fortress)
			{
				L2Clan clan = player.getClan();
				if(clan != null && siege == clan.getSiege() && !clan.isDefender())
				{
					L2Character topdam = getTopDamager(getAggroList());
					if(topdam == null)
						topdam = killer;
					double chancemod = Experience.penaltyModifier(calculateLevelDiffForDrop(topdam.getLevel()), 9);
					dropItem(player, EPAULETTE.getItemId(), Util.rollDrop(EPAULETTE.getMinDrop(), EPAULETTE.getMaxDrop(), EPAULETTE.getChance() * chancemod * ConfigSystem.getInt("RateDropEpaulette") * player.getRateItems(), true));
				}
			}
		}
		super.doDie(killer);
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}
}