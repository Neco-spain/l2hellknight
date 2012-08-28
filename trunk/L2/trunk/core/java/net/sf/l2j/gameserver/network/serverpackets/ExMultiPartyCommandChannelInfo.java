package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2CommandChannel;
import net.sf.l2j.gameserver.model.L2Party;

/**
 * @author chris_00 ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private static final String _S__FE_31_EXMULTIPARTYCOMMANDCHANNELINFO = "[S] FE:31 ExMultiPartyCommandChannelInfo";
	private L2CommandChannel _channel;

	public ExMultiPartyCommandChannelInfo(L2CommandChannel channel)
	{
		_channel = channel;
	}

	@Override
	protected void writeImpl()
	{
		if (_channel == null) return;
		
		writeC(0xfe);
		writeH(0x30);
		
		writeS(_channel.getChannelLeader().getName()); // Channelowner
		writeD(0); // Channelloot 0 or 1
		writeD(_channel.getMemberCount());
		
		writeD(_channel.getPartys().size());
		for (L2Party p : _channel.getPartys())
		{
			writeS(p.getLeader().getName()); // Leadername
			writeD(p.getPartyLeaderOID()); // Leaders ObjId
			writeD(p.getMemberCount()); // Membercount
		}
	}
	
	@Override
	public String getType()
	{
		return _S__FE_31_EXMULTIPARTYCOMMANDCHANNELINFO;
	}
}
