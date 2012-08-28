package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Effect.EffectType;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class EffectGrow extends L2Effect
{
  public EffectGrow(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public L2Effect.EffectType getEffectType()
  {
    return L2Effect.EffectType.BUFF;
  }

  public boolean onStart()
  {
    if ((getEffected() instanceof L2NpcInstance))
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();

      npc.setCollisionRadius((int)(npc.getCollisionRadius() * 1.19D));

      getEffected().startAbnormalEffect(65536);
      return true;
    }
    return false;
  }

  public boolean onActionTime()
  {
    if ((getEffected() instanceof L2NpcInstance))
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();

      npc.setCollisionRadius(npc.getTemplate().collisionRadius);

      getEffected().stopAbnormalEffect(65536);
    }
    return false;
  }

  public void onExit()
  {
    if ((getEffected() instanceof L2NpcInstance))
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();

      npc.setCollisionRadius(npc.getTemplate().collisionRadius);

      getEffected().stopAbnormalEffect(65536);
    }
  }
}