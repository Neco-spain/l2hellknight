package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopSellList extends L2GameServerPacket
{
  private static final String _S__D9_RecipeShopSellList = "[S] d9 RecipeShopSellList";
  private L2PcInstance _buyer;
  private L2PcInstance _manufacturer;

  public RecipeShopSellList(L2PcInstance buyer, L2PcInstance manufacturer)
  {
    _buyer = buyer;
    _manufacturer = manufacturer;
  }

  protected final void writeImpl()
  {
    L2ManufactureList createList = _manufacturer.getCreateList();

    if (createList != null)
    {
      writeC(217);
      writeD(_manufacturer.getObjectId());
      writeD((int)_manufacturer.getCurrentMp());
      writeD(_manufacturer.getMaxMp());
      writeD(_buyer.getAdena());

      int count = createList.size();
      writeD(count);

      for (int i = 0; i < count; i++)
      {
        L2ManufactureItem temp = (L2ManufactureItem)createList.getList().get(i);
        writeD(temp.getRecipeId());
        writeD(0);
        writeD(temp.getCost());
      }
    }
  }

  public String getType()
  {
    return "[S] d9 RecipeShopSellList";
  }
}