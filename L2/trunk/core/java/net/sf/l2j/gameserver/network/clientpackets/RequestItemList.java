package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public final class RequestItemList extends L2GameClientPacket
{
	private static final String _C__0F_REQUESTITEMLIST = "[C] 0F RequestItemList";

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		getClient().getActiveChar().cancelActiveTrade();
        if (getClient() != null && getClient().getActiveChar() != null && !getClient().getActiveChar().isInvetoryDisabled())
        {
    		ItemList il = new ItemList(getClient().getActiveChar(), true);
    		sendPacket(il);
        }
	}

	@Override
	public String getType()
	{
		return _C__0F_REQUESTITEMLIST;
	}
}
