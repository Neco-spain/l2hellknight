package l2p.gameserver.utils;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.handler.items.IItemHandler;
import l2p.gameserver.idfactory.IdFactory;
import l2p.gameserver.instancemanager.CursedWeaponsManager;
import l2p.gameserver.model.Playable;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.base.Element;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.instances.PetInstance;
import l2p.gameserver.model.items.Inventory;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.ItemInstance.ItemLocation;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.model.items.attachment.PickableAttachment;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.serverpackets.L2GameServerPacket;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.tables.PetDataTable;
import l2p.gameserver.templates.item.ArmorTemplate.ArmorType;
import l2p.gameserver.templates.item.ItemTemplate;
import l2p.gameserver.templates.item.ItemTemplate.Grade;
import l2p.gameserver.templates.item.WeaponTemplate.WeaponType;
import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.sets.IntSet;

public final class ItemFunctions
{
  public static final int[][] catalyst = { { 12362, 14078, 14702 }, { 12363, 14079, 14703 }, { 12364, 14080, 14704 }, { 12365, 14081, 14705 }, { 12366, 14082, 14706 }, { 12367, 14083, 14707 }, { 12368, 14084, 14708 }, { 12369, 14085, 14709 }, { 12370, 14086, 14710 }, { 12371, 14087, 14711 } };

  public static ItemInstance createItem(int itemId)
  {
    ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
    item.setLocation(ItemInstance.ItemLocation.VOID);
    item.setCount(1L);

    return item;
  }

  public static void addItem(Playable playable, int itemId, long count, boolean notify)
  {
    if ((playable == null) || (count < 1L))
      return;
    Playable player;
    Playable player;
    if (playable.isSummon())
      player = playable.getPlayer();
    else {
      player = playable;
    }
    ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
    if (t.isStackable())
      player.getInventory().addItem(itemId, count);
    else {
      for (long i = 0L; i < count; i += 1L)
        player.getInventory().addItem(itemId, 1L);
    }
    if (notify)
      player.sendPacket(SystemMessage2.obtainItems(itemId, count, 0));
  }

  public static long getItemCount(Playable playable, int itemId)
  {
    if (playable == null)
      return 0L;
    Playable player = playable.getPlayer();
    return player.getInventory().getCountOf(itemId);
  }

  public static long removeItem(Playable playable, int itemId, long count, boolean notify)
  {
    long removed = 0L;
    if ((playable == null) || (count < 1L)) {
      return removed;
    }
    Playable player = playable.getPlayer();

    ItemTemplate t = ItemHolder.getInstance().getTemplate(itemId);
    if (t.isStackable())
    {
      if (player.getInventory().destroyItemByItemId(itemId, count))
        removed = count;
    }
    else {
      for (long i = 0L; i < count; i += 1L)
        if (player.getInventory().destroyItemByItemId(itemId, 1L))
          removed += 1L;
    }
    if ((removed > 0L) && (notify)) {
      player.sendPacket(SystemMessage2.removeItems(itemId, removed));
    }
    return removed;
  }

  public static final boolean isClanApellaItem(int itemId)
  {
    return ((itemId >= 7860) && (itemId <= 7879)) || ((itemId >= 9830) && (itemId <= 9839));
  }

  public static final SystemMessage checkIfCanEquip(PetInstance pet, ItemInstance item)
  {
    if (!item.isEquipable()) {
      return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
    }
    int petId = pet.getNpcId();

    if ((item.getTemplate().isPendant()) || ((PetDataTable.isWolf(petId)) && (item.getTemplate().isForWolf())) || ((PetDataTable.isHatchling(petId)) && (item.getTemplate().isForHatchling())) || ((PetDataTable.isStrider(petId)) && (item.getTemplate().isForStrider())) || ((PetDataTable.isGWolf(petId)) && (item.getTemplate().isForGWolf())) || ((PetDataTable.isBabyPet(petId)) && (item.getTemplate().isForPetBaby())) || ((PetDataTable.isImprovedBabyPet(petId)) && (item.getTemplate().isForPetBaby())))
    {
      return null;
    }
    return Msg.ITEM_NOT_AVAILABLE_FOR_PETS;
  }

  public static final L2GameServerPacket checkIfCanEquip(Player player, ItemInstance item)
  {
    int itemId = item.getItemId();
    int targetSlot = item.getTemplate().getBodyPart();
    Clan clan = player.getClan();

    if (((item.isHeroWeapon()) || (item.getItemId() == 6842)) && (!player.isHero())) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }

    if ((player.getRace() == Race.kamael) && ((item.getItemType() == ArmorTemplate.ArmorType.HEAVY) || (item.getItemType() == ArmorTemplate.ArmorType.MAGIC) || (item.getItemType() == ArmorTemplate.ArmorType.SIGIL) || (item.getItemType() == WeaponTemplate.WeaponType.NONE))) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }

    if ((player.getRace() != Race.kamael) && ((item.getItemType() == WeaponTemplate.WeaponType.CROSSBOW) || (item.getItemType() == WeaponTemplate.WeaponType.RAPIER) || (item.getItemType() == WeaponTemplate.WeaponType.ANCIENTSWORD))) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }
    if ((itemId >= 7850) && (itemId <= 7859) && (player.getLvlJoinedAcademy() == 0)) {
      return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;
    }
    if ((isClanApellaItem(itemId)) && (player.getPledgeClass() < 4)) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }
    if ((item.getItemType() == WeaponTemplate.WeaponType.DUALDAGGER) && (player.getSkillLevel(Integer.valueOf(923)) < 1)) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }

    if ((ArrayUtils.contains(ItemTemplate.ITEM_ID_CASTLE_CIRCLET, itemId)) && ((clan == null) || (itemId != ItemTemplate.ITEM_ID_CASTLE_CIRCLET[clan.getCastle()]))) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }

    if ((itemId == 6841) && ((clan == null) || (!player.isClanLeader()) || (clan.getCastle() == 0))) {
      return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
    }

    if ((targetSlot == 16384) || (targetSlot == 256) || (targetSlot == 128))
    {
      if ((itemId != player.getInventory().getPaperdollItemId(7)) && (CursedWeaponsManager.getInstance().isCursed(player.getInventory().getPaperdollItemId(7))))
        return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
      if ((player.isCursedWeaponEquipped()) && (itemId != player.getCursedWeaponEquippedId())) {
        return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
      }
    }

    if (item.getTemplate().isCloak())
    {
      if ((item.getName().contains("Knight")) && ((player.getPledgeClass() < 3) || (player.getCastle() == null))) {
        return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
      }

      if ((item.getName().contains("Kamael")) && (player.getRace() != Race.kamael)) {
        return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
      }

      if (!player.getOpenCloak()) {
        return Msg.THE_CLOAK_CANNOT_BE_EQUIPPED_BECAUSE_A_NECESSARY_ITEM_IS_NOT_EQUIPPED;
      }
    }
    if (targetSlot == 4194304)
    {
      int count = player.getTalismanCount();
      if (count <= 0) {
        return new SystemMessage2(SystemMsg.YOU_CANNOT_WEAR_S1_BECAUSE_YOU_ARE_NOT_WEARING_A_BRACELET).addItemName(itemId);
      }

      for (int slot = 19; slot <= 24; slot++)
      {
        ItemInstance deco = player.getInventory().getPaperdollItem(slot);
        if (deco == null)
          continue;
        if (deco == item) {
          return null;
        }
        count--; if ((count <= 0) || (deco.getItemId() == itemId)) {
          return new SystemMessage2(SystemMsg.YOU_CANNOT_EQUIP_S1_BECAUSE_YOU_DO_NOT_HAVE_ANY_AVAILABLE_SLOTS).addItemName(itemId);
        }
      }
    }
    return null;
  }

  public static boolean checkIfCanPickup(Playable playable, ItemInstance item)
  {
    Player player = playable.getPlayer();
    return (item.getDropTimeOwner() <= System.currentTimeMillis()) || (item.getDropPlayers().contains(player.getObjectId()));
  }

  public static boolean canAddItem(Player player, ItemInstance item)
  {
    if (!player.getInventory().validateWeight(item))
    {
      player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
      return false;
    }

    if (!player.getInventory().validateCapacity(item))
    {
      player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
      return false;
    }

    if (!item.getTemplate().getHandler().pickupItem(player, item)) {
      return false;
    }
    PickableAttachment attachment = (item.getAttachment() instanceof PickableAttachment) ? (PickableAttachment)item.getAttachment() : null;

    return (attachment == null) || (attachment.canPickUp(player));
  }

  public static final boolean checkIfCanDiscard(Player player, ItemInstance item)
  {
    if (item.isHeroWeapon()) {
      return false;
    }
    if ((PetDataTable.isPetControlItem(item)) && (player.isMounted())) {
      return false;
    }
    if (player.getPetControlItem() == item) {
      return false;
    }
    if (player.getEnchantScroll() == item) {
      return false;
    }
    if (item.isCursed()) {
      return false;
    }

    return !item.getTemplate().isQuest();
  }

  public static final boolean isBlessedEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 6569:
    case 6570:
    case 6571:
    case 6572:
    case 6573:
    case 6574:
    case 6575:
    case 6576:
    case 6577:
    case 6578:
    case 21582:
      return true;
    }
    return false;
  }

  public static final boolean isAncientEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 20519:
    case 20520:
    case 22014:
    case 22015:
    case 22016:
    case 22017:
      return true;
    }
    return false;
  }

  public static final boolean isDestructionWpnEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 22221:
    case 22223:
    case 22225:
    case 22227:
    case 22229:
      return true;
    case 22222:
    case 22224:
    case 22226:
    case 22228: } return false;
  }

  public static final boolean isDestructionArmEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 22222:
    case 22224:
    case 22226:
    case 22228:
    case 22230:
      return true;
    case 22223:
    case 22225:
    case 22227:
    case 22229: } return false;
  }

  public static final boolean isItemMallEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 20517:
    case 20518:
    case 22006:
    case 22007:
    case 22008:
    case 22009:
    case 22010:
    case 22011:
    case 22012:
    case 22013:
      return true;
    }
    return isAncientEnchantScroll(itemId);
  }

  public static final boolean isDivineEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 20521:
    case 20522:
    case 22018:
    case 22019:
    case 22020:
    case 22021:
      return true;
    }
    return false;
  }

  public static final boolean isCrystallEnchantScroll(int itemId)
  {
    switch (itemId)
    {
    case 731:
    case 732:
    case 949:
    case 950:
    case 953:
    case 954:
    case 957:
    case 958:
    case 961:
    case 962:
      return true;
    }
    return false;
  }

  public static final int getEnchantCrystalId(ItemInstance item, ItemInstance scroll, ItemInstance catalyst)
  {
    boolean scrollValid = false; boolean catalystValid = false;

    for (int scrollId : getEnchantScrollId(item)) {
      if (scroll.getItemId() != scrollId)
        continue;
      scrollValid = true;
      break;
    }

    if (catalyst == null)
      catalystValid = true;
    else {
      for (int catalystId : getEnchantCatalystId(item)) {
        if (catalystId != catalyst.getItemId())
          continue;
        catalystValid = true;
        break;
      }
    }
    if ((scrollValid) && (catalystValid)) {
      switch (item.getCrystalType().cry)
      {
      case 0:
        return 0;
      case 1458:
        return 1458;
      case 1459:
        return 1459;
      case 1460:
        return 1460;
      case 1461:
        return 1461;
      case 1462:
        return 1462;
      }
    }
    return -1;
  }

  public static final int[] getEnchantScrollId(ItemInstance item)
  {
    if (item.getTemplate().getType2() == 0)
      switch (item.getCrystalType().cry)
      {
      case 0:
        return new int[] { 13540 };
      case 1458:
        return new int[] { 955, 6575, 957, 22006, 22229 };
      case 1459:
        return new int[] { 951, 6573, 953, 22007, 22227 };
      case 1460:
        return new int[] { 947, 6571, 949, 22008, 22014, 22018, 22225 };
      case 1461:
        return new int[] { 729, 6569, 731, 22009, 22015, 22019, 22223 };
      case 1462:
        return new int[] { 959, 6577, 961, 20517, 20519, 20521, 22221 };
      }
    else if ((item.getTemplate().getType2() == 1) || (item.getTemplate().getType2() == 2))
      switch (item.getCrystalType().cry)
      {
      case 0:
        return new int[] { 21581, 21582 };
      case 1458:
        return new int[] { 956, 6576, 958, 22010, 22230 };
      case 1459:
        return new int[] { 952, 6574, 954, 22011, 22228 };
      case 1460:
        return new int[] { 948, 6572, 950, 22012, 22016, 22020, 22226 };
      case 1461:
        return new int[] { 730, 6570, 732, 22013, 22017, 22021, 22224 };
      case 1462:
        return new int[] { 960, 6578, 962, 20518, 20520, 20522, 22222 };
      }
    return new int[0];
  }

  public static final int[] getEnchantCatalystId(ItemInstance item)
  {
    if (item.getTemplate().getType2() == 0)
      switch (item.getCrystalType().cry)
      {
      case 1461:
        return catalyst[3];
      case 1460:
        return catalyst[2];
      case 1459:
        return catalyst[1];
      case 1458:
        return catalyst[0];
      case 1462:
        return catalyst[4];
      }
    else if ((item.getTemplate().getType2() == 1) || (item.getTemplate().getType2() == 2))
      switch (item.getCrystalType().cry)
      {
      case 1461:
        return catalyst[8];
      case 1460:
        return catalyst[7];
      case 1459:
        return catalyst[6];
      case 1458:
        return catalyst[5];
      case 1462:
        return catalyst[9];
      }
    return new int[] { 0, 0, 0 };
  }

  public static final int getCatalystPower(int itemId)
  {
    for (int i = 0; i < catalyst.length; i++) {
      for (int id : catalyst[i])
        if (id == itemId)
          switch (i)
          {
          case 0:
            return 20;
          case 1:
            return 18;
          case 2:
            return 15;
          case 3:
            return 12;
          case 4:
            return 10;
          case 5:
            return 35;
          case 6:
            return 27;
          case 7:
            return 23;
          case 8:
            return 18;
          case 9:
            return 15;
          }
    }
    return 0;
  }

  public static final boolean checkCatalyst(ItemInstance item, ItemInstance catalyst)
  {
    if ((item == null) || (catalyst == null)) {
      return false;
    }
    int current = item.getEnchantLevel();
    if (current >= (item.getTemplate().getBodyPart() == 32768 ? 4 : 3)) { if (current <= 8); } else return false;

    for (int catalystRequired : getEnchantCatalystId(item)) {
      if (catalystRequired == catalyst.getItemId())
        return true;
    }
    return false;
  }

  public static final boolean isLifeStone(int itemId)
  {
    return ((itemId >= 8723) && (itemId <= 8762)) || ((itemId >= 9573) && (itemId <= 9576)) || ((itemId >= 10483) && (itemId <= 10486)) || ((itemId >= 14166) && (itemId <= 14169)) || ((itemId >= 16160) && (itemId <= 16167));
  }

  public static final boolean isAccessoryLifeStone(int itemId)
  {
    return ((itemId >= 12754) && (itemId <= 12763)) || ((itemId >= 12840) && (itemId <= 12851)) || (itemId == 12821) || (itemId == 12822) || (itemId == 14008) || (itemId == 16177) || (itemId == 16178);
  }

  public static final int getLifeStoneGrade(int itemId)
  {
    switch (itemId)
    {
    case 8723:
    case 8724:
    case 8725:
    case 8726:
    case 8727:
    case 8728:
    case 8729:
    case 8730:
    case 8731:
    case 8732:
    case 9573:
    case 10483:
    case 14166:
    case 16160:
    case 16164:
      return 0;
    case 8733:
    case 8734:
    case 8735:
    case 8736:
    case 8737:
    case 8738:
    case 8739:
    case 8740:
    case 8741:
    case 8742:
    case 9574:
    case 10484:
    case 14167:
    case 16161:
    case 16165:
      return 1;
    case 8743:
    case 8744:
    case 8745:
    case 8746:
    case 8747:
    case 8748:
    case 8749:
    case 8750:
    case 8751:
    case 8752:
    case 9575:
    case 10485:
    case 14168:
    case 16162:
    case 16166:
      return 2;
    case 8753:
    case 8754:
    case 8755:
    case 8756:
    case 8757:
    case 8758:
    case 8759:
    case 8760:
    case 8761:
    case 8762:
    case 9576:
    case 10486:
    case 14169:
    case 16163:
    case 16167:
      return 3;
    }
    return 0;
  }

  public static final int getLifeStoneLevel(int itemId)
  {
    switch (itemId)
    {
    case 8723:
    case 8733:
    case 8743:
    case 8753:
    case 12754:
    case 12840:
      return 1;
    case 8724:
    case 8734:
    case 8744:
    case 8754:
    case 12755:
    case 12841:
      return 2;
    case 8725:
    case 8735:
    case 8745:
    case 8755:
    case 12756:
    case 12842:
      return 3;
    case 8726:
    case 8736:
    case 8746:
    case 8756:
    case 12757:
    case 12843:
      return 4;
    case 8727:
    case 8737:
    case 8747:
    case 8757:
    case 12758:
    case 12844:
      return 5;
    case 8728:
    case 8738:
    case 8748:
    case 8758:
    case 12759:
    case 12845:
      return 6;
    case 8729:
    case 8739:
    case 8749:
    case 8759:
    case 12760:
    case 12846:
      return 7;
    case 8730:
    case 8740:
    case 8750:
    case 8760:
    case 12761:
    case 12847:
      return 8;
    case 8731:
    case 8741:
    case 8751:
    case 8761:
    case 12762:
    case 12848:
      return 9;
    case 8732:
    case 8742:
    case 8752:
    case 8762:
    case 12763:
    case 12849:
      return 10;
    case 9573:
    case 9574:
    case 9575:
    case 9576:
    case 12821:
    case 12850:
      return 11;
    case 10483:
    case 10484:
    case 10485:
    case 10486:
    case 12822:
    case 12851:
      return 12;
    case 14008:
    case 14166:
    case 14167:
    case 14168:
    case 14169:
      return 13;
    case 16160:
    case 16161:
    case 16162:
    case 16163:
    case 16177:
      return 14;
    case 16164:
    case 16165:
    case 16166:
    case 16167:
    case 16178:
      return 15;
    }
    return 1;
  }

  public static Element getEnchantAttributeStoneElement(int itemId, boolean isArmor)
  {
    Element element = Element.NONE;
    switch (itemId)
    {
    case 9546:
    case 9552:
    case 10521:
      element = Element.FIRE;
      break;
    case 9547:
    case 9553:
    case 10522:
      element = Element.WATER;
      break;
    case 9548:
    case 9554:
    case 10523:
      element = Element.EARTH;
      break;
    case 9549:
    case 9555:
    case 10524:
      element = Element.WIND;
      break;
    case 9550:
    case 9556:
    case 10525:
      element = Element.UNHOLY;
      break;
    case 9551:
    case 9557:
    case 10526:
      element = Element.HOLY;
    }

    if (isArmor) {
      return Element.getReverseElement(element);
    }
    return element;
  }
}