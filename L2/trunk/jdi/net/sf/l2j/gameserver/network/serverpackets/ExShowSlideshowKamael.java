package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowSlideshowKamael extends L2GameServerPacket
{
  private static final String _S__FE_5B_EXSHOWSLIDESHOWKAMAEL = "[S] FE:5B ExShowSlideshowKamael";

  protected void writeImpl()
  {
    writeC(254);
    writeH(91);
  }

  public String getType()
  {
    return "[S] FE:5B ExShowSlideshowKamael";
  }
}