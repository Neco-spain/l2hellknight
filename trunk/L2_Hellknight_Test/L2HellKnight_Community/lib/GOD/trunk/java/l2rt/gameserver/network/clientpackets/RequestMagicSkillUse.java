package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2SubClass;
import l2rt.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
	private Integer _magicId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0x39
	 * format:		cddc
	 */
	@Override
	public void readImpl()
	{
		_magicId = readD();
		_ctrlPressed = readD() != 0;
		_shiftPressed = readC() != 0;
	}

	@Override
	public void runImpl()
	{
		L2Player activeChar = getClient().getActiveChar();

		if(activeChar == null)
			return;

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		
		if (_magicId >= 1566 && _magicId <= 1569)
		{
			for (L2SubClass tmp : activeChar.getSubClasses().values()) 
			{
				if (_magicId == 1566 && tmp.isBase()) {
					activeChar.setActiveSubClass(tmp.getClassId(), true);
					break;
				}
				else if (_magicId == 1568 && tmp.getDualClass() != 0) {
					activeChar.setActiveSubClass(tmp.getClassId(), true);
					break;
				}
				else {
					activeChar.setActiveSubClass(tmp.getClassId(), true);
					break;
				}
			}
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_magicId, activeChar.getSkillLevel(_magicId));
		if(skill != null)
		{
			if(!(skill.isActive() || skill.isToggle()))
				return;

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.getTransformation() != 0 && !activeChar.getSkillFromAll(skill))
				return;

			if(skill.isToggle())
				if(activeChar.getEffectList().getEffectsBySkill(skill) != null)
				{
					activeChar.getEffectList().stopEffect(skill.getId());
					activeChar.sendActionFailed();
					return;
				}

			L2Character target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			activeChar.setGroundSkillLoc(null);
			activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
		}
		else
			activeChar.sendActionFailed();
	}
}