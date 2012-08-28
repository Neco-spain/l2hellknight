package net.sf.l2j.gameserver.network.serverpackets;

public class ExCloseMPCC extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(254);
    writeH(38);
  }
}