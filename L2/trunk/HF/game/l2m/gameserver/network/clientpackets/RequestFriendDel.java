package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.FriendList;
import l2m.gameserver.network.GameClient;

public class RequestFriendDel extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.getFriendList().removeFriend(_name);
  }
}