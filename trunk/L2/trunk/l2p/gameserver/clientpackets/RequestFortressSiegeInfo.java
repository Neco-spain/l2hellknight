package l2p.gameserver.clientpackets;

import java.util.List;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.residence.Fortress;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExShowFortressSiegeInfo;

public class RequestFortressSiegeInfo extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    List fortressList = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
    for (Fortress fort : fortressList)
      if ((fort != null) && (fort.getSiegeEvent().isInProgress()))
        activeChar.sendPacket(new ExShowFortressSiegeInfo(fort));
  }
}