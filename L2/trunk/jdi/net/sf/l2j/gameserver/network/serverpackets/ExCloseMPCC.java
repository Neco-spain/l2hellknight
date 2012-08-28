package net.sf.l2j.gameserver.network.serverpackets;

public class ExCloseMPCC extends L2GameServerPacket
{
  private static final String _S__FE_26_EXCLOSEMPCC = "[S] FE:26 ExCloseMPCC";

  protected void writeImpl()
  {
    writeC(254);
    writeH(38);
  }

  public String getType()
  {
    return "[S] FE:26 ExCloseMPCC";
  }
}