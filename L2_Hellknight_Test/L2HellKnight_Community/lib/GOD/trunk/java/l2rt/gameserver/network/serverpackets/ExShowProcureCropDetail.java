package l2rt.gameserver.network.serverpackets;

import javolution.util.FastMap;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.CastleManorManager;
import l2rt.gameserver.instancemanager.CastleManorManager.CropProcure;
import l2rt.gameserver.model.entity.residence.Castle;

/**
 * format
 * dd[dddc]
 * dd[dQQc] - Gracia Final
 */
public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private int _cropId;
	private FastMap<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new FastMap<Integer, CropProcure>();

		for(Castle c : CastleManager.getInstance().getCastles().values())
		{
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if(cropItem != null && cropItem.getAmount() > 0)
				_castleCrops.put(c.getId(), cropItem);
		}
	}

	@Override
	public void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x78);

		writeD(_cropId); // crop id
		writeD(_castleCrops.size()); // size

		for(int manorId : _castleCrops.keySet())
		{
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId); // manor name
			writeQ(crop.getAmount()); // buy residual
			writeQ(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}
}