package l2p.gameserver.serverpackets;

public class StartAllianceWar extends L2GameServerPacket
{
  private String _allianceName;
  private String _char;

  public StartAllianceWar(String alliance, String charName)
  {
    _allianceName = alliance;
    _char = charName;
  }

  protected final void writeImpl()
  {
    writeC(194);
    writeS(_char);
    writeS(_allianceName);
  }
}