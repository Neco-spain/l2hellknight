package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExResponseShowStepOne;

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