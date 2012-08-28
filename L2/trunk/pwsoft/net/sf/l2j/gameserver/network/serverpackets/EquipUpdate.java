package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.templates.L2Item;

public class EquipUpdate extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(EquipUpdate.class.getName());
  private L2ItemInstance _item;
  private int _change;

  public EquipUpdate(L2ItemInstance item, int change)
  {
    _item = item;
    _change = change;
  }

  protected final void writeImpl()
  {
    int bodypart = 0;
    writeC(75);
    writeD(_change);
    writeD(_item.getObjectId());
    switch (_item.getItem().getBodyPart())
    {
    case 4:
      bodypart = 1;
      break;
    case 2:
      bodypart = 2;
      break;
    case 8:
      bodypart = 3;
      break;
    case 16:
      bodypart = 4;
      break;
    case 32:
      bodypart = 5;
      break;
    case 64:
      bodypart = 6;
      break;
    case 128:
      bodypart = 7;
      break;
    case 256:
      bodypart = 8;
      break;
    case 512:
      bodypart = 9;
      break;
    case 1024:
      bodypart = 10;
      break;
    case 2048:
      bodypart = 11;
      break;
    case 4096:
      bodypart = 12;
      break;
    case 8192:
      bodypart = 13;
      break;
    case 16384:
      bodypart = 14;
      break;
    case 65536:
      bodypart = 15;
    }

    writeD(bodypart);
  }
}