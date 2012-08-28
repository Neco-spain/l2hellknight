package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2EffectPointInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignet;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;

public class EffectSignet extends L2Effect
{
  private L2Skill _skill;
  private L2EffectPointInstance _actor;

  public EffectSignet(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SIGNET_EFFECT;
  }

  public boolean onStart()
  {
    if ((getSkill() instanceof L2SkillSignet))
      _skill = SkillTable.getInstance().getInfo(((L2SkillSignet)getSkill()).effectId, getLevel());
    else if ((getSkill() instanceof L2SkillSignetCasttime))
      _skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime)getSkill()).effectId, getLevel());
    _actor = ((L2EffectPointInstance)getEffected());
    return true;
  }

  public boolean onActionTime()
  {
    if (_skill == null) return true;
    int mpConsume = _skill.getMpConsume();

    if (mpConsume > getEffector().getCurrentMp())
    {
      getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
      return false;
    }

    getEffector().reduceCurrentMp(mpConsume);

    for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
    {
      if (cha == null)
        continue;
      _skill.getEffects(_actor, cha);
      _actor.broadcastPacket(new MagicSkillUser(_actor, cha, 5123, 1, 0, 0));
    }
    return true;
  }

  public void onExit()
  {
    if (_actor != null)
    {
      _actor.deleteMe();
    }
  }
}