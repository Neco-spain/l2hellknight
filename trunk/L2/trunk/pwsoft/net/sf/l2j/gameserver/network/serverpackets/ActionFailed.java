package net.sf.l2j.gameserver.network.serverpackets;

public final class ActionFailed extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(37);
  }
}