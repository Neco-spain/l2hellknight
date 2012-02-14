package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.quest.QuestState;
import l2rt.util.GArray;

/**
 * format: h[dd]b
 */
public class QuestList extends L2GameServerPacket
{
	/**
	 * This text was wrote by XaKa
	 * QuestList packet structure:
	 * {
	 * 		1 byte - 0x80
	 * 		2 byte - Number of Quests
	 * 		for Quest in AvailibleQuests
	 * 		{
	 * 			4 byte - Quest ID
	 * 			4 byte - Quest Status
	 * 		}
	 * }
	 *
	 * NOTE: The following special constructs are true for the 4-byte Quest Status:
	 * If the most significant bit is 0, this means that no progress-step got skipped.
	 * In this case, merely passing the rank of the latest step gets the client to mark
	 * it as current and mark all previous steps as complete.
	 * If the most significant bit is 1, it means that some steps may have been skipped.
	 * In that case, each bit represents a quest step (max 30) with 0 indicating that it was
	 * skipped and 1 indicating that it either got completed or is currently active (the client
	 * will automatically assume the largest step as active and all smaller ones as completed).
	 * For example, the following bit sequences will yield the same results:
	 * 1000 0000 0000 0000 0000 0011 1111 1111: Indicates some steps may be skipped but each of
	 * the first 10 steps did not get skipped and current step is the 10th.
	 * 0000 0000 0000 0000 0000 0000 0000 1010: Indicates that no steps were skipped and current is the 10th.
	 * It is speculated that the latter will be processed faster by the client, so it is preferred when no
	 * steps have been skipped.
	 * However, the sequence "1000 0000 0000 0000 0000 0010 1101 1111" indicates that the current step is
	 * the 10th but the 6th and 9th are not to be shown at all (not completed, either).
	 */

	private GArray<int[]> questlist = new GArray<int[]>();
	private static byte[] unk = new byte[128];

	public QuestList(L2Player player)
	{
		if(player == null)
			return;
		for(QuestState quest : player.getAllQuestsStates())
			if(quest != null && ((quest.getQuest().getQuestIntId() < 999 || quest.getQuest().getQuestIntId() > 10000) && quest.getQuest().getQuestIntId() != 255) && quest.isStarted())
				questlist.add(new int[] { quest.getQuest().getQuestIntId(), quest.getRawInt("cond") }); // stage of quest progress
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x86);
		writeH(questlist.size());
		for(int[] q : questlist)
		{
			writeD(q[0]);
			writeD(q[1]);
		}
		writeB(unk);
	}
}