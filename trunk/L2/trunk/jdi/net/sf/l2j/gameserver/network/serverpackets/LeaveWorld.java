package net.sf.l2j.gameserver.network.serverpackets;

public class LeaveWorld extends L2GameServerPacket
{
  private static final String _S__96_LEAVEWORLD = "[S] 7e LeaveWorld";

  protected final void writeImpl()
  {
    writeC(126);
  }

  public String getType()
  {
    return "[S] 7e LeaveWorld";
  }
}