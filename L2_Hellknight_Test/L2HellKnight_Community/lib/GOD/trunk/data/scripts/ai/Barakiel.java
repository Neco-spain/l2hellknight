package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * @author PaInKiLlEr
 *         АИ для РБ Barakiel.
 *         Если координаты x и z меньше или больше предназначеных, телепортируется обратно и ресает хп.
 */
public class Barakiel extends Fighter
{
	private static final int x1 = 89800;
	private static final int x2 = 93200;
	private static final int y1 = -87038;

	public Barakiel(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		int x = actor.getX();
		int y = actor.getY();
		if(x < x1 || x > x2 || y < y1)
		{
			actor.teleToLocation(91008, -85904, -2736);
			actor.setCurrentHp(actor.getMaxHp(), false);
		}
		super.onEvtAttacked(attacker, damage);
	}
}