package l2p.gameserver.clientpackets;

import java.util.Collection;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.tables.SkillTable;
import l2p.gameserver.utils.Location;

public class RequestExMagicSkillUseGround extends L2GameClientPacket
{
  private Location _loc = new Location();
  private int _skillId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _loc.x = readD();
    _loc.y = readD();
    _loc.z = readD();
    _skillId = readD();
    _ctrlPressed = (readD() != 0);
    _shiftPressed = (readC() != 0);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    Skill skill = SkillTable.getInstance().getInfo(_skillId, activeChar.getSkillLevel(Integer.valueOf(_skillId)));
    if (skill != null)
    {
      if (skill.getAddedSkills().length == 0) {
        return;
      }

      if ((activeChar.getTransformation() != 0) && (!activeChar.getAllSkills().contains(skill))) {
        return;
      }
      if (!activeChar.isInRange(_loc, skill.getCastRange()))
      {
        activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
        activeChar.sendActionFailed();
        return;
      }

      Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

      if (skill.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true))
      {
        activeChar.setGroundSkillLoc(_loc);
        activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
      }
      else {
        activeChar.sendActionFailed();
      }
    } else {
      activeChar.sendActionFailed();
    }
  }
}