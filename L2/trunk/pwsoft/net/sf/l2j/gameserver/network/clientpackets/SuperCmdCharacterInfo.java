package net.sf.l2j.gameserver.network.clientpackets;

public final class SuperCmdCharacterInfo extends L2GameClientPacket
{
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
    return "[C] SuperCmdCharacterInfo";
  }
}