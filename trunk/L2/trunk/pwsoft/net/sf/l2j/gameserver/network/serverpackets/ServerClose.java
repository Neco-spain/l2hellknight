package net.sf.l2j.gameserver.network.serverpackets;

public class ServerClose extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(38);
  }
}