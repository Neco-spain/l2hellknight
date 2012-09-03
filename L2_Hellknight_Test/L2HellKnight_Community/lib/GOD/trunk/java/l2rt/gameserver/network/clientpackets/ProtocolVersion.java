package l2rt.gameserver.network.clientpackets;

import l2rt.Config;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.KeyPacket;
import l2rt.util.HWID;
import l2rt.util.HWID.HardwareID;
import l2rt.util.Log;
import l2rt.util.Protection;

import java.util.logging.Logger;

/**
 * packet type id 0x0E
 * format:	cdbd
 */
public class ProtocolVersion extends L2GameClientPacket
{
	static Logger _log = Logger.getLogger(ProtocolVersion.class.getName());

	private static final KeyPacket wrong_protocol = new KeyPacket(null);
	private KeyPacket pk;
	private long _version;
	private byte[] _data = new byte[256];
	private static final byte[] _xorB = { (byte) 0x4C, (byte) 0x32, (byte) 0x52, (byte) 0x2D, (byte) 0x44, (byte) 0x52,
			(byte) 0x69, (byte) 0x4E };

	@Override
	public void readImpl()
	{
		L2GameClient _client = getClient();
		if(_buf.remaining() < 4)
		{
			// Проверки рейтинга типа l2top.in.ua
			_client.close(wrong_protocol);
			return;
		}
		_version = readD();
		if(_version == -2 || _buf.remaining() == 0)
		{
			_client.close(wrong_protocol);
			return;
		}
		if(_buf.remaining() < _data.length)
		{
			if(_client.protect_used)
			{
				_log.info("Filed read ProtocolVersion [" + _buf.remaining() + "]! May be BOT or unprotected client! Client IP: " + _client.getIpAddr());
				_version = -2; // что бы не выполнялся runImpl
				_client.close(wrong_protocol);
			}
		}
		else
		{
			readB(_data);
			if(_buf.remaining() >= 4)
				readD(); //?
		}
		pk = new KeyPacket(_client.enableCrypt());
	}

	@Override
	public void runImpl()
	{
		if(_version == -2)
			return;
		L2GameClient _client = getClient();
		if(_version < Config.MIN_PROTOCOL_REVISION || _version > Config.MAX_PROTOCOL_REVISION)
		{
			_log.info("Client Protocol Revision: " + _version + ", client IP: " + _client.getIpAddr() + " not allowed. Supported protocols: from " + Config.MIN_PROTOCOL_REVISION + " to " + Config.MAX_PROTOCOL_REVISION + ". Closing connection.");
			_client.close(wrong_protocol);
			return;
		}
		_client.setRevision((int) _version);

		for(int i = 0; i < 256; i++)
			_data[i] ^= _xorB[i & 7];
		_client.client_lang = _data[255];
		if(_client.client_lang != 0 && _client.client_lang != 1)
			_client.client_lang = -1;
		if(_client.protect_used)
		{
			if(_client.client_lang == -1)
			{
				Log.add("ProtocolVersion lang wrong! May be BOT or unprotected client! Client IP: " + _client.getIpAddr(), "protect");
				_client.close(wrong_protocol);
				return;
			}
			String hwid = Protection.ExtractHWID(_data);
			if(hwid.isEmpty())
			{
				Log.add("ProtocolVersion CRC Check Fail! May be BOT or unprotected client! Client IP: " + _client.getIpAddr(), "protect");
				_client.close(wrong_protocol);
				return;
			}
			_client.HWID = new HardwareID(hwid);
			if(HWID.checkHWIDBanned(_client.HWID))
			{
				Log.add("Kicking banned HWID: " + _client.HWID + " Client IP: " + _client.getIpAddr(), "protect");
				_client.close(wrong_protocol);
				return;
			}
		}
		sendPacket(pk);
	}
}