package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2HennaInstance;

//ccccccdd[dd]
public class GMHennaInfo extends L2GameServerPacket
{
	private int _count, _str, _con, _dex, _int, _wit, _men;
	private final L2HennaInstance[] _hennas = new L2HennaInstance[3];

	public GMHennaInfo(final L2Player cha)
	{
		_str = cha.getHennaStatSTR();
		_con = cha.getHennaStatCON();
		_dex = cha.getHennaStatDEX();
		_int = cha.getHennaStatINT();
		_wit = cha.getHennaStatWIT();
		_men = cha.getHennaStatMEN();

		int j = 0;
		for(int i = 0; i < 3; i++)
		{
			L2HennaInstance h = cha.getHenna(i + 1);
			if(h != null)
				_hennas[j++] = h;
		}
		_count = j;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0xf0);

		writeC(_int);
		writeC(_str);
		writeC(_con);
		writeC(_men);
		writeC(_dex);
		writeC(_wit);
		writeD(3);
		writeD(_count);
		for(int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(_hennas[i].getSymbolId());
		}
	}
}