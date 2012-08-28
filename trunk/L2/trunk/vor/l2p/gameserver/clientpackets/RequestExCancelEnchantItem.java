package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.EnchantResult;

public class RequestExCancelEnchantItem extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar != null)
    {
      activeChar.setEnchantScroll(null);
      activeChar.sendPacket(EnchantResult.CANCEL);
    }
  }
}