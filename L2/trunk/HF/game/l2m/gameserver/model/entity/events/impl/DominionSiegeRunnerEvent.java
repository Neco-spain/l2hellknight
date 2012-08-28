package l2m.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.objects.SiegeClanObject;
import l2m.gameserver.model.entity.residence.Castle;
import l2m.gameserver.model.entity.residence.Dominion;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.UnitMember;
import l2m.gameserver.model.quest.Quest;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class DominionSiegeRunnerEvent extends GlobalEvent
{
  public static final String REGISTRATION = "registration";
  public static final String BATTLEFIELD = "battlefield";
  private boolean _battlefieldChatActive;
  private Future<?> _battlefieldChatFuture;
  private BattlefieldChatTask _battlefieldChatTask = new BattlefieldChatTask(null);

  private Calendar _startTime = Calendar.getInstance();
  private boolean _isInProgress;
  private boolean _isRegistrationOver;
  private Map<ClassId, Quest> _classQuests = new HashMap();
  private List<Quest> _breakQuests = new ArrayList();

  private List<Dominion> _registeredDominions = new ArrayList(9);

  public DominionSiegeRunnerEvent(MultiValueSet<String> set)
  {
    super(set);
    _startTime.setTimeInMillis(0L);
  }

  public void startEvent()
  {
    if (_startTime.getTimeInMillis() == 0L)
    {
      clearActions();
      return;
    }

    super.startEvent();
    setInProgress(true);

    if (_battlefieldChatFuture != null)
    {
      _battlefieldChatFuture.cancel(false);
      _battlefieldChatFuture = null;
    }

    for (Iterator i$ = _registeredDominions.iterator(); i$.hasNext(); ) { d = (Dominion)i$.next();

      List defenders = d.getSiegeEvent().getObjects("defenders");
      for (SiegeClanObject siegeClan : defenders)
      {
        for (i$ = siegeClan.getClan().iterator(); i$.hasNext(); ) { member = (UnitMember)i$.next();

          for (Dominion d2 : _registeredDominions)
          {
            DominionSiegeEvent siegeEvent2 = (DominionSiegeEvent)d2.getSiegeEvent();
            List defenderPlayers2 = siegeEvent2.getObjects("defender_players");

            defenderPlayers2.remove(Integer.valueOf(member.getObjectId()));

            if (d != d2)
              siegeEvent2.clearReward(member.getObjectId());
          }
        }
      }
      Iterator i$;
      UnitMember member;
      List defenderPlayers = d.getSiegeEvent().getObjects("defender_players");
      for (i$ = defenderPlayers.iterator(); i$.hasNext(); ) { i = ((Integer)i$.next()).intValue();

        for (Dominion d2 : _registeredDominions)
        {
          DominionSiegeEvent siegeEvent2 = (DominionSiegeEvent)d2.getSiegeEvent();

          if (d != d2)
            siegeEvent2.clearReward(i);
        }
      }
    }
    Dominion d;
    Iterator i$;
    int i;
    for (Dominion d : _registeredDominions)
    {
      d.getSiegeEvent().clearActions();
      d.getSiegeEvent().registerActions();
    }

    broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_BEGUN);
  }

  protected void validateSiegeDate(Calendar calendar, int add)
  {
    calendar.set(12, 0);
    calendar.set(13, 0);
    calendar.set(14, 0);

    while (calendar.getTimeInMillis() < System.currentTimeMillis())
      calendar.add(3, add);
  }

  public void stopEvent()
  {
    setInProgress(false);

    reCalcNextTime(false);

    for (Dominion d : _registeredDominions) {
      d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
    }
    broadcastToWorld(SystemMsg.TERRITORY_WAR_HAS_ENDED);

    _battlefieldChatFuture = ThreadPoolManager.getInstance().schedule(_battlefieldChatTask, 600000L);

    super.stopEvent();
  }

  public void announce(int val)
  {
    switch (val)
    {
    case -20:
      broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_WILL_BEGIN_IN_20_MINUTES);
      break;
    case -10:
      broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_10_MINUTES);
      break;
    case -5:
      broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_5_MINUTES);
      break;
    case -1:
      broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_BEGINS_IN_1_MINUTE);
      break;
    case 3600:
      broadcastToWorld(new SystemMessage2(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1HOURS).addInteger(val / 3600));
      break;
    case 60:
    case 300:
    case 600:
      broadcastToWorld(new SystemMessage2(SystemMsg.THE_TERRITORY_WAR_WILL_END_IN_S1MINUTES).addInteger(val / 60));
      break;
    case 1:
    case 2:
    case 3:
    case 4:
    case 5:
    case 10:
      broadcastToWorld(new SystemMessage2(SystemMsg.S1_SECONDS_TO_THE_END_OF_TERRITORY_WAR).addInteger(val));
    }
  }

  public Calendar getSiegeDate()
  {
    return _startTime;
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    if (onInit)
    {
      if (_startTime.getTimeInMillis() > 0L) {
        registerActions();
      }

    }
    else if (_startTime.getTimeInMillis() > 0L)
    {
      validateSiegeDate(_startTime, 2);
      registerActions();
    }
  }

  protected long startTimeMillis()
  {
    return _startTime.getTimeInMillis();
  }

  protected void printInfo()
  {
  }

  public void broadcastTo(IStaticPacket packet)
  {
    for (Dominion dominion : _registeredDominions)
      dominion.getSiegeEvent().broadcastTo(packet, new String[0]);
  }

  public void broadcastTo(L2GameServerPacket packet)
  {
    for (Dominion dominion : _registeredDominions)
      dominion.getSiegeEvent().broadcastTo(packet, new String[0]);
  }

  public boolean isBattlefieldChatActive()
  {
    return _battlefieldChatActive;
  }

  public void setBattlefieldChatActive(boolean battlefieldChatActive)
  {
    _battlefieldChatActive = battlefieldChatActive;
  }

  public boolean isInProgress()
  {
    return _isInProgress;
  }

  public void setInProgress(boolean inProgress)
  {
    _isInProgress = inProgress;
  }

  public boolean isRegistrationOver()
  {
    return _isRegistrationOver;
  }

  public void setRegistrationOver(boolean registrationOver)
  {
    _isRegistrationOver = registrationOver;
    for (Dominion d : _registeredDominions) {
      d.getSiegeEvent().setRegistrationOver(registrationOver);
    }
    if (registrationOver)
      broadcastToWorld(SystemMsg.THE_TERRITORY_WAR_REQUEST_PERIOD_HAS_ENDED);
  }

  public void addClassQuest(ClassId c, Quest quest)
  {
    _classQuests.put(c, quest);
  }

  public Quest getClassQuest(ClassId c)
  {
    return (Quest)_classQuests.get(c);
  }

  public void addBreakQuest(Quest q)
  {
    _breakQuests.add(q);
  }

  public List<Quest> getBreakQuests()
  {
    return _breakQuests;
  }

  public void action(String name, boolean start)
  {
    if (name.equalsIgnoreCase("registration"))
      setRegistrationOver(!start);
    else if (name.equalsIgnoreCase("battlefield"))
      setBattlefieldChatActive(start);
    else
      super.action(name, start);
  }

  public synchronized void registerDominion(Dominion d)
  {
    if (_registeredDominions.contains(d)) {
      return;
    }
    if (_registeredDominions.isEmpty())
    {
      Castle castle = d.getCastle();
      if (castle.getOwnDate().getTimeInMillis() == 0L) {
        return;
      }
      _startTime = ((Calendar)Config.CASTLE_VALIDATION_DATE.clone());
      _startTime.set(7, 7);
      if (_startTime.before(Config.CASTLE_VALIDATION_DATE))
        _startTime.add(3, 1);
      validateSiegeDate(_startTime, 2);

      d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());

      reCalcNextTime(false);
    }
    else
    {
      d.getSiegeDate().setTimeInMillis(_startTime.getTimeInMillis());
    }

    d.getSiegeEvent().spawnAction("territory_npc", true);
    d.rewardSkills();

    _registeredDominions.add(d);
  }

  public synchronized void unRegisterDominion(Dominion d)
  {
    if (!_registeredDominions.contains(d)) {
      return;
    }
    _registeredDominions.remove(d);

    d.getSiegeEvent().spawnAction("territory_npc", false);
    d.getSiegeDate().setTimeInMillis(0L);

    if (_registeredDominions.isEmpty())
    {
      clearActions();

      _startTime.setTimeInMillis(0L);

      reCalcNextTime(false);
    }
  }

  public List<Dominion> getRegisteredDominions()
  {
    return _registeredDominions;
  }

  private class BattlefieldChatTask extends RunnableImpl
  {
    private BattlefieldChatTask()
    {
    }

    public void runImpl()
      throws Exception
    {
      setBattlefieldChatActive(false);
      setRegistrationOver(false);

      for (Dominion d : _registeredDominions)
      {
        DominionSiegeEvent siegeEvent = (DominionSiegeEvent)d.getSiegeEvent();

        siegeEvent.updateParticles(false, new String[0]);

        siegeEvent.broadcastTo(SystemMsg.THE_BATTLEFIELD_CHANNEL_HAS_BEEN_DEACTIVATED, new String[0]);

        siegeEvent.removeObjects("attackers");
        siegeEvent.removeObjects("defenders");
        siegeEvent.removeObjects("attacker_players");
        siegeEvent.removeObjects("defender_players");
      }

      DominionSiegeRunnerEvent.access$102(DominionSiegeRunnerEvent.this, null);
    }
  }
}