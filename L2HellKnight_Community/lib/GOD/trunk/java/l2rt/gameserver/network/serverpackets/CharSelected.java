package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.L2GameClient;
import l2rt.util.Location;

public class CharSelected extends L2GameServerPacket
{
	//   SdSddddddddddffddddddddddddddddddddddddddddddddddddddddd d
	private int _sessionId, char_id, clan_id, sex, race, class_id;
	private String _name, _title;
	private Location _loc;
	private double curHp, curMp;
	private int _sp, level, karma, _int, _str, _con, _men, _dex, _wit, _pk;
	private long _exp;

	public CharSelected(final L2Player cha, final int sessionId)
	{
		_sessionId = sessionId;

		_name = cha.getName();
		char_id = cha.getObjectId(); //FIXME 0x00030b7a ??
		_title = cha.getTitle();
		clan_id = cha.getClanId();
		sex = cha.getSex();
		race = cha.getRace().ordinal();
		class_id = cha.getClassId().getId();
		_loc = cha.getLoc();
		curHp = cha.getCurrentHp();
		curMp = cha.getCurrentMp();
		_sp = cha.getIntSp();
		_exp = cha.getExp();
		level = cha.getLevel();
		karma = cha.getKarma();
		_pk = cha.getPkKills();
		_int = cha.getINT();
		_str = cha.getSTR();
		_con = cha.getCON();
		_men = cha.getMEN();
		_dex = cha.getDEX();
		_wit = cha.getWIT();
		if (cha.isAwaking())
			class_id = cha.getAwakingId();
	}

	@Override
	protected final void writeImpl()
	{
		L2GameClient client = getClient();

		writeC(0x0b);

		writeS(_name);
		writeD(char_id);
		writeS(_title);
		writeD(_sessionId);
		writeD(clan_id);
		writeD(0x00); //??
		writeD(sex);
		writeD(race);
		writeD(class_id);
		writeD(0x01); // active ??
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);

		writeF(curHp);
		writeF(curMp);
		writeD(_sp);
		writeQ(_exp);
		
		writeD(level);
		writeD(karma); //?
		writeD(_pk);
		writeD(_int);
		writeD(_str);
		writeD(_con);
		writeD(_men);
		writeD(_dex);
		writeD(_wit);
		writeD(GameTimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);
		
		writeD(class_id);
		
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		
		writeB(new byte[64]);
		writeD(0x00);
	}
}



