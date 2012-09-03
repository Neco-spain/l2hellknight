package l2rt.gameserver.network.clientpackets;

public class RequestLinkHtml extends L2GameClientPacket
{
	//Format: cS
	//private String _link;

	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
	/*
	L2Player actor = getClient().getActiveChar();
	if(actor == null)
		return;

	_link = readS();

	if(_link.contains("..") || !_link.contains(".htm"))
	{
		_log.warning("[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
		return;
	}

	NpcHtmlMessage msg = new NpcHtmlMessage(0);
	msg.setFile(_link);

	sendPacket(msg);
	*/
	}
}