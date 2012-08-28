//L2DDT
package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.quest.QuestState;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	private static final String _C__7b_REQUESTTUTORIALLINKHTML = "[C] 7b RequestTutorialLinkHtml";
	String _bypass;

	protected void readImpl()
	{
		_bypass = readS();
	}

	@Override
	protected void runImpl()
	{ 
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
			return;

		QuestState qs = player.getQuestState("255_Tutorial");
		if(qs != null)
			qs.getQuest().notifyEvent(_bypass, null, player);
	}

	@Override
	public String getType()
	{
		return _C__7b_REQUESTTUTORIALLINKHTML;
	}
} 
