package l2rt.gameserver.model.instances;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.instancemanager.FortressSiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2DropData;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.entity.residence.ResidenceType;
import l2rt.gameserver.model.entity.siege.fortress.FortressSiege;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Util;

public class L2CommanderInstance extends L2SiegeGuardInstance
{
	public L2CommanderInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	private static final L2DropData EPAULETTE = new L2DropData(9912, 50, 150, 1000000, 1, 85);

	@Override
	public void doDie(L2Character killer)
	{
		FortressSiege siege = FortressSiegeManager.getSiege(this);
		if(siege != null)
		{
			siege.killedCommander(this);

			if(siege.getSiegeUnit().getType() == ResidenceType.Fortress && killer.isPlayable())
			{
				L2Character topdam = getTopDamager(getAggroList());
				if(topdam == null)
					topdam = killer;

				double chancemod = Experience.penaltyModifier(calculateLevelDiffForDrop(topdam.getLevel()), 9);

				dropItem(killer.getPlayer(), EPAULETTE.getItemId(), Util.rollDrop(EPAULETTE.getMinDrop(), EPAULETTE.getMaxDrop(), EPAULETTE.getChance() * chancemod * ConfigSystem.getInt("RateDropEpaulette") * killer.getPlayer().getRateItems(), true));
			}
		}

		super.doDie(killer);
	}
}