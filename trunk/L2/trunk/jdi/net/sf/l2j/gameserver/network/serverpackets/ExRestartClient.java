package net.sf.l2j.gameserver.network.serverpackets;

public class ExRestartClient extends L2GameServerPacket
{
  private static final String _S__FE_47_EXRESTARTCLIENT = "[S] FE:47 ExRestartClient";

  protected void writeImpl()
  {
    writeC(254);
    writeH(71);
  }

  public String getType()
  {
    return "[S] FE:47 ExRestartClient";
  }
}