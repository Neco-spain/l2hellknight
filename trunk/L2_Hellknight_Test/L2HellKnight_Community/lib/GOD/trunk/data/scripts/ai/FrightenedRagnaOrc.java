package ai;

import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 * AI моба Frightened Ragna Orc для Den of Evil.<br>
 * - При его атаке пишет в чат сообщения, когда высвечивается сообщении со взяткой продолжаем бить или останавливаемся.
 * - Если останавливаемся, то он исчезает и ничего нам не даёт, если продолжаем то он умирает с сообщением в чат.
 * @author Drizzy
 * @date 16.08.10
 */
 
 public class FrightenedRagnaOrc extends Fighter
 {
	private boolean canSay = true;
	private long _lastAttackTime = 0;
	private static final long DESPAWN = 6 * 1000; // 6 сек
	private static String[] says = new String[] {
		"I... don`t want to fight...",
		"Is this really necessary...?" };
	private static String[] says2 = new String[] {
		"You`re pretty dumb to believe me!",
		"Thanks, but that thing about 10,000,000 adena was a lie! See ya!!" };
	
	public FrightenedRagnaOrc(L2NpcInstance actor)
	{
		super(actor);
	}
	
	@Override
	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();
		if(_lastAttackTime != 0 && _lastAttackTime + DESPAWN < System.currentTimeMillis())
		{
			if(actor.getCurrentHpPercents() < 30)
			{
				Functions.npcSay(actor, says2[Rnd.get(says2.length)]);
				actor.decayMe();
			}
			_lastAttackTime = 0;
		}
		super.thinkAttack();
	}
	
	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || attacker == null || attacker.getPlayer() == null)
			return;
			
		if(Rnd.chance(13))
			Functions.npcSay(actor, says[Rnd.get(says.length)]);
			
		if(actor.getCurrentHpPercents() < 30)
		{
			_lastAttackTime = System.currentTimeMillis();
			if(actor.getCurrentHpPercents() > 1)
			{
				if(canSay == true)
				{
					Functions.npcSay(actor, "Wait... Wait! Stop! Save me, and I`ll give you 10,000,000 adena!!");
					canSay = false;
				}	
			}
		}					
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;
			
		Functions.npcSay(actor, "Ugh... A curce upon you...!");
		canSay = true;
		_lastAttackTime = 0;
		
		super.onEvtDead(killer);
	}
}