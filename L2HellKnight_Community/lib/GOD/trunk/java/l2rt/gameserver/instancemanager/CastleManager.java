package l2rt.gameserver.instancemanager;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.residence.Residence;
import l2rt.util.GArray;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

public class CastleManager
{
	protected static Logger _log = Logger.getLogger(CastleManager.class.getName());

	private static CastleManager _instance;
	private HashMap<Integer, Castle> _castles;

	public static CastleManager getInstance()
	{
		if(_instance == null)
			_instance = new CastleManager();
		return _instance;
	}

	public CastleManager()
	{
		load();
	}

	private void load()
	{
		GArray<L2Zone> zones = ZoneManager.getInstance().getZoneByType(ZoneType.Castle);
		if(zones.size() == 0)
			_log.info("Not found zones for Castles!!!");
		else
			for(L2Zone zone : zones)
			{
				Castle castle = new Castle(zone.getIndex());
				castle.init();
				getCastles().put(zone.getIndex(), castle);
			}
		_log.info("Loaded: " + getCastles().size() + " castles.");
	}

	/**
	 * Возвращает замок, соответствующий индексу.
	 */
	public Castle getCastleByIndex(int index)
	{
		return getCastles().get(index);
	}

	/**
	 * Находит замок по имени. Если такого замка нет - возвращает null.
	 */
	public Castle getCastleByName(String name)
	{
		int index = getCastleIndexByName(name);
		if(index > 0)
			return getCastles().get(index);
		return null;
	}

	/**
	 * Если координаты принадлежат зоне какого-либо замка, возвращает этот замок.
	 * Иначе возвращает null.
	 */
	public Castle getCastleByObject(L2Object activeObject)
	{
		return getCastleByCoord(activeObject.getX(), activeObject.getY());
	}

	/**
	 * Если обьект находится в зоне какого-либо замка, возвращает этот замок.
	 * Иначе возвращает null.
	 */
	public Castle getCastleByCoord(int x, int y)
	{
		int index = getCastleIndexByCoord(x, y);
		if(index > 0)
			return getCastles().get(index);
		return null;
	}

	/**
	 * Если обьект находится в зоне какого-либо замка, возвращает индекс этого замка.
	 * Иначе возвращает -1.
	 */
	public int getCastleIndex(L2Object activeObject)
	{
		return getCastleIndexByCoord(activeObject.getX(), activeObject.getY());
	}

	/**
	 * Если координаты принадлежат зоне какого-либо замка, возвращает индекс этого замка.
	 * Иначе возвращает -1.
	 */
	public int getCastleIndexByCoord(int x, int y)
	{
		Residence castle;
		for(int i = 1; i <= getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if(castle != null && castle.checkIfInZone(x, y))
				return i;
		}
		return -1;
	}

	/**
	 * Находит замок по имени, без учета регистра.
	 * Если не найден - возвращает -1.
	 */
	public int getCastleIndexByName(String name)
	{
		Residence castle;
		for(int i = 1; i <= getCastles().size(); i++)
		{
			castle = getCastles().get(i);
			if(castle != null && castle.getName().equalsIgnoreCase(name.trim()))
				return i;
		}
		return -1;
	}

	/**
	 * Возвращает список, содержащий все замки.
	 */
	public HashMap<Integer, Castle> getCastles()
	{
		if(_castles == null)
			_castles = new HashMap<Integer, Castle>();
		return _castles;
	}

	public final Castle getCastleByOwner(L2Clan clan)
	{
		if(clan == null)
			return null;
		for(Castle castle : getCastles().values())
			if(clan.getClanId() == castle.getOwnerId())
				return castle;
		return null;
	}
	
	public String getCastleNameById(int castleId) {
		switch (castleId)
		{
		case 1:
		default:
			return "Глудио";
		case 2:
			return "Дион";
		case 3:
			return "Гиран";
		case 4:
			return "Орен";
		case 5:
			return "Аден";
		case 6:
			return "Иннадрил";
		case 7:
			return "Годдарт";
		case 8:
			return "Руна";
		case 9:
			}return "Шуттгарт";
		}
		 
	public String getAltSiegeTime(int castleId)
		{
			switch (castleId)
			{
			case 1:
			default:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_GLUDIO[0])) + " " + Config.DT_OF_SIEGE_GLUDIO[1] + ":" + Config.DT_OF_SIEGE_GLUDIO[2];
			case 2:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_DION[0])) + " " + Config.DT_OF_SIEGE_DION[1] + ":" + Config.DT_OF_SIEGE_DION[2];
			case 3:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_GIRAN[0])) + " " + Config.DT_OF_SIEGE_GIRAN[1] + ":" + Config.DT_OF_SIEGE_GIRAN[2];
			case 4:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_OREN[0])) + " " + Config.DT_OF_SIEGE_OREN[1] + ":" + Config.DT_OF_SIEGE_OREN[2];
			case 5:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_ADEN[0])) + " " + Config.DT_OF_SIEGE_ADEN[1] + ":" + Config.DT_OF_SIEGE_ADEN[2];
			case 6:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_INNADRIL[0])) + " " + Config.DT_OF_SIEGE_INNADRIL[1] + ":" + Config.DT_OF_SIEGE_INNADRIL[2];
			case 7:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_GODDARD[0])) + " " + Config.DT_OF_SIEGE_GODDARD[1] + ":" + Config.DT_OF_SIEGE_GODDARD[2];
			case 8:
				return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_RUNE[0])) + " " + Config.DT_OF_SIEGE_RUNE[1] + ":" + Config.DT_OF_SIEGE_RUNE[2];
			case 9:
			}return DayByNumb(Integer.parseInt(Config.DT_OF_SIEGE_SCHUTTGART[0])) + " " + Config.DT_OF_SIEGE_SCHUTTGART[1] + ":" + Config.DT_OF_SIEGE_SCHUTTGART[2];
		}

	public String DayByNumb(int a)
		{
		if (Calendar.getInstance().get(7) == a)
			return "Сегодня";
		if (Calendar.getInstance().get(7) + 1 == a)
			return "Завтра";
		switch (a) {
			case 1:
			default:
				return "Воскресенье";
			case 2:
				return "Понедельник";
			case 3:
				return "Вторник";
			case 4:
				return "Среда";
			case 5:
				return "Четверг";
			case 6:
				return "Пятница";
			case 7: } return "Суббота";
		}

	public String getOwner(int castleId)
		{
		String owner = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection(); 
	    	statement = con.prepareStatement("SELECT * FROM clan_data WHERE hasCastle > '0' ORDER BY hasCastle DESC LIMIT 10;");
	    	rs = statement.executeQuery();
			while (rs.next())
			{
				if (castleId == rs.getInt("hasCastle"))
					owner = rs.getString("clan_name");
			}
			statement.close(); 
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		} 
		finally 
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return owner;
	}
	
}