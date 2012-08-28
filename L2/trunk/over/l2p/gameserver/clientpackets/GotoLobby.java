package l2p.gameserver.clientpackets;

import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;

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