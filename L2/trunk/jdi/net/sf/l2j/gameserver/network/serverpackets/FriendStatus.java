package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class FriendStatus extends L2GameServerPacket
{
  private static final String _S__FC_FRIENDSTATUS = "[S] FC FriendStatus";
  private String char_name;
  private boolean _login = false;

  public FriendStatus(L2PcInstance player, boolean login)
  {
    if (player == null)
      return;
    _login = login;
    char_name = player.getName();
  }

  protected final void writeImpl()
  {
    if (char_name == null)
      return;
    writeC(252);
    writeD(_login ? 1 : 0);
    writeS(char_name);
    writeD(0);
  }

  public String getType()
  {
    return "[S] FC FriendStatus";
  }
}