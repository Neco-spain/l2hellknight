//L2DDT
package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private static final String _S__a2_TUTORIALENABLECLIENTEVENT = "[S] a2 TutorialEnableClientEvent";
	private int _eventId = 0;

	public TutorialEnableClientEvent(int event)
	{
		_eventId = event;
	}


	@Override
	protected void writeImpl()
	{
		writeC(0xa2);
		writeD(_eventId);
	}

	@Override
	public String getType()
	{
		return _S__a2_TUTORIALENABLECLIENTEVENT;
	}
} 
