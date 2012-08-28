package l2m.gameserver.skills.skillclasses;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.ExAutoSoulShot;
import l2m.gameserver.network.serverpackets.InventoryUpdate;
import l2m.gameserver.network.serverpackets.ShortCutInit;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.item.WeaponTemplate;

public class KamaelWeaponExchange extends Skill
{
  public KamaelWeaponExchange(StatsSet set)
  {
    super(set);
  }

  public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
  {
    Player p = (Player)activeChar;
    if ((p.isInStoreMode()) || (p.isProcessingRequest())) {
      return false;
    }
    ItemInstance item = activeChar.getActiveWeaponInstance();
    if ((item != null) && (((WeaponTemplate)item.getTemplate()).getKamaelConvert() == 0))
    {
      activeChar.sendPacket(SystemMsg.YOU_CANNOT_CONVERT_THIS_ITEM);
      return false;
    }

    return super.checkCondition(activeChar, target, forceUse, dontMove, first);
  }

  public void useSkill(Creature activeChar, List<Creature> targets)
  {
    Player player = (Player)activeChar;
    ItemInstance item = activeChar.getActiveWeaponInstance();
    if (item == null) {
      return;
    }
    int itemId = ((WeaponTemplate)item.getTemplate()).getKamaelConvert();

    if (itemId == 0) {
      return;
    }
    player.getInventory().unEquipItem(item);
    player.sendPacket(new InventoryUpdate().addRemovedItem(item));
    item.setItemId(itemId);

    player.sendPacket(new ShortCutInit(player));
    for (Iterator i$ = player.getAutoSoulShot().iterator(); i$.hasNext(); ) { int shotId = ((Integer)i$.next()).intValue();
      player.sendPacket(new ExAutoSoulShot(shotId, true));
    }
    player.sendPacket(new InventoryUpdate().addNewItem(item));
    player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EQUIPPED_YOUR_S1).addItemNameWithAugmentation(item));
    player.getInventory().equipItem(item);
  }
}