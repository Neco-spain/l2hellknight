package l2rt.util;

import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.tables.FakePlayersTable;

public class Stats
{
	public static int getOnline()
	{
		return L2ObjectsStorage.getAllPlayersCount();
	}

	public static int getOnline(boolean includeFake)
	{
		return getOnline() + (includeFake ? FakePlayersTable.getFakePlayersCount() : 0);
	}
}