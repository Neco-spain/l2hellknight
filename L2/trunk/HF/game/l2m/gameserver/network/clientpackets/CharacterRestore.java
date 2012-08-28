package l2m.gameserver.network.clientpackets;

import l2m.gameserver.loginservercon.SessionKey;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
{
  private int _charSlot;

  protected void readImpl()
  {
    _charSlot = readD();
  }

  protected void runImpl()
  {
    GameClient client = (GameClient)getClient();
    try
    {
      client.markRestoredChar(_charSlot);
    }
    catch (Exception e) {
    }
    CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
    sendPacket(cl);
    client.setCharSelection(cl.getCharInfo());
  }
}