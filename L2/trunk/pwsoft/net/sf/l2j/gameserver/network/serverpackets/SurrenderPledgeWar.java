package net.sf.l2j.gameserver.network.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket
{
  private String _pledgeName;
  private String _playerName;

  public SurrenderPledgeWar(String pledge, String charName)
  {
    _pledgeName = pledge;
    _playerName = charName;
  }

  protected final void writeImpl()
  {
    writeC(105);
    writeS(_pledgeName);
    writeS(_playerName);
  }

  public String getType()
  {
    return "S.SurrenderPledgeWar";
  }
}