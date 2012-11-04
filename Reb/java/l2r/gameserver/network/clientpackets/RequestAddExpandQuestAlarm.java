package l2r.gameserver.network.clientpackets;

import l2r.gameserver.instancemanager.QuestManager;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.network.serverpackets.ExQuestNpcLogList;

/**
 * @author VISTALL
 * @date 14:47/26.02.2011
 */
public class RequestAddExpandQuestAlarm extends L2GameClientPacket
{
	private int _questId;

	@Override
	protected void readImpl() throws Exception
	{
		_questId = readD();
	}

	@Override
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest quest = QuestManager.getQuest(_questId);
		if(quest == null)
			return;

		QuestState state = player.getQuestState(quest.getClass());
		if(state == null)
			return;

		player.sendPacket(new ExQuestNpcLogList(state));
	}
}
