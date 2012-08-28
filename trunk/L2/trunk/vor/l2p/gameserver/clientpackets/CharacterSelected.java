package l2p.gameserver.clientpackets;

import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.network.GameClient.GameClientState;
import l2p.gameserver.serverpackets.ActionFail;
import l2p.gameserver.serverpackets.CharSelected;
import l2p.gameserver.utils.AutoBan;

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