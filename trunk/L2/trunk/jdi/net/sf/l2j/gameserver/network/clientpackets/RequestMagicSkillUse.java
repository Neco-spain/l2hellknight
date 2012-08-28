package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class RequestMagicSkillUse extends L2GameClientPacket
{
  private static final String _C__2F_REQUESTMAGICSKILLUSE = "[C] 2F RequestMagicSkillUse";
  private static Logger _log = Logger.getLogger(RequestMagicSkillUse.class.getName());
  private int _magicId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _magicId = readD();
    _ctrlPressed = (readD() != 0);
    _shiftPressed = (readC() != 0);
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }

    int level = activeChar.getSkillLevel(_magicId);
    if (level <= 0)
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isOutOfControl())
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(_magicId, level);

    if (skill != null)
    {
      if ((skill.getSkillType() == L2Skill.SkillType.RECALL) && (!Config.ALT_GAME_KARMA_PLAYER_CAN_TELEPORT) && (activeChar.getKarma() > 0)) {
        return;
      }
      activeChar.useMagic(skill, _ctrlPressed, _shiftPressed);
    }
    else
    {
      activeChar.sendPacket(new ActionFailed());
      _log.warning("No skill found!!");
    }
  }

  public String getType()
  {
    return "[C] 2F RequestMagicSkillUse";
  }
}