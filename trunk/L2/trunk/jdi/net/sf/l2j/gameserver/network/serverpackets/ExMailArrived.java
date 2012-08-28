package net.sf.l2j.gameserver.network.serverpackets;

public class ExMailArrived extends L2GameServerPacket
{
  private static final String _S__FE_2D_EXMAILARRIVED = "[S] FE:2D ExMailArrived";

  protected void writeImpl()
  {
    writeC(254);
    writeH(45);
  }

  public String getType()
  {
    return "[S] FE:2D ExMailArrived";
  }
}