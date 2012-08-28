package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ShortCut;

public class ShortCutRegister extends L2GameServerPacket
{
  private L2ShortCut _shortcut;

  public ShortCutRegister(L2ShortCut shortcut)
  {
    _shortcut = shortcut;
  }

  protected final void writeImpl()
  {
    writeC(68);

    writeD(_shortcut.getType());
    writeD(_shortcut.getSlot() + _shortcut.getPage() * 12);
    switch (_shortcut.getType())
    {
    case 1:
      writeD(_shortcut.getId());
      break;
    case 2:
      writeD(_shortcut.getId());
      writeD(_shortcut.getLevel());
      writeC(0);
      break;
    case 3:
      writeD(_shortcut.getId());
      break;
    case 4:
      writeD(_shortcut.getId());
      break;
    case 5:
      writeD(_shortcut.getId());
      break;
    default:
      writeD(_shortcut.getId());
    }

    writeD(1);
  }

  public String getType()
  {
    return "S.ShrtCutRegister";
  }
}