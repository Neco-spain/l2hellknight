package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.instancemanager.games.FishingChampionShipManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

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