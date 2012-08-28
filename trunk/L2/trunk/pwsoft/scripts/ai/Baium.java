package scripts.ai;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.bosses.BaiumManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class Baium extends L2GrandBossInstance
{
  private long _lastHit = 0L;
  private static final long _sklChk = 60000L;
  private static final long _trgChk = 40000L;
  private static int sself_normal_attack = 4127;
  private static int s_energy_wave = 4128;
  private static int s_earth_quake = 4129;
  private static int s_thunderbolt = 4130;
  private static int s_group_hold = 4131;

  private FastList<L2GrandBossInstance> _angels = new FastList();

  public static final long _sleepLife = Config.BAIUM_UPDATE_LAIR;
  private static final int BOSS = 29020;
  private static GrandBossManager _gb = GrandBossManager.getInstance();

  public Baium(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();
    _lastHit = System.currentTimeMillis();
    ThreadPoolManager.getInstance().scheduleAi(new Task(1), 12000L, false);

    if (getSpawn() != null)
    {
      getSpawn().setLocx(115852);
      getSpawn().setLocy(17265);
      getSpawn().setLocz(10079);
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
    unSpawnAngels(false);
    BaiumManager.getInstance().notifyDie();
    return true;
  }

  public void deleteMe()
  {
    super.deleteMe();
  }

  private void changeTarget()
  {
    FastList _players = getKnownList().getKnownPlayersInRadius(1200);
    if (_players.isEmpty()) {
      return;
    }
    L2PcInstance trg = (L2PcInstance)_players.get(Rnd.get(_players.size() - 1));
    if (trg != null)
    {
      setTarget(trg);
      addDamageHate(trg, 0, 999);
      getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
    }
  }

  private void castRandomSkill()
  {
    int rnd = Rnd.get(100);
    if (getCurrentHp() > getMaxHp() * 3.0D / 4.0D)
    {
      if (rnd < 10)
        addUseSkillDesire(s_energy_wave, 1);
      else if (rnd < 15)
        addUseSkillDesire(s_earth_quake, 1);
      else
        addUseSkillDesire(sself_normal_attack, 1);
    }
    else if (getCurrentHp() > getMaxHp() * 2.0D / 4.0D)
    {
      if (Rnd.get(100) < 10)
        addUseSkillDesire(s_group_hold, 1);
      else if (Rnd.get(100) < 15)
        addUseSkillDesire(s_energy_wave, 1);
      else if (Rnd.get(100) < 20)
        addUseSkillDesire(s_earth_quake, 1);
      else
        addUseSkillDesire(sself_normal_attack, 1);
    }
    else if (rnd < 10)
      addUseSkillDesire(s_thunderbolt, 1);
    else if (rnd < 15)
      addUseSkillDesire(s_group_hold, 1);
    else if (rnd < 20)
      addUseSkillDesire(s_energy_wave, 1);
    else if (rnd < 25)
      addUseSkillDesire(s_earth_quake, 1);
    else
      addUseSkillDesire(sself_normal_attack, 1);
  }

  private void spawnAngels()
  {
    L2GrandBossInstance Angel1 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021, 115617, 17462, 10136, 0);
    Angel1.setRunning();
    L2GrandBossInstance Angel2 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021, 116070, 17130, 10136, 0);
    Angel2.setRunning();
    L2GrandBossInstance Angel3 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021, 115910, 16838, 10136, 0);
    Angel3.setRunning();
    L2GrandBossInstance Angel4 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021, 115585, 16954, 10136, 0);
    Angel4.setRunning();
    L2GrandBossInstance Angel5 = (L2GrandBossInstance)_gb.createOnePrivateEx(29021, 115649, 17207, 10136, 0);
    Angel5.setRunning();
    _angels.add(Angel1);
    _angels.add(Angel2);
    _angels.add(Angel3);
    _angels.add(Angel4);
    _angels.add(Angel5);
    ThreadPoolManager.getInstance().scheduleAi(new Task(5), 4000L, false);
  }

  private void angelTarget()
  {
    FastList _players = getKnownList().getKnownPlayersInRadius(1200);
    if (_players.isEmpty()) {
      return;
    }
    L2PcInstance trg = null;
    L2GrandBossInstance angel = null;
    FastList.Node n = _angels.head(); for (FastList.Node end = _angels.tail(); (n = n.getNext()) != end; )
    {
      angel = (L2GrandBossInstance)n.getValue();
      if (angel == null) {
        continue;
      }
      trg = (L2PcInstance)_players.get(Rnd.get(_players.size() - 1));
      if (trg == null) {
        continue;
      }
      angel.setRunning();
      angel.setTarget(trg);
      angel.addDamageHate(trg, 0, 999);
      angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, trg);
    }
  }

  public void unSpawnAngels(boolean sleep)
  {
    if (_angels.isEmpty())
      return;
    L2GrandBossInstance angel;
    FastList.Node n;
    if (sleep)
    {
      angel = null;
      n = _angels.head(); for (FastList.Node end = _angels.tail(); (n = n.getNext()) != end; )
      {
        angel = (L2GrandBossInstance)n.getValue();
        if (angel == null)
          continue;
        angel.setRunning();
        angel.setTarget(this);
        angel.addDamageHate(this, 0, 999);
        angel.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
      }
    }
    ThreadPoolManager.getInstance().scheduleAi(new Task(4), 10000L, false);
  }

  private void clearAngels()
  {
    if (_angels.isEmpty()) {
      return;
    }
    L2GrandBossInstance angel = null;
    FastList.Node n = _angels.head(); for (FastList.Node end = _angels.tail(); (n = n.getNext()) != end; )
    {
      angel = (L2GrandBossInstance)n.getValue();
      if (angel == null)
        continue;
      angel.deleteMe();
    }
    _angels.clear();
  }

  public long getLastHit()
  {
    return _lastHit;
  }

  public boolean checkRange()
  {
    return false;
  }

  private class Task
    implements Runnable
  {
    int id;

    Task(int id)
    {
      this.id = id;
    }

    public void run()
    {
      switch (id)
      {
      case 1:
        Baium.this.spawnAngels();
        ThreadPoolManager.getInstance().scheduleAi(new Task(Baium.this, 2), 60000L, false);
        ThreadPoolManager.getInstance().scheduleAi(new Task(Baium.this, 3), 40000L, false);
        break;
      case 2:
        Baium.this.castRandomSkill();
        ThreadPoolManager.getInstance().scheduleAi(new Task(Baium.this, 2), 60000L, false);
        break;
      case 3:
        Baium.this.changeTarget();
        ThreadPoolManager.getInstance().scheduleAi(new Task(Baium.this, 3), 40000L, false);
        break;
      case 4:
        Baium.this.clearAngels();
        break;
      case 5:
        Baium.this.angelTarget();
      }
    }
  }
}