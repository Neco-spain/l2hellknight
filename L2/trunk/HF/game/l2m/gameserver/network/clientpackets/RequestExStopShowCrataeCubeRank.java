package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.KrateisCubeEvent;
import l2m.gameserver.network.GameClient;

public class RequestExStopShowCrataeCubeRank extends L2GameClientPacket
{
  protected void readImpl()
    throws Exception
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
    cubeEvent.closeRank(player);
  }
}