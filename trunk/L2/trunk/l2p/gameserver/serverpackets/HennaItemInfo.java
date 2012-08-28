package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.templates.Henna;

public class HennaItemInfo extends L2GameServerPacket
{
  private int _str;
  private int _con;
  private int _dex;
  private int _int;
  private int _wit;
  private int _men;
  private long _adena;
  private Henna _henna;

  public HennaItemInfo(Henna henna, Player player)
  {
    _henna = henna;
    _adena = player.getAdena();
    _str = player.getSTR();
    _dex = player.getDEX();
    _con = player.getCON();
    _int = player.getINT();
    _wit = player.getWIT();
    _men = player.getMEN();
  }

  protected final void writeImpl()
  {
    writeC(228);
    writeD(_henna.getSymbolId());
    writeD(_henna.getDyeId());
    writeQ(_henna.getDrawCount());
    writeQ(_henna.getPrice());
    writeD(1);
    writeQ(_adena);
    writeD(_int);
    writeC(_int + _henna.getStatINT());
    writeD(_str);
    writeC(_str + _henna.getStatSTR());
    writeD(_con);
    writeC(_con + _henna.getStatCON());
    writeD(_men);
    writeC(_men + _henna.getStatMEN());
    writeD(_dex);
    writeC(_dex + _henna.getStatDEX());
    writeD(_wit);
    writeC(_wit + _henna.getStatWIT());
  }
}