package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI для Solina Knights ID: 18909
 * Пинают куклу...
 * и для Solina Knight Captain : 18910
 * Кричат в чат...
 * @author DarkShadow74
 */
public class SolinaGuardian extends Fighter
{
	private L2NpcInstance scarecrow;
	private long _lastSay;
	private static final String[] say = {
			"Меч Солины - меч истины!",
			"Поднимем оружие! За честь Солины!",
			"Наказать каждого, кто посмел ступить на эту землю!" };
			
	public SolinaGuardian(L2Character actor)
	{
		super(actor);
		AI_TASK_DELAY = 1000;
	}
	
	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return true;
			
		if(actor.getNpcId() == 18909)
		{
			if(getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
			{
				if(scarecrow == null)
				    for(L2NpcInstance npc : L2World.getAroundNpc(actor, 260, 200))
						if(npc.getNpcId() == 18912)
						{
							npc.addDamageHate(actor, 0, 100);
							scarecrow = npc;
						}
				if(scarecrow != null)
				    setIntention(CtrlIntention.AI_INTENTION_ATTACK, scarecrow);
			}
		}
		if(actor.getNpcId() == 18910)
		{
		    if(System.currentTimeMillis() - _lastSay > 100000)
			{
			    Functions.npcSay(actor, say[Rnd.get(say.length)]);
				_lastSay = System.currentTimeMillis();
			}
		}
		
		return true;
	}
}
