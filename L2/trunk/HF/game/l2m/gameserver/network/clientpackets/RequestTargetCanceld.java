package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.components.SystemMsg;

public class RequestTargetCanceld extends L2GameClientPacket
{
  private int _unselect;

  protected void readImpl()
  {
    _unselect = readH();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isLockedTarget())
    {
      if (activeChar.isClanAirShipDriver()) {
        activeChar.sendPacket(SystemMsg.THIS_ACTION_IS_PROHIBITED_WHILE_STEERING);
      }
      activeChar.sendActionFailed();
      return;
    }

    if (_unselect == 0)
    {
      if (activeChar.isCastingNow())
      {
        Skill skill = activeChar.getCastingSkill();
        activeChar.abortCast((skill != null) && ((skill.isHandler()) || (skill.getHitTime() > 1000)), false);
      }
      else if (activeChar.getTarget() != null) {
        activeChar.setTarget(null);
      }
    } else if (activeChar.getTarget() != null)
      activeChar.setTarget(null);
  }
}