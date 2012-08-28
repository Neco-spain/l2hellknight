package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ShortCutInit extends L2GameServerPacket
{
  private static final String _S__57_SHORTCUTINIT = "[S] 45 ShortCutInit";
  private L2ShortCut[] _shortCuts;
  private L2PcInstance _activeChar;

  public ShortCutInit(L2PcInstance activeChar)
  {
    _activeChar = activeChar;

    if (_activeChar == null) {
      return;
    }
    _shortCuts = _activeChar.getAllShortCuts();
  }

  protected final void writeImpl()
  {
    writeC(69);
    writeD(_shortCuts.length);

    for (int i = 0; i < _shortCuts.length; i++)
    {
      L2ShortCut sc = _shortCuts[i];
      writeD(sc.getType());
      writeD(sc.getSlot() + sc.getPage() * 12);

      switch (sc.getType())
      {
      case 1:
        writeD(sc.getId());
        writeD(1);
        writeD(-1);
        writeD(0);
        writeD(0);
        writeH(0);
        writeH(0);
        break;
      case 2:
        writeD(sc.getId());
        writeD(sc.getLevel());
        writeC(0);
        writeD(1);
        break;
      case 3:
        writeD(sc.getId());
        writeD(1);
        break;
      case 4:
        writeD(sc.getId());
        writeD(1);
        break;
      case 5:
        writeD(sc.getId());
        writeD(1);
        break;
      default:
        writeD(sc.getId());
        writeD(1);
      }
    }
  }

  public String getType()
  {
    return "[S] 45 ShortCutInit";
  }
}