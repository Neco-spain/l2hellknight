package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket
{
  private static final String _S__FE_37_EXSHOWADVENTURERGUIDEBOOK = "[S] FE:37 ExShowAdventurerGuideBook";

  protected void writeImpl()
  {
    writeC(254);
    writeH(55);
  }

  public String getType()
  {
    return "[S] FE:37 ExShowAdventurerGuideBook";
  }
}