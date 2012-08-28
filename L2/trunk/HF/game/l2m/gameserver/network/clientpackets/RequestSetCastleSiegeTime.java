package l2m.gameserver.network.clientpackets;

import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.CastleSiegeInfo;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestSetCastleSiegeTime extends L2GameClientPacket
{
  private int _id;
  private int _time;

  protected void readImpl()
  {
    _id = readD();
    _time = readD();
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _id);
    if (castle == null) {
      return;
    }
    if (player.getClan().getCastle() != castle.getId()) {
      return;
    }
    if ((player.getClanPrivileges() & 0x40000) != 262144)
    {
      player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_SIEGE_TIME);
      return;
    }

    CastleSiegeEvent siegeEvent = (CastleSiegeEvent)castle.getSiegeEvent();

    siegeEvent.setNextSiegeTime(_time);

    player.sendPacket(new CastleSiegeInfo(castle, player));
  }
}