package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExBR_ProductInfo;

public class RequestExBR_ProductInfo extends L2GameClientPacket
{
  private int _productId;

  protected void readImpl()
  {
    _productId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    activeChar.sendPacket(new ExBR_ProductInfo(_productId));
  }
}