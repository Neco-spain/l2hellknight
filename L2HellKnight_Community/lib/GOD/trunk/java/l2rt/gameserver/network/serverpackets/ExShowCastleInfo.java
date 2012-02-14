package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.tables.ClanTable;
import l2rt.util.GArray;

public class ExShowCastleInfo extends L2GameServerPacket
{
	private GArray<CastleInfo> _infos = new GArray<CastleInfo>();

	public ExShowCastleInfo()
	{
		String ownerName;
		int id, tax, nextSiege;

		for(Castle castle : CastleManager.getInstance().getCastles().values())
		{
			ownerName = castle.getOwnerId() == 0 ? "" : ClanTable.getInstance().getClan(castle.getOwnerId()).getName();
			id = castle.getId();
			tax = castle.getTaxPercent();
			nextSiege = (int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000);
			_infos.add(new CastleInfo(ownerName, id, tax, nextSiege));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x14);
		writeD(_infos.size());
		for(CastleInfo info : _infos)
		{
			writeD(info._id);
			writeS(info._ownerName);
			writeD(info._tax);
			writeD(info._nextSiege);
		}
		_infos.clear();
	}

	static class CastleInfo
	{
		public String _ownerName;
		public int _id, _tax, _nextSiege;

		public CastleInfo(String ownerName, int id, int tax, int nextSiege)
		{
			_ownerName = ownerName;
			_id = id;
			_tax = tax;
			_nextSiege = nextSiege;
		}
	}
}