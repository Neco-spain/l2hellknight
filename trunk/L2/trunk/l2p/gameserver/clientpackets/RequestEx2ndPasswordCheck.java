package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.security.SecondaryPasswordAuth;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.Ex2ndPasswordCheck;

public class RequestEx2ndPasswordCheck extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    if ((!Config.EX_SECOND_PASSWORD) || (((GameClient)getClient()).getSecondaryAuth().isAuthed()))
    {
      sendPacket(new Ex2ndPasswordCheck(2));
      return;
    }
    ((GameClient)getClient()).getSecondaryAuth().openDialog();
  }
}