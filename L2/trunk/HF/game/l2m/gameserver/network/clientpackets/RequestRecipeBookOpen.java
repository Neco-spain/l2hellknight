package l2m.gameserver.network.clientpackets;

import java.nio.ByteBuffer;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.RecipeBookItemList;

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