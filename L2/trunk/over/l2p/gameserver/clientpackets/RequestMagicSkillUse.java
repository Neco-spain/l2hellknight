package l2p.gameserver.clientpackets;

import java.util.Collection;
import l2p.gameserver.ai.PlayerAI;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.attachment.FlagItemAttachment;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket
{
  private Integer _magicId;
  private boolean _ctrlPressed;
  private boolean _shiftPressed;

  protected void readImpl()
  {
    _magicId = Integer.valueOf(readD());
    _ctrlPressed = (readD() != 0);
    _shiftPressed = (readC() != 0);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    activeChar.setActive();

    if (activeChar.isOutOfControl())
    {
      activeChar.sendActionFailed();
      return;
    }

    Skill skill = SkillTable.getInstance().getInfo(_magicId.intValue(), activeChar.getSkillLevel(_magicId));
    if (skill != null)
    {
      if ((!skill.isActive()) && (!skill.isToggle())) {
        return;
      }
      FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
      if ((attachment != null) && (!attachment.canCast(activeChar, skill)))
      {
        activeChar.sendActionFailed();
        return;
      }

      if ((activeChar.getTransformation() != 0) && (!activeChar.getAllSkills().contains(skill))) {
        return;
      }
      if ((skill.isToggle()) && 
        (activeChar.getEffectList().getEffectsBySkill(skill) != null))
      {
        activeChar.getEffectList().stopEffect(skill.getId());
        activeChar.sendActionFailed();
        return;
      }

      Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());

      activeChar.setGroundSkillLoc(null);
      activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
    }
    else {
      activeChar.sendActionFailed();
    }
  }
}