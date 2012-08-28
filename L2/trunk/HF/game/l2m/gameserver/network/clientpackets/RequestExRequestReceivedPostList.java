package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowReceivedPostList;

public class RequestExRequestReceivedPostList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player cha = ((GameClient)getClient()).getActiveChar();
    if (cha != null)
      cha.sendPacket(new ExShowReceivedPostList(cha));
  }
}