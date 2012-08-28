package scripts.ai;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.bosses.QueenAntManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2GrandBossInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.knownlist.MonsterKnownList;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class QueenAnt extends L2GrandBossInstance
{
  private static final int BOSS = 29001;
  private static L2Spawn _larva = null;
  private static FastList<L2Spawn> _nurses;

  public QueenAnt(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    super.onSpawn();
    cleanNest();
    createPrivates();
    ThreadPoolManager.getInstance().scheduleAi(new Timer(), 10000L, false);
  }

  public void reduceCurrentHp(double damage, L2Character attacker, boolean awake)
  {
    super.reduceCurrentHp(damage, attacker, awake);
  }

  public boolean doDie(L2Character killer)
  {
    super.doDie(killer);
    QueenAntManager.getInstance().notifyDie();
    return true;
  }

  public void onTeleported()
  {
    super.onTeleported();
  }

  public void deleteMe()
  {
    super.deleteMe();
  }

  public void teleToLocation(int x, int y, int z, boolean f)
  {
    deletePrivates();
    super.teleToLocation(x, y, z, false);
  }

  public void deletePrivates()
  {
    if ((_larva != null) && (_larva.getLastSpawn() != null))
      _larva.getLastSpawn().deleteMe();
    _larva = null;

    if ((_nurses != null) && (!_nurses.isEmpty()))
    {
      FastList.Node n = _nurses.head(); for (FastList.Node end = _nurses.tail(); (n = n.getNext()) != end; )
      {
        L2Spawn nurse = (L2Spawn)n.getValue();
        if ((nurse == null) || (nurse.getLastSpawn() == null)) {
          continue;
        }
        nurse.getLastSpawn().deleteMe();
      }
      _nurses.clear();
      _nurses = null;
    }
  }

  private void cleanNest()
  {
    FastList players = getKnownList().getKnownPlayersInRadius(2500);
    L2PcInstance pc = null;
    FastList.Node n = players.head(); for (FastList.Node end = players.tail(); (n = n.getNext()) != end; )
    {
      pc = (L2PcInstance)n.getValue();
      if (pc == null)
        continue;
      if (Rnd.get(100) < 33) {
        pc.teleToLocation(-19480, 187344, -5600); continue;
      }if (Rnd.get(100) < 50) {
        pc.teleToLocation(-17928, 180912, -5520); continue;
      }
      pc.teleToLocation(-23808, 182368, -5600);
    }
  }

  private void createPrivates()
  {
    _nurses = new FastList();
    GrandBossManager gb = GrandBossManager.getInstance();

    L2Spawn spawn = null;
    QueenAntNurse nurse = null;

    _larva = gb.createOneSpawnEx(29002, -21600, 179482, -5846, Rnd.get(65535), false);
    for (int i = 7; i > -1; i--)
    {
      spawn = gb.createOneSpawnEx(29003, getX() + Rnd.get(150), getY() + Rnd.get(150), getZ(), Rnd.get(65535), false);
      _nurses.add(spawn);
      nurse = (QueenAntNurse)spawn.spawnOne();
      nurse.setAq(this);
      nurse.setLarva((QueenAntLarva)_larva.spawnOne());
    }
    ThreadPoolManager.getInstance().scheduleAi(new RespawnNurses(), 15000L, false);
    ThreadPoolManager.getInstance().scheduleAi(new RespawnLarva(), 20000L, false);
  }

  class RespawnLarva
    implements Runnable
  {
    RespawnLarva()
    {
    }

    public void run()
    {
      if (QueenAnt._larva == null) {
        return;
      }
      if ((QueenAnt._larva.getLastKill() > 0L) && (System.currentTimeMillis() - QueenAnt._larva.getLastKill() >= 60000L))
      {
        QueenAnt._larva.spawnOne();
        QueenAnt._larva.setLastKill(0L);
      }

      ThreadPoolManager.getInstance().scheduleAi(new RespawnLarva(QueenAnt.this), 20000L, false);
    }
  }

  class RespawnNurses
    implements Runnable
  {
    RespawnNurses()
    {
    }

    public void run()
    {
      if ((QueenAnt._nurses == null) || (QueenAnt._nurses.isEmpty())) {
        return;
      }
      if (isDead()) {
        return;
      }
      FastList.Node n = QueenAnt._nurses.head(); for (FastList.Node end = QueenAnt._nurses.tail(); (n = n.getNext()) != end; )
      {
        L2Spawn nurse = (L2Spawn)n.getValue();
        if (nurse == null) {
          continue;
        }
        if ((nurse.getLastKill() > 0L) && (System.currentTimeMillis() - nurse.getLastKill() >= Config.AQ_NURSE_RESPAWN))
        {
          nurse.spawnOne();
          nurse.setLastKill(0L);
        }
      }
      ThreadPoolManager.getInstance().scheduleAi(new RespawnNurses(QueenAnt.this), Config.AQ_NURSE_RESPAWN, false);
    }
  }

  class Timer
    implements Runnable
  {
    Timer()
    {
    }

    public void run()
    {
      if (isDead()) {
        return;
      }
      if (Rnd.get(100) < 2)
        broadcastPacket(new SocialAction(getObjectId(), 1));
      else if (Rnd.get(100) < 10)
        addUseSkillDesire(4017, 1);
      else if (Rnd.get(100) < 20)
        addUseSkillDesire(4019, 1);
      else if (Rnd.get(100) < 30)
      {
        if (Rnd.get(100) < 50)
          broadcastPacket(new SocialAction(getObjectId(), 3));
        else
          broadcastPacket(new SocialAction(getObjectId(), 4));
      }
      else if (Rnd.get(100) < 70) {
        addUseSkillDesire(4018, 1);
      }

      ThreadPoolManager.getInstance().scheduleAi(new Timer(QueenAnt.this), 10000L, false);
    }
  }
}