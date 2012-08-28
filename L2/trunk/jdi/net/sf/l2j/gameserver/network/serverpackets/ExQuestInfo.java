package net.sf.l2j.gameserver.network.serverpackets;

public class ExQuestInfo extends L2GameServerPacket
{
  private static final String _S__FE_19_EXQUESTINFO = "[S] FE:19 EXQUESTINFO";

  protected void writeImpl()
  {
    writeC(254);
    writeH(25);
  }

  public String getType()
  {
    return "[S] FE:19 EXQUESTINFO";
  }
}