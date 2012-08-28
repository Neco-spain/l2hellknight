package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.network.GameClient;

public class RequestReload extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.sendUserInfo(true);
    World.showObjectsToPlayer(player);
  }
}