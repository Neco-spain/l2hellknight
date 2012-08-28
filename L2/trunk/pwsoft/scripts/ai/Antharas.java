package scripts.ai;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.bosses.AntharasManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class Antharas extends L2GrandBossInstance
{
  public static long _lastHit = 0L;
  private static final long _sklChk = 60000L;
  private static final long _trgChk = 40000L;
  private static final int anJump = 4106;
  private static final int anTail = 4107;
  private static final int anFear = 4108;
  private static final int anDebuff = 4109;
  private static final int anBreath = 4111;
  private static final int anNorm = 4112;
  private static final int anNormEx = 4113;

  public Antharas(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();
    StartAnim();
    _lastHit = System.currentTimeMillis();

    if (getSpawn() != null)
    {
      getSpawn().setLocx(179596);
      getSpawn().setLocy(114921);
      getSpawn().setLocz(-7708);
    }
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    _lastHit = System.currentTimeMillis();
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean doDie(L2Character killer)
  {
    super.doDie(killer);
    AntharasManager.getInstance().notifyDie();
    return true;
  }

  public void deleteMe()
  {
    super.deleteMe();
  }

  public long getLastHit()
  {
    return _lastHit;
  }

  public void StartAnim()
  {
    ThreadPoolManager.getInstance().scheduleAi(new Anima(1), 1000L, false);
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
      L2PcInstance pc = null;
      FastList _players = getKnownList().getKnownPlayersInRadius(2500);
      switch (_act)
      {
      case 1:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.specialCamera(Antharas.this, 700, 13, -19, 0, 10000);
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Antharas.this, 2), 3000L, false);
        break;
      case 2:
        broadcastPacket(new SocialAction(getObjectId(), 1));
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.specialCamera(Antharas.this, 700, 13, 0, 6000, 10000);
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Antharas.this, 3), 10000L, false);
        break;
      case 3:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.specialCamera(Antharas.this, 3800, 0, -3, 0, 10000);
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Antharas.this, 4), 200L, false);
        break;
      case 4:
        broadcastPacket(new SocialAction(getObjectId(), 2));
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.specialCamera(Antharas.this, 1200, 0, -3, 22000, 11000);
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Antharas.this, 5), 10800L, false);
        break;
      case 5:
        FastList.Node n = _players.head(); for (FastList.Node end = _players.tail(); (n = n.getNext()) != end; )
        {
          pc = (L2PcInstance)n.getValue();
          if (pc == null)
            continue;
          pc.specialCamera(Antharas.this, 1200, 0, -3, 300, 2000);
        }
        ThreadPoolManager.getInstance().scheduleAi(new Anima(Antharas.this, 6), 1900L, false);
        break;
      case 6:
        ThreadPoolManager.getInstance().scheduleAi(new Antharas.ChangeTarget(Antharas.this), 40000L, false);
        ThreadPoolManager.getInstance().scheduleAi(new Antharas.CastSkill(Antharas.this), 60000L, false);
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
      if (getCurrentHp() > getMaxHp() * 3 / 4)
      {
        if (rnd < 6)
          addUseSkillDesire(4111, 1);
        else if (rnd < 10)
          addUseSkillDesire(4106, 1);
        else if (rnd < 15)
          addUseSkillDesire(4108, 1);
        else if (rnd < 50)
          addUseSkillDesire(4112, 1);
        else if (rnd < 80)
          addUseSkillDesire(4107, 1);
        else
          addUseSkillDesire(4113, 1);
      }
      else if (getCurrentHp() > getMaxHp() * 2 / 4)
      {
        if (rnd < 6)
          addUseSkillDesire(4111, 1);
        else if (rnd < 10)
          addUseSkillDesire(4106, 1);
        else if (rnd < 15)
          addUseSkillDesire(4108, 1);
        else if (rnd < 40)
          addUseSkillDesire(4109, 1);
        else if (rnd < 50)
          addUseSkillDesire(4112, 1);
        else if (rnd < 80)
          addUseSkillDesire(4107, 1);
        else
          addUseSkillDesire(4113, 1);
      }
      else if (getCurrentHp() > getMaxHp() * 1 / 4)
      {
        if (rnd < 6)
          addUseSkillDesire(4111, 1);
        else if (rnd < 10)
          addUseSkillDesire(4106, 1);
        else if (rnd < 15)
          addUseSkillDesire(4108, 1);
        else if (rnd < 40)
          addUseSkillDesire(4109, 1);
        else if (rnd < 50)
          addUseSkillDesire(4112, 1);
        else if (rnd < 80)
          addUseSkillDesire(4107, 1);
        else
          addUseSkillDesire(4113, 1);
      }
      else if (rnd < 6)
        addUseSkillDesire(4111, 1);
      else if (rnd < 10)
        addUseSkillDesire(4108, 1);
      else if (rnd < 50)
        addUseSkillDesire(4112, 1);
      else if (rnd < 80)
        addUseSkillDesire(4107, 1);
      else {
        addUseSkillDesire(4113, 1);
      }
      ThreadPoolManager.getInstance().scheduleAi(new CastSkill(Antharas.this), 60000L, false);
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
      ThreadPoolManager.getInstance().scheduleAi(new ChangeTarget(Antharas.this), 40000L, false);
    }
  }
}