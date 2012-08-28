package l2m.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;
import l2p.commons.util.Rnd;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.instances.SummonInstance;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.Env;

public class EffectDiscord extends Effect
{
  public EffectDiscord(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public boolean checkCondition()
  {
    int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
    int lvldiff = _effected.getLevel() - _effector.getLevel();
    if ((skilldiff > 10) || ((skilldiff > 5) && (Rnd.chance(30))) || (Rnd.chance(Math.abs(lvldiff) * 2))) {
      return false;
    }
    boolean multitargets = _skill.isAoE();

    if (!_effected.isMonster())
    {
      if (!multitargets)
        getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    if ((_effected.isFearImmune()) || (_effected.isRaid()))
    {
      if (!multitargets)
        getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
      return false;
    }

    Player player = _effected.getPlayer();
    if (player != null)
    {
      SiegeEvent siegeEvent = (SiegeEvent)player.getEvent(SiegeEvent.class);
      if ((_effected.isSummon()) && (siegeEvent != null) && (siegeEvent.containsSiegeSummon((SummonInstance)_effected)))
      {
        if (!multitargets)
          getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
        return false;
      }
    }

    if (_effected.isInZonePeace())
    {
      if (!multitargets)
        getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
      return false;
    }

    return super.checkCondition();
  }

  public void onStart()
  {
    super.onStart();
    _effected.startConfused();

    onActionTime();
  }

  public void onExit()
  {
    super.onExit();

    if (!_effected.stopConfused())
    {
      _effected.abortAttack(true, true);
      _effected.abortCast(true, true);
      _effected.stopMove();
      _effected.getAI().setAttackTarget(null);
      _effected.setWalking();
      _effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
  }

  public boolean onActionTime()
  {
    List targetList = new ArrayList();

    for (Creature character : _effected.getAroundCharacters(900, 200)) {
      if ((character.isNpc()) && (character != getEffected())) {
        targetList.add(character);
      }
    }
    if (targetList.isEmpty()) {
      return true;
    }

    Creature target = (Creature)targetList.get(Rnd.get(targetList.size()));

    _effected.setRunning();
    _effected.getAI().Attack(target, true, false);

    return false;
  }
}