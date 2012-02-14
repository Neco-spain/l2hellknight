package l2rt.status.gshandlers;

import java.io.PrintWriter;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.xml.ItemTemplates;

public class HandlerGive
{

    public HandlerGive()
    {
    }

    public static void give(String fullCmd, String argv[], PrintWriter _print)
    {
        if(argv.length < 4 || argv[1] == null || argv[1].isEmpty() || argv[1].equalsIgnoreCase("?"))
        {
            _print.println("USAGE: give itemid amount player");
        } else
        {
            L2Player player = L2World.getPlayer(argv[3]);
            int itemId = Integer.parseInt(argv[1]);
            long amount = Integer.parseInt(argv[2]);
            L2ItemInstance item = ItemTemplates.getInstance().createItem(itemId);
            if(player != null && item != null)
            {
                item.setCount(amount);
                player.getInventory().addItem(item);
                InventoryUpdate iu = new InventoryUpdate();
                iu.addItem(item);
                player.sendPacket(new L2GameServerPacket[] {
                    iu
                });
                SystemMessage sm = new SystemMessage(1533);
                sm.addItemName(Integer.valueOf(itemId));
                sm.addNumber(Long.valueOf(amount));
                player.sendPacket(new L2GameServerPacket[] {
                    sm
                });
                _print.println("ok");
            } else
            {
                _print.println("Player not found");
            }
        }
    }
}
