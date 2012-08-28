package net.sf.l2j.gameserver.model.actor.instance;

import java.util.List;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FestivalMonsterInstance extends L2MonsterInstance
{
  protected int _bonusMultiplier = 1;

  public L2FestivalMonsterInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public void setOfferingBonus(int bonusMultiplier)
  {
    _bonusMultiplier = bonusMultiplier;
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return !(attacker instanceof L2FestivalMonsterInstance);
  }

  public boolean isAggressive()
  {
    return true;
  }

  public boolean hasRandomAnimation()
  {
    return false;
  }

  public void doItemDrop(L2Character lastAttacker)
  {
    L2PcInstance killingChar = null;

    if (!(lastAttacker instanceof L2PcInstance)) {
      return;
    }
    killingChar = (L2PcInstance)lastAttacker;
    L2Party associatedParty = killingChar.getParty();

    if (associatedParty == null) {
      return;
    }
    L2PcInstance partyLeader = (L2PcInstance)associatedParty.getPartyMembers().get(0);
    L2ItemInstance addedOfferings = partyLeader.getInventory().addItem("Sign", 5901, _bonusMultiplier, partyLeader, this);

    InventoryUpdate iu = new InventoryUpdate();

    if (addedOfferings.getCount() != _bonusMultiplier)
      iu.addModifiedItem(addedOfferings);
    else {
      iu.addNewItem(addedOfferings);
    }
    partyLeader.sendPacket(iu);

    super.doItemDrop(lastAttacker);
  }
}