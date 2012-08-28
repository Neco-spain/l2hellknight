package l2p.gameserver.clientpackets;

import java.nio.ByteBuffer;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.RecipeBookItemList;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
  private boolean isDwarvenCraft;

  protected void readImpl()
  {
    if (_buf.hasRemaining())
      isDwarvenCraft = (readD() == 0);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    sendPacket(new RecipeBookItemList(activeChar, isDwarvenCraft));
  }
}