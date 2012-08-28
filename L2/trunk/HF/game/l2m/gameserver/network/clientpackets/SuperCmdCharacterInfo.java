package l2m.gameserver.network.clientpackets;

class SuperCmdCharacterInfo extends L2GameClientPacket
{
  private String _characterName;

  protected void readImpl()
  {
    _characterName = readS();
  }

  protected void runImpl()
  {
  }
}