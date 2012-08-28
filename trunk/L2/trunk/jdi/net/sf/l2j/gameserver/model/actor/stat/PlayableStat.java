package net.sf.l2j.gameserver.model.actor.stat;

import java.io.PrintStream;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.status.PlayableStatus;

public class PlayableStat extends CharStat
{
  public PlayableStat(L2PlayableInstance activeChar)
  {
    super(activeChar);
  }

  public boolean addExp(long value)
  {
    if ((getExp() + value < 0L) || (getExp() == getExpForLevel(81) - 1L)) {
      return true;
    }
    if (getExp() + value >= getExpForLevel(81)) {
      value = getExpForLevel(81) - 1L - getExp();
    }
    setExp(getExp() + value);

    byte level = 0;
    for (level = 1; level <= 81; level = (byte)(level + 1))
    {
      if (getExp() < getExpForLevel(level)) {
        level = (byte)(level - 1);
        break;
      }
    }
    if (level != getLevel()) addLevel((byte)(level - getLevel()));

    return true;
  }

  public boolean removeExp(long value)
  {
    if (getExp() - value < 0L) {
      value = getExp() - 1L;
    }
    setExp(getExp() - value);

    byte level = 0;
    for (level = 1; level <= 81; level = (byte)(level + 1))
    {
      if (getExp() >= getExpForLevel(level))
        continue;
      level = (byte)(level - 1);
      break;
    }
    if (level != getLevel())
      addLevel((byte)(level - getLevel()));
    return true;
  }

  public boolean addExpAndSp(long addToExp, int addToSp)
  {
    boolean expAdded = false;
    boolean spAdded = false;
    if (addToExp >= 0L) expAdded = addExp(addToExp);
    if (addToSp >= 0) spAdded = addSp(addToSp);

    return (expAdded) || (spAdded);
  }

  public boolean removeExpAndSp(long removeExp, int removeSp)
  {
    boolean expRemoved = false;
    boolean spRemoved = false;
    if (removeExp > 0L) expRemoved = removeExp(removeExp);
    if (removeSp > 0) spRemoved = removeSp(removeSp);

    return (expRemoved) || (spRemoved);
  }

  public boolean addLevel(byte value)
  {
    if (getLevel() + value > 80)
    {
      if (getLevel() < 80)
        value = (byte)(80 - getLevel());
      else {
        return false;
      }
    }
    boolean levelIncreased = getLevel() + value > getLevel();
    value = (byte)(value + getLevel());
    setLevel(value);

    if ((getExp() >= getExpForLevel(getLevel() + 1)) || (getExpForLevel(getLevel()) > getExp())) setExp(getExpForLevel(getLevel()));

    if (!levelIncreased) return false;

    getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
    getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());

    return true;
  }

  public boolean addSp(int value)
  {
    if (value < 0)
    {
      System.out.println("wrong usage");
      return false;
    }
    int currentSp = getSp();
    if (currentSp == 2147483647) {
      return false;
    }
    if (currentSp > 2147483647 - value) {
      value = 2147483647 - currentSp;
    }
    setSp(currentSp + value);
    return true;
  }

  public boolean removeSp(int value)
  {
    int currentSp = getSp();
    if (currentSp < value)
      value = currentSp;
    setSp(getSp() - value);
    return true;
  }
  public long getExpForLevel(int level) {
    return level;
  }

  public L2PlayableInstance getActiveChar()
  {
    return (L2PlayableInstance)super.getActiveChar();
  }
}