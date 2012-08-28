package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.util.Location;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestExMagicSkillUseGround.class.getName());
  private int _x;
  private int _y;
  private int _z;
  private int _skillId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _x = readD();
    _y = readD();
    _z = readD();
    _skillId = readD();
    _ctrlPressed = (readD() != 0);
    _shiftPressed = (readC() != 0);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if ((player.isInZonePeace()) || (!player.isInsideRadius(_x, _y, _z, 1000, false, false)))
    {
      player.sendActionFailed();
      return;
    }

    int level = player.getSkillLevel(_skillId);
    if (level <= 0)
    {
      player.sendActionFailed();
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

    if (skill != null)
    {
      if ((skill.isBattleForceSkill()) || (skill.isSpellForceSkill()))
      {
        if (skill.checkForceCondition(player, _skillId))
        {
          player.setGroundSkillLoc(null);
          Location _loc = new Location(_x, _y, _z);
          player.setGroundSkillLoc(_loc);
          player.useMagic(skill, _ctrlPressed, _shiftPressed);
        }
        else
        {
          player.sendMessage("\u041D\u0435\u0434\u043E\u0441\u0442\u0430\u0442\u043E\u0447\u043D\u043E \u0441\u0438\u043B\u044B");
          player.sendActionFailed();
          return;
        }
      }
      else if (skill.checkCondition(player, player, false))
      {
        player.useMagic(skill, _ctrlPressed, _shiftPressed);
      }
      else
        player.sendActionFailed();
    }
    else
      player.sendActionFailed();
  }
}