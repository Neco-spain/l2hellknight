package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExListMpccWaiting;

public class RequestExListMpccWaiting extends L2GameClientPacket
{
  private int _listId;
  private int _locationId;
  private boolean _allLevels;

  protected void readImpl()
    throws Exception
  {
    _listId = readD();
    _locationId = readD();
    _allLevels = (readD() == 1);
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.sendPacket(new ExListMpccWaiting(player, _listId, _locationId, _allLevels));
  }
}