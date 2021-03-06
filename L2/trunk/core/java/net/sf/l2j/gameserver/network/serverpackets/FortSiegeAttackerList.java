package net.sf.l2j.gameserver.network.serverpackets;

//import java.util.Calendar; //signed time related
//import java.util.logging.Logger;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2SiegeClan;
import net.sf.l2j.gameserver.model.entity.Fort;

/**
 * Populates the Siege Attacker List in the SiegeInfo Window<BR>
 * <BR>
 * packet type id 0xca<BR>
 * format: cddddddd + dSSdddSSd<BR>
 * <BR>
 * c = ca<BR>
 * d = FortID<BR>
 * d = unknow (0x00)<BR>
 * d = unknow (0x01)<BR>
 * d = unknow (0x00)<BR>
 * d = Number of Attackers Clans?<BR>
 * d = Number of Attackers Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 * 
 * @author programmos, scoria dev
 */
public final class FortSiegeAttackerList extends L2GameServerPacket
{
	private static final String _S__CA_SiegeAttackerList = "[S] ca SiegeAttackerList";
	//private static Logger _log = Logger.getLogger(SiegeAttackerList.class.getName());
	private Fort _fort;

	public FortSiegeAttackerList(Fort fort)
	{
		_fort = fort;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xca);
		writeD(_fort.getFortId());
		writeD(0x00); //0
		writeD(0x01); //1
		writeD(0x00); //0
		int size = _fort.getSiege().getAttackerClans().size();
		if(size > 0)
		{
			L2Clan clan;

			writeD(size);
			writeD(size);
			for(L2SiegeClan siegeclan : _fort.getSiege().getAttackerClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if(clan == null)
				{
					continue;
				}

				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); //signed time (seconds) (not storated by L2J)
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); //AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}

	/* (non-Javadoc)
	 * @see com.l2jfrozen.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__CA_SiegeAttackerList;
	}

}