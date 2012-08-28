package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExShowSentPostList;

public class RequestExRequestSentPostList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player cha = ((GameClient)getClient()).getActiveChar();
    if (cha != null)
      cha.sendPacket(new ExShowSentPostList(cha));
  }
}