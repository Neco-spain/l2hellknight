package net.sf.l2j.gameserver.model;

import java.util.Arrays;

public class L2DropData
{
  public static final int MAX_CHANCE = 1000000;
  private int _itemId;
  private int _minDrop;
  private int _maxDrop;
  private int _chance;
  private String _questID = null;
  private String[] _stateID = null;

  public int getItemId()
  {
    return _itemId;
  }

  public void setItemId(int itemId)
  {
    _itemId = itemId;
  }

  public int getMinDrop()
  {
    return _minDrop;
  }

  public int getMaxDrop()
  {
    return _maxDrop;
  }

  public int getChance()
  {
    return _chance;
  }

  public void setMinDrop(int mindrop)
  {
    _minDrop = mindrop;
  }

  public void setMaxDrop(int maxdrop)
  {
    _maxDrop = maxdrop;
  }

  public void setChance(int chance)
  {
    _chance = chance;
  }

  public String[] getStateIDs()
  {
    return _stateID;
  }

  public void addStates(String[] list)
  {
    _stateID = list;
  }

  public String getQuestID()
  {
    return _questID;
  }

  public void setQuestID(String questID)
  {
    _questID = questID;
  }

  public boolean isQuestDrop()
  {
    return (_questID != null) && (_stateID != null);
  }

  public String toString()
  {
    String out = "ItemID: " + getItemId() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0D + "%";

    if (isQuestDrop())
    {
      out = out + " QuestID: " + getQuestID() + " StateID's: " + Arrays.toString(getStateIDs());
    }

    return out;
  }

  public boolean equals(Object o)
  {
    if ((o instanceof L2DropData))
    {
      L2DropData drop = (L2DropData)o;
      return drop.getItemId() == getItemId();
    }
    return false;
  }
}