package ai;

import l2rt.gameserver.ai.Mystic;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2Character;

public class HBMystic extends Mystic
{
	private L2Character _atacker;

	public HBMystic(L2Character actor)
	{
		super(actor);
	}

	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		_atacker = attacker;
		super.onEvtAttacked(_atacker, damage);
	}

	protected void onEvtDead(L2Character killer)
	{
		int id = getActor().getNpcId();
		int hLevel = HellboundManager.getInstance().getLevel();

		switch (id)
		{
			case 22321:
				if (hLevel <= 1)
					HellboundManager.getInstance().addPoints(1);
				break;
			case 22328:
				if (hLevel <= 1)
					HellboundManager.getInstance().addPoints(3);
				break;
			case 22342:
			case 22343:
				if (hLevel == 3)
					HellboundManager.getInstance().addPoints(3);
				break;
			case 22449:
				HellboundManager.getInstance().addPoints(50);
				break;
			case 25536:
				HellboundManager.getInstance().addPoints(200);
				break;
			case 18465:
				if (hLevel == 4)
				{
					HellboundManager.getInstance().addPoints(10000);
					HellboundManager.getInstance().changeLevel(5);
				}
				break;
		}

		super.onEvtDead(killer);
	}
}