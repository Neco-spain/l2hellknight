package l2m.gameserver.serverpackets;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
  private String _player;

  public PledgeShowMemberListDelete(String playerName)
  {
    _player = playerName;
  }

  protected final void writeImpl()
  {
    writeC(93);
    writeS(_player);
  }
}