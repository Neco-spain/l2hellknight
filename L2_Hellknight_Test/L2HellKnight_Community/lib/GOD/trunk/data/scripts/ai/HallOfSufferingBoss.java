package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;

/**
 * AI босов близнецов Yehan Klodekus и Yehan Klanikus для Seed of Infinity, инстанс Hall of Suffering:
 * - становится неуявзвимым, если далеко от брата
 * - если убивают и при этом у брата более 10% ХП, то ресаемся и хилим себя на 15%
 *
 * @author SYS
 */
public class HallOfSufferingBoss extends Fighter
{
	private static final long INVUL_DISTANCE = 300;
	private static final L2Skill SKILL_DEFEAT = SkillTable.getInstance().getInfo(5823, 1);
	private static final L2Skill SKILL_ARISE = SkillTable.getInstance().getInfo(5824, 1);

	private int _brotherId;
	private L2NpcInstance _brother;
	private long _wait_timeout = 0;

	public HallOfSufferingBoss(L2Character actor)
	{
		super(actor);
		if(actor.getNpcId() == 25665)
			_brotherId = 25666;
		else
			_brotherId = 25665;
	}

	private boolean searchBrother()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return false;

		if(_brother == null)
		{
			// Ищем брата не чаще, чем раз в 15 секунд, если по каким-то причинам его нету
			if(System.currentTimeMillis() > _wait_timeout)
			{
				_wait_timeout = System.currentTimeMillis() + 15000;
				for(L2NpcInstance npc : L2World.getAroundNpc(actor))
					if(npc.getNpcId() == _brotherId)
					{
						_brother = npc;
						return true;
					}
			}
		}
		return false;
	}

	@Override
	protected boolean thinkActive()
	{
		if(_brother == null)
			searchBrother();

		return super.thinkActive();
	}

	@Override
	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(_brother == null)
			searchBrother();
		else
		{
			if(!_brother.isDead() && !actor.isInRange(_brother, INVUL_DISTANCE))
				actor.setIsInvul(true);
			else
				actor.setIsInvul(false);
		}

		super.thinkAttack();
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

		if(_brother == null)
			searchBrother();
		else if(_brother.getCurrentHpPercents() > 20 && actor.getCurrentHp() - damage < actor.getMaxHp() / 10)
		{
			// Если у брата > 20% ХП, то невозможно опустить ХП ниже 10%
			actor.abortAttack(true, false);
			actor.abortCast(true);
			actor.stopMove();
			clearTasks();
			addTaskBuff(actor, SKILL_DEFEAT);
			addTaskBuff(actor, SKILL_ARISE);
			for(L2Playable playable : L2World.getAroundPlayables(actor))
			{
				if(playable.getTargetId() == actor.getObjectId())
				{
					playable.abortAttack(true, false);
					playable.abortCast(true);
					playable.setTarget(null);
				}
			}
			actor.setCurrentHp(actor.getMaxHp() / 3, true);
			return;
		}

		super.onEvtAttacked(attacker, damage);
	}
}