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
package ai.group_template;

import l2.hellknight.Config;
import l2.hellknight.ExternalConfig;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Fort;
import l2.hellknight.gameserver.model.entity.FortSiege;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

public class FortressReward extends Quest
{
	private static final int KE = 9912;
	
	private static final int[] ARCHER         = { 35789, 35822, 36167, 35853, 36203, 36241, 36279, 35889, 36136, 36029, 35720, 35753, 36065, 36103, 36312, 35922, 36348, 35958, 35996, 35684, 36386 };
	private static final int[] ARCHER_DROP    = { 4, 16, 71 };
	private static final int[] ARCHCAP        = { 36028, 35719, 35752, 36064, 36102, 36311, 35921, 36347, 35957, 35995, 35683, 36385, 35788, 35821, 36166, 35852, 36202, 36240, 36278, 35888, 36135 };
	private static final int[] ARCHCAP_DROP   = { 140, 300, 84 };
	private static final int[] CGUARD         = { 35673, 35705, 35811, 36156, 36050, 36088, 35842, 36125, 36333, 35943, 35981, 36018, 35742, 36371, 35774, 36226, 36264, 36301, 36188, 35911, 35874 };
	private static final int[] CGUARD_DROP    = { 4, 12, 66 };
	private static final int[] DECORATED      = { 35909, 35872, 35671, 35703, 35809, 36154, 36048, 36086, 35840, 36123, 36331, 35979, 36016, 35740, 36369, 35941, 35772, 36224, 36262, 36299, 36186 };
	private static final int[] DECORATED_DROP = { 4, 16, 78 };
	private static final int[] DGSERG         = { 35814, 36159, 35845, 36128, 36021, 35745, 36304, 35914, 35676 };
	private static final int[] DGSERG_DROP    = { 4, 12, 61 };
	private static final int[] SNOOP          = { 35711, 36056, 36094, 36339, 35949, 35987, 36377, 35780, 36232, 36270, 36194, 35880 };
	private static final int[] SNOOP_DROP     = { 24, 72, 2 };
	private static final int[] GENERAL        = { 36066, 36349, 35959, 35997, 36387, 35790, 36204, 36242, 36280, 35890, 35721 };
	private static final int[] GENERAL_DROP   = { 40, 154, 70 };	
	private static final int[] GUARD          = { 35944, 35982, 36019, 35743, 36372, 35775, 36227, 36265, 36302, 36189, 35912, 35875, 35674, 35706, 35812, 36157, 36051, 36089, 35843, 36126, 36334 };
	private static final int[] GUARD_DROP     = { 4, 12, 66 };
	private static final int[] EVILUS         = { 36198,  36236, 36274, 35848, 35884, 36131, 35784, 35679, 36098, 36060, 36162, 35817, 36307, 36343, 36381, 36162, 35953, 35917, 35991, 35748, 35715, 36024 };
	private static final int[] EVILUS_DROP    = { 4, 16, 73 };
	private static final int[] GUARDCAP       = { 36058, 36096, 36305, 35915, 36341, 35951, 35989, 35677, 36379, 35782, 35815, 36160, 36196, 36234, 36272, 35846, 35882, 36129, 36022, 35713, 35746 };
	private static final int[] GUARDCAP_DROP  = { 140, 300, 84 };
	private static final int[] HEALER         = { 36346, 35956, 35994, 35682, 36384, 35787, 35820, 36165, 35851, 36201, 36239, 36277, 35887, 36134, 36027, 35718, 35751, 36063, 36101, 36310, 35920 };
	private static final int[] HEALER_DROP    = { 4, 16, 82 };
	private static final int[] MINISTER       = { 36205, 36243, 36281, 35891, 35722, 36067, 36350, 35960, 35998, 36388, 35791 };
	private static final int[] MINISTER_DROP  = { 40, 154, 70 };	
	private static final int[] REBELCOM       = { 36122, 36330, 35978, 36015, 35739, 36368, 35940, 35771, 36223, 36261, 36298, 36185, 35908, 35871, 35670, 35702, 35808, 36153, 36047, 36085, 35839 };
	private static final int[] REBELCOM_DROP  = { 40, 200, 90 };
	private static final int[] REBELPRIV      = { 36332, 35980, 36017, 35741, 36370, 35942, 35773, 36225, 36263, 36300, 36187, 35910, 35873, 35672, 35704, 35810, 36155, 36049, 36087, 35841, 36124 };
	private static final int[] REBELPRIV_DROP = { 4, 12, 72 };
	private static final int[] SGOLEM         = { 35781, 36233, 36271, 36195, 35881, 35712, 36057, 36095, 36340, 35950, 35988, 36378 };
	private static final int[] SGOLEM_DROP    = { 4, 6, 46 };
	private static final int[] SBOX           = { 35665 };    
	private static final int[] SBOX_DROP      = { 40, 80, 100 };
	private static final int[] COURT          = { 35834 };
	private static final int[] COURT_DROP     = { 240, 480, 100 };
	private static final int[] FORT           = { 35803 };
	private static final int[] FORT_DROP      = { 200, 400, 100 };
	private static final int[] BOMB           = { 35766 };
	private static final int[] BOMB_DROP      = { 160, 320, 100 };
	private static final int[] BOOM           = { 35734 };
	private static final int[] BOOM_DROP      = { 120, 240, 100 };
	private static final int[] GANG           = { 35697 };
	private static final int[] GANG_DROP      = { 80, 160, 100 };
	private static final int[] SUPPORT        = { 36308, 35918, 36344, 35954, 35992, 35680, 36382, 35785, 35818, 36163, 36199, 36237, 36275, 35849, 35885, 36132, 36025, 35716, 35749, 36061, 36099 };
	private static final int[] SUPPORT_DROP   = { 140, 300, 84 };
	private static final int[] WIZARD         = { 36133, 36026, 35717, 35750, 36062, 36100, 36309, 35919, 36345, 35955, 35993, 35681, 36383, 35786, 35819, 36164, 35850, 36200, 36238, 36276, 35886 };
	private static final int[] WIZARD_DROP    = { 4, 16, 77 };
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}

		int npcid = npc.getNpcId();
		Fort fort = npc.getFort();
		L2Clan clan = player.getClan();
		if(fort != null && clan != null && fort.getOwnerClan() != clan)
		{
			FortSiege siege = fort.getSiege();
			if(siege != null && siege.checkIsAttacker(clan)) 
			{
			if(Util.contains(CGUARD, npcid))
				rewardPlayer(player, npc, CGUARD_DROP);
	 		else if (Util.contains(GUARD, npcid))
  				rewardPlayer(player, npc, GUARD_DROP);
			else if (Util.contains(ARCHER, npcid))
 				rewardPlayer(player, npc, ARCHER_DROP);
 			else if (Util.contains(HEALER, npcid))
				rewardPlayer(player, npc, HEALER_DROP);
			else if (Util.contains(WIZARD, npcid))
				rewardPlayer(player, npc, WIZARD_DROP);
	        else if (Util.contains(SUPPORT, npcid))
	            rewardPlayer(player, npc, SUPPORT_DROP);
	        else if (Util.contains(ARCHCAP, npcid))
	            rewardPlayer(player, npc, ARCHCAP_DROP);
 	        else if (Util.contains(GUARDCAP, npcid))
 	            rewardPlayer(player, npc, GUARDCAP_DROP);
	        else if (Util.contains(REBELPRIV, npcid))
	            rewardPlayer(player, npc, REBELPRIV_DROP);
	        else if (Util.contains(DECORATED, npcid))
	            rewardPlayer(player, npc, DECORATED_DROP);
	        else if (Util.contains(REBELCOM, npcid))
	            rewardPlayer(player, npc, REBELCOM_DROP);
	        else if (Util.contains(DGSERG, npcid))
	            rewardPlayer(player, npc, DGSERG_DROP);	
	        else if (Util.contains(GENERAL, npcid))
	            rewardPlayer(player, npc, GENERAL_DROP);
	        else if (Util.contains(MINISTER, npcid))
	            rewardPlayer(player, npc, MINISTER_DROP);
	        else if (Util.contains(SGOLEM, npcid))
	            rewardPlayer(player, npc, SGOLEM_DROP);
	        else if (Util.contains(SBOX, npcid))
	            rewardPlayer(player, npc, SBOX_DROP);
			else if (Util.contains(SNOOP, npcid))
	            rewardPlayer(player, npc, SNOOP_DROP);
			else if (Util.contains(EVILUS, npcid))
	            rewardPlayer(player, npc, EVILUS_DROP);
			else if (Util.contains(COURT, npcid))
	            rewardPlayer(player, npc, COURT_DROP);
			else if (Util.contains(FORT, npcid))
	            rewardPlayer(player, npc, FORT_DROP);
			else if (Util.contains(BOMB, npcid))
	            rewardPlayer(player, npc, BOMB_DROP);
			else if (Util.contains(BOOM, npcid))
	            rewardPlayer(player, npc, BOOM_DROP);	
			else if (Util.contains(GANG, npcid))
	            rewardPlayer(player, npc, GANG_DROP);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	private void rewardPlayer(L2PcInstance player, L2Npc npc,  int[] drop)
	{
		if(player == null || npc == null)
			return;
		
		QuestState st = player.getQuestState(getName());
		if (st == null)
			return;
		
		int chance = (drop[2] - (player.getLevel() - npc.getLevel()));

		if (Rnd.get(100) < chance)
        {
			if (player.getPremiumService() == 1) 
			{
                st.giveItems(KE,(long)(Rnd.get(drop[0],drop[1])*ExternalConfig.PREMIUM_RATE_DROP_ITEMS));
                st.playSound("ItemSound.quest_itemget");					
			}
			else
                st.giveItems(KE,(long)(Rnd.get(drop[0],drop[1])*Config.RATE_DROP_ITEMS));
                st.playSound("ItemSound.quest_itemget");
            }
		}   

	public FortressReward(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int court : CGUARD)
		  addKillId(court);
		for (int guard : GUARD)
		  addKillId(guard);
		for (int archer : ARCHER)
		  addKillId(archer);
		for (int healer : HEALER)
		  addKillId(healer);
		for (int wizard : WIZARD)
		  addKillId(wizard);
		for (int support : SUPPORT)
		  addKillId(support);
		for (int archcap : ARCHCAP)
		  addKillId(archcap);
		for (int guardcap : GUARDCAP)
		  addKillId(guardcap);
		for (int rebelpriv : REBELPRIV)
		  addKillId(rebelpriv);
		for (int decorated : DECORATED)
		  addKillId(decorated);
		for (int rebelcom : REBELCOM)
		  addKillId(rebelcom);
		for (int dgserg : DGSERG)
		  addKillId(dgserg);
		for (int general : GENERAL)
			addKillId(general);
		for (int minister : MINISTER)
			addKillId(minister);
		for (int golem : SGOLEM)
			addKillId(golem);
		for (int sbox : SBOX)
			addKillId(sbox);
	    for (int snoop : SNOOP)
			addKillId(snoop);
	    for (int evilus : EVILUS)
			addKillId(evilus);
	    for (int court : COURT)
			addKillId(court);
	    for (int fort : FORT)
			addKillId(fort);
	    for (int bomb : BOMB)
			addKillId(bomb);
	    for (int boom : BOOM)
			addKillId(boom);
	    for (int gang : GANG)
			addKillId(gang);
	}
	
	public static void main(String[] args)
	{
		new FortressReward(-1, "FortDrop", "addons");
	}
}