package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	// format: cd
	int _number = 0;

	@Override
	public void readImpl()
	{
		_number = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player player = getClient().getActiveChar();
		if(player == null)
			return;

		Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getName(), "QM" + _number, null);
	}
}