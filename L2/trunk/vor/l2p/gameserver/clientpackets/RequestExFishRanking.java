package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.instancemanager.games.FishingChampionShipManager;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class RequestExFishRanking extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
      FishingChampionShipManager.getInstance().showMidResult(((GameClient)getClient()).getActiveChar());
  }
}