package l2p.gameserver.model.entity.events.impl;

import java.util.Calendar;
import l2p.commons.collections.MultiValueSet;
import l2p.gameserver.Announcements;
import l2p.gameserver.model.entity.events.GlobalEvent;

public class March8Event extends GlobalEvent
{
  private Calendar _calendar = Calendar.getInstance();
  private static final long LENGTH = 604800000L;

  public March8Event(MultiValueSet<String> set)
  {
    super(set);
  }

  public void initEvent()
  {
  }

  public void startEvent()
  {
    super.startEvent();

    Announcements.getInstance().announceToAll("Test startEvent");
  }

  public void stopEvent()
  {
    super.stopEvent();

    Announcements.getInstance().announceToAll("Test stopEvent");
  }

  public void reCalcNextTime(boolean onInit)
  {
    clearActions();

    if (onInit)
    {
      _calendar.set(2, 2);
      _calendar.set(5, 8);
      _calendar.set(11, 0);
      _calendar.set(12, 0);
      _calendar.set(13, 0);

      if (_calendar.getTimeInMillis() + 604800000L < System.currentTimeMillis())
        _calendar.add(1, 1);
    }
    else
    {
      _calendar.add(1, 1);
    }
    registerActions();
  }

  protected long startTimeMillis()
  {
    return _calendar.getTimeInMillis();
  }
}