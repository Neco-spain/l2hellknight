package l2p.gameserver.serverpackets;

public class StartPledgeWar extends L2GameServerPacket
{
  private String _pledgeName;
  private String _char;

  public StartPledgeWar(String pledge, String charName)
  {
    _pledgeName = pledge;
    _char = charName;
  }

  protected final void writeImpl()
  {
    writeC(99);
    writeS(_char);
    writeS(_pledgeName);
  }
}