package l2m.gameserver.serverpackets;

public class StopPledgeWar extends L2GameServerPacket
{
  private String _pledgeName;
  private String _char;

  public StopPledgeWar(String pledge, String charName)
  {
    _pledgeName = pledge;
    _char = charName;
  }

  protected final void writeImpl()
  {
    writeC(101);
    writeS(_pledgeName);
    writeS(_char);
  }
}