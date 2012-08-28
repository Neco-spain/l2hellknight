package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
  private String _name;

  protected void readImpl()
  {
    _name = readS(16);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.setManufactureName(_name);
  }
}