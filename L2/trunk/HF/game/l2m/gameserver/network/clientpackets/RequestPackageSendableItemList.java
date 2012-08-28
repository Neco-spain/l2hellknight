package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PackageSendableList;

public class RequestPackageSendableItemList extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
    throws Exception
  {
    _objectId = readD();
  }

  protected void runImpl()
    throws Exception
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    player.sendPacket(new PackageSendableList(_objectId, player));
  }
}