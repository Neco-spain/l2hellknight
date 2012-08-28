package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.security.SecondaryPasswordAuth;
import l2p.gameserver.network.GameClient;

public class RequestEx2ndPasswordVerify extends L2GameClientPacket
{
  private String _password;

  protected void readImpl()
  {
    _password = readS();
  }

  protected void runImpl()
  {
    if (!Config.EX_SECOND_PASSWORD) {
      return;
    }
    ((GameClient)getClient()).getSecondaryAuth().checkPassword(_password, false);
  }
}