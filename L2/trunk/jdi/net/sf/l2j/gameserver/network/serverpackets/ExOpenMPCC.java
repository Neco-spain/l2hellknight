package net.sf.l2j.gameserver.network.serverpackets;

public class ExOpenMPCC extends L2GameServerPacket
{
  private static final String _S__FE_25_EXOPENMPCC = "[S] FE:25 ExOpenMPCC";

  protected void writeImpl()
  {
    writeC(254);
    writeH(37);
  }

  public String getType()
  {
    return "[S] FE:25 ExOpenMPCC";
  }
}