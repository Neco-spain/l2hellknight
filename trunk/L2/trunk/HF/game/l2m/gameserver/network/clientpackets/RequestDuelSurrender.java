package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.DuelEvent;
import l2m.gameserver.network.GameClient;

public class RequestDuelSurrender extends L2GameClientPacket
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
    DuelEvent duelEvent = (DuelEvent)player.getEvent(DuelEvent.class);
    if (duelEvent == null) {
      return;
    }
    duelEvent.packetSurrender(player);
  }
}