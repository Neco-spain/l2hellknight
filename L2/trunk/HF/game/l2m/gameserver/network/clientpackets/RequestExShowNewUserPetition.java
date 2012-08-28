package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExResponseShowStepOne;

public class RequestExShowNewUserPetition extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if ((player == null) || (!Config.EX_NEW_PETITION_SYSTEM)) {
      return;
    }
    player.sendPacket(new ExResponseShowStepOne(player));
  }
}