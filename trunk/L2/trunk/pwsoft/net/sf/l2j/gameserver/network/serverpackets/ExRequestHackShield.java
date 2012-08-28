package net.sf.l2j.gameserver.network.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket
{
  protected void writeImpl()
  {
    writeC(254);
    writeH(72);
  }
}