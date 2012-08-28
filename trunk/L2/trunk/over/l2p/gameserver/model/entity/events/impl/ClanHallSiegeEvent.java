package l2p.gameserver.model.entity.events.impl;

import java.util.List;
import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Zone.ZoneType;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;

public class ClanHallSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject>
{
  public static final String BOSS = "boss";

  public ClanHallSiegeEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void startEvent()
  {
    _oldOwner = ((ClanHall)getResidence()).getOwner();
    if (_oldOwner != null)
    {
      ((ClanHall)getResidence()).changeOwner(null);

      addObject("attackers", new SiegeClanObject("attackers", _oldOwner, 0L));
    }

    if (getObjects("attackers").size() == 0)
    {
      broadcastInZone2(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()) });
      reCalcNextTime(false);
      return;
    }

    SiegeClanDAO.getInstance().delete(getResidence());

    updateParticles(true, new String[] { "attackers" });

    broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), new String[] { "attackers" });

    super.startEvent();
  }

  public void stopEvent(boolean step)
  {
    Clan newOwner = ((ClanHall)getResidence()).getOwner();
    if (newOwner != null)
    {
      newOwner.broadcastToOnlineMembers(new L2GameServerPacket[] { PlaySound.SIEGE_VICTORY });

      newOwner.incReputation(1700, false, toString());

      broadcastTo(((SystemMessage2)new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()), new String[] { "attackers" });
      broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), new String[] { "attackers" });
    }
    else {
      broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), new String[] { "attackers" });
    }
    updateParticles(false, new String[] { "attackers" });

    removeObjects("attackers");

    super.stopEvent(step);

    _oldOwner = null;
  }

  public void setRegistrationOver(boolean b)
  {
    if (b) {
      broadcastTo(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()), new String[] { "attackers" });
    }
    super.setRegistrationOver(b);
  }

  public void processStep(Clan clan)
  {
    if (clan != null) {
      ((ClanHall)getResidence()).changeOwner(clan);
    }
    stopEvent(true);
  }

  public void loadSiegeClans()
  {
    addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
  }

  public int getUserRelation(Player thisPlayer, int result)
  {
    return result;
  }

  public int getRelation(Player thisPlayer, Player targetPlayer, int result)
  {
    return result;
  }

  public boolean canRessurect(Player resurrectPlayer, Creature target, boolean force)
  {
    boolean playerInZone = resurrectPlayer.isInZone(Zone.ZoneType.SIEGE);
    boolean targetInZone = target.isInZone(Zone.ZoneType.SIEGE);

    if ((!playerInZone) && (!targetInZone)) {
      return true;
    }
    if (!targetInZone) {
      return false;
    }
    Player targetPlayer = target.getPlayer();

    ClanHallSiegeEvent siegeEvent = (ClanHallSiegeEvent)target.getEvent(ClanHallSiegeEvent.class);
    if (siegeEvent != this)
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
      return false;
    }

    SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());

    if (targetSiegeClan.getFlag() == null)
    {
      if (force)
        targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
      resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
      return false;
    }

    if (force) {
      return true;
    }

    resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
    return false;
  }
}