package net.sf.l2j.gameserver.datatables;

import java.util.Date;
import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.gameserver.script.DateRange;

public class EventDroplist
{
  private static EventDroplist _instance;
  private List<DateDrop> _allNpcDateDrops;

  public static EventDroplist getInstance()
  {
    if (_instance == null)
    {
      _instance = new EventDroplist();
    }
    return _instance;
  }

  private EventDroplist()
  {
    _allNpcDateDrops = new FastList();
  }

  public void addGlobalDrop(int[] items, int[] count, int chance, DateRange range)
  {
    DateDrop date = new DateDrop();

    date.dateRange = range;
    date.items = items;
    date.min = count[0];
    date.max = count[1];
    date.chance = chance;

    _allNpcDateDrops.add(date);
  }

  public List<DateDrop> getAllDrops()
  {
    List list = new FastList();

    for (DateDrop drop : _allNpcDateDrops)
    {
      Date currentDate = new Date();

      if (drop.dateRange.isWithinRange(currentDate))
      {
        list.add(drop);
      }
    }

    return list;
  }

  public class DateDrop
  {
    public DateRange dateRange;
    public int[] items;
    public int min;
    public int max;
    public int chance;

    public DateDrop()
    {
    }
  }
}