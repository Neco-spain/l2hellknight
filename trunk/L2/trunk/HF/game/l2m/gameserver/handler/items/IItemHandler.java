package l2m.gameserver.handler.items;

import l2m.gameserver.Config;
import l2m.gameserver.model.Playable;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.taskmanager.ItemsAutoDestroy;
import l2m.gameserver.utils.Location;
import l2m.gameserver.utils.Log;
import org.apache.commons.lang3.ArrayUtils;

public abstract interface IItemHandler
{
  public static final IItemHandler NULL = new IItemHandler()
  {
    public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
    {
      return false;
    }

    public void dropItem(Player player, ItemInstance item, long count, Location loc)
    {
      if (item.isEquipped())
      {
        player.getInventory().unEquipItem(item);
        player.sendUserInfo(true);
      }

      item = player.getInventory().removeItemByObjectId(item.getObjectId(), count);
      if (item == null)
      {
        player.sendActionFailed();
        return;
      }

      if (Config.AUTODESTROY_ITEM_AFTER > 0)
      {
        ItemsAutoDestroy.getInstance().addItem(item);
      }

      Log.LogItem(player, "Drop", item);

      item.dropToTheGround(player, loc);
      player.disableDrop(1000);

      player.sendChanges();
    }

    public boolean pickupItem(Playable playable, ItemInstance item)
    {
      return true;
    }

    public int[] getItemIds()
    {
      return ArrayUtils.EMPTY_INT_ARRAY;
    }
  };

  public abstract boolean useItem(Playable paramPlayable, ItemInstance paramItemInstance, boolean paramBoolean);

  public abstract void dropItem(Player paramPlayer, ItemInstance paramItemInstance, long paramLong, Location paramLocation);

  public abstract boolean pickupItem(Playable paramPlayable, ItemInstance paramItemInstance);

  public abstract int[] getItemIds();
}