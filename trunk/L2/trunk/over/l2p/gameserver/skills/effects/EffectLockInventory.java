package l2p.gameserver.skills.effects;

import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.LockType;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.stats.Env;
import l2p.gameserver.templates.StatsSet;

public class EffectLockInventory extends Effect
{
  private LockType _lockType;
  private int[] _lockItems;

  public EffectLockInventory(Env env, EffectTemplate template)
  {
    super(env, template);
    _lockType = ((LockType)template.getParam().getEnum("lockType", LockType.class));
    _lockItems = template.getParam().getIntegerArray("lockItems");
  }

  public void onStart()
  {
    super.onStart();

    Player player = _effector.getPlayer();

    player.getInventory().lockItems(_lockType, _lockItems);
  }

  public void onExit()
  {
    super.onExit();

    Player player = _effector.getPlayer();

    player.getInventory().unlock();
  }

  protected boolean onActionTime()
  {
    return false;
  }
}