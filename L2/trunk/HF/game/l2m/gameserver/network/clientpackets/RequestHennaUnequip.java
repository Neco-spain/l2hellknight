package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.Henna;

public class RequestHennaUnequip extends L2GameClientPacket
{
  private int _symbolId;

  protected void readImpl()
  {
    _symbolId = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    for (int i = 1; i <= 3; i++)
    {
      Henna henna = player.getHenna(i);
      if (henna == null) {
        continue;
      }
      if (henna.getSymbolId() != _symbolId)
        continue;
      long price = henna.getPrice() / 5L;
      if (player.getAdena() < price)
      {
        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
        break;
      }

      player.reduceAdena(price);

      player.removeHenna(i);

      player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_DELETED);
      break;
    }
  }
}