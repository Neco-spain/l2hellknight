package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.base.L2EnchantSkillLearn;
import l2rt.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import l2rt.gameserver.tables.SkillTreeTable;

public final class RequestExEnchantSkillInfoDetail extends L2GameClientPacket
{
	private static final int TYPE_NORMAL_ENCHANT = 0;
	private static final int TYPE_SAFE_ENCHANT = 1;
	private static final int TYPE_UNTRAIN_ENCHANT = 2;
	private static final int TYPE_CHANGE_ENCHANT = 3;

	private int _type;
	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_type = readD();
		_skillId = readD();
		_skillLvl = readD();
	}

	@Override
	protected void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.getTransformation() != 0)
		{
			activeChar.sendMessage("You must leave transformation mode first.");
			return;
		}

		if(activeChar.getLevel() < 76 || activeChar.getClassId().getLevel() < 4)
		{
			activeChar.sendMessage("You must have 3rd class change quest completed.");
			return;
		}

		int bookId = 0;
		int sp = 0;
		int adenaCount = 0;
		float spMult = SkillTreeTable.NORMAL_ENCHANT_COST_MULTIPLIER;

		L2EnchantSkillLearn esd;

		switch(_type)
		{
			case TYPE_NORMAL_ENCHANT:
				if(_skillId < 10000 &_skillLvl % 100 == 1)
					bookId = SkillTreeTable.NORMAL_ENCHANT_BOOK;
				else if (_skillId >= 10000)
					bookId = SkillTreeTable.NEW_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				break;
			case TYPE_SAFE_ENCHANT:
				if (_skillId < 10000)
					bookId = SkillTreeTable.SAFE_ENCHANT_BOOK;
				else
					bookId = SkillTreeTable.NEW_SAFE_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl);
				spMult = SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
			case TYPE_UNTRAIN_ENCHANT:
				if (_skillId < 10000)
					bookId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
				else
					bookId = SkillTreeTable.UNTRAIN_NEW_ENCHANT_BOOK;
				esd = SkillTreeTable.getSkillEnchant(_skillId, _skillLvl + 1);
				break;
			case TYPE_CHANGE_ENCHANT:
				if (_skillId < 10000)
					bookId = SkillTreeTable.CHANGE_ENCHANT_BOOK;
				else
					bookId = SkillTreeTable.NEW_CHANGE_ENCHANT_BOOK;
				esd = SkillTreeTable.getEnchantsForChange(_skillId, _skillLvl).get(0);
				spMult = 1f / SkillTreeTable.SAFE_ENCHANT_COST_MULTIPLIER;
				break;
			default:
				_log.severe("Unknown skill enchant type: " + _type);
				return;
		}

		if(esd == null)
			return;

		spMult *= esd.getCostMult();
		int[] cost = esd.getCost();

		sp = (int) (cost[1] * spMult);

		if(_type != TYPE_UNTRAIN_ENCHANT)
			adenaCount = (int) (cost[0] * spMult);

		// send skill enchantment detail
		activeChar.sendPacket(new ExEnchantSkillInfoDetail(_skillId, _skillLvl, sp, esd.getRate(activeChar), bookId, adenaCount));
	}
}