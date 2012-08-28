package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2EffectPointInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.NpcKnownList;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

final class EffectSignetAntiSummon extends L2Effect
{
  private L2EffectPointInstance _actor;

  public EffectSignetAntiSummon(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.SIGNET_GROUND;
  }

  public boolean onStart()
  {
    _actor = ((L2EffectPointInstance)getEffected());
    return true;
  }

  public boolean onActionTime()
  {
    if (getCount() == getTotalCount() - 1) return true;
    int mpConsume = getSkill().getMpConsume();

    for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
    {
      if (cha == null) {
        continue;
      }
      if ((cha instanceof L2PlayableInstance))
      {
        L2PcInstance owner = null;

        if ((cha instanceof L2Summon))
          owner = ((L2Summon)cha).getOwner();
        else {
          owner = (L2PcInstance)cha;
        }
        if ((owner != null) && (owner.getPet() != null))
        {
          if (mpConsume > getEffector().getCurrentMp())
          {
            getEffector().sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
            return false;
          }

          getEffector().reduceCurrentMp(mpConsume);

          owner.getPet().unSummon(owner);
          owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
        }
      }
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