package net.sf.l2j.gameserver.network.serverpackets;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
  private static final String _S__FE_4A_EXMPCCSHOWPARTYMEMBERINFO = "[S] FE:4A ExMPCCShowPartyMemberInfo";

  protected void writeImpl()
  {
    writeC(254);
    writeH(74);
  }

  public String getType()
  {
    return "[S] FE:4A ExMPCCShowPartyMemberInfo";
  }
}