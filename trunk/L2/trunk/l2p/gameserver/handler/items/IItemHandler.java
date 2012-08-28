package l2p.gameserver.handler.items;

import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.utils.Location;
import l2p.gameserver.utils.Log;
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