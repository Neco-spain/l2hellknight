package l2m.gameserver.skills;

import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.items.ItemInstance;

public final class Env
{
  public Creature character;
  public Creature target;
  public ItemInstance item;
  public Skill skill;
  public double value;

  public Env()
  {
  }

  public Env(Creature cha, Creature tar, Skill sk)
  {
    character = cha;
    target = tar;
    skill = sk;
  }
}