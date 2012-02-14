package l2rt.gameserver.model.instances;

import javolution.util.FastMap;
import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.idfactory.IdFactory;
import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager;
import l2rt.gameserver.instancemanager.RaidBossSpawnManager.Status;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.tables.NpcTable;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class L2RaidBossInstance extends L2MonsterInstance
{
	protected static Logger _log = Logger.getLogger(L2RaidBossInstance.class.getName());

	private ScheduledFuture<?> minionMaintainTask;

	private static final int RAIDBOSS_MAINTENANCE_INTERVAL = 60000;
	private static final int MINION_UNSPAWN_INTERVAL = 5000; //time to unspawn minions when boss is dead, msec

	private RaidBossSpawnManager.Status _raidStatus;

	public L2RaidBossInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	protected int getMaintenanceInterval()
	{
		return RAIDBOSS_MAINTENANCE_INTERVAL;
	}

	protected int getMinionUnspawnInterval()
	{
		return MINION_UNSPAWN_INTERVAL;
	}

	protected int getKilledInterval(L2MinionInstance minion)
	{
		return 120000; //2 minutes to respawn
	}

	@Override
	public void notifyMinionDied(L2MinionInstance minion)
	{
		minionMaintainTask = ThreadPoolManager.getInstance().scheduleAi(new maintainKilledMinion(minion.getNpcId()), getKilledInterval(minion), false);
		super.notifyMinionDied(minion);
	}

	private class maintainKilledMinion implements Runnable
	{
		private int _minion;

		public maintainKilledMinion(int minion)
		{
			_minion = minion;
		}

		public void run()
		{
			try
			{
				if(!L2RaidBossInstance.this.isDead())
				{
					MinionList list = L2RaidBossInstance.this.getMinionList();
					if(list != null)
						list.spawnSingleMinionSync(_minion);
				}
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	static class DamagerInfo
	{
		double damage;
		GArray<String> skills = new GArray<String>();

		public DamagerInfo(double _damage)
		{
			damage = _damage;
		}

		public DamagerInfo()
		{
			this(0);
		}

		@Override
		public String toString()
		{
			String result = String.valueOf((int) damage);
			if(skills.size() > 0)
			{
				result += " | Skills: " + skills.removeFirst();
				for(String skill : skills)
					result += ", " + skill;
			}
			return result;
		}

		public String toTime()
		{
			return Util.formatTime((int) ((System.currentTimeMillis() - damage) / 1000));
		}
	}

	private final FastMap<String, DamagerInfo> lastDamagers = new FastMap<String, DamagerInfo>().setShared(true);

	@Override
	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill, boolean awake, boolean standUp, boolean directHp, boolean canReflect)
	{
		if(attacker == null || attacker.getPlayer() == null || (attacker == this || i > getMaxHp() / 10) && !attacker.getPlayer().isGM())
			return;
		String attackerName = attacker.getPlayer().getName();
		DamagerInfo di;
		synchronized (lastDamagers)
		{
			di = lastDamagers.get(attackerName);
			if(di == null)
			{
				di = new DamagerInfo();
				lastDamagers.put(attackerName, di);
			}
			di.damage += i;
			if(skill != null && !di.skills.contains(skill.getName()))
				di.skills.add(skill.getName());
			if(!lastDamagers.containsKey("@"))
				lastDamagers.put("@", new DamagerInfo(System.currentTimeMillis()));
		}
		super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect);
	}

	@Override
	public void doRegen()
	{
		super.doRegen();
		if(isInCombat() || !isCurrentHpFull() || lastDamagers.isEmpty())
			return;
		lastDamagers.clear();
	}

	@Override
	public void doDie(L2Character killer)
	{
		if(minionMaintainTask != null)
		{
			minionMaintainTask.cancel(false);
			minionMaintainTask = null;
		}

		int points = RaidBossSpawnManager.getInstance().getPoinstForRaid(getNpcId());
		if(points > 0)
			calcRaidPointsReward(points);

		if(this instanceof L2ReflectionBossInstance)
		{
			super.doDie(killer);
			return;
		}

		synchronized (lastDamagers)
		{
			String killTime = lastDamagers.containsKey("@") ? lastDamagers.remove("@").toTime() : "-";
			Log.add(PrintfFormat.LOG_BOSS_KILLED, new Object[] { getTypeName(), getName(), getNpcId(), killer, getX(),
					getY(), getZ(), killTime }, "bosses");
			for(String damagerName : lastDamagers.keySet())
				Log.add("\tDamager [" + damagerName + "] = " + lastDamagers.get(damagerName), "bosses");
			lastDamagers.clear();
		}

		if(killer.isPlayable())
		{
			L2Player player = killer.getPlayer();
			if(player.isInParty())
				player.getParty().broadcastToPartyMembers(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
			else
				player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);

			Quest q = QuestManager.getQuest(508);
			if(q != null)
			{
				String qn = q.getName();
				if(player.getClan() != null && player.getClan().getLeader().isOnline() && player.getClan().getLeader().getPlayer().getQuestState(qn) != null)
				{
					QuestState st = player.getClan().getLeader().getPlayer().getQuestState(qn);
					st.getQuest().onKill(this, st);
				}
			}
		}

		unspawnMinions();

		int boxId = 0;
		switch(getNpcId())
		{
			case 25035: // Shilens Messenger Cabrio
				boxId = 31027;
				break;
			case 25054: // Demon Kernon
				boxId = 31028;
				break;
			case 25126: // Golkonda, the Longhorn General
				boxId = 31029;
				break;
			case 25220: // Death Lord Hallate
				boxId = 31030;
				break;
		}

		if(boxId != 0)
		{
			L2NpcTemplate boxTemplate = NpcTable.getTemplate(boxId);
			if(boxTemplate != null)
			{
				final L2NpcInstance box = new L2NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
				box.onSpawn();
				box.spawnMe(getLoc());
				box.setSpawnedLoc(getLoc());

				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable(){
					public void run()
					{
						box.deleteMe();
					}
				}, 60000);
			}
		}

		super.doDie(killer);
		setRaidStatus(Status.DEAD);
	}

	@SuppressWarnings("unchecked")
	private void calcRaidPointsReward(int totalPoints)
	{
		// Object groupkey (L2Party/L2CommandChannel/L2Player) | [GArray<L2Player> group, Long GroupDdamage]
		HashMap<Object, Object[]> participants = new HashMap<Object, Object[]>();
		double totalHp = getMaxHp();

		// Разбиваем игроков по группам. По возможности используем наибольшую из доступных групп: Command Channel → Party → StandAlone (сам плюс пет :)
		for(AggroInfo ai : getAggroList())
		{
			L2Player player = ai.attacker.getPlayer();
			Object key = player.getParty() != null ? player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel() : player.getParty() : player.getPlayer();
			Object[] info = participants.get(key);
			if(info == null)
			{
				info = new Object[] { new HashSet<L2Player>(), new Long(0) };
				participants.put(key, info);
			}

			// если это пати или командный канал то берем оттуда весь список участвующих, даже тех кто не в аггролисте
			// дубликаты не страшны - это хашсет
			if(key instanceof L2CommandChannel)
			{
				for(L2Player p : ((L2CommandChannel) key).getMembers())
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						((HashSet<L2Player>) info[0]).add(p);
			}
			else if(key instanceof L2Party)
			{
				for(L2Player p : ((L2Party) key).getPartyMembers())
					if(p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
						((HashSet<L2Player>) info[0]).add(p);
			}
			else
				((HashSet<L2Player>) info[0]).add(player);

			info[1] = ((Long) info[1]).longValue() + ai.damage;
		}

		for(Object[] groupInfo : participants.values())
		{
			HashSet<L2Player> players = (HashSet<L2Player>) groupInfo[0];
			// это та часть, которую игрок заслужил дамагом группы, но на нее может быть наложен штраф от уровня игрока
			int perPlayer = (int) Math.round(totalPoints * ((Long) groupInfo[1]).longValue() / (totalHp * players.size()));
			for(L2Player player : players)
			{
				int playerReward = perPlayer;
				// применяем штраф если нужен
				playerReward = (int) Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player.getLevel()), 9));
				if(playerReward == 0)
					continue;
				player.sendPacket(new SystemMessage(SystemMessage.YOU_HAVE_EARNED_S1_RAID_POINTS).addNumber(playerReward));
				RaidBossSpawnManager.getInstance().addPoints(player.getObjectId(), getNpcId(), playerReward);
			}
		}

		RaidBossSpawnManager.getInstance().updatePointsDb();
		RaidBossSpawnManager.getInstance().calculateRanking();
	}

	@Override
	public void onDecay()
	{
		super.onDecay();
		RaidBossSpawnManager.getInstance().onBossDespawned(this);
	}

	public void unspawnMinions()
	{
		if(hasMinions())
			ThreadPoolManager.getInstance().scheduleAi(new Runnable(){
				public void run()
				{
					try
					{
						removeMinions();
					}
					catch(Throwable e)
					{
						_log.log(Level.SEVERE, "", e);
						e.printStackTrace();
					}
				}
			}, getMinionUnspawnInterval(), false);
	}

	@Override
	public void onSpawn()
	{
		addSkill(SkillTable.getInstance().getInfo(4045, 1)); // Resist Full Magic Attack
		RaidBossSpawnManager.getInstance().onBossSpawned(this);
		super.onSpawn();
	}

	public void setRaidStatus(RaidBossSpawnManager.Status status)
	{
		_raidStatus = status;
	}

	public RaidBossSpawnManager.Status getRaidStatus()
	{
		return _raidStatus;
	}

	@Override
	public boolean isFearImmune()
	{
		return true;
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return true;
	}

	@Override
	public boolean isLethalImmune()
	{
		return true;
	}

	@Override
	public boolean hasRandomWalk()
	{
		return false;
	}

	@Override
	public boolean canChampion()
	{
		return false;
	}
}