package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2p.gameserver.network.GameClient;

public class RequestExStartShowCrataeCubeRank extends L2GameClientPacket
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
    KrateisCubeEvent cubeEvent = (KrateisCubeEvent)player.getEvent(KrateisCubeEvent.class);
    if (cubeEvent == null) {
      return;
    }
    cubeEvent.showRank(player);
  }
}