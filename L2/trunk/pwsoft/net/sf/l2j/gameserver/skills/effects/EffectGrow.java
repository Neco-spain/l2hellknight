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

  public void onStart()
  {
    if (getEffected().isL2Npc())
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();
      npc.setCollisionHeight((int)(npc.getCollisionHeight() * 1.24D));
      npc.setCollisionRadius((int)(npc.getCollisionRadius() * 1.19D));

      getEffected().startAbnormalEffect(65536);
    }
  }

  public boolean onActionTime()
  {
    if (getEffected().isL2Npc())
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();
      npc.setCollisionHeight(npc.getTemplate().collisionHeight);
      npc.setCollisionRadius(npc.getTemplate().collisionRadius);

      getEffected().stopAbnormalEffect(65536);
    }
    return false;
  }

  public void onExit()
  {
    if (getEffected().isL2Npc())
    {
      L2NpcInstance npc = (L2NpcInstance)getEffected();
      npc.setCollisionHeight(npc.getTemplate().collisionHeight);
      npc.setCollisionRadius(npc.getTemplate().collisionRadius);

      getEffected().stopAbnormalEffect(65536);
    }
  }
}