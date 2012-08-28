package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2CubicInstance;

public final class Env
{
  public L2Character player;
  public L2CubicInstance cubic;
  public L2Character target;
  public L2ItemInstance item;
  public L2Skill skill;
  public double value;
  public double baseValue;
  public boolean skillMastery = false;
}