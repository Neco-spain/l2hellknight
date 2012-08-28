package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.util.Util;
import net.sf.l2j.util.Point3D;

public final class RequestExMagicSkillUseGround extends L2GameClientPacket
{
  private static final String _C__D0_2F_REQUESTEXMAGICSKILLUSEGROUND = "[C] D0:2F RequestExMagicSkillUseGround";
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
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }

    int level = activeChar.getSkillLevel(_skillId);
    if (level <= 0)
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);

    if (skill != null)
    {
      activeChar.setCurrentSkillWorldPosition(new Point3D(_x, _y, _z));

      activeChar.setHeading(Util.calculateHeadingFrom(activeChar.getX(), activeChar.getY(), _x, _y));
      activeChar.broadcastPacket(new ValidateLocation(activeChar));

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
    return "[C] D0:2F RequestExMagicSkillUseGround";
  }
}