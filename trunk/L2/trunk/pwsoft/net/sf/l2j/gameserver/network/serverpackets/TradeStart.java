package net.sf.l2j.gameserver.network.serverpackets;

import java.util.ArrayList;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.TradeList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class TradeStart extends L2GameServerPacket
{
  private ArrayList<L2ItemInstance> _tradelist = new ArrayList();
  private boolean can_writeImpl = false;
  private int requester_obj_id;

  public TradeStart(L2PcInstance me)
  {
    if (me == null) {
      return;
    }

    if ((me.getActiveTradeList() == null) || (me.getActiveTradeList().getPartner() == null)) {
      return;
    }

    requester_obj_id = me.getActiveTradeList().getPartner().getObjectId();

    L2ItemInstance[] inventory = me.getInventory().getAvailableItems(true);
    for (L2ItemInstance item : inventory) {
      if ((!item.isEquipped()) && (item.getItem().getType2() != 3) && (item.isTradeable())) {
        _tradelist.add(item);
      }
    }

    can_writeImpl = true;
  }

  protected final void writeImpl()
  {
    if (!can_writeImpl) {
      return;
    }

    writeC(30);
    writeD(requester_obj_id);
    int count = _tradelist.size();
    writeH(count);

    for (L2ItemInstance temp : _tradelist) {
      writeH(temp.getItem().getType1());
      writeD(temp.getObjectId());
      writeD(temp.getItemId());
      writeD(temp.getCount());
      writeH(temp.getItem().getType2());
      writeH(0);

      writeD(temp.getItem().getBodyPart());
      writeH(temp.getEnchantLevel());
      writeH(0);
      writeH(0);
    }
  }

  public void gc()
  {
    _tradelist.clear();
    _tradelist = null;
  }
}