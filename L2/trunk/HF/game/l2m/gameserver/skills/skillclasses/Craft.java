package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.network.serverpackets.RecipeBookItemList;
import l2m.gameserver.templates.StatsSet;

public class Craft extends Skill
{
  private final boolean _dwarven;

  public Craft(StatsSet set)
  {
    super(set);
    _dwarven = set.getBool("isDwarven");
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    Player p = (Player)activeChar;
    if ((p.isInStoreMode()) || (p.isProcessingRequest())) {
      return false;
    }
    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    activeChar.sendPacket(new RecipeBookItemList((Player)activeChar, _dwarven));
  }
}