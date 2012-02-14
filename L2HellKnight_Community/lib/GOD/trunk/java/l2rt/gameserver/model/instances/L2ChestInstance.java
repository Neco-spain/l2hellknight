package l2rt.gameserver.model.instances;

import l2rt.Config;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.base.ItemToDrop;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.GArray;
import l2rt.util.Rnd;

public class L2ChestInstance extends L2MonsterInstance
{
	private boolean _fake;

	public L2ChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onSpawn()
	{
		_fake = !Rnd.chance(Config.ALT_TRUE_CHESTS);
		super.onSpawn();
	}

	public void onOpen(L2Player opener)
	{
		if(_fake)
		{
			opener.sendMessage(new CustomMessage("l2rt.gameserver.model.instances.L2ChestInstance.Fake", opener));
			getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
		}
		else
		{
			setSpoiled(false, null);
			int trueId = getTrueId();
			if(NpcTable.getTemplate(trueId) != null && NpcTable.getTemplate(trueId).getDropData() != null)
			{
				final GArray<ItemToDrop> drops = NpcTable.getTemplate(getTrueId()).getDropData().rollDrop(0, this, opener, 1.);
				for(final ItemToDrop drop : drops)
					dropItem(opener, drop.itemId, drop.count);
			}
			doDie(opener);
		}
	}

	private int getTrueId()
	{
		switch(getNpcId())
		{
			case 21671: // Otherworldly Invader Food
				return Rnd.get(18287, 18288);
			case 21694: // Dimension Invader Food
				return Rnd.get(18289, 18290);
			case 21717: // Purgatory Invader Food
				return Rnd.get(18291, 18292);
			case 21740: // Forbidden Path Invader Food
				return Rnd.get(18293, 18294);
			case 21763: // Dark Omen Invader Food
				return Rnd.get(18295, 18296);
			case 21786: // Messenger Invader Food
				return Rnd.get(18297, 18298);
			default:
				return getNpcId() - 3536;
		}
	}

	@Override
	public void reduceCurrentHp(final double damage, final L2Character attacker, L2Skill skill, final boolean awake, final boolean standUp, boolean directHp, boolean canReflect)
	{
		if(_fake)
			super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect);
		else
			doDie(attacker);
	}

	public boolean isFake()
	{
		return _fake;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}