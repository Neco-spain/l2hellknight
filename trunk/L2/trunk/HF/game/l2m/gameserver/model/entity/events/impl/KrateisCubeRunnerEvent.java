package l2m.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import l2p.commons.collections.MultiValueSet;
import l2p.commons.time.cron.SchedulingPattern;
import l2m.gameserver.data.xml.holder.EventHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.events.EventType;
import l2m.gameserver.model.entity.events.GlobalEvent;
import l2m.gameserver.model.entity.events.objects.SpawnExObject;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.scripts.Functions;
import l2m.gameserver.network.serverpackets.components.NpcString;

public class KrateisCubeRunnerEvent extends GlobalEvent
{
  private static final SchedulingPattern DATE_PATTERN = new SchedulingPattern("0,30 * * * *");
  public static final String MANAGER = "manager";
  public static final String REGISTRATION = "registration";
  private boolean _isInProgress;
  private boolean _isRegistrationOver;
  private List<KrateisCubeEvent> _cubes = new ArrayList(3);
  private Calendar _calendar = Calendar.getInstance();

  public KrateisCubeRunnerEvent(MultiValueSet<String> set)
  {
    super(set);
  }

  public void initEvent()
  {
    super.initEvent();
    _cubes.add(EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 2));
    _cubes.add(EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 3));
    _cubes.add(EventHolder.getInstance().getEvent(EventType.PVP_EVENT, 4));
  }

  public void startEvent()
  {
    super.startEvent();
    _isInProgress = true;
  }

  public void stopEvent()
  {
    _isInProgress = false;

    super.stopEvent();

    reCalcNextTime(false);
  }

  public void announce(int val)
  {
    NpcInstance npc = getNpc();
    switch (val)
    {
    case -600:
    case -300:
      Functions.npcSay(npc, NpcString.THE_MATCH_WILL_BEGIN_IN_S1_MINUTES, new String[] { String.valueOf(-val / 60) });
      break;
    case -540:
    case -330:
    case 60:
    case 360:
    case 660:
    case 960:
      Functions.npcSay(npc, NpcString.REGISTRATION_FOR_THE_NEXT_MATCH_WILL_END_AT_S1_MINUTES_AFTER_HOUR, new String[] { String.valueOf(_calendar.get(12) == 30 ? 57 : 27) });
      break;
    case -480:
      Functions.npcSay(npc, NpcString.THERE_ARE_5_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH, new String[0]);
      break;
    case -360:
      Functions.npcSay(npc, NpcString.THERE_ARE_3_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH, new String[0]);
      break;
    case -240:
      Functions.npcSay(npc, NpcString.THERE_ARE_1_MINUTES_REMAINING_TO_REGISTER_FOR_KRATEIS_CUBE_MATCH, new String[0]);
      break;
    case -180:
    case -120:
    case -60:
      Functions.npcSay(npc, NpcString.THE_MATCH_WILL_BEGIN_SHORTLY, new String[0]);
      break;
    case 600:
      Functions.npcSay(npc, NpcString.THE_MATCH_WILL_BEGIN_IN_S1_MINUTES, new String[] { String.valueOf(20) });
    }
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    _calendar.setTimeInMillis(DATE_PATTERN.next(System.currentTimeMillis()));

    registerActions();
  }

  public NpcInstance getNpc()
  {
    SpawnExObject obj = (SpawnExObject)getFirstObject("manager");

    return obj.getFirstSpawned();
  }

  public boolean isInProgress()
  {
    return _isInProgress;
  }

  public boolean isRegistrationOver()
  {
    return _isRegistrationOver;
  }

  protected long startTimeMillis()
  {
    return _calendar.getTimeInMillis();
  }

  protected void printInfo()
  {
  }

  public void action(String name, boolean start)
  {
    if (name.equalsIgnoreCase("registration"))
      _isRegistrationOver = (!start);
    else
      super.action(name, start);
  }

  public List<KrateisCubeEvent> getCubes()
  {
    return _cubes;
  }

  public boolean isRegistered(Player player)
  {
    for (KrateisCubeEvent cubeEvent : _cubes)
      if ((cubeEvent.getRegisteredPlayer(player) != null) && (cubeEvent.getParticlePlayer(player) != null))
        return true;
    return false;
  }
}