package net.sf.l2j.gameserver.network.serverpackets;

public final class ActionFailed extends L2GameServerPacket
{
  private static final String _S__35_ACTIONFAILED = "[S] 25 ActionFailed";

  protected void writeImpl()
  {
    writeC(37);
  }

  public String getType()
  {
    return "[S] 25 ActionFailed";
  }
}