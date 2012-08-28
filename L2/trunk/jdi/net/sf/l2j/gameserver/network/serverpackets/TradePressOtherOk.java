package net.sf.l2j.gameserver.network.serverpackets;

public final class TradePressOtherOk extends L2GameServerPacket
{
  public static final TradePressOtherOk STATIC_PACKET = new TradePressOtherOk();

  public String getType()
  {
    return "[S] 7c TradePressOtherOk";
  }

  protected void writeImpl()
  {
    writeC(124);
  }
}