package l2rt.gameserver.ai;

import l2rt.Config;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;

public class L2PlayerAI extends L2PlayableAI
{
	public L2PlayerAI(L2Player actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionRest()
	{
		changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		setAttackTarget(null);
		clientStopMoving();
	}

	@Override
	protected void onIntentionActive()
	{
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	public void onIntentionInteract(L2Object object)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;

		if(actor.getSittingTask())
		{
			setNextAction(nextAction.INTERACT, object, null, false, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionInteract(object);
	}

	@Override
	public void onIntentionPickUp(L2Object object)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;

		if(actor.getSittingTask())
		{
			setNextAction(nextAction.PICKUP, object, null, false, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionPickUp(object);
	}

	@Override
	protected void thinkAttack(boolean checkRange)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;

		if(actor.isInFlyingTransform())
		{
			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			return;
		}

		super.thinkAttack(checkRange);
	}

	@Override
	public void Attack(L2Object target, boolean forceUse, boolean dontMove)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;

		if(actor.isInFlyingTransform())
		{
			actor.sendActionFailed();
			return;
		}

		if(System.currentTimeMillis() - actor.getLastAttackPacket() < Config.ATTACK_PACKET_DELAY)
		{
			actor.sendActionFailed();
			return;
		}
		actor.setLastAttackPacket();

		if(actor.getSittingTask())
		{
			setNextAction(nextAction.ATTACK, target, null, forceUse, false);
			return;
		}
		else if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.Attack(target, forceUse, dontMove);
	}

	@Override
	public void Cast(L2Skill skill, L2Character target, boolean forceUse, boolean dontMove)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;

		if(!skill.altUse() && !(skill.getSkillType() == L2Skill.SkillType.CRAFT && Config.ALLOW_TALK_WHILE_SITTING))
			// Если в этот момент встаем, то использовать скилл когда встанем
			if(actor.getSittingTask())
			{
				setNextAction(nextAction.CAST, skill, target, forceUse, dontMove);
				clientActionFailed();
				return;
			}
			// если сидим - скиллы нельзя использовать
			else if(actor.isSitting())
			{
				if(skill.getSkillType() == L2Skill.SkillType.TRANSFORMATION)
					actor.sendPacket(Msg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
				else
					actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);

				clientActionFailed();
				return;
			}
		super.Cast(skill, target, forceUse, dontMove);
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2Player actor = getActor();
		if(actor == null)
			return;
		// notify the tamed beast of attacks
		if(actor.getTrainedBeast() != null)
			actor.getTrainedBeast().onOwnerGotAttacked(attacker);
		super.onEvtAttacked(attacker, damage);
	}

	@Override
	public L2Player getActor()
	{
		return (L2Player) super.getActor();
	}
}