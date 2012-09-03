package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.entity.residence.Castle;
import l2rt.gameserver.model.entity.siege.territory.TerritorySiege;
import l2rt.util.GArray;

// d[dSSd[d]d]
public class ExReplyDominionInfo extends L2GameServerPacket
{
	private GArray<TerritoryInfo> _ti = new GArray<TerritoryInfo>();

	public ExReplyDominionInfo()
	{
		for(Castle c : CastleManager.getInstance().getCastles().values())
			_ti.add(new TerritoryInfo(c.getId(), c.getName(), c.getOwner() == null ? "" : c.getOwner().getName(), c.getFlags()));
	}

	@Override
	protected void writeImpl()
	{
		writeC(EXTENDED_PACKET);
		writeH(0x92);
		writeD(_ti.size());
		for(TerritoryInfo cf : _ti)
		{
			writeD(0x50 + cf.id);
			writeS(cf.terr);
			writeS(cf.clan);
			writeD(cf.flags.length);
			for(int f : cf.flags)
				writeD(0x50 + f);
			writeD((int) (TerritorySiege.getSiegeDate().getTimeInMillis() / 1000));
		}
	}

	private class TerritoryInfo
	{
		public int id;
		public String terr;
		public String clan;
		public int[] flags;

		public TerritoryInfo(int id_, String terr_, String clan_, int[] flags_)
		{
			id = id_;
			terr = terr_;
			clan = clan_;
			flags = flags_;
		}
	}
}