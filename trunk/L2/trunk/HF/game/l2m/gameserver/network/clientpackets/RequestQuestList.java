package l2m.gameserver.network.clientpackets;

import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.QuestList;

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