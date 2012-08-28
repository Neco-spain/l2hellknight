package scripts.ai;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.bosses.ValakasManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.network.serverpackets.SpecialCamera;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class Valakas extends L2GrandBossInstance
{
  public static long _lastHit = 0L;
  private static final long _sklChk = 60000L;
  private static final long _trgChk = 40000L;
  private static int NorMalAttackLeft = 4681;
  private static int NorMalAttackRight = 4682;
  private static int RearStrike = 4685;
  private static int RearThrow = 4688;
  private static int Meteor = 4690;
  private static int BreathHigh = 4684;
  private static int BreathLow = 4683;
  private static int Fear = 4689;

  private static int PowerUp = 4680;

  public Valakas(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    startAnim();
    _lastHit = System.currentTimeMillis();
    super.onSpawn();
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    _lastHit = System.currentTimeMillis();
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean doDie(L2Character killer)
  {
    super.doDie(killer);
    deathAnim();
    ValakasManager.getInstance().notifyDie();
    return true;
  }

  public void deleteMe()
  {
    super.deleteMe();
  }

  public void startAnim()
  {
    if (Config.DISABLE_BOSS_INTRO)
      addUseSkillDesire(4691, 1);
    else
      ThreadPoolManager.getInstance().scheduleAi(new Anima(1), 2000L, false);
  }

  public void deathAnim()
  {
    ThreadPoolManager.getInstance().scheduleAi(new Anima(11), 500L, false);
  }

  public long getLastHit()
  {
    return _lastHit;
  }

  public boolean checkRange()
  {
    return false;
  }

  class Anima
    implements Runnable
  {
    private int _act;

    Anima(int act)
    {
      _act = act;
    }

    public void run()
    {
      int oobjId = getObjectId();
      L2PcInstance pc = null;
      FastList _players = getKnownList().getKnownPlayersInRadius(2500);
      FastList.Node n;
      switch (_act)
      {
      case 1:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1800, 180, -1, 1500, 15000, 0, 0, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 2), 1500L, false);
        break;
      case 2:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1300, 180, -5, 3000, 15000, 0, -5, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 3), 3300L, false);
        break;
      case 3:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 500, 180, -8, 600, 15000, 0, 60, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 4), 2900L, false);
        break;
      case 4:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 800, 180, -8, 2700, 15000, 0, 30, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 5), 2700L, false);
        break;
      case 5:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 200, 250, 70, 0, 15000, 30, 80, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 6), 1L, false);
        break;
      case 6:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1100, 250, 70, 2500, 15000, 30, 80, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 7), 3200L, false);
        break;
      case 7:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 700, 150, 30, 0, 15000, -10, 60, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 8), 1400L, false);
        break;
      case 8:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1200, 150, 20, 2900, 15000, -10, 30, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 9), 6700L, false);
        break;
      case 9:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 750, 170, -10, 3400, 15000, 10, -15, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 10), 5700L, false);
        break;
      case 10:
        addUseSkillDesire(4691, 1);

        break;
      case 11:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1100, 210, -5, 3000, 15000, -13, 0, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 12), 3500L, false);
        break;
      case 12:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1300, 200, -8, 3000, 15000, 0, 15, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 13), 4500L, false);
        break;
      case 13:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1000, 190, 0, 500, 15000, 0, 10, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 14), 500L, false);
        break;
      case 14:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1700, 120, 0, 2500, 15000, 12, 40, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 15), 4600L, false);
        break;
      case 15:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1700, 20, 0, 700, 15000, 10, 10, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 16), 750L, false);
        break;
      case 16:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1700, 10, 0, 1000, 15000, 20, 70, 1, 0));
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Valakas.this, 17), 2500L, false);
        break;
      case 17:
        n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.sendPacket(new SpecialCamera(oobjId, 1700, 10, 0, 300, 15000, 20, -20, 1, 0));
        }
      }
    }
  }

  class CastSkill
    implements Runnable
  {
    CastSkill()
    {
    }

    public void run()
    {
      int rnd = Rnd.get(100);
      if (getCurrentHp() > getMaxHp() * 3.0D / 4.0D)
      {
        if (rnd < 6)
        {
          addUseSkillDesire(Valakas.Meteor, 1);
        }
        else if (rnd < 15) {
          addUseSkillDesire(Valakas.Fear, 1);
        } else if (rnd < 60)
        {
          int rndEx = Rnd.get(100);
          if (rndEx < 7)
            addUseSkillDesire(Valakas.RearStrike, 1);
          else if (rndEx < 10)
            addUseSkillDesire(Valakas.RearThrow, 1);
          else if (rndEx < 15) {
            addUseSkillDesire(Valakas.BreathLow, 1);
          }
          else if (Rnd.get(100) < 50)
            addUseSkillDesire(Valakas.NorMalAttackRight, 1);
          else
            addUseSkillDesire(Valakas.NorMalAttackLeft, 1);
        }
        else
        {
          addUseSkillDesire(Valakas.BreathHigh, 1);
        }
      } else if (getCurrentHp() > getMaxHp() * 2.0D / 4.0D)
      {
        if (rnd < 6)
        {
          addUseSkillDesire(Valakas.Meteor, 1);
        }
        else if (rnd < 10) {
          addUseSkillDesire(Valakas.Fear, 1);
        } else if (rnd < 60)
        {
          int rndEx = Rnd.get(100);
          if (rndEx < 15)
            addUseSkillDesire(Valakas.RearStrike, 1);
          else if (rndEx < 17)
            addUseSkillDesire(Valakas.RearThrow, 1);
          else if (rndEx < 20) {
            addUseSkillDesire(Valakas.BreathLow, 1);
          }
          else if (Rnd.get(100) < 50)
            addUseSkillDesire(Valakas.NorMalAttackRight, 1);
          else
            addUseSkillDesire(Valakas.NorMalAttackLeft, 1);
        }
        else
        {
          addUseSkillDesire(Valakas.BreathHigh, 1);
        }
      } else if (getCurrentHp() > getMaxHp() * 1.0D / 4.0D)
      {
        if (rnd < 20)
        {
          addUseSkillDesire(Valakas.Meteor, 1);
        }
        else if (rnd < 25) {
          addUseSkillDesire(Valakas.Fear, 1);
        } else if (rnd < 60)
        {
          int rndEx = Rnd.get(100);
          if (rndEx < 17)
            addUseSkillDesire(Valakas.RearStrike, 1);
          else if (rndEx < 19)
            addUseSkillDesire(Valakas.RearThrow, 1);
          else if (rndEx < 35) {
            addUseSkillDesire(Valakas.BreathLow, 1);
          }
          else if (Rnd.get(100) < 50)
            addUseSkillDesire(Valakas.NorMalAttackRight, 1);
          else
            addUseSkillDesire(Valakas.NorMalAttackLeft, 1);
        }
        else
        {
          addUseSkillDesire(Valakas.BreathHigh, 1);
        }

      }
      else if (rnd < 6)
      {
        addUseSkillDesire(Valakas.Meteor, 1);
      }
      else if (rnd < 10) {
        addUseSkillDesire(Valakas.Fear, 1);
      } else if (rnd < 60)
      {
        int rndEx = Rnd.get(100);
        if (rndEx < 5)
          addUseSkillDesire(Valakas.RearStrike, 1);
        else if (rndEx < 7)
          addUseSkillDesire(Valakas.RearThrow, 1);
        else if (rndEx < 15) {
          addUseSkillDesire(Valakas.BreathLow, 1);
        }
        else if (Rnd.get(100) < 50)
          addUseSkillDesire(Valakas.NorMalAttackRight, 1);
        else
          addUseSkillDesire(Valakas.NorMalAttackLeft, 1);
      }
      else
      {
        addUseSkillDesire(Valakas.BreathHigh, 1);
      }

      ThreadPoolManager.getInstance().scheduleAi(new CastSkill(Valakas.this), 60000L, false);
    }
  }

  class ChangeTarget
    implements Runnable
  {
    ChangeTarget()
    {
    }

    public void run()
    {
      FastList _players = getKnownList().getKnownPlayersInRadius(1200);
      if (!_players.isEmpty())
      {
        L2PcInstance trg = (L2PcInstance)_players.get(Rnd.get(_players.size() - 1));
        if (trg != null)
        {
          setTarget(trg);
          addDamageHate(trg, 0, 999);
          getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
        }
      }
      ThreadPoolManager.getInstance().scheduleAi(new ChangeTarget(Valakas.this), 40000L, false);
    }
  }
}