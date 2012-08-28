package l2p.gameserver.clientpackets;

import l2p.gameserver.Config;
import l2p.gameserver.model.Creature;
import l2p.gameserver.model.Effect;
import l2p.gameserver.model.EffectList;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.Skill.SkillType;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.skills.EffectType;
import l2p.gameserver.skills.effects.EffectTemplate;

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
        if ((!e.isOffensive()) && ((!e.getSkill().isMusic()) || (Config.DANCE_CANCEL_BUFF)) && (e.getSkill().isSelfDispellable()) && (e.getSkill().getSkillType() != Skill.SkillType.TRANSFORMATION) && (e.getTemplate().getEffectType() != EffectType.Hourglass))
        {
          e.exit();
        }
        else return;
  }
}