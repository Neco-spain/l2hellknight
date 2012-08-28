package net.sf.l2j.gameserver.model;

import java.util.concurrent.Future;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;

public class L2Potion extends L2Object
{
  protected static final Logger _log = Logger.getLogger(L2Character.class.getName());
  private L2Character _target;
  private Future<?> _potionhpRegTask;
  private Future<?> _potionmpRegTask;
  protected int _milliseconds;
  protected double _effect;
  protected int _duration;
  private int _potion;
  protected Object _mpLock = new Object();
  protected Object _hpLock = new Object();

  public L2Potion(int objectId)
  {
    super(objectId);
  }

  public void stopPotionHpRegeneration()
  {
    if (_potionhpRegTask != null)
    {
      _potionhpRegTask.cancel(false);
    }
    _potionhpRegTask = null;
    if (Config.DEBUG) _log.fine("Potion HP regen stop");
  }

  public void setCurrentHpPotion2()
  {
    if (_duration == 0)
    {
      stopPotionHpRegeneration();
    }
  }

  public void setCurrentHpPotion1(L2Character activeChar, int item) {
    _potion = item;
    _target = activeChar;

    switch (_potion)
    {
    case 1540:
      double nowHp = activeChar.getCurrentHp();
      nowHp += 435.0D;
      if (nowHp >= activeChar.getMaxHp())
      {
        nowHp = activeChar.getMaxHp();
      }
      activeChar.setCurrentHp(nowHp);
      break;
    case 728:
      double nowMp = activeChar.getMaxMp();
      nowMp += 435.0D;
      if (nowMp >= activeChar.getMaxMp())
      {
        nowMp = activeChar.getMaxMp();
      }
      activeChar.setCurrentMp(nowMp);
      break;
    case 726:
      _milliseconds = 500;
      _duration = 15;
      _effect = 1.5D;
      startPotionMpRegeneration(activeChar);
    }
  }

  private void startPotionMpRegeneration(L2Character activeChar)
  {
    _potionmpRegTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new PotionMpHealing(activeChar), 1000L, _milliseconds);

    if (Config.DEBUG) _log.fine("Potion MP regen Started");
  }

  public void stopPotionMpRegeneration()
  {
    if (_potionmpRegTask != null)
    {
      _potionmpRegTask.cancel(false);
    }

    _potionmpRegTask = null;
    if (Config.DEBUG) _log.fine("Potion MP regen stop");
  }

  public void setCurrentMpPotion2()
  {
    if (_duration == 0)
    {
      stopPotionMpRegeneration();
    }
  }

  public void setCurrentMpPotion1(L2Character activeChar, int item)
  {
    _potion = item;
    _target = activeChar;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }

  class PotionMpHealing
    implements Runnable
  {
    L2Character _instance;

    public PotionMpHealing(L2Character instance)
    {
      _instance = instance;
    }

    public void run()
    {
      try
      {
        synchronized (_mpLock)
        {
          double nowMp = _instance.getCurrentMp();
          if (_duration == 0)
          {
            stopPotionMpRegeneration();
          }
          if (_duration != 0)
          {
            nowMp += _effect;
            _instance.setCurrentMp(nowMp);
            _duration -= _milliseconds / 1000;
            setCurrentMpPotion2();
          }
        }

      }
      catch (Exception e)
      {
        L2Potion._log.warning("error in mp potion task:" + e);
      }
    }
  }

  class PotionHpHealing
    implements Runnable
  {
    L2Character _instance;

    public PotionHpHealing(L2Character instance)
    {
      _instance = instance;
    }

    public void run()
    {
      try
      {
        synchronized (_hpLock)
        {
          double nowHp = _instance.getCurrentHp();
          if (_duration == 0)
          {
            stopPotionHpRegeneration();
          }
          if (_duration != 0)
          {
            nowHp += _effect;
            _instance.setCurrentHp(nowHp);
            _duration -= _milliseconds / 1000;
            setCurrentHpPotion2();
          }
        }
      }
      catch (Exception e)
      {
        L2Potion._log.warning("Error in hp potion task:" + e);
      }
    }
  }
}