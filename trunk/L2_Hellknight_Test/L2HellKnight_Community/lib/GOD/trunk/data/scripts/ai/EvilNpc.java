package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Rnd;

public class EvilNpc extends DefaultAI
{
	private long _lastAction;
	private static final String[] _txt = { "отстань!", "уймись!", "я тебе отомщу, потом будешь прощения просить!",
			"у тебя будут неприятности!", "я на тебя пожалуюсь, тебя арестуют!" };

	public EvilNpc(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || attacker == null || attacker.getPlayer() == null)
			return;

		actor.startAttackStanceTask();

		// Ругаемся и кастуем скилл не чаще, чем раз в 3 секунды
		if(System.currentTimeMillis() - _lastAction > 3000)
		{
			int chance = Rnd.get(0, 100);
			if(chance < 2)
			{
				attacker.getPlayer().setKarma(attacker.getPlayer().getKarma() + 5);
				attacker.sendChanges();
			}
			else if(chance < 4)
				actor.doCast(SkillTable.getInstance().getInfo(4578, 1), attacker, true); // Petrification
			else
				actor.doCast(SkillTable.getInstance().getInfo(4185, 7), attacker, true); // Sleep

			Functions.npcSay(actor, attacker.getName() + ", " + _txt[Rnd.get(_txt.length)]);
			_lastAction = System.currentTimeMillis();
		}
	}
}