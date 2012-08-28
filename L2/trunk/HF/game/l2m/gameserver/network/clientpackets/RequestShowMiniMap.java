package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.ShowMiniMap;

public class RequestShowMiniMap extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }

    if ((activeChar.isActionBlocked("open_minimap")) || ((activeChar.isInZone("[Hellbound_territory]")) && (Functions.getItemCount(activeChar, 9994) == 0L)))
    {
      activeChar.sendPacket(Msg.THIS_IS_AN_AREA_WHERE_YOU_CANNOT_USE_THE_MINI_MAP_THE_MINI_MAP_WILL_NOT_BE_OPENED);
      return;
    }

    sendPacket(new ShowMiniMap(activeChar, 0));
  }
}