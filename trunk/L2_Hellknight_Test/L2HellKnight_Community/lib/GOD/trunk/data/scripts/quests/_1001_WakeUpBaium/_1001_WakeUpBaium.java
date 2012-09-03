package quests._1001_WakeUpBaium;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.Location;
import bosses.BaiumManager;

public class _1001_WakeUpBaium extends Quest implements ScriptFile
{
	private static final int Baium = 29020;
	private static final int BaiumNpc = 29025;
	private static final int AngelicVortex = 31862;
	private static final int BloodedFabric = 4295;
	//private static final Location BAIUM_SPAWN_POSITION = new Location(116127, 17368, 10107, 35431);
	private static final Location TELEPORT_POSITION = new Location(113100, 14500, 10077);

	public void onLoad()
	{
		System.out.println("Loaded Quest: 1001: Wake Up Baium");
	}

	public void onReload()
	{}

	public void onShutdown()
	{}

	public _1001_WakeUpBaium()
	{
		super("Wake Up Baium", true);

		addStartNpc(BaiumNpc);
		addStartNpc(AngelicVortex);
	}

	@Override
	public synchronized String onTalk(L2NpcInstance npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == BaiumNpc)
		{
			if(st.getInt("ok") != 1)
			{
				st.exitCurrentQuest(true);
				return "Conditions are not right to wake up Baium!";
			}
			if(npc.isBusy())
				return "Baium is busy!";
			npc.setBusy(true);
			npc.setBusyMessage("Attending another player's request");
			Functions.npcSay(npc, "You call my name! Now you gonna die!");
			BaiumManager.spawnBaium(npc, st.getPlayer());
			return "You call my name! Now you gonna die!";
		}
		else if(npcId == AngelicVortex)
		{
			if(st.getQuestItemsCount(BloodedFabric) > 0)
			{
				L2NpcInstance baiumBoss = L2ObjectsStorage.getByNpcId(Baium);
				if(baiumBoss != null)
					return "<html><head><body>Angelic Vortex:<br>Baium is already woken up! You can't enter!</body></html>";
				L2NpcInstance isbaiumNpc = L2ObjectsStorage.getByNpcId(BaiumNpc);
				if(isbaiumNpc == null)
					return "<html><head><body>Angelic Vortex:<br>Baium now here is not present!</body></html>";
				st.takeItems(BloodedFabric, 1);
				st.getPlayer().teleToLocation(TELEPORT_POSITION);
				st.set("ok", "1");
				return "";
			}
			return "<html><head><body>Angelic Vortex:<br>You do not have enough items!</body></html>";
		}
		return null;
	}
}