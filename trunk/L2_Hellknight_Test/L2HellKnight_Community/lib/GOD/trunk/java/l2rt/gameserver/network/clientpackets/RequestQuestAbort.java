package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.QuestManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.gameserver.network.serverpackets.SystemMessage;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _QuestID;

	/**
	 * packet type id 0x63
	 * format: cd
	 */
	@Override
	public void readImpl()
	{
		_QuestID = readD();
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null || QuestManager.getQuest(_QuestID) == null)
			return;
		QuestState qs = activeChar.getQuestState(QuestManager.getQuest(_QuestID).getName());
		if(qs != null)
		{
			qs.abortQuest();
			activeChar.sendPacket(new SystemMessage(SystemMessage.S1_IS_ABORTED).addString(QuestManager.getQuest(_QuestID).getDescr(activeChar)));
		}
	}
}