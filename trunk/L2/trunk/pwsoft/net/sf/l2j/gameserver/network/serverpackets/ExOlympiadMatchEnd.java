package net.sf.l2j.gameserver.network.serverpackets;

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(254);
    writeH(45);
  }
}