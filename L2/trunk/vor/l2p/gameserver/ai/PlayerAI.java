package l2p.gameserver.ai;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.geodata.GeoEngine;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.model.items.attachment.FlagItemAttachment;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.ExRotation;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SocialAction;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class PlayerAI extends PlayableAI
{
  public PlayerAI(Player actor)
  {
    super(actor);
  }

  protected void onIntentionRest()
  {
    changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
    setAttackTarget(null);
    clientStopMoving();
  }

  protected void onIntentionActive()
  {
    clearNextAction();
    changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
  }

  public void onIntentionInteract(GameObject object)
  {
    Player actor = getActor();

    if (actor.getSittingTask())
    {
      setNextAction(PlayableAI.nextAction.INTERACT, object, null, false, false);
      return;
    }
    if (actor.isSitting())
    {
      actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
      clientActionFailed();
      return;
    }
    super.onIntentionInteract(object);
  }

  public void onIntentionPickUp(GameObject object)
  {
    Player actor = getActor();

    if (actor.getSittingTask())
    {
      setNextAction(PlayableAI.nextAction.PICKUP, object, null, false, false);
      return;
    }
    if (actor.isSitting())
    {
      actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
      clientActionFailed();
      return;
    }
    super.onIntentionPickUp(object);
  }

  protected void thinkAttack(boolean checkRange)
  {
    Player actor = getActor();

    if (actor.isInFlyingTransform())
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      return;
    }

    FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
    if ((attachment != null) && (!attachment.canAttack(actor)))
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    if (actor.isFrozen())
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC });
      return;
    }

    super.thinkAttack(checkRange);
  }

  protected void thinkCast(boolean checkRange)
  {
    Player actor = getActor();

    FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
    if ((attachment != null) && (!attachment.canCast(actor, _skill)))
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendActionFailed();
      return;
    }

    if (actor.isFrozen())
    {
      setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
      actor.sendPacket(new IStaticPacket[] { SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC });
      return;
    }

    super.thinkCast(checkRange);
  }

  protected void thinkCoupleAction(Player target, Integer socialId, boolean cancel)
  {
    Player actor = getActor();
    if ((target == null) || (!target.isOnline()))
    {
      actor.sendPacket(Msg.COUPLE_ACTION_WAS_CANCELED);
      return;
    }

    if ((cancel) || (!actor.isInRange(target, 50L)) || (actor.isInRange(target, 20L)) || (actor.getReflection() != target.getReflection()) || (!GeoEngine.canSeeTarget(actor, target, false)))
    {
      target.sendPacket(Msg.COUPLE_ACTION_WAS_CANCELED);
      actor.sendPacket(Msg.COUPLE_ACTION_WAS_CANCELED);
      return;
    }
    if (_forceUse) {
      target.getAI().setIntention(CtrlIntention.AI_INTENTION_COUPLE_ACTION, actor, socialId);
    }
    int heading = actor.calcHeading(target.getX(), target.getY());
    actor.setHeading(heading);
    actor.broadcastPacket(new L2GameServerPacket[] { new ExRotation(actor.getObjectId(), heading) });

    actor.broadcastPacket(new L2GameServerPacket[] { new SocialAction(actor.getObjectId(), socialId.intValue()) });
  }

  public void Attack(GameObject target, boolean forceUse, boolean dontMove)
  {
    Player actor = getActor();

    if (actor.isInFlyingTransform())
    {
      actor.sendActionFailed();
      return;
    }

    if (System.currentTimeMillis() - actor.getLastAttackPacket() < Config.ATTACK_PACKET_DELAY)
    {
      actor.sendActionFailed();
      return;
    }

    actor.setLastAttackPacket();

    if (actor.getSittingTask())
    {
      setNextAction(PlayableAI.nextAction.ATTACK, target, null, forceUse, false);
      return;
    }
    if (actor.isSitting())
    {
      actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
      clientActionFailed();
      return;
    }

    super.Attack(target, forceUse, dontMove);
  }

  public void Cast(Skill skill, Creature target, boolean forceUse, boolean dontMove)
  {
    Player actor = getActor();

    if ((!skill.altUse()) && (!skill.isToggle()) && ((skill.getSkillType() != Skill.SkillType.CRAFT) || (!Config.ALLOW_TALK_WHILE_SITTING)))
    {
      if (actor.getSittingTask())
      {
        setNextAction(PlayableAI.nextAction.CAST, skill, target, forceUse, dontMove);
        clientActionFailed();
        return;
      }
      if ((skill.getSkillType() == Skill.SkillType.SUMMON) && (actor.getPrivateStoreType() != 0))
      {
        actor.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
        clientActionFailed();
        return;
      }

      if (actor.isSitting())
      {
        if (skill.getSkillType() == Skill.SkillType.TRANSFORMATION)
          actor.sendPacket(Msg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
        else {
          actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
        }
        clientActionFailed();
        return;
      }
    }
    super.Cast(skill, target, forceUse, dontMove);
  }

  public Player getActor()
  {
    return (Player)super.getActor();
  }
}