package net.sf.l2j.gameserver.network.serverpackets;

public class StartPledgeWar extends L2GameServerPacket
{
  private String _pledgeName;
  private String _playerName;

  public StartPledgeWar(String pledge, String charName)
  {
    _pledgeName = pledge;
    _playerName = charName;
  }

  protected final void writeImpl()
  {
    writeC(101);
    writeS(_playerName);
    writeS(_pledgeName);
  }

  public String getType()
  {
    return "S.StartPledgeWar";
  }
}