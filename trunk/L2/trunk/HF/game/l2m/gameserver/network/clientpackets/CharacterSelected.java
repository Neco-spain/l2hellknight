package l2m.gameserver.network.clientpackets;

import l2m.gameserver.loginservercon.SessionKey;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.GameClient.GameClientState;
import l2m.gameserver.network.serverpackets.ActionFail;
import l2m.gameserver.network.serverpackets.CharSelected;
import l2m.gameserver.utils.AutoBan;

public class CharacterSelected extends L2GameClientPacket
{
  private int _charSlot;

  protected void readImpl()
  {
    _charSlot = readD();
  }

  protected void runImpl()
  {
    GameClient client = (GameClient)getClient();

    if (client.getActiveChar() != null) {
      return;
    }
    int objId = client.getObjectIdForSlot(_charSlot);
    if (AutoBan.isBanned(objId))
    {
      sendPacket(ActionFail.STATIC);
      return;
    }

    Player activeChar = client.loadCharFromDisk(_charSlot);
    if (activeChar == null)
    {
      sendPacket(ActionFail.STATIC);
      return;
    }

    if (activeChar.getAccessLevel() < 0) {
      activeChar.setAccessLevel(0);
    }
    client.setState(GameClient.GameClientState.IN_GAME);

    sendPacket(new CharSelected(activeChar, client.getSessionKey().playOkID1));
  }
}