package net.sf.l2j.gameserver.network.serverpackets;

public class PartySmallWindowDeleteAll extends L2GameServerPacket
{
  private static final String _S__65_PARTYSMALLWINDOWDELETEALL = "[S] 50 PartySmallWindowDeleteAll";

  protected final void writeImpl()
  {
    writeC(80);
  }

  public String getType()
  {
    return "[S] 50 PartySmallWindowDeleteAll";
  }
}