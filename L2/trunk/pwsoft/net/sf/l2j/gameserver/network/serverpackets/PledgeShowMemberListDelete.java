package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
  private String _player;

  public PledgeShowMemberListDelete(String playerName)
  {
    _player = playerName;
  }

  protected final void writeImpl()
  {
    writeC(86);
    writeS(_player);
  }

  public String getType()
  {
    return "S.PledgeShowMemberListDelete";
  }
}