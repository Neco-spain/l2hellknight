package l2p.gameserver.clientpackets;

import java.util.List;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.data.xml.holder.ResidenceHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.CastleSiegeDefenderList;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class RequestConfirmCastleSiegeWaitingList extends L2GameClientPacket
{
  private boolean _approved;
  private int _unitId;
  private int _clanId;

  protected void readImpl()
  {
    _unitId = readD();
    _clanId = readD();
    _approved = (readD() == 1);
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (player.getClan() == null) {
      return;
    }
    Castle castle = (Castle)ResidenceHolder.getInstance().getResidence(Castle.class, _unitId);

    if ((castle == null) || (player.getClan().getCastle() != castle.getId()))
    {
      player.sendActionFailed();
      return;
    }

    CastleSiegeEvent siegeEvent = (CastleSiegeEvent)castle.getSiegeEvent();

    SiegeClanObject siegeClan = siegeEvent.getSiegeClan("defenders_waiting", _clanId);
    if (siegeClan == null) {
      siegeClan = siegeEvent.getSiegeClan("defenders", _clanId);
    }
    if (siegeClan == null) {
      return;
    }
    if ((player.getClanPrivileges() & 0x40000) != 262144)
    {
      player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_MODIFY_THE_CASTLE_DEFENDER_LIST);
      return;
    }

    if (siegeEvent.isRegistrationOver())
    {
      player.sendPacket(SystemMsg.THIS_IS_NOT_THE_TIME_FOR_SIEGE_REGISTRATION_AND_SO_REGISTRATIONS_CANNOT_BE_ACCEPTED_OR_REJECTED);
      return;
    }

    int allSize = siegeEvent.getObjects("defenders").size();
    if (allSize >= 20)
    {
      player.sendPacket(SystemMsg.NO_MORE_REGISTRATIONS_MAY_BE_ACCEPTED_FOR_THE_DEFENDER_SIDE);
      return;
    }

    siegeEvent.removeObject(siegeClan.getType(), siegeClan);

    if (_approved)
      siegeClan.setType("defenders");
    else {
      siegeClan.setType("defenders_refused");
    }
    siegeEvent.addObject(siegeClan.getType(), siegeClan);

    SiegeClanDAO.getInstance().update(castle, siegeClan);

    player.sendPacket(new CastleSiegeDefenderList(castle));
  }
}