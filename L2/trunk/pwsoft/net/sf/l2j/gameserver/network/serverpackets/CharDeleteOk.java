package net.sf.l2j.gameserver.network.serverpackets;

public class CharDeleteOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(35);
  }
}