package zone_scripts.Hellbound.Natives;

import l2.brick.Config;
import l2.brick.gameserver.datatables.DoorTable;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2DoorInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.network.clientpackets.Say2;
import l2.brick.gameserver.network.serverpackets.NpcSay;

public class Natives extends Quest
{
	private static final int NATIVE = 32362;
	private static final int INSURGENT = 32363;
	private static final int TRAITOR = 32364;
	private static final int INCASTLE = 32357;
	private static final int MARK_OF_BETRAYAL = 9676;
	private static final int BADGES = 9674;
	
	private static final int[] FSTRING_ID = 
	{
		1800073, //Hun.. hungry
		1800111 //Alright, now Leodas is yours!
	};
	
	private static final int[] doors = { 19250003, 19250004 };
	
	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		int hellboundLevel = HellboundManager.getInstance().getLevel();
		int npcId = npc.getNpcId();
		
		if (npcId == NATIVE)
			return hellboundLevel > 5 ? "32362-01.htm" : "32362.htm"; 

		else if (npcId == INSURGENT)
			return hellboundLevel > 5 ? "32363-01.htm" : "32363.htm";
		
		else if (npcId == INCASTLE)
		{
			if (hellboundLevel < 9)
				return "32357-01a.htm";

			else if (hellboundLevel == 9)
				return "32357-01.htm";

			else
				return "32357-01b.htm";
		}
		
		return null;
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		if (npc.getNpcId() == TRAITOR)
		{
			if (event.equalsIgnoreCase("open_door"))
			{
				if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) >= 10)
				{
					if (player.destroyItemByItemId("Quest", MARK_OF_BETRAYAL, 10, npc, true))
					{
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), FSTRING_ID[1]));
						HellboundManager.getInstance().updateTrust(-50, true);

						for (int doorId : doors)
						{
							L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
							if (door != null)
								door.openMe();
						}
					
						cancelQuestTimers("close_doors");
						startQuestTimer("close_doors", 1800000, npc, player); //30 min
					} 
				}
			
				else if (player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) > 0 && player.getInventory().getInventoryItemCount(MARK_OF_BETRAYAL, -1, false) < 10)
					htmltext = "32364-01.htm";
			
				else
					htmltext = "32364-02.htm";
			}
			
			else if (event.equalsIgnoreCase("close_doors"))
			{
				for (int doorId : doors)
				{
					L2DoorInstance door = DoorTable.getInstance().getDoor(doorId);
					if (door != null)
						door.closeMe();
				}
			}
		}
		
		else if (npc.getNpcId() == NATIVE && event.equalsIgnoreCase("hungry_death"))
		{
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), Say2.ALL, npc.getNpcId(), FSTRING_ID[0]));
			npc.doDie(null);
		}
		
		else if (npc.getNpcId() == INCASTLE)
		{
		  if (event.equalsIgnoreCase("FreeSlaves"))
		  {
				if (player.getInventory().getInventoryItemCount(BADGES, -1, false) >= 5)
		  	{
					if (player.destroyItemByItemId("Quest", BADGES, 5, npc, true))
					{
						HellboundManager.getInstance().updateTrust(100, true);
						htmltext = "32357-02.htm";
						startQuestTimer("delete_me", 3000, npc, null);	
					}
				}
				else
					htmltext = "32357-02a.htm";
			}
			
			else if (event.equalsIgnoreCase("delete_me"))
			{
				npc.deleteMe();
				npc.getSpawn().decreaseCount(npc);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		if (npc.getNpcId() == NATIVE && HellboundManager.getInstance().getLevel() < 6)
			startQuestTimer("hungry_death", 600000, npc, null);
		
		return super.onSpawn(npc);
	}


	public Natives(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(NATIVE);
		addFirstTalkId(INSURGENT);
		addFirstTalkId(INCASTLE);
		addStartNpc(TRAITOR);
		addStartNpc(INCASTLE);
		addTalkId(TRAITOR);
		addTalkId(INCASTLE);
		addSpawnId(NATIVE);
	}

	public static void main(String[] args)
	{
		new Natives(-1, Natives.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Natives");
	}
}
