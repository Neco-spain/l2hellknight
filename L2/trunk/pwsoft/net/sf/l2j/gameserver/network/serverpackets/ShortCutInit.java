package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.model.L2ShortCut;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class ShortCutInit extends L2GameServerPacket
{
  private FastTable<L2ShortCut> _shortCuts;
  private L2PcInstance _activeChar;

  public ShortCutInit(L2PcInstance activeChar)
  {
    _activeChar = activeChar;

    if (_activeChar == null)
      return;
    _shortCuts = new FastTable();
    _shortCuts.addAll(_activeChar.getAllShortCuts());
  }

  protected final void writeImpl()
  {
    writeC(69);
    writeD(_shortCuts.size());

    int i = 0; for (int n = _shortCuts.size(); i < n; i++)
    {
      L2ShortCut sc = (L2ShortCut)_shortCuts.get(i);
      if (sc == null) {
        continue;
      }
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
        writeC(-1);
        writeD(-1);
        break;
      case 3:
      case 4:
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

  public void gc()
  {
    _shortCuts.clear();
    _shortCuts = null;
  }

  public String getType()
  {
    return "S.ShortCutInit";
  }
}