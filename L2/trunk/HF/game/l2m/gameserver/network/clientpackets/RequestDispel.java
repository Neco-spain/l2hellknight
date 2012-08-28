package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Effect;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Skill.SkillType;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.skills.EffectType;
import l2m.gameserver.skills.effects.EffectTemplate;

public class RequestDispel extends L2GameClientPacket
{
  private int _objectId;
  private int _id;
  private int _level;

  protected void readImpl()
    throws Exception
  {
    _objectId = readD();
    _id = readD();
    _level = readD();
  }

  protected void runImpl()
    throws Exception
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || ((activeChar.getObjectId() != _objectId) && (activeChar.getPet() == null))) {
      return;
    }
    Creature target = activeChar;
    if (activeChar.getObjectId() != _objectId) {
      target = activeChar.getPet();
    }
    for (Effect e : target.getEffectList().getAllEffects())
      if ((e.getDisplayId() == _id) && (e.getDisplayLevel() == _level))
        if ((!e.isOffensive()) && (!e.getSkill().isMusic()) && (e.getSkill().isSelfDispellable()) && (e.getSkill().getSkillType() != Skill.SkillType.TRANSFORMATION) && (e.getTemplate().getEffectType() != EffectType.Hourglass))
          e.exit();
        else
          return;
  }
}