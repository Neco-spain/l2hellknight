package l2p.gameserver.clientpackets;

import l2p.gameserver.instancemanager.MatchingRoomManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

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