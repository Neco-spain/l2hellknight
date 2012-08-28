package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class HennaItemInfo extends L2GameServerPacket
{
  private static final String _S__E3_HennaItemInfo = "[S] E3 HennaItemInfo";
  private L2PcInstance _activeChar;
  private L2HennaInstance _henna;

  public HennaItemInfo(L2HennaInstance henna, L2PcInstance player)
  {
    _henna = henna;
    _activeChar = player;
  }

  protected final void writeImpl()
  {
    writeC(227);
    writeD(_henna.getSymbolId());
    writeD(_henna.getItemIdDye());
    writeD(_henna.getAmountDyeRequire());
    writeD(_henna.getPrice());
    writeD(1);
    writeD(_activeChar.getAdena());

    writeD(_activeChar.getINT());
    writeC(_activeChar.getINT() + _henna.getStatINT());
    writeD(_activeChar.getSTR());
    writeC(_activeChar.getSTR() + _henna.getStatSTR());
    writeD(_activeChar.getCON());
    writeC(_activeChar.getCON() + _henna.getStatCON());
    writeD(_activeChar.getMEN());
    writeC(_activeChar.getMEN() + _henna.getStatMEM());
    writeD(_activeChar.getDEX());
    writeC(_activeChar.getDEX() + _henna.getStatDEX());
    writeD(_activeChar.getWIT());
    writeC(_activeChar.getWIT() + _henna.getStatWIT());
  }

  public String getType()
  {
    return "[S] E3 HennaItemInfo";
  }
}