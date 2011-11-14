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
package events.FreyaCelebration;

import com.l2js.gameserver.instancemanager.QuestManager;
import com.l2js.gameserver.model.L2Object;
import com.l2js.gameserver.model.L2Skill;
import com.l2js.gameserver.model.actor.L2Npc;
import com.l2js.gameserver.model.actor.instance.L2PcInstance;
import com.l2js.gameserver.model.quest.Quest;
import com.l2js.gameserver.model.quest.QuestState;
import com.l2js.gameserver.model.quest.State;
import com.l2js.gameserver.network.NpcStringId;
import com.l2js.gameserver.network.SystemMessageId;
import com.l2js.gameserver.network.clientpackets.Say2;
import com.l2js.gameserver.network.serverpackets.CreatureSay;
import com.l2js.gameserver.network.serverpackets.SystemMessage;
import com.l2js.gameserver.util.Util;
import com.l2js.util.Rnd;

/**
 ** @author Gnacik
 **
 ** Retail Event : 'Freya Celebration'
 */
public class FreyaCelebration extends Quest
{
	private static final int _freya = 13296;
	private static final int _freya_potion = 15440;
	private static final int _freya_gift = 17138;
	private static final int _hours = 20;
	
	private static final int[] _skills = { 9150, 9151, 9152, 9153, 9154, 9155, 9156 };
	
	private static final NpcStringId[] _freya_texts =
	{
		NpcStringId.EVEN_THOUGH_YOU_BRING_SOMETHING_CALLED_A_GIFT_AMONG_YOUR_HUMANS_IT_WOULD_JUST_BE_PROBLEMATIC_FOR_ME,
		NpcStringId.I_JUST_DONT_KNOW_WHAT_EXPRESSION_I_SHOULD_HAVE_IT_APPEARED_ON_ME_ARE_HUMANS_EMOTIONS_LIKE_THIS_FEELING,
		NpcStringId.THE_FEELING_OF_THANKS_IS_JUST_TOO_MUCH_DISTANT_MEMORY_FOR_ME,
		NpcStringId.BUT_I_KIND_OF_MISS_IT_LIKE_I_HAD_FELT_THIS_FEELING_BEFORE,
		NpcStringId.I_AM_ICE_QUEEN_FREYA_THIS_FEELING_AND_EMOTION_ARE_NOTHING_BUT_A_PART_OF_MELISSAA_MEMORIES
	};
	
	private static final int[][] _spawns = {
		{ -119494, 44882, 360, 24576 },
		{ -117239, 46842, 360, 49151 },
		{ -84023, 243051, -3728, 4096 },
		{ -84411, 244813, -3728, 57343 },
		{ 46908, 50856, -2992, 8192 },
		{ 45538, 48357, -3056, 18000 },
		{ -45372, -114104, -240, 16384 },
		{ -45278, -112766, -240, 0 },
		{ 9929, 16324, -4568, 62999 },
		{ 11546, 17599, -4584, 46900 },
		{ 115096, -178370, -880, 0 },
		{ -13727, 122117, -2984, 16384 },
		{ -14129, 123869, -3112, 40959 },
		{ -83156, 150994, -3120, 0 },
		{ -81031, 150038, -3040, 0 },
		{ 16111, 142850, -2696, 16000 },
		{ 17275, 145000, -3032, 25000 },
		{ 111004, 218928, -3536, 16384 },
		{ 81755, 146487, -3528, 32768 },
		{ 82145, 148609, -3464, 0 },
		{ 83037, 149324, -3464, 44000 },
		{ 81987, 53723, -1488, 0 },
		{ 147200, 25614, -2008, 16384 },
		{ 148557, 26806, -2200, 32768 },
		{ 147421, -55435, -2728, 49151 },
		{ 148206, -55786, -2776, 61439 },
		{ 85584, -142490, -1336, 0 },
		{ 86865, -142915, -1336, 26000 },
		{ 43966, -47709, -792, 49999 },
		{ 43165, -48461, -792, 17000 }
	};
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		Quest q = QuestManager.getInstance().getQuest(getName());
		if (st == null || q == null)
			return null;
		
		if (event.equalsIgnoreCase("give_potion"))
		{
			if (st.getQuestItemsCount(57) > 1)
			{
				long _curr_time = System.currentTimeMillis();
				String value = q.loadGlobalQuestVar(player.getAccountName());
				long _reuse_time = value == "" ? 0 : Long.parseLong(value);
				
				if (_curr_time > _reuse_time)
				{
					st.setState(State.STARTED);
					st.takeItems(57, 1);
					st.giveItems(_freya_potion, 1);
					q.saveGlobalQuestVar(player.getAccountName(), Long.toString(System.currentTimeMillis() + (_hours * 3600000)));
				}
				else
				{
					long remainingTime = (_reuse_time - System.currentTimeMillis()) / 1000;
					int hours = (int) (remainingTime / 3600);
					int minutes = (int) ((remainingTime % 3600) / 60);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AVAILABLE_AFTER_S1_S2_HOURS_S3_MINUTES);
					sm.addItemName(_freya_potion);
					sm.addNumber(hours);
					sm.addNumber(minutes);
					player.sendPacket(sm);
				}
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_UNIT_OF_THE_ITEM_S1_REQUIRED);
				sm.addItemName(57);
				sm.addNumber(1);
				player.sendPacket(sm);
			}
		}
		return null;
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if ((caster == null) || (npc == null))
			return null;
		
		if ((npc.getNpcId() == _freya) && Util.contains(targets, npc) && Util.contains(_skills, skill.getId()))
		{
			if (Rnd.get(100) < 5)
			{
				CreatureSay cs = new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), NpcStringId.DEAR_S1_THINK_OF_THIS_AS_MY_APPRECIATION_FOR_THE_GIFT_TAKE_THIS_WITH_YOU_THERES_NOTHING_STRANGE_ABOUT_IT_ITS_JUST_A_BIT_OF_MY_CAPRICIOUSNESS);
				cs.addStringParameter(caster.getName());

				npc.broadcastPacket(cs);

				caster.addItem("FreyaCelebration", _freya_gift, 1, npc, true);
			}
			else
			{
				if (Rnd.get(10) < 2)
				{
					npc.broadcastPacket(new CreatureSay(npc.getObjectId(), Say2.ALL, npc.getName(), _freya_texts[Rnd.get(_freya_texts.length - 1)]));
				}
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		return "13296.htm";
	}
	
	public FreyaCelebration(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		addStartNpc(_freya);
		addFirstTalkId(_freya);
		addTalkId(_freya);
		addSkillSeeId(_freya);
		for (int[] _spawn : _spawns)
		{
			addSpawn(_freya, _spawn[0], _spawn[1], _spawn[2], _spawn[3], false, 0);
		}
	}
	
	public static void main(String[] args)
	{
		new FreyaCelebration(-1, "FreyaCelebration", "events");
	}
}
