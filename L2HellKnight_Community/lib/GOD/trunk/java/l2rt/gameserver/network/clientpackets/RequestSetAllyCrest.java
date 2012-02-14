package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.CrestCache;
import l2rt.gameserver.model.L2Alliance;
import l2rt.gameserver.model.L2Player;

import java.util.logging.Logger;

public class RequestSetAllyCrest extends L2GameClientPacket
{
	// format: cdb
	static Logger _log = Logger.getLogger(RequestSetPledgeCrest.class.getName());

	private int _length;
	private byte[] _data;

	@Override
	public void readImpl()
	{
		_length = readD();
		if(_length > _buf.remaining() || _length > Short.MAX_VALUE || _length < 0)
		{
			_log.warning("Possibly server crushing packet: [C] 91 RequestSetAllyCrest with length " + _length);
			_buf.clear();
			return;
		}
		_data = new byte[_length];
		readB(_data);
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		L2Alliance ally = activeChar.getAlliance();
		if(ally != null && activeChar.isAllyLeader())
		{
			if(ally.hasAllyCrest())
				CrestCache.removeAllyCrest(ally);

			if(_data.length != 0)
				CrestCache.saveAllyCrest(ally, _data);

			ally.broadcastAllyStatus(false);
		}
	}
}