package l2m.gameserver.network.clientpackets;

import l2m.gameserver.loginservercon.SessionKey;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.CharacterSelectionInfo;

public class GotoLobby extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    CharacterSelectionInfo cl = new CharacterSelectionInfo(((GameClient)getClient()).getLogin(), ((GameClient)getClient()).getSessionKey().playOkID1);
    sendPacket(cl);
  }
}