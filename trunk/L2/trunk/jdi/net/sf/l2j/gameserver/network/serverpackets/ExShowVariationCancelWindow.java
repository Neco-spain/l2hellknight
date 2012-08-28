package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowVariationCancelWindow extends L2GameServerPacket
{
  private static final String _S__FE_51_EXSHOWVARIATIONCANCELWINDOW = "[S] FE:51 ExShowVariationCancelWindow";

  protected void writeImpl()
  {
    writeC(254);
    writeH(81);
  }

  public String getType()
  {
    return "[S] FE:51 ExShowVariationCancelWindow";
  }
}