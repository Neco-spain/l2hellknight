package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg;

public final class RequestRecipeShopListSet extends L2GameClientPacket
{
  private int _count;
  private int[] _items;

  protected void readImpl()
  {
    _count = readD();
    if ((_count < 0) || (_count * 8 > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
      _count = 0;
    _items = new int[_count * 2];
    for (int x = 0; x < _count; x++)
    {
      int recipeID = readD(); _items[(x * 2 + 0)] = recipeID;
      int cost = readD(); _items[(x * 2 + 1)] = cost;
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.isInDuel())
    {
      player.sendPacket(Static.CANT_CRAFT_DURING_COMBAT);
      return;
    }

    if ((_count == 0) || (player.getCreateList() == null))
    {
      player.setPrivateStoreType(0);
      player.broadcastUserInfo();
      player.standUp();
    }
    else
    {
      L2ManufactureList createList = new L2ManufactureList();

      for (int x = 0; x < _count; x++)
      {
        int recipeID = _items[(x * 2 + 0)];
        int cost = _items[(x * 2 + 1)];
        createList.add(new L2ManufactureItem(recipeID, cost));
      }
      createList.setStoreName(player.getCreateList() != null ? player.getCreateList().getStoreName() : "");
      player.setCreateList(createList);

      player.setPrivateStoreType(5);
      player.sitDown();
      player.saveTradeList();
      player.broadcastUserInfo();
      player.sendPacket(new RecipeShopMsg(player));
      player.broadcastPacket(new RecipeShopMsg(player));
    }
  }

  public String getType()
  {
    return "C.RecipeShopListSet";
  }
}