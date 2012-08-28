package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExBR_MiniGameLoadScores;

public class RequestBR_MiniGameLoadScores extends L2GameClientPacket
{
  protected void readImpl()
    throws Exception
  {
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!Config.EX_JAPAN_MINIGAME)) {
      return;
    }
    player.sendPacket(new ExBR_MiniGameLoadScores(player));
  }
}