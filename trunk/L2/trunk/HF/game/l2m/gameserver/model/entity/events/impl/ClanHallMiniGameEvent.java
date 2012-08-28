package l2m.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import l2p.commons.collections.CollectionUtils;
import l2p.commons.collections.MultiValueSet;
import l2m.gameserver.data.dao.SiegeClanDAO;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject.SiegeClanComparatorImpl;
import l2m.gameserver.model.entity.residence.ClanHall;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.PlaySound;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.data.tables.ClanTable;

public class ClanHallMiniGameEvent extends SiegeEvent<ClanHall, CMGSiegeClanObject>
{
  public static final String NEXT_STEP = "next_step";
  public static final String REFUND = "refund";
  private boolean _arenaClosed = true;

  public ClanHallMiniGameEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void startEvent()
  {
    _oldOwner = ((ClanHall)getResidence()).getOwner();

    List siegeClans = getObjects("attackers");
    if (siegeClans.size() < 2)
    {
      CMGSiegeClanObject siegeClan = (CMGSiegeClanObject)CollectionUtils.safeGet(siegeClans, 0);
      if (siegeClan != null)
      {
        CMGSiegeClanObject oldSiegeClan = (CMGSiegeClanObject)getSiegeClan("refund", siegeClan.getObjectId());
        if (oldSiegeClan != null)
        {
          SiegeClanDAO.getInstance().delete(getResidence(), siegeClan);

          oldSiegeClan.setParam(oldSiegeClan.getParam() + siegeClan.getParam());

          SiegeClanDAO.getInstance().update(getResidence(), oldSiegeClan);
        }
        else
        {
          siegeClan.setType("refund");

          siegeClans.remove(siegeClan);

          addObject("refund", siegeClan);

          SiegeClanDAO.getInstance().update(getResidence(), siegeClan);
        }
      }
      siegeClans.clear();

      broadcastTo(SystemMsg.THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED, new String[] { "attackers" });
      broadcastInZone2(new L2GameServerPacket[] { new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()) });
      reCalcNextTime(false);
      return;
    }

    CMGSiegeClanObject[] clans = (CMGSiegeClanObject[])siegeClans.toArray(new CMGSiegeClanObject[siegeClans.size()]);
    Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());

    List temp = new ArrayList(4);

    for (int i = 0; i < clans.length; i++)
    {
      CMGSiegeClanObject siegeClan = clans[i];
      SiegeClanDAO.getInstance().delete(getResidence(), siegeClan);

      if (temp.size() == 4)
      {
        siegeClans.remove(siegeClan);

        siegeClan.broadcast(new IStaticPacket[] { SystemMsg.YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR });
      }
      else
      {
        temp.add(siegeClan);

        siegeClan.broadcast(new IStaticPacket[] { SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR });
      }
    }

    _arenaClosed = false;

    super.startEvent();
  }

  public void stopEvent(boolean step)
  {
    removeBanishItems();

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
      broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()), new String[] { "attackers" });
    }
    updateParticles(false, new String[] { "attackers" });

    removeObjects("attackers");

    super.stopEvent(step);

    _oldOwner = null;
  }

  public void nextStep()
  {
    List siegeClans = getObjects("attackers");
    for (int i = 0; i < siegeClans.size(); i++) {
      spawnAction("arena_" + i, true);
    }
    _arenaClosed = true;

    updateParticles(true, new String[] { "attackers" });

    broadcastTo(new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN).addResidenceName(getResidence()), new String[] { "attackers" });
  }

  public void setRegistrationOver(boolean b)
  {
    if (b) {
      broadcastTo(SystemMsg.THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED, new String[] { "attackers" });
    }
    super.setRegistrationOver(b);
  }

  public CMGSiegeClanObject newSiegeClan(String type, int clanId, long param, long date)
  {
    Clan clan = ClanTable.getInstance().getClan(clanId);
    return clan == null ? null : new CMGSiegeClanObject(type, clan, param, date);
  }

  public void announce(int val)
  {
    int seconds = val % 60;
    int min = val / 60;
    if (min > 0)
    {
      SystemMsg msg = min > 10 ? SystemMsg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA : SystemMsg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW;

      broadcastTo(new SystemMessage2(msg).addInteger(min), new String[] { "attackers" });
    }
    else {
      broadcastTo(new SystemMessage2(SystemMsg.IN_S1_SECONDS_THE_GAME_WILL_BEGIN).addInteger(seconds), new String[] { "attackers" });
    }
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
    addObjects("refund", SiegeClanDAO.getInstance().load(getResidence(), "refund"));
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

  public boolean isArenaClosed()
  {
    return _arenaClosed;
  }

  public void onAddEvent(GameObject object)
  {
    if (object.isItem())
      addBanishItem((ItemInstance)object);
  }
}