/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q512_BladeUnderFoot;

import gnu.trove.map.hash.TIntObjectHashMap;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.instancemanager.FortManager;
import l2.hellknight.gameserver.instancemanager.GlobalVariablesManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Playable;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.actor.instance.L2RaidBossInstance;
import l2.hellknight.gameserver.model.entity.Castle;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.model.holders.SkillHolder;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import java.lang.Math;
import java.util.Arrays;

public final class Q512_BladeUnderFoot extends Quest
{

	private class CAUWorld extends InstanceWorld
	{		
	}
	
	public static class CastleDungeon
	{
		private final int INSTANCEID;
		private final int _wardenId;
		
		public CastleDungeon(int iId, int wardenId)
		{
			INSTANCEID = iId;
			_wardenId = wardenId;
		}
		
		public int getInstanceId()
		{
			return INSTANCEID;
		}
		
		public long getReEnterTime()
		{
			String tmp = GlobalVariablesManager.getInstance().getStoredVariable ("Castle_dungeon_" + Integer.toString(_wardenId));
						
			return tmp == null ? 0 : Long.parseLong(tmp);
		}
		
		public void setReEnterTime(long time)
		{
			GlobalVariablesManager.getInstance().storeVariable("Castle_dungeon_" + Integer.toString(_wardenId), Long.toString(time));
		}
	}
	
	private static final String qn = "Q512_BladeUnderFoot";
	private static final boolean debug = false;
	private static final boolean CHECK_FOR_CONTRACT = true;
	private static final long REENTER_INTERVAL = 14400000;
	private static final long RAID_SPAWN_DELAY = 120000;
	
	private TIntObjectHashMap<CastleDungeon> _castleDungeons = new TIntObjectHashMap<CastleDungeon>(21);
	
	// QUEST ITEMS
	private static final int DL_MARK = 9797;

	// REWARDS
	private static final int KNIGHT_EPALUETTE = 9912;

	// MONSTER TO KILL -- Only last 3 Raids (lvl ordered) give DL_MARK
	private static final int[] RAIDS1 = { 25546, 25549, 25552 };
	private static final int[] RAIDS2 = { 25553, 25554, 25557, 25560 };
	private static final int[] RAIDS3 = { 25563, 25566, 25569 };
	private static final int[] SINGLE_REWARD_AMOUNT = { 175, 176, 177 };
	private static final int[] PARTY_REWARD_AMOUNT = { 1443, 1447, 1450 };
	
	private static final SkillHolder RAID_CURSE = new SkillHolder(5456, 1);
	
	private String checkConditions(L2PcInstance player)
	{
		if (debug)
			return null;
		L2Party party = player.getParty();
		if (party == null)
			return "CastleWarden-03.htm";
		if (party.getLeader() != player)
			return getHtm(player.getHtmlPrefix(), "CastleWarden-04.htm").replace("%leader%", party.getLeader().getName());
		for (L2PcInstance partyMember : party.getMembers())
		{
			QuestState st = partyMember.getQuestState(qn);
			if (st == null || st.getInt("cond") < 1)
				return getHtm(player.getHtmlPrefix(), "CastleWarden-05.htm").replace("%player%", partyMember.getName());
			if (!Util.checkIfInRange(1000, player, partyMember, true))
				return getHtm(player.getHtmlPrefix(), "CastleWarden-06.htm").replace("%player%", partyMember.getName());
		}
		return null;
	}

	private void teleportPlayer(L2PcInstance player, int[] coords, int instanceId)
	{
		player.setInstanceId(instanceId);
		player.teleToLocation(coords[0], coords[1], coords[2]);
	}

	protected String enterInstance(L2PcInstance player, String template, int[] coords, CastleDungeon dungeon, String ret)
	{
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if (world != null)
		{
			if (!(world instanceof CAUWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return "";
			}
			teleportPlayer(player, coords, world.instanceId);
			return "";
		}
		//New instance
		if (ret != null)
			return ret;
		ret = checkConditions(player);
		if (ret != null)
			return ret;
		L2Party party = player.getParty();
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		Instance ins = InstanceManager.getInstance().getInstance(instanceId);
		ins.setSpawnLoc(new int[]{player.getX(),player.getY(),player.getZ()});
		world = new CAUWorld();
		world.instanceId = instanceId;
		world.templateId = dungeon.getInstanceId();
		world.status = 0;
		dungeon.setReEnterTime(System.currentTimeMillis() + REENTER_INTERVAL);
		InstanceManager.getInstance().addWorld(world);
		_log.info("Castle BladeUnderFoot started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid((CAUWorld) world), RAID_SPAWN_DELAY);

		// teleport players
		if (player.getParty() == null)
		{
			teleportPlayer(player, coords, instanceId);
			world.allowed.add(player.getObjectId());
		}
		else
		{
			for (L2PcInstance partyMember : party.getMembers())
			{
				teleportPlayer(partyMember, coords, instanceId);
				world.allowed.add(partyMember.getObjectId());
				if (partyMember.getQuestState(qn) == null)
					newQuestState(partyMember);
			}
		}
		return getHtm(player.getHtmlPrefix(), "CastleWarden-08.htm").replace("%clan%", player.getClan().getName());
	}
	
	private class spawnRaid  implements Runnable
	{
		private CAUWorld _world;
		
		public spawnRaid(CAUWorld world)
		{
			_world = world;
		}
		
		@Override
		public void run()
		{
			try
			{
				int spawnId;
				if (_world.status == 0)
					spawnId = RAIDS1[Rnd.get(RAIDS1.length)];
				else if (_world.status == 1)
					spawnId = RAIDS2[Rnd.get(RAIDS2.length)];
				else
					spawnId = RAIDS3[Rnd.get(RAIDS3.length)];
				L2Npc raid = addSpawn(spawnId,11793,-49190,-3008,0,false,0,false, _world.instanceId);
				if (raid instanceof L2RaidBossInstance)
					((L2RaidBossInstance)raid).setUseRaidCurse(false);
			}
			catch (Exception e)
			{
				_log.warning("Castle BladeUnderFoot Raid Spawn error: " + e);
			}
		}
	}
	
	private String checkCastleCondition(L2PcInstance player, L2Npc npc, boolean isEnter)
	{
		Castle castle = npc.getCastle();
		CastleDungeon dungeon = _castleDungeons.get(npc.getNpcId());
		boolean haveContract = false;
		
		if (player == null || castle == null || dungeon == null)
			return "CastleWarden-01.htm";
        if (player.getClan() == null || player.getClan().getCastleId() != castle.getCastleId())
			return "CastleWarden-01.htm";

		if (CHECK_FOR_CONTRACT)
		{
			for (Fort fort : FortManager.getInstance().getForts())
			{
				if (fort.getCastleId() == castle.getCastleId())
				{
					haveContract = true;
					break;
				}
			}
		
			if (isEnter && !haveContract)
				return "CastleWarden-19.htm";
		}

		if (isEnter && castle.getSiege().getIsInProgress() || castle.getSiegeDate().getTimeInMillis() - System.currentTimeMillis() <= 7200000)
			return "CastleWarden-18.htm";
		if (isEnter && dungeon.getReEnterTime() > System.currentTimeMillis())
			return "CastleWarden-07.htm";

		return null;
	}
	
	private void rewardPlayer(L2PcInstance player, int npcId)
	{
		int idx = Arrays.binarySearch(RAIDS3, npcId);
		if (idx >= 0)
		{
			QuestState st = player.getQuestState(qn);
			if (st.getInt("cond") == 1)
			{ 
				st.giveItems(DL_MARK, SINGLE_REWARD_AMOUNT[idx]);
				st.playSound("ItemSound.quest_itemget");
			}
		}
	}

	private void rewardParty(L2Party party, int npcId)
	{
		int idx = Arrays.binarySearch(RAIDS3, npcId);
		if (idx >= 0)
		{
			int count = (int) Math.floor(PARTY_REWARD_AMOUNT[idx] / party.getMemberCount());
			
			for (L2PcInstance player : party.getMembers())
			{
				QuestState st = player.getQuestState(qn);
				if (st.getInt("cond") == 1)
				{
					st.giveItems(DL_MARK, count);
					st.playSound("ItemSound.quest_itemget");
				}
			}
		}
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("enter"))
		{
			int[] tele = new int[3];
			tele[0] = 12188;
			tele[1] = -48770;
			tele[2] = -3008;
			return enterInstance(player, "castledungeon.xml", tele, _castleDungeons.get(npc.getNpcId()), checkCastleCondition(player, npc, true));
		}
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);

		int cond = st.getInt("cond");
		if (event.equalsIgnoreCase("CastleWarden-10.htm"))
		{
			if (cond == 0)
			{
				st.set("cond","1");
				st.setState(State.STARTED);
				st.playSound("ItemSound.quest_accept");
			}
		}
		else if (event.equalsIgnoreCase("CastleWarden-15.htm"))
		{
			st.playSound("ItemSound.quest_finish");
			st.exitQuest(true);
		}
		return htmltext;
	}

	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
	{
		String htmltext = Quest.getNoQuestMsg(player);
		QuestState st = player.getQuestState(qn);
		String ret = checkCastleCondition(player, npc, false);
		if (ret != null)
			return ret;
		else if (st != null)
		{
			int npcId = npc.getNpcId();
			int cond = 0;
			if (st.getState() == State.CREATED)
				st.set("cond","0");
			else
				cond = st.getInt("cond");
			if (_castleDungeons.containsKey(npcId) && cond == 0)
			{
				if (player.getLevel() >= 70)
					htmltext = "CastleWarden-09.htm";
				else
				{
					htmltext = "CastleWarden-00.htm";
					st.exitQuest(true);
				}
			}
			else if (_castleDungeons.containsKey(npcId) && cond > 0 && st.getState() == State.STARTED)
			{
				long count = st.getQuestItemsCount(DL_MARK);
				if (cond == 1 && count > 0)
				{
					htmltext = "CastleWarden-14.htm";
					st.takeItems(DL_MARK,count);
					st.rewardItems(KNIGHT_EPALUETTE, count);
				}
				else if (cond == 1 && count == 0)
					htmltext = "CastleWarden-10a.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(L2Npc npc,L2PcInstance player, int damage, boolean isPet)
	{
		L2Playable attacker = (isPet ? player.getPet() : player);
		if (attacker.getLevel() - npc.getLevel() >= 9)
		{
			if (attacker.getBuffCount() > 0 || attacker.getDanceCount() > 0)
			{
				npc.setTarget(attacker);
				npc.doSimultaneousCast(RAID_CURSE.getSkill());
			}
			else if (player.getParty() != null)
				for(L2PcInstance pmember : player.getParty().getMembers())
				{
					if (pmember.getBuffCount() > 0 || pmember.getDanceCount() > 0)
					{
						npc.setTarget(pmember);
						npc.doSimultaneousCast(RAID_CURSE.getSkill());
					}
				}
		}
		return super.onAttack(npc, player, damage, isPet);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpworld instanceof CAUWorld)
		{
			CAUWorld world = (CAUWorld) tmpworld;
			if (Util.contains(RAIDS3, npc.getNpcId()))
			{
				if (player.getParty() != null)
					rewardParty(player.getParty(), npc.getNpcId());
				else
					rewardPlayer(player, npc.getNpcId());
				
				Instance instanceObj = InstanceManager.getInstance().getInstance(world.instanceId);
				instanceObj.setDuration(360000);
				instanceObj.removeNpcs();
			}
			else
			{
				world.status++;
				ThreadPoolManager.getInstance().scheduleGeneral(new spawnRaid(world), RAID_SPAWN_DELAY);
			}
		}
		return null;
	}

	public Q512_BladeUnderFoot(int questId, String name, String descr)
	{
		super(questId, name, descr);
		_castleDungeons.put(36403, new CastleDungeon(13, 36403));
		_castleDungeons.put(36404, new CastleDungeon(14, 36404));
		_castleDungeons.put(36405, new CastleDungeon(15, 36405));
		_castleDungeons.put(36406, new CastleDungeon(16, 36406));
		_castleDungeons.put(36407, new CastleDungeon(17, 36407));
		_castleDungeons.put(36408, new CastleDungeon(18, 36408));
		_castleDungeons.put(36409, new CastleDungeon(19, 36409));
		_castleDungeons.put(36410, new CastleDungeon(20, 36410));
		_castleDungeons.put(36411, new CastleDungeon(21, 36411));

		for(int i : _castleDungeons.keys())
		{
			addStartNpc(i);
			addTalkId(i);
		}
		
		for(int i : RAIDS1)
			addKillId(i);
		for(int i : RAIDS2)
			addKillId(i);
		for(int i : RAIDS3)
			addKillId(i);
		
		for(int i = 25572; i <= 25595; i++)
			addAttackId(i);
	}
	
	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new Q512_BladeUnderFoot(512,qn,"Blade Under Foot");
	}
}