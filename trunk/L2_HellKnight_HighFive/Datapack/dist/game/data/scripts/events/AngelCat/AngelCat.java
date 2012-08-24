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
package events.AngelCat;

import l2.hellknight.gameserver.instancemanager.GlobalVariablesManager;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 *
 */
public class AngelCat extends Quest
{
	private final int _angelCat = 4308;
	private final int _gift = 21726;
	private final int _amount = 1;
	
	private final int[][] _spawns =
	{
		{ 148557, 26745, -2200, 32768 },
		{ 147476, -55385, -2728, 49151 },
		{ 148188, -55849, -2776, 61439 }, 
		{ 85628, -142429, -1344, 0 },
		{ 86891, -142848, -1336, 26000 },
		{ 43908, -47714, -792, 49999 },
		{ 43104, -48456, -792, 17000 },
		{ 82002, 53771, -1488, 0 },
		{ 81086, 56062, -1552, 32768 },
		{ 81743, 146431, -3528, 32768 }, 
		{ 82984, 149343, -3464, 44000 },
		{ 82156, 148669, -3464, 0 },
		{ 16117, 142909, -2696, 16000 },
		{ 17307, 145044, -3040, 25000 },
		{ 111057, 218934, -3536, 16384 },
		{ -13777, 122114, -2984, 16384 },
		{ -14081, 123829, -3112, 40959 },
		{ -83131, 150956, -3120, 0 },
		{ -81006, 149991, -3040, 0 },
		{ -84034, 243107, -3728, 4096 },
		{ -84365, 244848, -3728, 57343 },
		{ 9926, 16263, -4568, 62999 },
		{ 11482, 17600, -4584, 46900 },
		{ -45299, -114093, -240, 16384 },
		{ -45272, -112700, -240, 0 },
		{ 46869, 50913, -2992, 8192 },
		{ 115100, -178306, -880, 0 },
		{ 147265, 25624, -2008, 16384 },
	};
	
	public AngelCat(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(_angelCat);
		addFirstTalkId(_angelCat);
		addTalkId(_angelCat);
		
		for (int[] spawn : _spawns)
		{
			addSpawn(_angelCat, spawn[0], spawn[1], spawn[2], spawn[3], false, 0);
		}
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String var = GlobalVariablesManager.getInstance().getStoredVariable("AngelCatGift-" + player.getAccountName());
		if (var == null)
		{
			player.addItem("AngelCat-Gift", _gift, _amount, npc, true);
			GlobalVariablesManager.getInstance().storeVariable("AngelCatGift-" + player.getAccountName(), String.valueOf(System.currentTimeMillis()));
		}
		else
		{
			player.sendPacket(SystemMessage.getSystemMessage(3289));
		}
		return super.onTalk(npc, player);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getNpcId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new AngelCat(-1, AngelCat.class.getSimpleName(), "events");
	}
}