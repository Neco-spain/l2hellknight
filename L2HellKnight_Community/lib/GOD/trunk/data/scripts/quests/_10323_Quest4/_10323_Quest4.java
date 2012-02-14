package quests._10323_Quest4;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.cache.Msg;
import javolution.util.FastMap;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.ExShowScreenMessage; 
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import quests._10322_Quest3._10322_Quest3;
import l2rt.util.GArray;
import l2rt.util.Location;

public class _10323_Quest4 extends Quest implements ScriptFile
{
	int IVEN = 33464;
	int XOLDEN = 33194;
	int OXRANA = 33021;
	int XAMON = 33193;
	int SHENON = 32974;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10323_Quest4()
	{
		super(false);
		addStartNpc(IVEN);
		addTalkId(XOLDEN);
		addTalkId(OXRANA);
		addTalkId(XAMON);
		addTalkId(SHENON);
		addKillId(22991);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1");
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		}
		if(event.equalsIgnoreCase("5.htm"))
		{
			st.set("cond", "4");
			st.set("cond", "5");
			if (!player.isMageClass()) {
				st.giveItems(2509,500);
			} else {
				st.giveItems(1835,500);
			}
			st.playSound(SOUND_ACCEPT);
		}
		if(event.equalsIgnoreCase("return"))
		{
			returnToAden(player);
		}
		else if(event.equalsIgnoreCase("9.htm"))
		{
			st.giveItems(57, 9000);
			st.addExpAndSp(300, 1500, true);
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(false);
			st.unset("kill");
			st.unset("kill2");
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2NpcInstance npc, QuestState st)
	{
		String htmltext = "noquest";
		int npcId = npc.getNpcId();
		int cond = st.getInt("cond");
		L2Player player = st.getPlayer();
		Reflection r = player.getReflection();
		if(npcId == IVEN)
		{
			QuestState qs = player.getQuestState(_10322_Quest3.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 1)
				htmltext = "3.htm";
		}
		else if(npcId == XOLDEN)
		{
			if(cond == 1) {
				st.set("cond", "2");
				st.set("kill", "0");
				enterInstance(player);
				return "";
			}
		}
		else if(npcId == OXRANA)
		{
			if(cond == 3) {
				htmltext = "4.htm";
			} else if (cond >= 4 && cond < 7)
			{
				htmltext = "6.htm";
				st.set("cond", "6");
				st.set("cond", "7");
				L2NpcInstance mob1 = addSpawnToInstance(22991, new Location(-114120,248376,-7902), 0, r.getId()); 
				L2NpcInstance mob2 = addSpawnToInstance(22991, new Location(-114520,248440,-7902), 0, r.getId()); 
				L2NpcInstance mob3 = addSpawnToInstance(22991, new Location(-115048,247784,-7902), 0, r.getId()); 
				L2NpcInstance mob4 = addSpawnToInstance(22991, new Location(-114440,247768,-7902), 0, r.getId()); 
			}
		}
		else if (npcId == XAMON)
		{
			if (cond == 8)
				htmltext = "7.htm";
		} else if (npcId == SHENON)
		{
			if (cond == 8)
				htmltext = "8.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;
		if(st.getInt("cond") == 2) {
			if(npc.getNpcId() == 22991) {
				st.set("kill", Integer.toString(st.getInt("kill") + 1));
				if (st.getInt("kill") == 4)
					st.set("cond", "3");
			}
		} else if (st.getInt("cond") >= 6)
		{
			if(npc.getNpcId() == 22991) {
				st.set("kill2", Integer.toString(st.getInt("kill2") + 1));
				if (st.getInt("kill2") == 4) {
					st.set("cond", "8");
				}
			}
		}

		
		return null;
	}
	
	private void enterInstance(L2Player player)
	{
		int instancedZoneId = 5000;
		InstancedZoneManager ilm = InstancedZoneManager.getInstance();
		FastMap<Integer, InstancedZone> ils = ilm.getById(instancedZoneId);
		
		if(ils == null)
		{
			player.sendPacket(Msg.SYSTEM_ERROR);
			return;
		}

		InstancedZone il = ils.get(0);

		assert il != null;

		Reflection r = new Reflection(il.getName());
		r.setInstancedZoneId(instancedZoneId);
		for(InstancedZone i : ils.values())
		{
			if(r.getReturnLoc() == null)
				r.setReturnLoc(i.getReturnCoords());
			if(r.getTeleportLoc() == null)
				r.setTeleportLoc(i.getTeleportCoords());
			r.FillSpawns(i.getSpawnsInfo());
		}

		player.setReflection(r);
		player.teleToLocation(il.getTeleportCoords());
		player.setVar("backCoords", r.getReturnLoc().toXYZString());
	}
	
	private void returnToAden(L2Player player)
	{
		Reflection r = player.getReflection();
		if(r.getReturnLoc() != null)
		    player.teleToLocation(r.getReturnLoc(), 0);
		else
		    player.setReflection(0);
		    player.unsetVar("backCoords");
	}
}
