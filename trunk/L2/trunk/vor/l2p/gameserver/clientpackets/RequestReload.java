package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.World;
import l2p.gameserver.network.GameClient;

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