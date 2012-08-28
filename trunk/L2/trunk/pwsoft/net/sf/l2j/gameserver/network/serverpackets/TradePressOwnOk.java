package net.sf.l2j.gameserver.network.serverpackets;

public class TradePressOwnOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(117);
  }

  public String getType()
  {
    return "S.TradePressOwnOk";
  }
}