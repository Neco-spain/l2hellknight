package scripts.commands.usercommandhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import scripts.commands.IUserCommandHandler;

public class Escape
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 52 };

  public boolean useUserCommand(int id, L2PcInstance activeChar)
  {
    if (!TvTEvent.onEscapeUse(activeChar.getName()))
    {
      activeChar.sendActionFailed();
      return false;
    }

    if ((activeChar.isCastingNow()) || (activeChar.isMovementDisabled()) || (activeChar.isMuted()) || (activeChar.isAlikeDead()) || (activeChar.isInOlympiadMode()) || (activeChar.isEventWait()) || (activeChar.getChannel() > 1))
    {
      activeChar.sendActionFailed();
      return false;
    }

    if (activeChar.isFestivalParticipant())
    {
      activeChar.sendMessage("You may not use an escape command in a festival.");
      return false;
    }

    if (activeChar.isInJail())
    {
      activeChar.sendMessage("You can not escape from jail.");
      return false;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(2099, 1);

    if (skill.checkCondition(activeChar, activeChar, false))
      activeChar.useMagic(skill, false, false);
    else {
      activeChar.sendActionFailed();
    }
    return true;
  }

  public int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}