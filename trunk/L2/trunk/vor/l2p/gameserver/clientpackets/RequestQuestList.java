package l2p.gameserver.clientpackets;

import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    sendPacket(new QuestList(((GameClient)getClient()).getActiveChar()));
  }
}