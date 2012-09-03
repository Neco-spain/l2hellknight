package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.CastleManorManager;
import l2rt.gameserver.instancemanager.CastleManorManager.SeedProduction;
import l2rt.util.GArray;

/**
 * Format: (ch) dd [ddd]
 * d - manor id
 * d - size
 * [
 * d - seed id
 * d - sales
 * d - price
 * ]
 */
public class RequestSetSeed extends L2GameClientPacket
{
	private int _size, _manorId;

	private long[] _items; // _size*3

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		_size = readD();
		if(_size * 20 > _buf.remaining() || _size > Short.MAX_VALUE || _size <= 0)
		{
			_size = 0;
			return;
		}
		_items = new long[_size * 3];
		for(int i = 0; i < _size; i++)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readQ();
			_items[i * 3 + 2] = readQ();
			if(_items[i * 3 + 0] < 1 || _items[i * 3 + 1] < 0 || _items[i * 3 + 2] < 0)
			{
				_size = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		if(_size < 1)
			return;

		GArray<SeedProduction> seeds = new GArray<SeedProduction>();
		for(int i = 0; i < _size; i++)
		{
			int id = (int) _items[i * 3 + 0];
			long sales = _items[i * 3 + 1];
			long price = _items[i * 3 + 2];
			if(id > 0)
			{
				SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
				seeds.add(s);
			}
		}

		CastleManager.getInstance().getCastleByIndex(_manorId).setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		CastleManager.getInstance().getCastleByIndex(_manorId).saveSeedData(CastleManorManager.PERIOD_NEXT);
	}
}