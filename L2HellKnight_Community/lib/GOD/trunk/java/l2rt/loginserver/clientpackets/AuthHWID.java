package l2rt.loginserver.clientpackets;

import l2rt.loginserver.L2LoginClient;
import l2rt.util.Log;

public class AuthHWID extends L2LoginClientPacket
{
	private int[] _data = null;

	@Override
	public boolean readImpl()
	{
		if(getAvaliableBytes() >= 32)
		{
			_data = new int[8];
			for(int i = 0; i < 8; i++)
				_data[i] = readD();
			return true;
		}
		return false;
	}

	private String toHexStr(int i)
	{
		String ret = Integer.toHexString(i).toLowerCase();
		while(ret.length() < 8)
			ret = '0' + ret;
		return ret;
	}

	@Override
	public void runImpl()
	{
		L2LoginClient client = getClient();
		if(client.isProtectUsed())
		{
			if(_data == null)
			{
				Log.add("HWID data fail! May be BOT or unprotected client! Client IP: " + client.getIpAddress(), "protect");
				client.closeNow(true);
				return;
			}
			int[] hwid_data = new int[4];
			hwid_data[0] = _data[1] ^ _data[0] ^ 0x668B15D1;
			hwid_data[1] = _data[2] ^ _data[0] ^ 0x0B1116A7;
			hwid_data[2] = _data[3] ^ _data[0] ^ 0x1139EB20;
			hwid_data[3] = _data[4] ^ _data[0] ^ 0x24C2E990;

			//System.out.print(toHexStr(hwid_data[0]) + toHexStr(hwid_data[1]) + toHexStr(hwid_data[2]) + toHexStr(hwid_data[3]));

			if((_data[1] ^ _data[2] ^ _data[3] ^ _data[4] ^ _data[6] ^ _data[7]) != 0x37CA306A)
			{
				Log.add("HWID CRC Level 2 Check Fail! May be BOT or unprotected client! Client IP: " + client.getIpAddress(), "protect");
				client.closeNow(true);
				return;
			}

			if((hwid_data[0] ^ hwid_data[2] ^ _data[5] ^ _data[6]) != 0x40B80A6D)
			{
				Log.add("HWID CRC Level 1 Check Fail! May be BOT or unprotected client! Client IP: " + client.getIpAddress(), "protect");
				client.closeNow(true);
				return;
			}

			if((hwid_data[1] ^ hwid_data[3] ^ _data[5] ^ _data[0]) != 0x208431C9)
			{
				Log.add("HWID CRC Level 0 Check Fail! May be BOT or unprotected client! Client IP: " + client.getIpAddress(), "protect");
				client.closeNow(true);
				return;
			}

			client.setHWID(toHexStr(hwid_data[0]) + toHexStr(hwid_data[1]) + toHexStr(hwid_data[2]) + toHexStr(hwid_data[3]));

			/*if(HWID.checkHWIDBanned(client.getHWID()))
			{
				Log.add("Kicking banned HWID: " + _client.HWID + " Client IP: " + _client.getIpAddr(), "protect");
				_client.close(wrong_protocol);
				return;
			}*/
		}
	}
}