package l2p.gameserver.model.entity.events.impl;

import java.util.List;
import l2p.commons.collections.CollectionUtils;
import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.dao.SiegeClanDAO;
import l2p.gameserver.dao.SiegePlayerDAO;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.RestartType;
import l2p.gameserver.model.entity.events.objects.CTBSiegeClanObject;
import l2p.gameserver.model.entity.events.objects.CTBTeamObject;
import l2p.gameserver.model.entity.events.objects.SiegeClanObject;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.PlaySound;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.ClanTable;
import l2p.gameserver.utils.Location;

public class ClanHallTeamBattleEvent extends SiegeEvent<ClanHall, CTBSiegeClanObject>
{
  public static final String TRYOUT_PART = "tryout_part";
  public static final String CHALLENGER_RESTART_POINTS = "challenger_restart_points";
  public static final String FIRST_DOORS = "first_doors";
  public static final String SECOND_DOORS = "second_doors";
  public static final String NEXT_STEP = "next_step";

  public ClanHallTeamBattleEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void startEvent()
  {
    _oldOwner = ((ClanHall)getResidence()).getOwner();

    List attackers = getObjects("attackers");
    if (attackers.isEmpty())
    {
      if (_oldOwner == null)
        broadcastInZone2(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST).addResidenceName(getResidence()) });
      else {
        broadcastInZone2(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED).addResidenceName(getResidence()) });
      }
      reCalcNextTime(false);
      return;
    }

    if (_oldOwner != null) {
      addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
    }
    SiegeClanDAO.getInstance().delete(getResidence());
    SiegePlayerDAO.getInstance().delete(getResidence());

    List teams = getObjects("tryout_part");
    for (int i = 0; i < 5; i++)
    {
      CTBTeamObject team = (CTBTeamObject)teams.get(i);

      team.setSiegeClan((CTBSiegeClanObject)CollectionUtils.safeGet(attackers, i));
    }

    broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });
    broadcastTo(SystemMsg.THE_TRYOUTS_ARE_ABOUT_TO_BEGIN, new String[] { "attackers" });

    super.startEvent();
  }

  public void nextStep()
  {
    broadcastTo(SystemMsg.THE_TRYOUTS_HAVE_BEGUN, new String[] { "attackers", "defenders" });

    updateParticles(true, new String[] { "attackers", "defenders" });
  }

  public void processStep(CTBTeamObject team)
  {
    if (team.getSiegeClan() != null)
    {
      CTBSiegeClanObject object = team.getSiegeClan();

      object.setEvent(false, this);

      teleportPlayers("spectators");
    }

    team.despawnObject(this);

    List teams = getObjects("tryout_part");

    boolean hasWinner = false;
    CTBTeamObject winnerTeam = null;

    for (CTBTeamObject t : teams)
    {
      if (t.isParticle())
      {
        hasWinner = winnerTeam == null;

        winnerTeam = t;
      }
    }

    if (!hasWinner) {
      return;
    }
    SiegeClanObject clan = winnerTeam.getSiegeClan();
    if (clan != null) {
      ((ClanHall)getResidence()).changeOwner(clan.getClan());
    }
    stopEvent(true);
  }

  public void announce(int val)
  {
    int minute = val / 60;
    if (minute > 0)
      broadcastTo(new SystemMessage2(SystemMsg.THE_CONTEST_WILL_BEGIN_IN_S1_MINUTES).addInteger(minute), new String[] { "attackers", "defenders" });
    else
      broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_WILL_BEGIN_IN_S1_SECONDS).addInteger(val), new String[] { "attackers", "defenders" });
  }

  public void stopEvent(boolean step)
  {
    Clan newOwner = ((ClanHall)getResidence()).getOwner();
    if (newOwner != null)
    {
      if (_oldOwner != newOwner)
      {
        newOwner.broadcastToOnlineMembers(new L2GameServerPacket[] { PlaySound.SIEGE_VICTORY });

        newOwner.incReputation(1700, false, toString());
      }

      broadcastTo(((SystemMessage2)new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });
      broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName(getResidence()), new String[] { "attackers", "defenders" });
    }
    else {
      broadcastTo(new SystemMessage2(SystemMsg.THE_PRELIMINARY_MATCH_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), new String[] { "attackers" });
    }
    updateParticles(false, new String[] { "attackers", "defenders" });

    removeObjects("defenders");
    removeObjects("attackers");

    super.stopEvent(step);

    _oldOwner = null;
  }

  public void loadSiegeClans()
  {
    List siegeClanObjectList = SiegeClanDAO.getInstance().load(getResidence(), "attackers");
    addObjects("attackers", siegeClanObjectList);

    List objects = getObjects("attackers");
    for (CTBSiegeClanObject clan : objects)
      clan.select(getResidence());
  }

  public CTBSiegeClanObject newSiegeClan(String type, int clanId, long i, long date)
  {
    Clan clan = ClanTable.getInstance().getClan(clanId);
    return clan == null ? null : new CTBSiegeClanObject(type, clan, i, date);
  }

  public boolean isParticle(Player player)
  {
    if ((!isInProgress()) || (player.getClan() == null))
      return false;
    CTBSiegeClanObject object = (CTBSiegeClanObject)getSiegeClan("attackers", player.getClan());
    return (object != null) && (object.getPlayers().contains(Integer.valueOf(player.getObjectId())));
  }

  public Location getRestartLoc(Player player, RestartType type)
  {
    if (!checkIfInZone(player)) {
      return null;
    }
    SiegeClanObject attackerClan = getSiegeClan("attackers", player.getClan());

    Location loc = null;
    switch (1.$SwitchMap$l2p$gameserver$model$base$RestartType[type.ordinal()])
    {
    case 1:
      if ((attackerClan == null) || (!checkIfInZone(player)))
        break;
      List objectList = getObjects("attackers");
      List teleportList = getObjects("challenger_restart_points");

      int index = objectList.indexOf(attackerClan);

      loc = (Location)teleportList.get(index);
    }

    return loc;
  }

  public void action(String name, boolean start)
  {
    if (name.equalsIgnoreCase("next_step"))
      nextStep();
    else
      super.action(name, start);
  }

  public int getUserRelation(Player thisPlayer, int result)
  {
    return result;
  }

  public int getRelation(Player thisPlayer, Player targetPlayer, int result)
  {
    return result;
  }
}