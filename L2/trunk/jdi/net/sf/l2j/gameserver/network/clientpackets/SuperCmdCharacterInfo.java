package net.sf.l2j.gameserver.network.clientpackets;

public final class SuperCmdCharacterInfo extends L2GameClientPacket
{
  private static final String _C__39_00_SUPERCMDCHARACTERINFO = "[C] 39:00 SuperCmdCharacterInfo";
  private String _characterName;

  protected void readImpl()
  {
    _characterName = readS();
  }

  protected void runImpl()
  {
  }

  public String getType()
  {
    return "[C] 39:00 SuperCmdCharacterInfo";
  }
}