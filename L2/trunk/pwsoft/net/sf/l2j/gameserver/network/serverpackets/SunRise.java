package net.sf.l2j.gameserver.network.serverpackets;

public class SunRise extends L2GameServerPacket
{
  protected final void writeImpl()
  {
    writeC(18);
  }

  public String getType()
  {
    return "S.SunRise";
  }
}