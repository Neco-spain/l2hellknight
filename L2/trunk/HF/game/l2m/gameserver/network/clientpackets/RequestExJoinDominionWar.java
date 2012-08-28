package l2m.gameserver.network.clientpackets;

import java.util.List;
import l2m.gameserver.data.dao.SiegeClanDAO;
import l2m.gameserver.data.dao.SiegePlayerDAO;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.entity.events.impl.DominionSiegeEvent;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.residence.Dominion;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.Privilege;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExReplyRegisterDominion;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestExJoinDominionWar extends L2GameClientPacket
{
  private int _dominionId;
  private boolean _clanRegistration;
  private boolean _isRegistration;

  protected void readImpl()
  {
    _dominionId = readD();
    _clanRegistration = (readD() == 1);
    _isRegistration = (readD() == 1);
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Dominion dominion = (Dominion)ResidenceHolder.getInstance().getResidence(Dominion.class, _dominionId);
    if (dominion == null)
      return;
    DominionSiegeEvent siegeEvent = (DominionSiegeEvent)dominion.getSiegeEvent();
    if (siegeEvent.isRegistrationOver())
    {
      player.sendPacket(SystemMsg.IT_IS_NOT_A_TERRITORY_WAR_REGISTRATION_PERIOD_SO_A_REQUEST_CANNOT_BE_MADE_AT_THIS_TIME);
      return;
    }

    if ((player.getClan() != null) && (player.getClan().getCastle() > 0))
    {
      player.sendPacket(SystemMsg.THE_CLAN_WHO_OWNS_THE_TERRITORY_CANNOT_PARTICIPATE_IN_THE_TERRITORY_WAR_AS_MERCENARIES);
      return;
    }

    if ((player.getLevel() < 40) || (player.getClassId().getLevel() <= 2))
    {
      player.sendPacket(SystemMsg.ONLY_CHARACTERS_WHO_ARE_LEVEL_40_OR_ABOVE_WHO_HAVE_COMPLETED_THEIR_SECOND_CLASS_TRANSFER_CAN_REGISTER_IN_A_TERRITORY_WAR);
      return;
    }

    int playerReg = 0;
    int clanReg = 0;
    for (Dominion d : ResidenceHolder.getInstance().getResidenceList(Dominion.class))
    {
      DominionSiegeEvent dominionSiegeEvent = (DominionSiegeEvent)d.getSiegeEvent();
      if (dominionSiegeEvent.getObjects("defender_players").contains(Integer.valueOf(player.getObjectId())))
        playerReg = d.getId();
      else if (dominionSiegeEvent.getSiegeClan("defenders", player.getClan()) != null) {
        clanReg = d.getId();
      }
    }
    if (_isRegistration)
    {
      if (clanReg > 0)
      {
        player.sendPacket(SystemMsg.YOUVE_ALREADY_REQUESTED_A_TERRITORY_WAR_IN_ANOTHER_TERRITORY_ELSEWHERE);
        return;
      }

      if ((!_clanRegistration) && ((clanReg > 0) || (playerReg > 0)))
      {
        player.sendPacket(SystemMsg.YOUVE_ALREADY_REQUESTED_A_TERRITORY_WAR_IN_ANOTHER_TERRITORY_ELSEWHERE);
        return;
      }

      if (_clanRegistration)
      {
        if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR))
        {
          player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
          return;
        }

        SiegeClanObject object = new SiegeClanObject("defenders", player.getClan(), 0L);
        siegeEvent.addObject("defenders", object);
        SiegeClanDAO.getInstance().insert(dominion, object);

        player.sendPacket(new SystemMessage2(SystemMsg.CLAN_PARTICIPATION_IS_REQUESTED_IN_S1_TERRITORY).addResidenceName(dominion));
      }
      else
      {
        siegeEvent.addObject("defender_players", Integer.valueOf(player.getObjectId()));
        SiegePlayerDAO.getInstance().insert(dominion, 0, player.getObjectId());

        player.sendPacket(new SystemMessage2(SystemMsg.MERCENARY_PARTICIPATION_IS_REQUESTED_IN_S1_TERRITORY).addResidenceName(dominion));
      }
    }
    else
    {
      if ((_clanRegistration) && (clanReg != dominion.getId()))
        return;
      if ((!_clanRegistration) && (playerReg != dominion.getId())) {
        return;
      }
      if (_clanRegistration)
      {
        if (!player.hasPrivilege(Privilege.CS_FS_SIEGE_WAR))
        {
          player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
          return;
        }

        SiegeClanObject clanObject = siegeEvent.getSiegeClan("defenders", player.getClan());
        siegeEvent.removeObject("defenders", clanObject);
        SiegeClanDAO.getInstance().delete(dominion, clanObject);

        player.sendPacket(new SystemMessage2(SystemMsg.CLAN_PARTICIPATION_REQUEST_IS_CANCELLED_IN_S1_TERRITORY).addResidenceName(dominion));
      }
      else
      {
        siegeEvent.removeObject("defender_players", Integer.valueOf(player.getObjectId()));
        SiegePlayerDAO.getInstance().delete(dominion, 0, player.getObjectId());

        player.sendPacket(new SystemMessage2(SystemMsg.MERCENARY_PARTICIPATION_REQUEST_IS_CANCELLED_IN_S1_TERRITORY).addResidenceName(dominion));
      }
    }

    player.sendPacket(new ExReplyRegisterDominion(dominion, true, _isRegistration, _clanRegistration));
  }
}