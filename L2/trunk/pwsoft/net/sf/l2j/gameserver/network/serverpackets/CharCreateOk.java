package net.sf.l2j.gameserver.network.serverpackets;

public class CharCreateOk extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(25);
    writeD(1);
  }
}