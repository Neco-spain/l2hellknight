package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.templates.L2Item;

public class MultiSellList extends L2GameServerPacket
{
  private static final String _S__D0_MULTISELLLIST = "[S] D0 MultiSellList";
  protected int _listId;
  protected int _page;
  protected int _finished;
  protected L2Multisell.MultiSellListContainer _list;

  public MultiSellList(L2Multisell.MultiSellListContainer list, int page, int finished)
  {
    _list = list;
    _listId = list.getListId();
    _page = page;
    _finished = finished;
  }

  protected void writeImpl()
  {
    writeC(208);
    writeD(_listId);
    writeD(_page);
    writeD(_finished);
    writeD(40);
    writeD(_list == null ? 0 : _list.getEntries().size());

    if (_list != null)
    {
      for (L2Multisell.MultiSellEntry ent : _list.getEntries())
      {
        writeD(ent.getEntryId());
        writeD(0);
        writeD(0);
        writeC(1);
        writeH(ent.getProducts().size());
        writeH(ent.getIngredients().size());

        for (L2Multisell.MultiSellIngredient i : ent.getProducts())
        {
          writeH(i.getItemId());
          writeD(0);
          try
          {
            writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
          }
          catch (Exception e)
          {
            _log.warning("[S] D0 MultiSellList: list ID:" + _listId + " page:" + _page + " product id:" + i.getItemId() + " ERROR! Use correct ProductId in Multisells!");
          }
          writeD(i.getItemCount());
          writeH(i.getEnchantmentLevel());
          writeD(0);
          writeD(0);
        }

        for (L2Multisell.MultiSellIngredient i : ent.getIngredients())
        {
          int items = i.getItemId();
          int typeE = 65535;

          if (items > 0)
          {
            try
            {
              typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
            }
            catch (Exception e)
            {
              if ((i.getItemId() != 65436) && (i.getItemId() != 65336))
                _log.warning("[S] D0 MultiSellList: list ID:" + _listId + " page:" + _page + " ingredient id:" + i.getItemId() + " ERROR! Use correct IngridientId in Multisells!");
            }
          }
          writeH(items);
          writeH(typeE);
          writeD(i.getItemCount());
          writeH(i.getEnchantmentLevel());
          writeD(0);
          writeD(0);
        }
      }
    }
  }

  public String getType()
  {
    return "[S] D0 MultiSellList";
  }
}