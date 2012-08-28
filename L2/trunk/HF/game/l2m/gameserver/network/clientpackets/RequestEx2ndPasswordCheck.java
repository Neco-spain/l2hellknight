package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.security.SecondaryPasswordAuth;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.Ex2ndPasswordCheck;

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