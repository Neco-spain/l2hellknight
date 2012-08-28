package l2m.gameserver.network.clientpackets;

import l2m.gameserver.Config;
import l2m.gameserver.ai.SummonAI;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.instances.PetInstance;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PetInventory;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

public class RequestPetUseItem extends L2GameClientPacket
{
  private int _objectId;

  protected void readImpl()
  {
    _objectId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    activeChar.setActive();

    PetInstance pet = (PetInstance)activeChar.getPet();
    if (pet == null) {
      return;
    }
    ItemInstance item = pet.getInventory().getItemByObjectId(_objectId);

    if ((item == null) || (item.getCount() < 1L)) {
      return;
    }
    if ((activeChar.isAlikeDead()) || (pet.isDead()) || (pet.isOutOfControl()))
    {
      activeChar.sendPacket(new SystemMessage(113).addItemName(item.getItemId()));
      return;
    }

    if (pet.tryFeedItem(item)) {
      return;
    }
    if (ArrayUtils.contains(Config.ALT_ALLOWED_PET_POTIONS, item.getItemId()))
    {
      Skill[] skills = item.getTemplate().getAttachedSkills();
      if (skills.length > 0)
      {
        for (Skill skill : skills)
        {
          Creature aimingTarget = skill.getAimingTarget(pet, pet.getTarget());
          if (skill.checkCondition(pet, aimingTarget, false, false, true))
            pet.getAI().Cast(skill, aimingTarget, false, false);
        }
      }
      return;
    }

    SystemMessage sm = ItemFunctions.checkIfCanEquip(pet, item);
    if (sm == null)
    {
      if (item.isEquipped())
        pet.getInventory().unEquipItem(item);
      else
        pet.getInventory().equipItem(item);
      pet.broadcastCharInfo();
      return;
    }

    activeChar.sendPacket(sm);
  }
}