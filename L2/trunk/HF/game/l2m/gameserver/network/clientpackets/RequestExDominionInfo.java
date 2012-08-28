package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExReplyDominionInfo;
import l2m.gameserver.network.serverpackets.ExShowOwnthingPos;

public class RequestExDominionInfo extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.sendPacket(new ExReplyDominionInfo());

    DominionSiegeRunnerEvent runnerEvent = (DominionSiegeRunnerEvent)EventHolder.getInstance().getEvent(EventType.MAIN_EVENT, 1);
    if (runnerEvent.isInProgress())
      activeChar.sendPacket(new ExShowOwnthingPos());
  }
}