package quests.Q504_CompetitionfortheBanditStronghold;

import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.instancemanager.CHSiegeManager;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.clanhall.SiegableHall;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.quest.State;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;
import l2.hellknight.gameserver.util.Util;

/**
 * @author BiggBoss
 */
public final class Q504_CompetitionfortheBanditStronghold extends Quest
{
	private static final String qn = 	"504_CompetitionfortheBanditStronghold";
	// Quest reward item
	private static final int TARLK_AMULET = 4332;
	private static final int TROPHY_OF_ALLIANCE = 5009;
	// Quest npc
	private static final int MESSENGER = 35437;
	private static final int[] MOBS = {20570,20571,20572,20573,20574};
	
	private static final SiegableHall BANDIT_STRONGHOLD = CHSiegeManager.getInstance().getSiegableHall(35);
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public Q504_CompetitionfortheBanditStronghold(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
		for(int mob : MOBS)
			addKillId(mob);
	}
	
	@Override
	public final String onTalk(L2Npc npc, L2PcInstance player)
	{
		String result = "azit_messenger_q0504_01.htm";
		QuestState st = player.getQuestState(qn);
		final L2Clan clan = player.getClan();
		
		if(st == null)
			result = getNoQuestMsg(player);
		else if(!BANDIT_STRONGHOLD.isWaitingBattle())
		{
			sendDatePage("azit_messenger_q0504_09.htm", player, npc);
			result = null;
		}
		else if(player.getClan() == null || player.getClan().getLevel() < 4)
			 result = "azit_messenger_q0504_04.htm";
		else if(!player.isClanLeader())
			 result = "azit_messenger_q0504_05.htm";
		else if(clan.getHasHideout() > 0 || clan.getHasFort() > 0 || clan.getHasCastle() > 0)
			result = "azit_messenger_q0504_10.htm";
		else
		{
			switch(st.getState())
			{
				case State.CREATED:
					if(BANDIT_STRONGHOLD.getSiege().getAttackers().size() >= 5)
						result = "35437-3.htm";
					else 
					{
						result = "azit_messenger_q0504_02.htm";
						st.setState(State.STARTED);
						st.set("cond", "1");
					}
					break;
				case State.STARTED:
					if(st.getQuestItemsCount(TARLK_AMULET) < 30)
						result = "azit_messenger_q0504_07.htm";
					else
					{
						st.takeItems(TARLK_AMULET, 30);
						st.rewardItems(TROPHY_OF_ALLIANCE, 1);
						st.exitQuest(true);
						result = "azit_messenger_q0504_08.htm";
					}
					break;
				case State.COMPLETED:
					result = "azit_messenger_q0504_07a.htm";
					break;
			}
		}
		return result;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!BANDIT_STRONGHOLD.isInSiege())
			return null;
		
		QuestState st = killer.getQuestState(qn);
		
		if(st == null)
			return null;
		
		if(!Util.contains(MOBS, npc.getNpcId()))
			return null;
		
		if(st.isStarted() && st.getInt("cond") == 1)
		{
			st.giveItems(TARLK_AMULET, 1);
			if(st.getQuestItemsCount(TARLK_AMULET) < 30)
				st.playSound("Itemsound.quest_itemget");
			else
			{
				st.playSound("Itemsound.quest_middle");
				st.set("cond", "2");
			}
		}
		
		return null;
	}
	
	private final void sendDatePage(final String page, final L2PcInstance player, final L2Npc npc)
	{
		String result = HtmCache.getInstance().getHtm(null, "data/scripts/quests/Q504_CompetitionfortheBanditStronghold/"+page+".htm");
		if(result != null)
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
			msg.setHtml(result);
			msg.replace("%nextSiege%", BANDIT_STRONGHOLD.getSiegeDate().getTime().toString());
			msg.replace("%objectId%", String.valueOf(npc.getObjectId()));
		
			player.sendPacket(msg);
		}
	}
	
	public static void main(String[] args)
	{
		new Q504_CompetitionfortheBanditStronghold(504, qn, "Right to Participate");
	}
}
