package l2p.gameserver.serverpackets;

public class StopAllianceWar extends L2GameServerPacket
{
  private String _allianceName;
  private String _char;

  public StopAllianceWar(String alliance, String charName)
  {
    _allianceName = alliance;
    _char = charName;
  }

  protected final void writeImpl()
  {
    writeC(196);
    writeS(_allianceName);
    writeS(_char);
  }
}