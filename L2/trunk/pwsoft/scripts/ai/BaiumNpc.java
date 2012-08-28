package scripts.ai;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.instancemanager.bosses.BaiumManager;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;
import scripts.zone.type.L2BossZone;

public class BaiumNpc extends L2NpcInstance
{
  private static L2PcInstance _talker = null;
  private static Baium _baium = null;
  private Lock shed = new ReentrantLock();
  private ScheduledFuture<?> _spawnTask = null;
  private static final int BOSS = 29020;

  public BaiumNpc(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void onSpawn()
  {
    _spawnTask = null;
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (GrandBossManager.getInstance().getZone(116040, 17455, 10078).isPlayerAllowed(player)) {
      spawnMe(player);
    }
    player.sendActionFailed();
  }

  private void spawnMe(L2PcInstance player)
  {
    shed.lock();
    try
    {
      if (_spawnTask != null)
        return;
      _spawnTask = ThreadPoolManager.getInstance().scheduleAi(new SpawnBaium(player), 1000L, false);
    }
    finally
    {
      shed.unlock();
    }
  }

  class WakeUp
    implements Runnable
  {
    WakeUp()
    {
    }

    public void run()
    {
      if (BaiumNpc._baium == null) {
        return;
      }
      BaiumNpc._baium.broadcastPacket(new CreatureSay(BaiumNpc._baium.getObjectId(), 0, "Baium", "\u041A\u0442\u043E \u043F\u043E\u0441\u043C\u0435\u043B \u043F\u043E\u0442\u0440\u0435\u0432\u043E\u0436\u0438\u0442\u044C \u043C\u043E\u0439 \u0441\u043E\u043D!?"));
      BaiumNpc._baium.broadcastPacket(new SocialAction(BaiumNpc._baium.getObjectId(), 1));
      BaiumNpc._baium.broadcastPacket(new Earthquake(BaiumNpc._baium.getX(), BaiumNpc._baium.getY(), BaiumNpc._baium.getZ(), 40, 5));
      if ((BaiumNpc._talker != null) && (Rnd.get(100) < 70))
      {
        BaiumNpc._baium.setTarget(BaiumNpc._talker);
        BaiumNpc._baium.addUseSkillDesire(4136, 1);
      }
      deleteMe();
    }
  }

  class Come
    implements Runnable
  {
    Come()
    {
    }

    public void run()
    {
      if (BaiumNpc._baium == null) {
        return;
      }
      if (BaiumNpc._talker != null)
        BaiumNpc._talker.teleToLocation(115922, 17342, 10051);
    }
  }

  class SpawnBaium
    implements Runnable
  {
    private L2PcInstance player;

    SpawnBaium(L2PcInstance player)
    {
      this.player = player;
    }

    public void run()
    {
      decayMe();
      BaiumNpc.access$002(player);
      BaiumNpc.access$102((Baium)GrandBossManager.getInstance().createOnePrivateEx(29020, 116033, 17447, 10107, -25348));
      BaiumNpc._baium.setRunning();
      BaiumNpc._baium.broadcastPacket(new SocialAction(BaiumNpc._baium.getObjectId(), 2));
      BaiumManager.getInstance().setBaium(BaiumNpc._baium);
      ThreadPoolManager.getInstance().scheduleGeneral(new BaiumNpc.Come(BaiumNpc.this), 9000L);
      ThreadPoolManager.getInstance().scheduleGeneral(new BaiumNpc.WakeUp(BaiumNpc.this), 11000L);
      BaiumNpc.access$202(BaiumNpc.this, null);
    }
  }
}