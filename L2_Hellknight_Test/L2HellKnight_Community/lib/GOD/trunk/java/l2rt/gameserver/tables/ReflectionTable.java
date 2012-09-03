package l2rt.gameserver.tables;

import javolution.util.FastMap;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.entity.KamalokaNightmare;

public class ReflectionTable
{
	public static long SOD_REFLECTION_ID = 0;
	public static final long DEFAULT = 0;
	public static final long PARNASSUS = -1;
	public static final long GH = -2;
	public static final long JAIL = -3;
	public static final long MULTILAYER = -4;
	public static final int SOI_HEART_OF_INFINITY_DEFENCE = 122;
	public static final int SOI_HALL_OF_EROSION_DEFENCE = 120;
	public static final int SOI_HALL_OF_EROSION_ATTACK = 119;
	public static final int SOI_HALL_OF_SUFFERING_SECTOR2 = 116;
	public static final int SOI_HALL_OF_SUFFERING_SECTOR1 = 115;
	public static final int DISCIPLES_NECROPOLIS = 112;

	private static ReflectionTable _instance;
	private static Reflection _default = new Reflection(0);

	public static ReflectionTable getInstance()
	{
		if(_instance == null)
			_instance = new ReflectionTable();
		return _instance;
	}

	private FastMap<Integer, Long> _soloKamalokaList = new FastMap<Integer, Long>();

	private Reflection[] staticData = new Reflection[10];

	public synchronized void addReflection(Reflection r)
	{
		if(r.getId() <= 0) // отрицательные номера у статичных отражений
		{
			staticData[(int) Math.abs(r.getId())] = r;
			return;
		}

		r.setId(L2ObjectsStorage.put(r));
	}

	public Reflection get(long index, boolean CreateIfNonExist)
	{
		Reflection ret = null;
		if(index <= 0)
		{
			if(staticData.length > Math.abs(index))
				ret = staticData[(int) Math.abs(index)];
		}
		else
			ret = (Reflection) L2ObjectsStorage.get(index);
		if(CreateIfNonExist && ret == null)
			ret = new Reflection(index);
		return ret;
	}

	public Reflection get(long index)
	{
		return get(index, false);
	}

	public Reflection getDefault()
	{
		return _default;
	}

	public void addSoloKamaloka(Integer player, KamalokaNightmare r)
	{
		_soloKamalokaList.put(player, r.getId());
	}

	public void removeSoloKamaloka(Integer player)
	{
		_soloKamalokaList.remove(player);
	}

	public KamalokaNightmare findSoloKamaloka(Integer player)
	{
		Long index = _soloKamalokaList.get(player);
		if(index == null)
			return null;
		Reflection found = get(index);
		if(found == null || !(found instanceof KamalokaNightmare) || ((KamalokaNightmare) found).getPlayerId() != player)
		{
			_soloKamalokaList.remove(player);
			return null;
		}
		return (KamalokaNightmare) get(index);
	}
}