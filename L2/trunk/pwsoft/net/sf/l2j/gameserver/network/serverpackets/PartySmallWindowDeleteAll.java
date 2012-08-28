package net.sf.l2j.gameserver.network.serverpackets;

public class PartySmallWindowDeleteAll extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(80);
  }
}