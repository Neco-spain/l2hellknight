package net.sf.l2j.gameserver.network.serverpackets;

public class TradeStartOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(31);
  }
}