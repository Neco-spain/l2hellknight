package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.MatchingRoomManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
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
    MatchingRoomManager.getInstance().removeFromWaitingList(player);
  }
}