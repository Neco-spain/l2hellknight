package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.instancemanager.games.MiniGameScoreManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestBR_MiniGameInsertScore extends L2GameClientPacket
{
  private int _score;

  protected void readImpl()
    throws Exception
  {
    _score = readD();
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!Config.EX_JAPAN_MINIGAME)) {
      return;
    }
    MiniGameScoreManager.getInstance().insertScore(player, _score);
  }
}