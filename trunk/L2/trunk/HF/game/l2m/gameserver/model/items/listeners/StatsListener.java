package l2m.gameserver.model.items.listeners;

import l2m.gameserver.listener.inventory.OnEquipListener;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.skills.funcs.Func;

public final class StatsListener
  implements OnEquipListener
{
  private static final StatsListener _instance = new StatsListener();

  public static StatsListener getInstance()
  {
    return _instance;
  }

  public void onUnequip(int slot, ItemInstance item, Playable actor)
  {
    actor.removeStatsOwner(item);
    actor.updateStats();
  }

  public void onEquip(int slot, ItemInstance item, Playable actor)
  {
    Func[] funcs = item.getStatFuncs();
    actor.addStatFuncs(funcs);
    actor.updateStats();
  }
}