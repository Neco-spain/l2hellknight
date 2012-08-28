package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowCastleInfo;

public class RequestAllCastleInfo extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    ((GameClient)getClient()).getActiveChar().sendPacket(new ExShowCastleInfo());
  }
}