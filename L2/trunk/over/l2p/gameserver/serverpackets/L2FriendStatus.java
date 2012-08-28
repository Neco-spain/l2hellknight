package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;

public class L2FriendStatus extends L2GameServerPacket
{
  private String _charName;
  private boolean _login;

  public L2FriendStatus(Player player, boolean login)
  {
    _login = login;
    _charName = player.getName();
  }

  protected final void writeImpl()
  {
    writeC(119);
    writeD(_login ? 1 : 0);
    writeS(_charName);
    writeD(0);
  }
}