package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.npc.NpcTemplate;

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