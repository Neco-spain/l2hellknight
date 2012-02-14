package items;

import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class TeleportBookmark
    implements IItemHandler
{

    public TeleportBookmark()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
    {
        if(playable == null || item == null || !(playable instanceof L2Player))
            return;
        L2Player player = (L2Player)playable;
        if(player.getBookMarkSlot() >= 30)
        {
            player.sendPacket(new L2GameServerPacket[] {
                new SystemMessage(2390)
            });
            return;
        } else
        {
            player.getInventory().destroyItem(item, 1L, false);
            player.setBookMarkSlot(player.getBookMarkSlot() + 3);
            player.sendPacket(new L2GameServerPacket[] {
                new SystemMessage(2409)
            });
            SystemMessage sm = new SystemMessage(302);
            sm.addItemName(Integer.valueOf(item.getItemId()));
            player.sendPacket(new L2GameServerPacket[] {
                sm
            });
            return;
        }
    }

    public int[] getItemIds()
    {
        return _itemIds;
    }

    public void onLoad()
    {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    private static final int _itemIds[] = {
        13015
    };

}
