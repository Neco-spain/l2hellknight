package l2m.gameserver.skills.skillclasses;

import java.util.List;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.ItemTemplate;

public class EnergyReplenish extends Skill
{
  private int _addEnergy;

  public EnergyReplenish(StatsSet set)
  {
    super(set);
    _addEnergy = set.getInteger("addEnergy");
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    if (!super.checkCondition(activeChar, target, forceUse, dontMove, first)) {
      return false;
    }
    if (!activeChar.isPlayer()) {
      return false;
    }
    Player player = (Player)activeChar;
    ItemInstance item = player.getInventory().getPaperdollItem(18);
    if ((item == null) || (item.getTemplate().getAgathionEnergy() - item.getAgathionEnergy() < _addEnergy))
    {
      player.sendPacket(SystemMsg.YOUR_ENERGY_CANNOT_BE_REPLENISHED_BECAUSE_CONDITIONS_ARE_NOT_MET);
      return false;
    }

    return true;
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    for (Creature cha : targets)
    {
      cha.setAgathionEnergy(cha.getAgathionEnergy() + _addEnergy);
      cha.sendPacket(new SystemMessage2(SystemMsg.ENERGY_S1_REPLENISHED).addInteger(_addEnergy));
    }
  }
}