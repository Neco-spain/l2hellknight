package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExShowCastleInfo;

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