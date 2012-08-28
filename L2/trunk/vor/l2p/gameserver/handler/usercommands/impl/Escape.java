package l2p.gameserver.handler.usercommands.impl;

import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.handler.usercommands.IUserCommandHandler;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.base.PlayerAccess;
import l2p.gameserver.model.base.TeamType;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.tables.SkillTable;

public class Escape
  implements IUserCommandHandler
{
  private static final int[] COMMAND_IDS = { 52 };

  public boolean useUserCommand(int id, Player activeChar)
  {
    if (id != COMMAND_IDS[0]) {
      return false;
    }
    if ((activeChar.isMovementDisabled()) || (activeChar.isInOlympiadMode())) {
      return false;
    }
    if ((activeChar.getTeleMode() != 0) || (!activeChar.getPlayerAccess().UseTeleport))
    {
      activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar, new Object[0]));
      return false;
    }

    if (activeChar.isTerritoryFlagEquipped())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
      return false;
    }

    if ((activeChar.isInDuel()) || (activeChar.getTeam() != TeamType.NONE))
    {
      activeChar.sendMessage(new CustomMessage("common.RecallInDuel", activeChar, new Object[0]));
      return false;
    }

    activeChar.abortAttack(true, true);
    activeChar.abortCast(true, true);
    activeChar.stopMove();
    Skill skill;
    Skill skill;
    if (activeChar.getPlayerAccess().FastUnstuck)
      skill = SkillTable.getInstance().getInfo(1050, 2);
    else {
      skill = SkillTable.getInstance().getInfo(2099, 1);
    }
    if ((skill != null) && (skill.checkCondition(activeChar, activeChar, false, false, true))) {
      activeChar.getAI().Cast(skill, activeChar, false, true);
    }
    return true;
  }

  public final int[] getUserCommandList()
  {
    return COMMAND_IDS;
  }
}