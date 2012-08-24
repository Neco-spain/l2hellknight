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

import java.util.Calendar;
import java.util.List;

import javolution.util.FastList;

import l2.hellknight.Config;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.instancemanager.TownManager;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.zone.type.L2TownZone;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.util.Rnd;

/**
 * ¶Ä§L¶¤ªø
 * @author mrTJO
 * Mercenary Broadcast in the city a random message every hour
 * at hh:55 (Real time) * 
 */

public class MercenaryCaptain extends Quest
{
	private static final int CAPTAIN_GLUDIO     = 36481;  // Gludio
	private static final int CAPTAIN_DION       = 36482;  // Dion
	private static final int CAPTAIN_GIRAN      = 36483;  // Giran
	private static final int CAPTAIN_OREN       = 36484;  // Oren
	private static final int CAPTAIN_ADEN       = 36485;  // Aden
	private static final int CAPTAIN_INNADRIL   = 36486;  // Innadril
	private static final int CAPTAIN_GODDARD    = 36487;  // Goddard
	private static final int CAPTAIN_RUNE       = 36488;  // Rune
	private static final int CAPTAIN_SCHUTTGART = 36489;  // Schuttgart

	List<L2Npc> _npcs = new FastList<L2Npc>();

	private static final NpcStringId[] dlog = 
	{
		NpcStringId.COURAGE_AMBITION_PASSION_MERCENARIES_WHO_WANT_TO_REALIZE_THEIR_DREAM_OF_FIGHTING_IN_THE_TERRITORY_WAR_COME_TO_ME_FORTUNE_AND_GLORY_ARE_WAITING_FOR_YOU,
		NpcStringId.DO_YOU_WISH_TO_FIGHT_ARE_YOU_AFRAID_NO_MATTER_HOW_HARD_YOU_TRY_YOU_HAVE_NOWHERE_TO_RUN_BUT_IF_YOU_FACE_IT_HEAD_ON_OUR_MERCENARY_TROOP_WILL_HELP_YOU_OUT
	};

	public MercenaryCaptain(int questId, String name, String descr)
	{
		super(questId, name, descr);

		// Register Events
		addEventId(CAPTAIN_GLUDIO, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_DION, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_GIRAN, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_OREN, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_ADEN, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_INNADRIL, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_GODDARD, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_RUNE, Quest.QuestEventType.ON_SPAWN);
		addEventId(CAPTAIN_SCHUTTGART, Quest.QuestEventType.ON_SPAWN);

		// Schedule Broadcast for Shout
		scheduleBroadcast();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if (!_npcs.contains(npc))
			_npcs.add(npc);

		return super.onSpawn(npc);
	}

	private void scheduleBroadcast()
	{
		Calendar cal = Calendar.getInstance();
		int tfhTime = cal.get(Calendar.HOUR_OF_DAY);
		long currTime = tfhTime * 3600000 + cal.get(Calendar.MINUTE) * 60000 + cal.get(Calendar.SECOND) * 1000 + cal.get(Calendar.MILLISECOND);
		long nextTime = 0;
		if ((tfhTime + 1) > 24)
			nextTime = 6900000;
		else if (cal.get(Calendar.MINUTE) < 55)
			nextTime = tfhTime * 3600000 + 3300000;
		else
			nextTime = (tfhTime + 1) * 3600000 + 3300000;
		long initial = nextTime - currTime;
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new BroadcastToZone(), initial, 3600000L);
	}

	class BroadcastToZone implements Runnable
	{
		@Override
		public void run()
		{
			for (L2Npc npc : _npcs)
			{
				if (npc != null)
				{
					if (Config.DEBUG)
						_log.info("Broadcasting Mercenary Captain Message to Zone");
					int dg = Rnd.get(0, 1);
					NpcSay ns = new NpcSay(npc.getObjectId(), Say2.SHOUT, npc.getNpcId(), dlog[dg]);
					L2TownZone town = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ());
					{
						if (town.getCharactersInside() != null && !town.getCharactersInside().isEmpty())
						{
							for (L2Character obj : town.getCharactersInside())
							{
								if (obj == null)
									continue;
								if (obj instanceof L2PcInstance)
									obj.sendPacket(ns);
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new MercenaryCaptain(-1,"MercenaryCaptain","ai");
	}

}