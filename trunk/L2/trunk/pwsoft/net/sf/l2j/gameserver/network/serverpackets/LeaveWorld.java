package net.sf.l2j.gameserver.network.serverpackets;

public class LeaveWorld extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(126);
  }
}