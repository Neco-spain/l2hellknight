package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class Scrolls
    implements IItemHandler, ScriptFile
{

    public Scrolls()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item, Boolean ctrl)
    {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player player = (L2Player)playable;
        if(player.isActionsDisabled() || player.isSitting())
        {
            player.sendActionFailed();
            return;
        }
        int itemId = item.getItemId();
        if(!validateScrollGrade(player.getLevel(), itemId))
        {
            player.sendPacket(new L2GameServerPacket[] {
                INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED
            });
            return;
        } else
        {
            player.getInventory().destroyItem(item, 1L, true);
            player.sendPacket(new L2GameServerPacket[] {
                (new SystemMessage(110)).addItemName(Integer.valueOf(itemId))
            });
            player.setCharmOfCourage(true);
            return;
        }
    }

    public boolean validateScrollGrade(byte playerLvl, int itemId)
    {
        switch(itemId)
        {
        case 8515: 
            return playerLvl <= 19;

        case 8516: 
            return playerLvl >= 20 && playerLvl <= 39;

        case 8517: 
            return playerLvl >= 40 && playerLvl <= 51;

        case 8518: 
            return playerLvl >= 52 && playerLvl <= 60;

        case 8519: 
            return playerLvl >= 61 && playerLvl <= 75;

        case 8520: 
            return playerLvl >= 76;
        }
        return false;
    }

    public final int[] getItemIds()
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
        8515, 8516, 8517, 8518, 8519, 8520
    };
    static final SystemMessage INCOMPATIBLE_ITEM_GRADE_THIS_ITEM_CANNOT_BE_USED = new SystemMessage(1902);

}
