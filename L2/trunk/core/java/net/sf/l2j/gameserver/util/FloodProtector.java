package net.sf.l2j.gameserver.util;

import java.util.logging.Logger;

import javolution.util.FastMap;
import javolution.util.FastMap.Entry;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;

public class FloodProtector
{
	private static final Logger _log = Logger.getLogger(FloodProtector.class.getName());
	private static FloodProtector _instance;

	public static final FloodProtector getInstance()
	{
		if (_instance == null)
		{
			_instance = new FloodProtector();
		}
		return _instance;
	}

	private FastMap<Integer,Integer[]> _floodClient;
	private static final int[] REUSEDELAY = new int[] { 0, 42, 42, 16, 100, 5, (Config.SUBCLASS_PROTECT * 10), (Config.PVPPK_PROTECT * 10), 100, 50,(Config.TRADE_FLOOD_TIME * 10), 15, 10, (Config.SHOUT_FLOOD_TIME * 10), (Config.WH_FLOOD_TIME * 10), (Config.ENCHANT_FLOOD_TIME * 10), 100};

	public static final int PROTECTED_USEITEM		= 0;
	public static final int PROTECTED_ROLLDICE		= 1;
	public static final int PROTECTED_FIREWORK		= 2;
	public static final int PROTECTED_ITEMPETSUMMON	= 3;
	public static final int PROTECTED_HEROVOICE		= 4;
	public static final int PROTECTED_NONDUPE = 5;
	public static final int PROTECTED_SUBCLASSPROTECT = 6;
	public static final int PROTECTED_PVPPK = 7;
	public static final int PROTECTED_IPBLOCK = 8;
	public static final int PROTECTED_SOCIAL = 9;
	public static final int PROTECTED_TRADE = 10;
	public static final int PROTECTED_UNKNOWNPACKET = 11;
	public static final int PROTECTED_MULTISELL = 12;
	public static final int PROTECTED_SHOUT = 13;
	public static final int PROTECTED_WH = 14;
	public static final int PROTECTED_ENCH = 15;
	public static final int PROTECTED_ITEMMBUFF = 16;
	public static final int PROTECTED_MACROSES = 17;

	private FloodProtector()
	{
		_log.info("Initializing FloodProtector");
		_floodClient = new FastMap<Integer, Integer[]>(Config.FLOODPROTECTOR_INITIALSIZE).setShared(true);
	}

	public void registerNewPlayer(int playerObjId)
	{
		Integer[] array = new Integer[REUSEDELAY.length];
		for (int i=0; i<array.length; i++)
			array[i] = 0;

		_floodClient.put(playerObjId, array);
	}

	public void removePlayer(int playerObjId)
	{
		_floodClient.remove(playerObjId);
	}

	public int getSize()
	{
		return _floodClient.size();
	}

	public boolean tryPerformAction(int playerObjId, int action)
	{
		Entry<Integer, Integer[]> entry = _floodClient.getEntry(playerObjId);
		if (entry == null)
			return false;
		Integer[] value = entry.getValue();

		if (value[action] < GameTimeController.getGameTicks())
		{
			value[action] = GameTimeController.getGameTicks()+REUSEDELAY[action];
			entry.setValue(value);
			return true;
		}
		return false;
	}
}