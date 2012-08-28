package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PetInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class PetItemList extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(PetItemList.class.getName());
  private static final String _S__cb_PETITEMLIST = "[S] b2  PetItemList";
  private L2PetInstance _activeChar;

  public PetItemList(L2PetInstance character)
  {
    _activeChar = character;
    if (Config.DEBUG)
    {
      L2ItemInstance[] items = _activeChar.getInventory().getItems();
      for (L2ItemInstance temp : items)
      {
        _log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
      }
    }
  }

  protected final void writeImpl()
  {
    writeC(178);

    L2ItemInstance[] items = _activeChar.getInventory().getItems();
    int count = items.length;
    writeH(count);

    for (L2ItemInstance temp : items)
    {
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
  }

  public String getType()
  {
    return "[S] b2  PetItemList";
  }
}