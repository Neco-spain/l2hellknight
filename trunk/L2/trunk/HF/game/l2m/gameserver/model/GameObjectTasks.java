package l2m.gameserver.model;

import java.util.List;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ai.CharacterAI;
import l2m.gameserver.ai.CtrlEvent;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.network.serverpackets.ExVoteSystemInfo;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MagicSkillLaunched;
import l2m.gameserver.network.serverpackets.SystemMessage;

public class GameObjectTasks
{
  public static class NotifyAITask extends RunnableImpl
  {
    private final CtrlEvent _evt;
    private final Object _agr0;
    private final Object _agr1;
    private final HardReference<? extends Creature> _charRef;

    public NotifyAITask(Creature cha, CtrlEvent evt, Object agr0, Object agr1)
    {
      _charRef = cha.getRef();
      _evt = evt;
      _agr0 = agr0;
      _agr1 = agr1;
    }

    public NotifyAITask(Creature cha, CtrlEvent evt)
    {
      this(cha, evt, null, null);
    }

    public void runImpl()
    {
      Creature character = (Creature)_charRef.get();
      if ((character == null) || (!character.hasAI())) {
        return;
      }
      character.getAI().notifyEvent(_evt, _agr0, _agr1);
    }
  }

  public static class MagicLaunchedTask extends RunnableImpl
  {
    public boolean _forceUse;
    private final HardReference<? extends Creature> _charRef;

    public MagicLaunchedTask(Creature cha, boolean forceUse)
    {
      _charRef = cha.getRef();
      _forceUse = forceUse;
    }

    public void runImpl()
    {
      Creature character = (Creature)_charRef.get();
      if (character == null)
        return;
      Skill castingSkill = character.getCastingSkill();
      Creature castingTarget = character.getCastingTarget();
      if ((castingSkill == null) || (castingTarget == null))
      {
        character.clearCastVars();
        return;
      }
      List targets = castingSkill.getTargets(character, castingTarget, _forceUse);
      character.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(character.getObjectId(), castingSkill.getDisplayId(), castingSkill.getDisplayLevel(), targets) });
    }
  }

  public static class MagicUseTask extends RunnableImpl
  {
    public boolean _forceUse;
    private final HardReference<? extends Creature> _charRef;

    public MagicUseTask(Creature cha, boolean forceUse)
    {
      _charRef = cha.getRef();
      _forceUse = forceUse;
    }

    public void runImpl()
    {
      Creature character = (Creature)_charRef.get();
      if (character == null)
        return;
      Skill castingSkill = character.getCastingSkill();
      Creature castingTarget = character.getCastingTarget();
      if ((castingSkill == null) || (castingTarget == null))
      {
        character.clearCastVars();
        return;
      }
      character.onMagicUseTimer(castingTarget, castingSkill, _forceUse);
    }
  }

  public static class HitTask extends RunnableImpl
  {
    boolean _crit;
    boolean _miss;
    boolean _shld;
    boolean _soulshot;
    boolean _unchargeSS;
    boolean _notify;
    int _damage;
    private final HardReference<? extends Creature> _charRef;
    private final HardReference<? extends Creature> _targetRef;

    public HitTask(Creature cha, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, boolean shld, boolean unchargeSS, boolean notify)
    {
      _charRef = cha.getRef();
      _targetRef = target.getRef();
      _damage = damage;
      _crit = crit;
      _shld = shld;
      _miss = miss;
      _soulshot = soulshot;
      _unchargeSS = unchargeSS;
      _notify = notify;
    }

    public void runImpl()
    {
      Creature character;
      Creature target;
      if (((character = (Creature)_charRef.get()) == null) || ((target = (Creature)_targetRef.get()) == null))
        return;
      Creature target;
      if (character.isAttackAborted()) {
        return;
      }
      character.onHitTimer(target, _damage, _crit, _miss, _soulshot, _shld, _unchargeSS);

      if (_notify)
        character.getAI().notifyEvent(CtrlEvent.EVT_READY_TO_ACT);
    }
  }

  public static class CastEndTimeTask extends RunnableImpl
  {
    private final HardReference<? extends Creature> _charRef;

    public CastEndTimeTask(Creature character)
    {
      _charRef = character.getRef();
    }

    public void runImpl()
    {
      Creature character = (Creature)_charRef.get();
      if (character == null)
        return;
      character.onCastEndTime();
    }
  }

  public static class AltMagicUseTask extends RunnableImpl
  {
    public final Skill _skill;
    private final HardReference<? extends Creature> _charRef;
    private final HardReference<? extends Creature> _targetRef;

    public AltMagicUseTask(Creature character, Creature target, Skill skill)
    {
      _charRef = character.getRef();
      _targetRef = target.getRef();
      _skill = skill;
    }

    public void runImpl()
    {
      Creature cha;
      Creature target;
      if (((cha = (Creature)_charRef.get()) == null) || ((target = (Creature)_targetRef.get()) == null))
        return;
      Creature target;
      cha.altOnMagicUseTimer(target, _skill);
    }
  }

  public static class EndStandUpTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public EndStandUpTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.sittingTaskLaunched = false;
      player.setSitting(false);
      if (!player.getAI().setNextIntention())
        player.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }
  }

  public static class EndSitDownTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public EndSitDownTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.sittingTaskLaunched = false;
      player.getAI().clearNextAction();
    }
  }

  public static class UnJailTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public UnJailTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.unblock();
      player.standUp();
      String[] re = player.getVar("jailedFrom").split(";");
      player.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]), Integer.parseInt(re[3]));

      player.unsetVar("jailed");
      player.unsetVar("jailedFrom");
    }
  }

  public static class KickTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public KickTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.setOfflineMode(false);
      player.kick();
    }
  }

  public static class WaterTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public WaterTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      if ((player.isDead()) || (!player.isInWater()))
      {
        player.stopWaterTask();
        return;
      }

      double reduceHp = player.getMaxHp() / 100;
      player.reduceCurrentHp(reduceHp, player, null, false, false, true, false, false, false, false);
      player.sendPacket(new SystemMessage(297).addNumber(()reduceHp));
    }
  }

  public static class RecomBonusTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public RecomBonusTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.setRecomBonusTime(0);
      player.sendPacket(new ExVoteSystemInfo(player));
    }
  }

  public static class HourlyTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public HourlyTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null) {
        return;
      }
      int hoursInGame = player.getHoursInGame();
      player.sendPacket(new SystemMessage(764).addNumber(hoursInGame));
      player.sendPacket(new SystemMessage(3207).addNumber(player.addRecomLeft()));
    }
  }

  public static class PvPFlagTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public PvPFlagTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null) {
        return;
      }
      long diff = Math.abs(System.currentTimeMillis() - player.getlastPvpAttack());
      if (diff > Config.PVP_TIME)
        player.stopPvPFlag();
      else if (diff > Config.PVP_TIME - 20000)
        player.updatePvPFlag(2);
      else
        player.updatePvPFlag(1);
    }
  }

  public static class SoulConsumeTask extends RunnableImpl
  {
    private final HardReference<Player> _playerRef;

    public SoulConsumeTask(Player player)
    {
      _playerRef = player.getRef();
    }

    public void runImpl()
    {
      Player player = (Player)_playerRef.get();
      if (player == null)
        return;
      player.setConsumedSouls(player.getConsumedSouls() + 1, null);
    }
  }

  public static class DeleteTask extends RunnableImpl
  {
    private final HardReference<? extends Creature> _ref;

    public DeleteTask(Creature c)
    {
      _ref = c.getRef();
    }

    public void runImpl()
    {
      Creature c = (Creature)_ref.get();

      if (c != null)
        c.deleteMe();
    }
  }
}