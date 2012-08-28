package l2m.gameserver.skills.effects;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.skills.Env;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class EffectGrow extends Effect
{
  public EffectGrow(Env env, EffectTemplate template)
  {
    super(env, template);
  }

  public void onStart()
  {
    super.onStart();
    if (_effected.isNpc())
    {
      NpcInstance npc = (NpcInstance)_effected;
      npc.setCollisionHeight(npc.getCollisionHeight() * 1.24D);
      npc.setCollisionRadius(npc.getCollisionRadius() * 1.19D);
    }
  }

  public void onExit()
  {
    super.onExit();
    if (_effected.isNpc())
    {
      NpcInstance npc = (NpcInstance)_effected;
      npc.setCollisionHeight(npc.getTemplate().collisionHeight);
      npc.setCollisionRadius(npc.getTemplate().collisionRadius);
    }
  }

  public boolean onActionTime()
  {
    return false;
  }
}