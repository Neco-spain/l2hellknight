package l2p.gameserver.clientpackets;

import l2p.gameserver.data.xml.holder.EventHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.EventType;
import l2p.gameserver.model.entity.events.impl.DominionSiegeRunnerEvent;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExReplyDominionInfo;
import l2p.gameserver.serverpackets.ExShowOwnthingPos;

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