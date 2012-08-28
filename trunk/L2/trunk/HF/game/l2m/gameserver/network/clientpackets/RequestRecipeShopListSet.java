package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2m.gameserver.Config;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ManufactureItem;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.RecipeShopMsg;
import l2m.gameserver.utils.TradeHelper;

public class RequestRecipeShopListSet extends L2GameClientPacket
{
  private int[] _recipes;
  private long[] _prices;
  private int _count;

  protected void readImpl()
  {
    _count = readD();
    if ((_count * 12 > _buf.remaining()) || (_count > 32767) || (_count < 1))
    {
      _count = 0;
      return;
    }
    _recipes = new int[_count];
    _prices = new long[_count];
    for (int i = 0; i < _count; i++)
    {
      _recipes[i] = readD();
      _prices[i] = readQ();
      if (_prices[i] >= 0L)
        continue;
      _count = 0;
      return;
    }
  }

  protected void runImpl()
  {
    Player manufacturer = ((GameClient)getClient()).getActiveChar();
    if ((manufacturer == null) || (_count == 0)) {
      return;
    }
    if (!TradeHelper.checksIfCanOpenStore(manufacturer, 5))
    {
      manufacturer.sendActionFailed();
      return;
    }

    if (_count > Config.MAX_PVTCRAFT_SLOTS)
    {
      sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
      return;
    }

    List createList = new CopyOnWriteArrayList();
    for (int i = 0; i < _count; i++)
    {
      int recipeId = _recipes[i];
      long price = _prices[i];
      if (!manufacturer.findRecipe(recipeId)) {
        continue;
      }
      ManufactureItem mi = new ManufactureItem(recipeId, price);
      createList.add(mi);
    }

    if (!createList.isEmpty())
    {
      manufacturer.setCreateList(createList);
      manufacturer.saveTradeList();
      manufacturer.setPrivateStoreType(5);
      manufacturer.broadcastPacket(new L2GameServerPacket[] { new RecipeShopMsg(manufacturer) });
      manufacturer.sitDown(null);
      manufacturer.broadcastCharInfo();
    }

    manufacturer.sendActionFailed();
  }
}