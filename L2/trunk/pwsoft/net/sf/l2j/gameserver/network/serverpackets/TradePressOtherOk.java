package net.sf.l2j.gameserver.network.serverpackets;

public class TradePressOtherOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(124);
  }

  public String getType()
  {
    return "S.TradePressOtherOk";
  }
}