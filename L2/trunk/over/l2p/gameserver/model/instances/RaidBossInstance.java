package l2p.gameserver.model.instances;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import l2p.commons.threading.RunnableImpl;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.NpcHolder;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.QuestManager;
import l2p.gameserver.instancemanager.RaidBossSpawnManager;
import l2p.gameserver.model.AggroList;
import l2p.gameserver.model.AggroList.HateInfo;
import l2p.gameserver.model.CommandChannel;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.GameObjectTasks.DeleteTask;
import l2p.gameserver.model.MinionList;
import l2p.gameserver.model.Party;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Experience;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.UnitMember;
import l2p.gameserver.model.quest.Quest;
import l2p.gameserver.model.quest.QuestState;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.IStaticPacket;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.templates.npc.NpcTemplate;

public class RaidBossInstance extends MonsterInstance
{
  private ScheduledFuture<?> minionMaintainTask;
  private static final int MINION_UNSPAWN_INTERVAL = 5000;

  public RaidBossInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);
  }

  public boolean isRaid()
  {
    return true;
  }

  protected int getMinionUnspawnInterval()
  {
    return 5000;
  }

  protected int getKilledInterval(MinionInstance minion)
  {
    return 120000;
  }

  public void notifyMinionDied(MinionInstance minion)
  {
    minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion));
    super.notifyMinionDied(minion);
  }

  protected void onDeath(Creature killer)
  {
    if (minionMaintainTask != null)
    {
      minionMaintainTask.cancel(false);
      minionMaintainTask = null;
    }

    int points = getTemplate().rewardRp;
    if (points > 0) {
      calcRaidPointsReward(points);
    }
    if ((this instanceof ReflectionBossInstance))
    {
      super.onDeath(killer);
      return;
    }

    if (killer.isPlayable())
    {
      Player player = killer.getPlayer();
      if (player.isInParty())
      {
        for (Player member : player.getParty().getPartyMembers())
          if (member.isNoble())
            Hero.getInstance().addHeroDiary(member.getObjectId(), 1, getNpcId());
        player.getParty().broadCast(new IStaticPacket[] { Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL });
      }
      else
      {
        if (player.isNoble())
          Hero.getInstance().addHeroDiary(player.getObjectId(), 1, getNpcId());
        player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
      }

      Quest q = QuestManager.getQuest(508);
      if (q != null)
      {
        String qn = q.getName();
        if ((player.getClan() != null) && (player.getClan().getLeader().isOnline()) && (player.getClan().getLeader().getPlayer().getQuestState(qn) != null))
        {
          QuestState st = player.getClan().getLeader().getPlayer().getQuestState(qn);
          st.getQuest().onKill(this, st);
        }
      }
    }

    if (getMinionList().hasAliveMinions()) {
      ThreadPoolManager.getInstance().schedule(new RunnableImpl()
      {
        public void runImpl()
          throws Exception
        {
          if (isDead())
            getMinionList().unspawnMinions();
        }
      }
      , getMinionUnspawnInterval());
    }

    int boxId = 0;
    switch (getNpcId())
    {
    case 25035:
      boxId = 31027;
      break;
    case 25054:
      boxId = 31028;
      break;
    case 25126:
      boxId = 31029;
      break;
    case 25220:
      boxId = 31030;
    }

    if (boxId != 0)
    {
      NpcTemplate boxTemplate = NpcHolder.getInstance().getTemplate(boxId);
      if (boxTemplate != null)
      {
        NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
        box.spawnMe(getLoc());
        box.setSpawnedLoc(getLoc());

        ThreadPoolManager.getInstance().schedule(new GameObjectTasks.DeleteTask(box), 60000L);
      }
    }

    super.onDeath(killer);
  }

  private void calcRaidPointsReward(int totalPoints)
  {
    Map participants = new HashMap();
    double totalHp = getMaxHp();

    for (AggroList.HateInfo ai : getAggroList().getPlayableMap().values())
    {
      Player player = ai.attacker.getPlayer();
      Object key = player.getParty() != null ? player.getParty() : player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getPlayer();
      Object[] info = (Object[])participants.get(key);
      if (info == null)
      {
        info = new Object[] { new HashSet(), new Long(0L) };
        participants.put(key, info);
      }

      if ((key instanceof CommandChannel))
      {
        for (Player p : (CommandChannel)key)
          if (p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
            ((Set)info[0]).add(p);
      }
      else if ((key instanceof Party))
      {
        for (Player p : ((Party)key).getPartyMembers())
          if (p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
            ((Set)info[0]).add(p);
      }
      else {
        ((Set)info[0]).add(player);
      }
      info[1] = Long.valueOf(((Long)info[1]).longValue() + ai.damage);
    }

    for (Object[] groupInfo : participants.values())
    {
      Set players = (HashSet)groupInfo[0];

      perPlayer = (int)Math.round(totalPoints * ((Long)groupInfo[1]).longValue() / (totalHp * players.size()));
      for (Player player : players)
      {
        int playerReward = perPlayer;

        playerReward = (int)Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9.0D));
        if (playerReward == 0)
          continue;
        player.sendPacket(new SystemMessage(1725).addNumber(playerReward));
        RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
      }
    }
    int perPlayer;
    RaidBossSpawnManager.getInstance().updatePointsDb();
    RaidBossSpawnManager.getInstance().calculateRanking();
  }

  protected void onDecay()
  {
    super.onDecay();
    RaidBossSpawnManager.getInstance().onBossDespawned(this);
  }

  protected void onSpawn()
  {
    super.onSpawn();
    addSkill(SkillTable.getInstance().getInfo(4045, 1));
    RaidBossSpawnManager.getInstance().onBossSpawned(this);
  }

  public boolean isFearImmune()
  {
    return true;
  }

  public boolean isParalyzeImmune()
  {
    return true;
  }

  public boolean isLethalImmune()
  {
    return true;
  }

  public boolean hasRandomWalk()
  {
    return false;
  }

  public boolean canChampion()
  {
    return false;
  }

  private class MaintainKilledMinion extends RunnableImpl
  {
    private final MinionInstance minion;

    public MaintainKilledMinion(MinionInstance minion)
    {
      this.minion = minion;
    }

    public void runImpl()
      throws Exception
    {
      if (!isDead())
      {
        minion.refreshID();
        spawnMinion(minion);
      }
    }
  }
}