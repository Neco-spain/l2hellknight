package net.sf.l2j.gameserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastList;

public class MultiSort
{
  public static final int SORT_ASCENDING = 0;
  public static final int SORT_DESCENDING = 1;
  private List<?> _keyList;
  private List<Integer> _valueList;
  private boolean _isSortDescending;
  private boolean _isSorted;

  public MultiSort(int[] valueList)
  {
    _valueList = getIntList(valueList);
  }

  public MultiSort(Collection<Integer> valueList)
  {
    _valueList = getIntList(valueList);
  }

  public MultiSort(Object[] keyList, int[] valueList)
  {
    _keyList = getList(keyList);
    _valueList = getIntList(valueList);
  }

  public MultiSort(Map<?, Integer> valueMap)
  {
    _keyList = getList(valueMap.keySet());
    _valueList = getIntList(valueMap.values());
  }

  private final List<Integer> getIntList(Collection<Integer> valueList)
  {
    return Arrays.asList(valueList.toArray(new Integer[valueList.size()]));
  }

  private final List<Integer> getIntList(int[] valueList)
  {
    Integer[] tempIntList = new Integer[valueList.length];

    for (int i = 0; i < valueList.length; i++) {
      tempIntList[i] = new Integer(valueList[i]);
    }
    return Arrays.asList(tempIntList);
  }

  private final List<?> getList(Collection<?> valueList)
  {
    return getList(valueList.toArray(new Object[valueList.size()]));
  }

  private final List<Object> getList(Object[] valueList)
  {
    return Arrays.asList(valueList);
  }

  public final int getCount()
  {
    return getValues().size();
  }

  public final int getHarmonicMean()
  {
    if (getValues().isEmpty()) {
      return -1;
    }
    int totalValue = 0;

    for (Iterator i$ = getValues().iterator(); i$.hasNext(); ) { int currValue = ((Integer)i$.next()).intValue();
      totalValue += 1 / currValue;
    }
    return getCount() / totalValue;
  }

  public final List<?> getKeys()
  {
    if (_keyList == null) {
      return new FastList();
    }
    return _keyList;
  }

  public final int getFrequency(int checkValue)
  {
    return Collections.frequency(getValues(), Integer.valueOf(checkValue));
  }

  public final int getMaxValue()
  {
    return ((Integer)Collections.max(getValues())).intValue();
  }

  public final int getMinValue()
  {
    return ((Integer)Collections.min(getValues())).intValue();
  }

  public final int getMean()
  {
    if (getValues().isEmpty()) {
      return -1;
    }
    return getTotalValue() / getCount();
  }

  public final double getStandardDeviation()
  {
    if (getValues().isEmpty()) {
      return -1.0D;
    }
    List tempValList = new FastList();

    int meanValue = getMean();
    int numValues = getCount();

    for (Iterator i$ = getValues().iterator(); i$.hasNext(); ) { int value = ((Integer)i$.next()).intValue();

      double adjValue = Math.pow(value - meanValue, 2.0D);
      tempValList.add(Double.valueOf(adjValue));
    }

    double totalValue = 0.0D;

    for (Iterator i$ = tempValList.iterator(); i$.hasNext(); ) { double storedVal = ((Double)i$.next()).doubleValue();
      totalValue += storedVal;
    }
    return Math.sqrt(totalValue / (numValues - 1));
  }

  public final int getTotalValue()
  {
    if (getValues().isEmpty()) {
      return 0;
    }
    int totalValue = 0;

    for (Iterator i$ = getValues().iterator(); i$.hasNext(); ) { int currValue = ((Integer)i$.next()).intValue();
      totalValue += currValue;
    }
    return totalValue;
  }

  public final List<Integer> getValues()
  {
    if (_valueList == null) {
      return new FastList();
    }
    return _valueList;
  }

  public final boolean isSortDescending()
  {
    return _isSortDescending;
  }

  public final boolean isSorted()
  {
    return _isSorted;
  }

  public final void setSortDescending(boolean isDescending)
  {
    _isSortDescending = isDescending;
  }

  public boolean sort()
  {
    try
    {
      List newKeyList = new FastList();
      List newValueList = new FastList();

      Collections.sort(getValues());

      int lastValue = 0;

      if (!isSortDescending())
      {
        if (getKeys().isEmpty()) {
          return true;
        }

        for (int i = getValues().size() - 1; i > -1; i--)
        {
          int currValue = ((Integer)getValues().get(i)).intValue();

          if (currValue == lastValue)
          {
            continue;
          }
          lastValue = currValue;

          for (int j = 0; j < getKeys().size(); j++)
          {
            Object currKey = getKeys().get(j);

            if (((Integer)getValues().get(j)).intValue() != currValue)
              continue;
            newKeyList.add(currKey);
            newValueList.add(Integer.valueOf(currValue));
          }

        }

      }
      else
      {
        if (getKeys().isEmpty())
        {
          Collections.reverse(getValues());
          return true;
        }

        for (int i = 0; i < getValues().size(); i++)
        {
          int currValue = ((Integer)getValues().get(i)).intValue();

          if (currValue == lastValue) {
            continue;
          }
          lastValue = currValue;

          for (int j = 0; j < getKeys().size(); j++)
          {
            Object currKey = getKeys().get(j);

            if (((Integer)getValues().get(j)).intValue() != currValue)
              continue;
            newKeyList.add(currKey);
            newValueList.add(Integer.valueOf(currValue));
          }
        }

      }

      _keyList = newKeyList;
      _valueList = newValueList;
      _isSorted = true;
      return true;
    }
    catch (Exception e) {
    }
    return false;
  }
}