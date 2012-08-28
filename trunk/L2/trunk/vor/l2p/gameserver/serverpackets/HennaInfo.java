package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.templates.Henna;

public class HennaInfo extends L2GameServerPacket
{
  private final Henna[] _hennas = new Henna[3];
  private final int _str;
  private final int _con;
  private final int _dex;
  private final int _int;
  private final int _wit;
  private final int _men;
  private int _count;

  public HennaInfo(Player player)
  {
    _count = 0;

    for (int i = 0; i < 3; i++)
    {
      Henna h;
      if ((h = player.getHenna(i + 1)) != null)
        _hennas[(_count++)] = new Henna(h.getSymbolId(), h.isForThisClass(player));
    }
    _str = player.getHennaStatSTR();
    _con = player.getHennaStatCON();
    _dex = player.getHennaStatDEX();
    _int = player.getHennaStatINT();
    _wit = player.getHennaStatWIT();
    _men = player.getHennaStatMEN();
  }

  protected final void writeImpl()
  {
    writeC(229);
    writeC(_int);
    writeC(_str);
    writeC(_con);
    writeC(_men);
    writeC(_dex);
    writeC(_wit);
    writeD(3);
    writeD(_count);
    for (int i = 0; i < _count; i++)
    {
      writeD(_hennas[i]._symbolId);
      writeD(_hennas[i]._valid ? _hennas[i]._symbolId : 0);
    }
  }

  private static class Henna {
    private int _symbolId;
    private boolean _valid;

    public Henna(int sy, boolean valid) {
      _symbolId = sy;
      _valid = valid;
    }
  }
}