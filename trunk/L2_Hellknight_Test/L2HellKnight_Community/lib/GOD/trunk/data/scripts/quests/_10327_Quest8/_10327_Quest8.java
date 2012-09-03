package quests._10327_Quest8;

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
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.InstancedZoneManager.InstancedZone;
import quests._10326_Quest7._10326_Quest7;
import l2rt.util.GArray;
import l2rt.util.Location;

// -114692 243930 -7968
public class _10327_Quest8 extends Quest implements ScriptFile
{
	int PANTEON = 32972;
	int GALLINT = 32980;
	public void onLoad()
	{}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _10327_Quest8()
	{
		super(false);
		addStartNpc(PANTEON);
		addTalkId(PANTEON);
		addKillId(23121);
	}

	@Override
	public String onEvent(String event, QuestState st, L2NpcInstance npc)
	{
		String htmltext = event;
		L2Player player = st.getPlayer();
		Reflection r = player.getReflection();
		if(event.equalsIgnoreCase("3.htm"))
		{
			st.set("cond", "1", true);
			st.setState(STARTED);
			st.playSound(SOUND_ACCEPT);
		} 
		else if(event.equalsIgnoreCase("enter"))
		{
			st.set("cond", "2", true);
			enterInstance(player);
			L2NpcInstance mob1 = addSpawnToInstance(23121, new Location(-114456,244168,-7999), 0, r.getId()); 
			L2NpcInstance mob2 = addSpawnToInstance(23121, new Location(-114920,244168,-7999), 0, r.getId()); 
		} else if(event.equalsIgnoreCase("5.htm"))
		{
			player.sendPacket(new ExShowScreenMessage("Open the inventory and check accessories.", 1100)); //wtf rus??
			st.giveItems(57, 16000);
			st.giveItems(112, 2);
			st.addExpAndSp(7800, 3500, true);
			st.playSound(SOUND_FINISH);
			st.exitCurrentQuest(false);
			st.unset("kill");
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
		if(npcId == PANTEON)
		{
			QuestState qs = player.getQuestState(_10326_Quest7.class);
			if(cond == 0 && qs != null && qs.getState() == COMPLETED)
				htmltext = "1.htm";
			else if(cond == 3)
				htmltext = "4.htm";
		}
		else if(npcId == PANTEON)
		{
			if(cond == 1) 
				htmltext = "4.htm";
		}
		return htmltext;
	}
	
	private void enterInstance(L2Player player)
	{
		int instancedZoneId = 5001;
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
	
	@Override
	public String onKill(L2NpcInstance npc, QuestState st)
	{
		if(st.getState() != STARTED)
			return null;
		if(st.getInt("cond") == 2) {
			if(npc.getNpcId() == 23121) {
				st.set("kill", Integer.toString(st.getInt("kill") + 1));
				if (st.getInt("kill") == 2)
					st.set("cond", "3");
			}
		}

		
		return null;
	}

}
