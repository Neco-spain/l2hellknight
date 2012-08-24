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
package teleports.GrandBossTeleporters;

import l2.hellknight.Config;
import l2.hellknight.gameserver.datatables.DoorTable;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2GrandBossInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.zone.type.L2BossZone;

/**
 * @author Plim
 * Original python script by Emperorc
 */
public class GrandBossTeleporters extends Quest
{
	private static final int[] NPCs = 
	{ 
		13001, //Heart of Warding : Teleport into Lair of Antharas
		31859, //Teleportation Cubic : Teleport out of Lair of Antharas
		31384, //Gatekeeper of Fire Dragon : Opening some doors
		31385, //Heart of Volcano : Teleport into Lair of Valakas
		31540, //Watcher of Valakas Klein : Teleport into Hall of Flames
		31686, //Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
		31687, //Gatekeeper of Fire Dragon : Opens doors to Heart of Volcano
		31759 //Teleportation Cubic : Teleport out of Lair of Valakas
	};
	 
    private Quest valakasAI()
    {
        return QuestManager.getInstance().getQuest("valakas");
    }
    
    private Quest antharasAI()
    {
        return QuestManager.getInstance().getQuest("antharas");
    }
 
	private static int playerCount = 0;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
			st = newQuestState(player);
		
		if (st.hasQuestItems(7267))
		{
			// st.takeItems(7267, 1); // No longer consumed in h5
			player.teleToLocation(183813, -115157, -3303);
			st.set("allowEnter", "1");
		}
		else
			htmltext = "31540-06.htm";
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		
		if (st == null)
			return null;
		
		switch (npc.getNpcId())
		{
			case 13001:
				if (antharasAI() != null)
				{
					int status = GrandBossManager.getInstance().getBossStatus(29019);
					int statusW = GrandBossManager.getInstance().getBossStatus(29066);
					int statusN = GrandBossManager.getInstance().getBossStatus(29067);
					int statusS = GrandBossManager.getInstance().getBossStatus(29068);
					
					if (status == 2 || statusW == 2 || statusN == 2 || statusS == 2)
						htmltext = "13001-02.htm";
					
					else if (status == 3 || statusW == 3 || statusN == 3 || statusS == 3)
						htmltext = "13001-01.htm";
					
					else if (status == 0 || status == 1) //If entrance to see Antharas is unlocked (he is Dormant or Waiting)
					{
						if (st.hasQuestItems(3865))
						{
							// st.takeItems(3865, 1); // No longer consumed in h5
							L2BossZone zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
							
							if (zone != null)
								zone.allowPlayerEntry(player, 30);
							
							player.teleToLocation(179700 + getRandom(700), 113800 + getRandom(2100), -7709);
							
							if (status == 0)
							{
								L2GrandBossInstance antharas = GrandBossManager.getInstance().getBoss(29019);
								antharasAI().notifyEvent("waiting", antharas, player);
							}
						}
						
						else
							htmltext = "13001-03.htm";
					}
				}
				break;
			
			case 31859:
				player.teleToLocation(79800 + getRandom(600), 151200 + getRandom(1100), -3534);
				break;
			
			case 31385:
				if (valakasAI() != null)
				{
					int status = GrandBossManager.getInstance().getBossStatus(29028);
					
					if (status == 0 || status == 1)
					{
						if (playerCount >= 200)
							htmltext = "31385-03.htm";
						
						else if (st.getInt("allowEnter") == 1)
						{
							st.unset("allowEnter");
							L2BossZone zone = GrandBossManager.getInstance().getZone(212852, -114842, -1632);
							
							if (zone != null)
								zone.allowPlayerEntry(player, 30);
							
							player.teleToLocation(204328 + getRandom(600), -111874 + getRandom(600), 70);
							
							playerCount++;
							
							if (status == 0)
							{
								L2GrandBossInstance valakas = GrandBossManager.getInstance().getBoss(29028);
								valakasAI().startQuestTimer("1001", Config.Valakas_Wait_Time, valakas, null);
								GrandBossManager.getInstance().setBossStatus(29028, 1);
							}
						}
						
						else
							htmltext = "31385-04.htm";
					}
					else if (status == 2)
						htmltext = "31385-02.htm";
					else
						htmltext = "31385-01.htm";
				}
				
				else
					htmltext = "31385-01.htm";
				break;
			
			case 31384:
				DoorTable.getInstance().getDoor(24210004).openMe();
				break;
			
			case 31686:
				DoorTable.getInstance().getDoor(24210006).openMe();
				break;
			
			case 31687:
				DoorTable.getInstance().getDoor(24210005).openMe();
				break;
			
			case 31540:
				if (playerCount < 50)
					htmltext = "31540-01.htm";
				
				else if (playerCount < 100)
					htmltext = "31540-02.htm";
				
				else if (playerCount < 150)
					htmltext = "31540-03.htm";
				
				else if (playerCount < 200)
					htmltext = "31540-04.htm";
				
				else
					htmltext = "31540-05.htm";
				break;
				
			case 31759:
				player.teleToLocation(150037 + getRandom(500), -57720 + getRandom(500), -2976);
				break;
		}
		
		return htmltext;
	}
	
	public GrandBossTeleporters(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		for (int npcId : NPCs)
		{
			addStartNpc(npcId);
			addTalkId(npcId);
		}
	}
	
	public static void main(String[] args)
	{
		new GrandBossTeleporters(-1, GrandBossTeleporters.class.getSimpleName(), "teleports");
	}
}
