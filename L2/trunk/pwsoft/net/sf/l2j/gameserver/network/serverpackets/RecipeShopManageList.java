package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import net.sf.l2j.gameserver.model.L2ManufactureItem;
import net.sf.l2j.gameserver.model.L2ManufactureList;
import net.sf.l2j.gameserver.model.L2RecipeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class RecipeShopManageList extends L2GameServerPacket
{
  private L2PcInstance _seller;
  private boolean _isDwarven;
  private L2RecipeList[] _recipes;

  public RecipeShopManageList(L2PcInstance seller, boolean isDwarven)
  {
    _seller = seller;
    _isDwarven = isDwarven;

    if ((_isDwarven) && (_seller.hasDwarvenCraft()))
      _recipes = _seller.getDwarvenRecipeBook();
    else
      _recipes = _seller.getCommonRecipeBook();
    L2ManufactureList list;
    if (_seller.getCreateList() != null)
    {
      list = _seller.getCreateList();
      for (L2ManufactureItem item : list.getList())
      {
        if (item.isDwarven() != _isDwarven)
          list.getList().remove(item);
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(216);
    writeD(_seller.getObjectId());
    writeD(_seller.getAdena());
    writeD(_isDwarven ? 0 : 1);

    if (_recipes == null)
    {
      writeD(0);
    }
    else
    {
      writeD(_recipes.length);

      for (int i = 0; i < _recipes.length; i++)
      {
        L2RecipeList temp = _recipes[i];
        writeD(temp.getId());
        writeD(i + 1);
      }
    }

    if (_seller.getCreateList() == null)
    {
      writeD(0);
    }
    else
    {
      L2ManufactureList list = _seller.getCreateList();
      writeD(list.size());

      for (L2ManufactureItem item : list.getList())
      {
        writeD(item.getRecipeId());
        writeD(0);
        writeD(item.getCost());
      }
    }
  }

  public String getType()
  {
    return "S.RecipeShopManageList";
  }
}