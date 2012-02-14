package l2rt.gameserver.network.serverpackets;

import gnu.trove.list.array.TIntArrayList;
import l2rt.gameserver.model.L2SkillLearn;
import l2rt.gameserver.model.base.ClassId;
import l2rt.gameserver.tables.SkillSpellbookTable;
import l2rt.gameserver.tables.SkillTreeTable;
import l2rt.gameserver.instancemanager.AwakingManager;
import l2rt.util.GArray;

/**
** Автор: ALF
** L2-WT
**/
public class ExAcquireSkillInfo extends L2GameServerPacket
{
	private GArray<Req> _reqs;
	private GArray<DelSk> _delSkill;
	private int _id;
	private byte _level;
	private byte _minlevel;
	private int _spCost;
	private int _mode;
	private ClassId _classId;

	class Req
	{
		public int id;
		public long count;

		Req(int id, long count)
		{
			this.id = id;//0
			this.count = count;//count spb
		}
	}
	
	class DelSk
	{
		public int id;
		public int level;

		DelSk(int _id, int _level)
		{
			this.id = _id;
			this.level = _level;
		}
	}

	public ExAcquireSkillInfo(int id, byte level, ClassId classid, int mode)
	{
		_reqs = new GArray<Req>();
		_delSkill = new GArray<DelSk>();
		_id = id;
		_level = level;
		_classId = classid;
		_mode = mode;
		fillRequirements();
	}

	private void fillRequirements()
	{
		L2SkillLearn SkillLearn = SkillTreeTable.getSkillLearn(_id, _level, _classId, null, false);
		if(SkillLearn == null)
			return;
		_spCost = SkillLearn.getSpCost();
		_minlevel = SkillLearn.getMinLevel();

		Integer spb_id = SkillSpellbookTable._skillSpellbooks.get(SkillSpellbookTable.hashCode(new int[] { _id, _level }));
		if(spb_id != null)
			_reqs.add(new Req(spb_id.intValue(), SkillLearn.getItemCount()));
		if (_mode > 0)
		{
			int[] delSkills = AwakingManager.getInstance().getRelationSkillById(_id);
			if (delSkills != null)
			{
				for(int _dskId : delSkills)
					_delSkill.add(new DelSk(_dskId,1));
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
	
		writeC(0xFE);
		writeH(0xFB);
		
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeH(_minlevel); 
		
		writeD(_reqs.size());
		for(Req temp : _reqs)
		{
			writeD(temp.id);
			writeQ(temp.count);
		}

		writeD(_delSkill.size());
		for(DelSk temp2 : _delSkill)
		{
			writeD(temp2.id); 
			writeD(temp2.level);
		}
	}
}