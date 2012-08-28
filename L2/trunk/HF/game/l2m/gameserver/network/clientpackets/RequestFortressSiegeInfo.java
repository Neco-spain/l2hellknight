package l2m.gameserver.network.clientpackets;

import java.util.List;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.residence.Fortress;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowFortressSiegeInfo;

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