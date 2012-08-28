package net.sf.l2j.gameserver.network.serverpackets;

import java.util.List;

import javolution.util.FastList;

public class AquireShopSkillList extends L2GameServerPacket
{
	//private static Logger _log = Logger.getLogger(AquireSkillList.class.getName());
	private static final String _S__A3_AQUIRESHOPSKILLLIST = "[S] 8a AquireShopSkillList";

	private List<Skill> _ShopSkills;

	private class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int getCostId;
		public int getCostCount;
		public int getBookId;
		public int getListId;

		public Skill(int pId, int pNextLevel, int pMaxLevel, int pSpCost, int pGetCostId, int pGetCostCount, int pGetBookId, int pGetListId)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;;
			getCostId = pGetCostId;
			getCostCount = pGetCostCount;
			getBookId = pGetBookId;
			getListId = pGetListId;
		}
	}

	public AquireShopSkillList()
	{
		_ShopSkills = new FastList<Skill>();
	}

	public void addSkill(int id, int nextLevel, int maxLevel, int spCost, int getCostId, int getCostCount, int getBookId, int getListId)
	{
		_ShopSkills.add(new Skill(id, nextLevel, maxLevel, spCost, getCostId, getCostCount, getBookId, getListId));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(0x8a);
		writeD(_ShopSkills.size());

		for (Skill temp : _ShopSkills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeQ(temp.getCostId);
			writeQ(temp.getCostCount);
			writeQ(temp.getBookId);
			writeQ(temp.getListId);
		}
	}

	/* (non-Javadoc)
	 * @see net.sf.l2j.gameserver.serverpackets.ServerBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__A3_AQUIRESHOPSKILLLIST;
	}

}
