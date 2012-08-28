package l2m.gameserver.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
  private String _pledgeName;
  private String _char;

  public SurrenderPledgeWar(String pledge, String charName)
  {
    _pledgeName = pledge;
    _char = charName;
  }

  protected final void writeImpl()
  {
    writeC(103);
    writeS(_pledgeName);
    writeS(_char);
  }
}