package l2rt.gameserver.network.serverpackets;

import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.tables.SkillSpellbookTable;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.util.GArray;

/**
 * Пример снифа изучения дополнительных клановыз скилов:
 * 0000: 91 67 02 00 00 01 00 00 00 a0 28 00 00 03 00 00    .g........(.....
 * 0010: 00 01 00 00 00 05 00 00 00 b6 26 00 00 04 00 00    ..........&.....
 * 0020: 00 00 00 00 00 00 00 00 00                         .........
 */
public class AcquireSkillInfo extends L2GameServerPacket
{
	private GArray<Req> _reqs;
	private int _id;
	private byte _level;
	private int _spCost;
	private int _mode;
	private ClassId _classId;
	private L2Clan _clan;

	class Req
	{
		public int id, type, unk;
		public long count;

		Req(int type, int id, long count, int unk)
		{
			this.id = id;//0
			this.type = type;//2
			this.count = count;//count spb
			this.unk = unk;//2
		}
	}

	public AcquireSkillInfo(int id, byte level, ClassId classid, L2Clan clan, int mode)
	{
		_reqs = new GArray<Req>();
		_id = id;
		_level = level;
		_classId = classid;
		_clan = clan;
		_mode = mode;
		fillRequirements();
	}

	private void fillRequirements()
	{
		L2SkillLearn SkillLearn = SkillTreeTable.getSkillLearn(_id, _level, _classId, _clan, _mode == AcquireSkillList.TRANSFER);
		if(SkillLearn == null)
			return;
		_spCost = _clan != null ? SkillLearn.getRepCost() : SkillLearn.getSpCost();

		if (SkillLearn.itemId > 0 && SkillLearn.itemCount>0 &&  (_mode == AcquireSkillList.NPCSKILLLEARN || _mode == AcquireSkillList.CLANSKILLLEARN))
			_reqs.add(new Req(4, SkillLearn.itemId, SkillLearn.itemCount, 2));

		Integer spb_id = SkillSpellbookTable._skillSpellbooks.get(SkillSpellbookTable.hashCode(new int[] { _id, _level }));
		if(spb_id != null && _mode != AcquireSkillList.CLANSKILLLEARN)
			_reqs.add(new Req(SkillLearn.common ? 4 : _clan != null ? 2 : 99, spb_id.intValue(), SkillLearn.getItemCount(), SkillLearn.common || _clan != null ? 2 : 50));

		if(_mode == AcquireSkillList.TRANSFER)
		{
			int id = 0;
			switch(_classId)
			{
				case cardinal:
					id = 15307;
					break;
				case evaSaint:
					id = 15308;
					break;
				case shillienSaint:
					id = 15309;
					break;
			}

			_reqs.add(new Req(99, id, 1, 50)); // TODO проверить type и unk
			_spCost = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x91);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_mode);

		writeD(_reqs.size());

		for(Req temp : _reqs)
		{
			writeD(temp.type);
			writeD(temp.id);
			writeQ(temp.count);
			writeD(temp.unk);
		}
	}
}