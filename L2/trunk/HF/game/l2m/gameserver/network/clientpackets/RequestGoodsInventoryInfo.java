package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;

public class RequestGoodsInventoryInfo extends L2GameClientPacket
{
  protected void readImpl()
    throws Exception
  {
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
  }
}