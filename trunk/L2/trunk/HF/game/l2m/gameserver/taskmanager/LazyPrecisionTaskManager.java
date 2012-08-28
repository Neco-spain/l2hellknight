package l2m.gameserver.taskmanager;

import java.util.concurrent.Future;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.threading.SteppingRunnableQueueManager;
import l2p.commons.util.Rnd;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.data.dao.AccountBonusDAO;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.actor.instances.player.Bonus;
import l2m.gameserver.model.instances.NpcInstance;
import l2m.gameserver.network.serverpackets.ExBR_PremiumState;
import l2m.gameserver.network.serverpackets.ExShowScreenMessage;
import l2m.gameserver.network.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.IStaticPacket;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
  private static final LazyPrecisionTaskManager _instance = new LazyPrecisionTaskManager();

  public static final LazyPrecisionTaskManager getInstance()
  {
    return _instance;
  }

  private LazyPrecisionTaskManager()
  {
    super(1000L);
    ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);

    ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
    {
      public void runImpl()
        throws Exception
      {
        purge();
      }
    }
    , 60000L, 60000L);
  }

  public Future<?> addPCCafePointsTask(Player player)
  {
    long delay = Config.ALT_PCBANG_POINTS_DELAY * 60000L;

    return scheduleAtFixedRate(new RunnableImpl(player)
    {
      public void runImpl()
        throws Exception
      {
        if ((val$player.isInOfflineMode()) || (val$player.getLevel() < Config.ALT_PCBANG_POINTS_MIN_LVL)) {
          return;
        }
        val$player.addPcBangPoints(Config.ALT_PCBANG_POINTS_BONUS, (Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0.0D) && (Rnd.chance(Config.ALT_PCBANG_POINTS_BONUS_DOUBLE_CHANCE)));
      }
    }
    , delay, delay);
  }

  public Future<?> addVitalityRegenTask(Player player)
  {
    long delay = 60000L;

    return scheduleAtFixedRate(new RunnableImpl(player)
    {
      public void runImpl()
        throws Exception
      {
        if ((val$player.isInOfflineMode()) || (!val$player.isInPeaceZone())) {
          return;
        }
        val$player.setVitality(val$player.getVitality() + 1.0D);
      }
    }
    , delay, delay);
  }

  public Future<?> startBonusExpirationTask(Player player)
  {
    long delay = player.getBonus().getBonusExpire() * 1000L - System.currentTimeMillis();

    return schedule(new RunnableImpl(player)
    {
      public void runImpl()
        throws Exception
      {
        val$player.getBonus().setRateXp(1.0D);
        val$player.getBonus().setRateSp(1.0D);
        val$player.getBonus().setDropAdena(1.0D);
        val$player.getBonus().setDropItems(1.0D);
        val$player.getBonus().setDropSpoil(1.0D);

        if (val$player.getParty() != null) {
          val$player.getParty().recalculatePartyData();
        }
        String msg = new CustomMessage("scripts.services.RateBonus.LuckEnded", val$player, new Object[0]).toString();
        val$player.sendPacket(new IStaticPacket[] { new ExShowScreenMessage(msg, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true), new ExBR_PremiumState(val$player, false) });
        val$player.sendMessage(msg);

        if (Config.SERVICES_RATE_TYPE == 2)
          AccountBonusDAO.getInstance().delete(val$player.getAccountName());
      }
    }
    , delay);
  }

  public Future<?> addNpcAnimationTask(NpcInstance npc)
  {
    return scheduleAtFixedRate(new RunnableImpl(npc)
    {
      public void runImpl()
        throws Exception
      {
        if ((val$npc.isVisible()) && (!val$npc.isActionsDisabled()) && (!val$npc.isMoving) && (!val$npc.isInCombat()))
          val$npc.onRandomAnimation();
      }
    }
    , 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
  }
}