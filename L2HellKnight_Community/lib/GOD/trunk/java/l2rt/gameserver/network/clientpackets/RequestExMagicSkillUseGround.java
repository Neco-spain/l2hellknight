package l2rt.gameserver.network.clientpackets;

import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;

/**
 * @author SYS
 * @date 08/9/2007
 * Format: chdddddc
 *
 * Пример пакета:
 * D0
 * 2F 00
 * E4 35 00 00 x
 * 62 D1 02 00 y
 * 22 F2 FF FF z
 * 90 05 00 00 skill id
 * 00 00 00 00 ctrlPressed
 * 00 shiftPressed
 */
public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
	private Location _loc = new Location();
	private int _skillId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;

	/**
	 * packet type id 0xd0
	 */
	@Override
	public void readImpl()
	{
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_skillId = readD();
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

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, activeChar.getSkillLevel(_skillId));
		if(skill != null)
		{
			if(skill.getAddedSkills() == null)
				return;

			// В режиме трансформации доступны только скилы трансформы
			if(activeChar.getTransformation() != 0 && !activeChar.getSkillFromAll(skill))
				return;

			if(!activeChar.isInRange(_loc, skill.getCastRange()))
			{
				activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
				activeChar.sendActionFailed();
				return;
			}

			L2Character target = skill.getAimingTarget(activeChar, activeChar.getTarget());

			if(skill.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
			{
				activeChar.setGroundSkillLoc(_loc);
				activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
			}
			else
				activeChar.sendActionFailed();
		}
		else
			activeChar.sendActionFailed();
	}
}