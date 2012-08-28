package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public final class HennaInfo extends L2GameServerPacket
{
  private static final String _S__E4_HennaInfo = "[S] E4 HennaInfo";
  private final L2PcInstance _activeChar;
  private final L2HennaInstance[] _hennas = new L2HennaInstance[3];
  private int _count;

  public HennaInfo(L2PcInstance player)
  {
    _activeChar = player;

    int j = 0;
    for (int i = 0; i < 3; i++)
    {
      L2HennaInstance h = _activeChar.getHenna(i + 1);
      if (h == null)
        continue;
      _hennas[(j++)] = h;
    }

    _count = j;
  }

  protected final void writeImpl()
  {
    writeC(228);

    writeC(_activeChar.getHennaStatINT());
    writeC(_activeChar.getHennaStatSTR());
    writeC(_activeChar.getHennaStatCON());
    writeC(_activeChar.getHennaStatMEN());
    writeC(_activeChar.getHennaStatDEX());
    writeC(_activeChar.getHennaStatWIT());

    writeD(3);

    writeD(_count);
    for (int i = 0; i < _count; i++)
    {
      writeD(_hennas[i].getSymbolId());
      writeD(_hennas[i].getSymbolId());
    }
  }

  public String getType()
  {
    return "[S] E4 HennaInfo";
  }
}