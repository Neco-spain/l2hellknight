package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.sf.l2j.gameserver.datatables.ItemTable;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellEntry;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellIngredient;
import net.sf.l2j.gameserver.model.L2Multisell.MultiSellListContainer;
import net.sf.l2j.gameserver.templates.L2Item;

public class MultiSellList extends L2GameServerPacket
{
  protected int _listId;
  protected int _page;
  protected int _finished;
  private ConcurrentLinkedQueue<L2Multisell.MultiSellEntry> _possiblelist = new ConcurrentLinkedQueue();

  public MultiSellList(L2Multisell.MultiSellListContainer list, int page, int finished, boolean flag)
  {
    _possiblelist = list.getEntries();
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
    writeD(_possiblelist == null ? 0 : _possiblelist.size());

    if (_possiblelist == null) {
      return;
    }
    ItemTable it = ItemTable.getInstance();
    for (L2Multisell.MultiSellEntry ent : _possiblelist)
    {
      writeD(ent.getEntryId());
      writeD(0);
      writeD(0);
      writeC(1);
      writeH(ent.getProducts().size());
      writeH(ent.getIngredients().size());

      for (L2Multisell.MultiSellIngredient prod : ent.getProducts())
      {
        writeH(prod.getItemId());
        writeD(it.getTemplate(prod.getItemId()).getBodyPart());
        writeH(it.getTemplate(prod.getItemId()).getType2());
        writeD(prod.getItemCount());
        writeH(prod.getEnchantmentLevel());
        writeD(0);
        writeD(0);
      }

      for (L2Multisell.MultiSellIngredient i : ent.getIngredients())
      {
        int itemId = i.getItemId();
        writeH(itemId);
        switch (itemId)
        {
        case 65336:
        case 65436:
          writeH(itemId);
          break;
        default:
          writeH(it.getTemplate(i.getItemId()).getType2());
        }

        writeD(i.getItemCount());
        writeH(i.getEnchantmentLevel());
        writeD(0);
        writeD(0);
      }
    }
  }

  public void gc()
  {
    _possiblelist.clear();
    _possiblelist = null;
  }
}