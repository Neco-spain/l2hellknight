package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PetItemList extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(PetItemList.class.getName());
  private L2PetInstance _pet;
  private FastTable<L2ItemInstance> _items;

  public PetItemList(L2PetInstance pet)
  {
    _pet = pet;
    _items = new FastTable();
    _items.addAll(pet.getInventory().listItems());
  }

  protected final void writeImpl()
  {
    writeC(178);

    writeH(_items.size());

    for (int i = _items.size() - 1; i > -1; i--)
    {
      L2ItemInstance temp = (L2ItemInstance)_items.get(i);
      if ((temp == null) || (temp.getItem() == null)) {
        continue;
      }
      writeH(temp.getItem().getType1());
      writeD(temp.getObjectId());
      writeD(temp.getItemId());
      writeD(temp.getCount());

      writeH(temp.getItem().getType2());
      writeH(255);
      if (temp.isEquipped())
      {
        writeH(1);
      }
      else
      {
        writeH(0);
      }
      writeD(temp.getItem().getBodyPart());

      writeH(temp.getEnchantLevel());
      writeH(0);
    }
    _items.clear();
  }
}