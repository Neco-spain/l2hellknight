package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.StatsSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Default extends Skill
{
  private static final Logger _log = LoggerFactory.getLogger(Default.class);

  public Default(StatsSet set)
  {
    super(set);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    if (activeChar.isPlayer())
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Default.NotImplemented", (Player)activeChar, new Object[0]).addNumber(getId()).addString("" + getSkillType()));
    _log.warn("NOTDONE skill: " + getId() + ", used by" + activeChar);
    activeChar.sendActionFailed();
  }
}