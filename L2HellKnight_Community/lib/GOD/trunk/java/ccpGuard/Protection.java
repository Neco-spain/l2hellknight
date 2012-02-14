package ccpGuard;


import ccpGuard.managers.HwidBan;
import ccpGuard.managers.HwidManager;
import ccpGuard.managers.ProtectManager;
import static ccpGuard.commons.utils.Util.verifyChecksum;
import static ccpGuard.commons.utils.Util.LastErrorConvertion;
import ccpGuard.commons.utils.Log;
import ccpGuard.packets.ServerTitleSpecial;


import java.nio.ByteBuffer;
import java.util.logging.Logger;

import l2rt.gameserver.handler.AdminCommandHandler;
import l2rt.gameserver.network.L2GameClient;
import l2rt.gameserver.network.serverpackets.ServerClose;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.network.serverpackets.NpcHtmlMessage;
import l2rt.gameserver.model.L2Player;


public final class Protection
{
	private static final int GGOPCODE = 0x77033077;
	private static final Logger _log = Logger.getLogger(Protection.class.getName());

	private static String _logFile = "protection_logs";
	private static String[] _positionName = {
			"Ingame bot",
			"Cheat client console CMD: <Enabled>",
			"Cheat client hooked connect (maybe l2phx)",
			"Cheat client hooked send (maybe l2phx)",
			"Cheat client hooked recv (maybe l2phx)",
			"Cheat client use L2Control",
			"Cheat client use L2ext",
			"Cheat client VEH detect",
			"Cheat client use L2ext hook AddPkt",
			"Unknow Soft"};

	public static boolean protect_use = false;

	public static void Init()
	{
		ConfigProtect.load();
		protect_use = ConfigProtect.PROTECT_ENABLE;
		if(protect_use)
		{
			_log.config("******************[ Protection System: Loading ]*******************");
			HwidBan.getInstance();
			HwidManager.getInstance();
			ProtectManager.getInstance();
			AdminCommandHandler.getInstance().registerAdminCommandHandler(new AdminHWID());
			_log.config("******************[ Protection System: Finish ]********************");
		}
	}

	public static boolean checkPlayerWithHWID(L2GameClient client, int playerID, String playerName)
	{
		if(!protect_use)
			return true;

		ProtectInfo pi = client._prot_info;
		pi.setPlayerName(playerName);
		pi.setPlayerId(playerID);

		if(ConfigProtect.PROTECT_ENABLE_HWID_LOCK)
		{
			if(HwidManager.checkLockedHWID(pi))
			{
				_log.info("An attempt to log in to locked character, " + pi.toString());
				client.close(new ServerClose());
				return false;
			}
		}
		if(ConfigProtect.PROTECT_WINDOWS_COUNT != 0)
		{
			final int count = ProtectManager.getInstance().getCountByHWID(pi.getHWID());
			if(count > ConfigProtect.PROTECT_WINDOWS_COUNT)
			{
				final int count2 = HwidManager.getAllowedWindowsCount(pi);
				if ( count2 > 0 )
				{
					if  (count > count2)
					{
						_log.info("Multi windows: " + pi.toString());
						client.close(new ServerClose());
						return false;
					}
				}
				else
				{
					client.close(new ServerClose());
					return false;
				}
			}
		}
		addPlayer(pi);
		return true;
	}

	public static int calcPenalty(byte[] data, ProtectInfo pi)
	{
		int sum = -1;
		if(verifyChecksum(data, 0, data.length))
		{
			ByteBuffer buf = ByteBuffer.wrap(data, 0, data.length - 4);
			int lenPenalty = (data.length - 4) / 4;
			sum = 0;
			int[] dump = new int[lenPenalty-9];
			int idx= 0;
			for(int i = 0; i < lenPenalty; i++)
			{
				int tmp = buf.getInt();
				if (i<9)
					sum += Protection.dumpData(tmp, i, pi);
				else
					dump[idx++] = tmp;
			}
			sum += Protection.dumpData2(dump, pi);
		}
		return sum;
	}

	public static int dumpData(int _id, int position, ProtectInfo pi)
	{
		int value = 0;
		position = position>8 ? 9 : position;
		boolean isIdZero = false;
		if(_id == 0)
		{
			isIdZero = true;
			Log.add("Cannot read DumpId|Target:" + _positionName[position] + "|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
		}

		switch(position)
		{
			case 0:
				//IG
				if(_id != 1435233386)
				{
					if(!isIdZero)
					{
						Log.add(_positionName[position] + " was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
					}
					value = ConfigProtect.PROTECT_PENALTY_IG;
				}
				break;
			case 1:
				//Console CMD
				if(_id != 16)
				{
					if(!isIdZero)
					{
						Log.add(_positionName[position] + " was found|"+ pi.toString()+ "|DEBUG INFO:" + _id, _logFile);
					}
					value = ConfigProtect.PROTECT_PENALTY_CONSOLE_CMD;
				}
				break;
			case 2:
			case 3:
			case 4:

				//check debuger (0xСС) or hook (0xE9)
				int code =  _id >> 24  & 0xFF;
				if(code == 0xCC)
				{
					Log.add("Attempts!!! Debuger was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
				}

				//L2phx (connect, send, recv)
				if(code == 0xE9)
				{
					Log.add(_positionName[position] + " was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
					value = ConfigProtect.PROTECT_PENALTY_L2PHX;
				}
				break;

			case 5: //*AddNetwork;
				if ( _id != 1435233386)  // До freay 1398167435
				{
					if(!isIdZero)
					{
						Log.add(_positionName[position] + " was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
					}
					value = ConfigProtect.PROTECT_PENALTY_L2CONTROL;
				}
				break;

			case 6:  //hook WndProc
				if (_id !=0x8B493CE9)
				{
					if(!isIdZero)
					{
						Log.add(_positionName[position] + " was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
					}
					value = ConfigProtect.PROTECT_PENALTY_BOT;
				}
				break;

			case 7: //updater
				int flag = Integer.reverseBytes(_id);
				if (ConfigProtect.PROTECT_UPDATER > 0)
				{
					String msg = "";

					if ( flag == 0 )
					{
						msg = "Error updater: Don`t call the function checks|";
						value = ConfigProtect.PROTECT_UPDATER > 2 ? 10:0;
					}
					else if (flag == -1 )
					{
						msg = "The client is not running out of updater|";
						value = ConfigProtect.PROTECT_UPDATER > 2 ? 10:0;
					}
					else if (flag != ConfigProtect.PROTECT_OPCODE)
					{
						msg = "The client is not running out of updater|";
						value = ConfigProtect.PROTECT_UPDATER > 2 ? 10:0;
					}

					if (ConfigProtect.PROTECT_UPDATER > 1)
					{
						Log.add(msg+ pi.toString() + "|DEBUG INFO:" + flag, _logFile);
					}
				}
				break;

			case 8: //
				if (_id !=5622903)
				{
					if(!isIdZero)
					{
						Log.add(_positionName[position] + " was found|"+ pi.toString() + "|DEBUG INFO:" + _id, _logFile);
					}
					value = ConfigProtect.PROTECT_PENALTY_BOT;
				}
				break;
			default:
				value = 0;
				break;
		}
		return value;
	}

	public static int dumpData2(int[] _ids, ProtectInfo pi)
	{
		int value = 0;

		for(int i = 0; i < _ids.length-1; ++i)
			if(_ids[i] == 0 )
				Log.add("Cannot read DumpId|Target pos: " + i + "|" + pi.toString(), _logFile);

		if(_ids[0] != _ids[2] || _ids[1] != _ids[2])
		{
			Log.add("L2ext or other soft was found|" + pi.toString() + "|DEBUG INFO:" + _ids[0] + "|" + _ids[1] + "|" + _ids[2]+"="+_ids[6], _logFile);
			value = ConfigProtect.PROTECT_PENALTY_BOT;
		}

		if(_ids[3] != _ids[5] || _ids[4] != _ids[5])
		{
			Log.add("Snifer soft was found|" + pi.toString() + "|DEBUG INFO:" + _ids[3] + "|" + _ids[4] + "|" + _ids[5]+"="+_ids[6], _logFile);
			value = ConfigProtect.PROTECT_PENALTY_BOT;
		}

		if(_ids[6] == 0xCC)
		{
			Log.add("Snifer soft was found|" + pi.toString() + "|DEBUG INFO:" + _ids[6], _logFile);
		}

		if( !checkVerfiFlag(pi, _ids[7]))
		{
			value= ConfigProtect.PROTECT_PENALTY_BOT;
		}

		return value;
	}

	public static boolean checkVerfiFlag(ProtectInfo pi, int flag)
	{
		boolean result = true;
		int fl = Integer.reverseBytes(flag);

		if (fl == 0xFFFFFFFF)
		{
			Log.add("Error Verify Flag|" + pi.toString(), _logFile);
			return false;
		}

		if (fl == 0x50000000)
		{
			Log.add("Error get net data client|" + pi.toString() + "|DEBUG INFO:" + fl, _logFile);
			return false;
		}

		/*if ((fl & 0x01) != 0)
		{
			Log.add("Sniffer detect |" + pi.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}

		if ((fl & 0x10) != 0)
		{
			Log.add("Sniffer detect2 |" + pi.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}*/

		if ((fl & 0x10000000) != 0)
		{
			Log.add("L2ext detect |" + pi.toString() + "|DEBUG INFO:" + fl, _logFile);
			result = false;
		}

		return result;
	}


	public static void checkProtocolVersion(L2GameClient client, int version)
	{
		ProtectInfo.ProtectionClientState pst = ProtectInfo.ProtectionClientState.DISABLED;
		if(protect_use)
		{
			if(version == ConfigProtect.PROTECT_PROTOCOL_VERSION)
				pst = ProtectInfo.ProtectionClientState.ENABLED;
			else
				pst = ProtectInfo.ProtectionClientState.LIMITED;
		}
		client._prot_info.setProtectState(pst);
		client._crypt.setState(pst);
	}

	public static String fillHex(int data, int digits)
	{
		String number = Integer.toHexString(data);

		for(int i = number.length(); i < digits; i++)
			number = "0" + number;

		return number;
	}

	public static String ExtractHWID(byte[] _data)
	{
		if(verifyChecksum(_data, 0, _data.length))
		{
			StringBuilder resultHWID = new StringBuilder();
			for(int i = 0; i < _data.length - 8; i++)
				resultHWID.append(fillHex(_data[i] & 0xff, 2));
			return resultHWID.toString();
		}
		else
		{
			return null;
		}
	}

	public static boolean CheckHWIDs(ProtectInfo pi, int LastError1, int LastError2)
	{
		boolean resultHWID = false;
		boolean resultLastError = false;
		String HWID = pi.getHWID();
		String HWIDSec = pi.getHWIDSec();

		if(HWID.equalsIgnoreCase("fab800b1cc9de379c8046519fa841e6"))
		{
			Log.add("HWID:" + HWID + "|is empty, account:" + pi.getLoginName() + "|IP: " + pi.getIpAddr(), _logFile);
			if(ConfigProtect.PROTECT_KICK_WITH_EMPTY_HWID)
				resultHWID = true;
		}
		if(HWIDSec.equalsIgnoreCase("fab800b1cc9de379c8046519fa841e6"))
		{
			Log.add("Attempts!!! HWIDSec:" + HWIDSec + "|is empty, account:" + pi.getLoginName() + "|IP: " + pi.getIpAddr(), _logFile);
		}
		if(LastError1 != 0)
		{
			Log.add("LastError(HWID):" + LastError1 + "|" + LastErrorConvertion(LastError1) + "|isn't empty, " + pi.toString(), _logFile);
			if(ConfigProtect.PROTECT_KICK_WITH_LASTERROR_HWID)
				resultLastError = true;
		}
		return resultHWID || resultLastError;
	}

	public static boolean BF_Init(int[] P, int[] S0, int[] S1, int[] S2, int[] S3)
	{
		return false;
	}

	public static void addPlayer(ProtectInfo pi)
	{
		if(protect_use && pi != null)
		{
			ProtectManager.getInstance().addPlayer(pi);
		}
	}

	public static void removePlayer(ProtectInfo pi)
	{
		if(protect_use && pi != null)
		{
			ProtectManager.getInstance().removePlayer(pi.getPlayerName());
		}
	}

	public static void doReadAuthLogin(L2GameClient client, ByteBuffer buf, byte[] data)
	{
		if(!protect_use)
			return;

		int size = buf.remaining() - data.length;
		if(size < 0)
		{
			_log.info("Filed read AuthLogin! May be BOT or unprotected client! Client IP: " + client._prot_info.getIpAddr());
			client.close(new ServerClose());
			return;
		}
		if(size > 0)
		{
			buf.position(buf.position()+size);
		}
		buf.get(data);
		ProtectInfo pi = client._prot_info;
		if (pi != null)
			pi.GGdec.decrypt(data,0,data,0,data.length);
	}

	public static boolean doAuthLogin(L2GameClient client, byte[] data, String loginName)
	{
		if(!protect_use)
			return true;

		ProtectInfo pi = client._prot_info;
		pi.setLoginName(loginName);


		String fullHWID = Protection.ExtractHWID(data);
		if(fullHWID == null)
		{
			_log.info("AuthLogin CRC Check Fail! May be BOT or unprotected client! Client IP: " + pi.getIpAddr());
			client.close(new ServerClose());
			return false;
		}

		pi.setHWID(fullHWID.substring(0, 31));
		pi.setHWIDSec(fullHWID.substring(32 + 4 * 2, 32 + 4 * 2 + 32 - 1));

		Log.add(loginName + "|" + pi.getIpAddr() + "|" + pi.getHWID() + "|" + pi.getHWIDSec(), "hwid_log");

		int LastError1 = ByteBuffer.wrap(data, 16, 4).getInt();
		int Flag = ByteBuffer.wrap(data, 16 + 4 + 16, 4).getInt();

		if(Protection.CheckHWIDs(pi, LastError1, 0))
		{
			_log.info("HWID error, look protection_logs.txt file, from IP: " + client.getIpAddr());
			client.close(new ServerClose());
			return false;
		}

		if(HwidBan.checkFullHWIDBanned(pi))
		{
			Log.add("Rejected banned HWID main|" + "|HWID: " + pi.getHWID() + " IP: " + pi.getIpAddr(), _logFile);
			client.close(new ServerClose());
			return false;
		}

		int VerfiFlag = ByteBuffer.wrap(data, 40, 4).getInt();

		if ( !checkVerfiFlag(pi, VerfiFlag) )
		{
			return false;
		}

		if(ConfigProtect.PROTECT_ENABLE_HWID_LOCK)
		{
			if(HwidManager.checkLockedHWID(pi))
			{
				Log.add("An attempt to log in to locked account|" + "|HWID: " + pi.getHWID() + " IP: " + pi.getIpAddr(), _logFile);
				client.close(new ServerClose());
				return false;
			}
		}

		if (ConfigProtect.PROTECT_SERVER_TITLE != null )
		{
			client.sendPacket(ServerTitleSpecial.STATIC);
		}

		return true;
	}

	public static void doReadReplyGameGuard(L2GameClient client, ByteBuffer buf, byte[] data)
	{
		if (!protect_use)
			return;

		ProtectInfo pi = client._prot_info;

		pi.setGGStatus(true);
		pi.GGReaded = false;

		int op = buf.getInt();
		if (buf.remaining() < data.length + 2 || op != GGOPCODE )
		{
			Log.add("Filed read ReplyGameGuardQuery! Error GG packet opcode = "+op+" size: "+buf.remaining()+"!| "+pi.toString(), _logFile);
			client.close(new ServerClose());
			return;
		}

		buf.getShort();

		buf.get(data);
		pi.GGdec.decrypt(data,0,data,0,data.length);
		pi.GGReaded = true;
	}


	public static void doReplyGameGuard(L2GameClient client, byte[] data)
	{
		if (!protect_use || !ConfigProtect.PROTECT_ENABLE_GG_SYSTEM || !client._prot_info.GGReaded)
			return;

		ProtectInfo pi = client._prot_info;

		int penalty = calcPenalty(data, pi);
		if(penalty == -1)
		{
			Log.add("Checksumm (ReplyGameGuardQuery) is wrong|" + pi.toString(), _logFile);
			client.close(new ServerClose());
			return;
		}

		pi.addProtectPenalty(penalty);

		if(pi.getProtectPenalty() >= ConfigProtect.PROTECT_TOTAL_PENALTY)
		{
			L2Player player = client.getActiveChar();
			if(player == null)
			{
				Log.add("Cheater was found" + pi.toString(), _logFile);
				if(!ConfigProtect.PROTECT_HTML_SHOW.equals("none"))
				{
					String filename = "data/html/" + ConfigProtect.PROTECT_HTML_SHOW;
					NpcHtmlMessage msg = new NpcHtmlMessage(5);
					msg.setFile(filename);
					client.close(msg);
					return;
				}
				else
					client.close(new ServerClose());
				return;
			}

			if(ConfigProtect.PROTECT_PUNISHMENT_ILLEGAL_SOFT == 0)
			{
				Log.add("Cheater was found|" + pi.toString(), _logFile);
				pi.setProtectPenalty(0);
			}
			else
			{
				if(ConfigProtect.PROTECT_PUNISHMENT_ILLEGAL_SOFT == 1)
				{
					Log.add("Cheater was kicked|" + pi.toString(), _logFile);
				}
				else if(ConfigProtect.PROTECT_PUNISHMENT_ILLEGAL_SOFT == 2)
				{
					player.setAccountAccesslevel(-66, "Cheater was banned|" + pi.toString(), -1);
					player.setAccessLevel(-66);
					Log.add("Cheater was banned|" + pi.toString(), _logFile);
				}
				else if(ConfigProtect.PROTECT_PUNISHMENT_ILLEGAL_SOFT == 3)
				{
					HwidBan.addHwidBan(client);
					client.sendPacket(new SystemMessage("You have been banned by HWID"));
					player.setAccountAccesslevel(-66, "Cheater was banned, HWID ban|" + pi.toString(), -1);
					player.setAccessLevel(-66);
					Log.add("Cheater was banned, HWID ban| " + pi.toString(), _logFile);
				}
				if(!ConfigProtect.PROTECT_HTML_SHOW.equals("none"))
				{
					String filename = "data/html/" + ConfigProtect.PROTECT_HTML_SHOW;
					NpcHtmlMessage msg = new NpcHtmlMessage(5);
					msg.setFile(filename);
					client.close(msg);
					return;
				}
			}
			client.close(new ServerClose());
		}
	}

	public static void doDisconection(L2GameClient client)
	{
		removePlayer(client._prot_info);
	}

}
