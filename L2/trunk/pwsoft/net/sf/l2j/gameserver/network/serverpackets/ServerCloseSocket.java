package net.sf.l2j.gameserver.network.serverpackets;

public class ServerCloseSocket extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(175);
    writeD(1);
  }
}