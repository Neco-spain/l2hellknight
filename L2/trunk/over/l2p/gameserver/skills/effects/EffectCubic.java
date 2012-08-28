package l2p.gameserver.skills.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.ai.CharacterAI;
import l2p.gameserver.ai.CtrlEvent;
import l2p.gameserver.data.xml.holder.CubicHolder;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.GameObject;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.MagicSkillLaunched;
import l2p.gameserver.serverpackets.MagicSkillUse;
import l2p.gameserver.stats.Env;
import l2p.gameserver.stats.Formulas;
import l2p.gameserver.templates.CubicTemplate;
import l2p.gameserver.templates.CubicTemplate.SkillInfo;
import l2p.gameserver.templates.StatsSet;

public class EffectCubic extends Effect
{
  private final CubicTemplate _template;
  private Future<?> _task = null;

  public EffectCubic(Env env, EffectTemplate template)
  {
    super(env, template);
    _template = CubicHolder.getInstance().getTemplate(getTemplate().getParam().getInteger("cubicId"), getTemplate().getParam().getInteger("cubicLevel"));
  }

  public void onStart()
  {
    super.onStart();
    Player player = _effected.getPlayer();
    if (player == null) {
      return;
    }
    player.addCubic(this);
    _task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(null), 1000L, 1000L);
  }

  public void onExit()
  {
    super.onExit();
    Player player = _effected.getPlayer();
    if (player == null) {
      return;
    }
    player.removeCubic(getId());
    _task.cancel(true);
    _task = null;
  }

  public void doAction(Player player)
  {
    for (Map.Entry entry : _template.getSkills())
      if (Rnd.chance(((Integer)entry.getKey()).intValue()))
      {
        for (CubicTemplate.SkillInfo skillInfo : (List)entry.getValue())
        {
          if (player.isSkillDisabled(skillInfo.getSkill()))
            continue;
          switch (5.$SwitchMap$l2p$gameserver$templates$CubicTemplate$ActionType[skillInfo.getActionType().ordinal()])
          {
          case 1:
            doAttack(player, skillInfo, _template.getDelay());
            break;
          case 2:
            doDebuff(player, skillInfo, _template.getDelay());
            break;
          case 3:
            doHeal(player, skillInfo, _template.getDelay());
            break;
          case 4:
            doCancel(player, skillInfo, _template.getDelay());
          }
        }

        break;
      }
  }

  protected boolean onActionTime()
  {
    return false;
  }

  public boolean isHidden()
  {
    return true;
  }

  public boolean isCancelable()
  {
    return false;
  }

  public int getId()
  {
    return _template.getId();
  }

  private static void doHeal(Player player, CubicTemplate.SkillInfo info, int delay)
  {
    Skill skill = info.getSkill();
    Creature target = null;
    double currentHp;
    if (player.getParty() == null)
    {
      if ((!player.isCurrentHpFull()) && (!player.isDead()))
        target = player;
    }
    else
    {
      currentHp = 2147483647.0D;
      for (Player member : player.getParty().getPartyMembers())
      {
        if (member == null) {
          continue;
        }
        if ((player.isInRange(member, info.getSkill().getCastRange())) && (!member.isCurrentHpFull()) && (!member.isDead()) && (member.getCurrentHp() < currentHp))
        {
          currentHp = member.getCurrentHp();
          target = member;
        }
      }
    }

    if (target == null) {
      return;
    }
    int chance = info.getChance((int)target.getCurrentHpPercents());

    if (!Rnd.chance(chance)) {
      return;
    }
    Creature aimTarget = target;
    player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L) });
    player.disableSkill(skill, delay * 1000L);
    ThreadPoolManager.getInstance().schedule(new RunnableImpl(aimTarget, player, skill)
    {
      public void runImpl()
        throws Exception
      {
        List targets = new ArrayList(1);
        targets.add(val$aimTarget);
        val$player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(val$player.getObjectId(), val$skill.getDisplayId(), val$skill.getDisplayLevel(), targets) });
        val$player.callSkill(val$skill, targets, false);
      }
    }
    , skill.getHitTime());
  }

  private static void doAttack(Player player, CubicTemplate.SkillInfo info, int delay)
  {
    if (!Rnd.chance(info.getChance())) {
      return;
    }
    Skill skill = info.getSkill();
    Creature target = null;
    if (player.isInCombat())
    {
      GameObject object = player.getTarget();
      target = (object != null) && (object.isCreature()) ? (Creature)object : null;
    }
    if ((target == null) || (target.isDead()) || ((target.isDoor()) && (!info.isCanAttackDoor())) || (!player.isInRangeZ(target, skill.getCastRange())) || (!target.isAutoAttackable(player)))
      return;
    Creature aimTarget = target;
    player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L) });
    player.disableSkill(skill, delay * 1000L);
    ThreadPoolManager.getInstance().schedule(new RunnableImpl(aimTarget, player, skill)
    {
      public void runImpl()
        throws Exception
      {
        List targets = new ArrayList(1);
        targets.add(val$aimTarget);

        val$player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(val$player.getObjectId(), val$skill.getDisplayId(), val$skill.getDisplayLevel(), targets) });
        val$player.callSkill(val$skill, targets, false);

        if (val$aimTarget.isNpc())
          if (val$aimTarget.paralizeOnAttack(val$player))
          {
            if (Config.PARALIZE_ON_RAID_DIFF)
              val$player.paralizeMe(val$aimTarget);
          }
          else
          {
            int damage = val$skill.getEffectPoint() != 0 ? val$skill.getEffectPoint() : (int)val$skill.getPower();
            val$aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, val$player, Integer.valueOf(damage));
          }
      }
    }
    , skill.getHitTime());
  }

  private static void doDebuff(Player player, CubicTemplate.SkillInfo info, int delay)
  {
    if (!Rnd.chance(info.getChance())) {
      return;
    }
    Skill skill = info.getSkill();
    Creature target = null;
    if (player.isInCombat())
    {
      GameObject object = player.getTarget();
      target = (object != null) && (object.isCreature()) ? (Creature)object : null;
    }
    if ((target == null) || (target.isDead()) || ((target.isDoor()) && (!info.isCanAttackDoor())) || (!player.isInRangeZ(target, skill.getCastRange())) || (!target.isAutoAttackable(player)))
      return;
    Creature aimTarget = target;
    player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L) });
    player.disableSkill(skill, delay * 1000L);
    ThreadPoolManager.getInstance().schedule(new RunnableImpl(aimTarget, player, skill, info)
    {
      public void runImpl()
        throws Exception
      {
        List targets = new ArrayList(1);
        targets.add(val$aimTarget);
        val$player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(val$player.getObjectId(), val$skill.getDisplayId(), val$skill.getDisplayLevel(), targets) });
        boolean succ = Formulas.calcSkillSuccess(val$player, val$aimTarget, val$skill, val$info.getChance());
        if (succ) {
          val$player.callSkill(val$skill, targets, false);
        }
        if (val$aimTarget.isNpc())
          if (val$aimTarget.paralizeOnAttack(val$player))
          {
            if (Config.PARALIZE_ON_RAID_DIFF)
              val$player.paralizeMe(val$aimTarget);
          }
          else
          {
            int damage = val$skill.getEffectPoint() != 0 ? val$skill.getEffectPoint() : (int)val$skill.getPower();
            val$aimTarget.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, val$player, Integer.valueOf(damage));
          }
      }
    }
    , skill.getHitTime());
  }

  private static void doCancel(Player player, CubicTemplate.SkillInfo info, int delay)
  {
    if (!Rnd.chance(info.getChance())) {
      return;
    }
    Skill skill = info.getSkill();
    boolean hasDebuff = false;
    for (Effect e : player.getEffectList().getAllEffects()) {
      if ((e != null) && (e.isOffensive()) && (e.isCancelable()) && (!e.getTemplate()._applyOnCaster))
        hasDebuff = true;
    }
    if (!hasDebuff) {
      return;
    }
    player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L) });
    player.disableSkill(skill, delay * 1000L);
    ThreadPoolManager.getInstance().schedule(new RunnableImpl(player, skill)
    {
      public void runImpl()
        throws Exception
      {
        List targets = new ArrayList(1);
        targets.add(val$player);
        val$player.broadcastPacket(new L2GameServerPacket[] { new MagicSkillLaunched(val$player.getObjectId(), val$skill.getDisplayId(), val$skill.getDisplayLevel(), targets) });
        val$player.callSkill(val$skill, targets, false);
      }
    }
    , skill.getHitTime());
  }

  private class ActionTask extends RunnableImpl
  {
    private ActionTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      if (!isActive()) {
        return;
      }
      Player player = (_effected != null) && (_effected.isPlayer()) ? (Player)_effected : null;
      if (player == null) {
        return;
      }
      doAction(player);
    }
  }
}