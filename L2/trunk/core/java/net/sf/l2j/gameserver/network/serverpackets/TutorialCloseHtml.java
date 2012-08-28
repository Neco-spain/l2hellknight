package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket
{
	private static final String _S__a3_TUTORIALCLOSEHTML = "[S] a3 TutorialCloseHtml";

	@Override
	protected void writeImpl()
	{
		writeC(0xa3);
	}

	@Override
	public String getType()
	{
		return _S__a3_TUTORIALCLOSEHTML;
	}
} 
