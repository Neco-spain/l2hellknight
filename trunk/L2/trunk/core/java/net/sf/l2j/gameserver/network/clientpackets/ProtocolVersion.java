package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.gameserver.network.serverpackets.SendStatus;

public final class ProtocolVersion extends L2GameClientPacket
{
	private static final String _C__00_PROTOCOLVERSION = "[C] 00 ProtocolVersion";
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());

    private int _version;

	@Override
	protected void readImpl()
	{
		_version  = readH();
	}

	@Override
	protected void runImpl()
	{
		if (_version == 65534)
		{
            if (Config.DEBUG) _log.info("Ping received");
            getClient().closeNow();
		}
        else if (_version == 65533)
        {
			if(Config.RWHO_LOG && getClient().getConnection().getSocket().getInetAddress().getHostAddress().equals(Config.RWHO_CLIENT))
			{
				_log.warning(getClient().toString()+" remote status request");
				getClient().close(new SendStatus());
			}
        }
        else if (_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
        {
            _log.info("Client: "+getClient().toString()+" -> Protocol Revision: " + _version + " is invalid. Minimum is "+Config.MIN_PROTOCOL_REVISION+" and Maximum is "+Config.MAX_PROTOCOL_REVISION+" are supported. Closing connection.");
            _log.warning("Wrong Protocol Version "+_version);
            getClient().closeNow();
        }
        else
        {
        	if (Config.DEBUG)
        	{
        		_log.fine("Client Protocol Revision is ok: "+_version);
        	}

        	KeyPacket pk = new KeyPacket(getClient().enableCrypt());
        	getClient().sendPacket(pk);
        }
	}

	@Override
	public String getType()
	{
		return _C__00_PROTOCOLVERSION;
	}
}