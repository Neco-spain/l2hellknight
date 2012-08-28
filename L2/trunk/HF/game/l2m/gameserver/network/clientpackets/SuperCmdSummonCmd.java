package l2m.gameserver.network.clientpackets;

class SuperCmdSummonCmd extends L2GameClientPacket
{
  private String _summonName;

  protected void readImpl()
  {
    _summonName = readS();
  }

  protected void runImpl()
  {
  }
}