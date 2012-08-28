package net.sf.l2j.gameserver.network.serverpackets;

public class SunRise extends L2GameServerPacket
{
  private static final String _S__28_SUNRISE = "[S] 1c SunRise";

  protected final void writeImpl()
  {
    writeC(28);
  }

  public String getType()
  {
    return "[S] 1c SunRise";
  }
}