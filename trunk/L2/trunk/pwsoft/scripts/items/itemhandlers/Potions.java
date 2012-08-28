package scripts.items.itemhandlers;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExUseSharedGroupItem;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import scripts.items.IItemHandler;

public class Potions
  implements IItemHandler
{
  protected static final Logger _log = Logger.getLogger(Potions.class.getName());
  private int _herbstask;
  private static final int[] ITEM_IDS = { 65, 725, 726, 727, 728, 734, 735, 1060, 1061, 1062, 1073, 1374, 1375, 1539, 1540, 5591, 5592, 6035, 6036, 8193, 8194, 8195, 8196, 8197, 8198, 8199, 8200, 8201, 8202, 8600, 8601, 8602, 8603, 8604, 8605, 8606, 8607, 8608, 8609, 8610, 8611, 8612, 8613, 8614, 8786, 8787, 8622, 8623, 8624, 8625, 8626, 8627, 8628, 8629, 8630, 8631, 8632, 8633, 8634, 8635, 8636, 8637, 8638, 8639 };

  public Potions()
  {
    _herbstask = 0;
  }

  public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    boolean res = false;
    L2PcInstance activeChar;
    if (playable.isPlayer()) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if (playable.isPet())
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    if (activeChar.isOutOfControl()) {
      activeChar.sendActionFailed();
      return;
    }

    if (item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY) {
      activeChar.sendActionFailed();
      return;
    }

    int itemId = item.getItemId();

    if (!TvTEvent.onPotionUse(activeChar.getName(), itemId)) {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInOlympiadMode()) {
      activeChar.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return;
    }

    int itmobj = item.getObjectId();
    switch (itemId)
    {
    case 726:
      manaPotion(activeChar, itemId);
      break;
    case 728:
      manaPotion(activeChar, itemId);
      break;
    case 65:
      res = usePotion(activeChar, 2001, 1);
      break;
    case 725:
      if (!isEffectReplaceable(activeChar, 2002, itemId)) {
        return;
      }
      res = usePotion(activeChar, 2002, 1);
      break;
    case 727:
      if (!isEffectReplaceable(activeChar, 2032, itemId)) {
        return;
      }
      res = usePotion(activeChar, 2032, 1);
      break;
    case 734:
      res = usePotion(activeChar, 2011, 1);
      break;
    case 735:
      res = usePotion(activeChar, 2012, 1);
      break;
    case 1060:
    case 1073:
      if (!isEffectReplaceable(activeChar, 2031, itemId)) {
        return;
      }
      res = usePotion(activeChar, 2031, 1);
      break;
    case 1061:
      if (!isEffectReplaceable(activeChar, 2032, itemId)) {
        return;
      }
      res = usePotion(activeChar, 2032, 1);
      break;
    case 1062:
      res = usePotion(activeChar, 2033, 1);
      break;
    case 1374:
      res = usePotion(activeChar, 2034, 1);
      break;
    case 1375:
      res = usePotion(activeChar, 2035, 1);
      break;
    case 1539:
      if (!isEffectReplaceable(activeChar, 2037, itemId)) {
        return;
      }
      res = usePotion(activeChar, 2037, 1);
      break;
    case 1540:
      qhpPotion(activeChar, itmobj);
      break;
    case 5591:
    case 5592:
      if (activeChar.getCpReuseTime(itemId) < Config.CP_REUSE_TIME)
      {
        activeChar.sendActionFailed();
        return;
      }
      activeChar.setCpReuseTime(itemId);

      cpPotion(activeChar, itmobj, itemId == 5591 ? 50 : 200);
      break;
    case 6035:
      res = usePotion(activeChar, 2169, 1);
      break;
    case 6036:
      res = usePotion(activeChar, 2169, 2);
      break;
    case 8622:
    case 8623:
    case 8624:
    case 8625:
    case 8626:
    case 8627:
      if (((itemId == 8622) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8623) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8624) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8625) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8626) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8627) && (activeChar.getExpertiseIndex() == 5)))
      {
        useElixir(activeChar, 2287, activeChar.getExpertiseIndex() + 1, itemId, itmobj);
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCOMPATIBLE_ITEM_GRADE).addItemName(itemId));
        return;
      }

    case 8628:
    case 8629:
    case 8630:
    case 8631:
    case 8632:
    case 8633:
      if (((itemId == 8628) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8629) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8630) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8631) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8632) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8633) && (activeChar.getExpertiseIndex() == 5)))
      {
        useElixir(activeChar, 2288, activeChar.getExpertiseIndex() + 1, itemId, itmobj);
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCOMPATIBLE_ITEM_GRADE).addItemName(itemId));
        return;
      }

    case 8634:
    case 8635:
    case 8636:
    case 8637:
    case 8638:
    case 8639:
      if (((itemId == 8634) && (activeChar.getExpertiseIndex() == 0)) || ((itemId == 8635) && (activeChar.getExpertiseIndex() == 1)) || ((itemId == 8636) && (activeChar.getExpertiseIndex() == 2)) || ((itemId == 8637) && (activeChar.getExpertiseIndex() == 3)) || ((itemId == 8638) && (activeChar.getExpertiseIndex() == 4)) || ((itemId == 8639) && (activeChar.getExpertiseIndex() == 5)))
      {
        useElixir(activeChar, 2289, activeChar.getExpertiseIndex() + 1, itemId, itmobj);
      } else {
        activeChar.sendPacket(SystemMessage.id(SystemMessageId.INCOMPATIBLE_ITEM_GRADE).addItemName(itemId));
        return;
      }

    case 8600:
      res = usePotion(activeChar, 2278, 1);
      break;
    case 8601:
      res = usePotion(activeChar, 2278, 2);
      break;
    case 8602:
      res = usePotion(activeChar, 2278, 3);
      break;
    case 8603:
      res = usePotion(activeChar, 2279, 1);
      break;
    case 8604:
      res = usePotion(activeChar, 2279, 2);
      break;
    case 8605:
      res = usePotion(activeChar, 2279, 3);
      break;
    case 8606:
      res = usePotion(activeChar, 2280, 1);
      break;
    case 8607:
      res = usePotion(activeChar, 2281, 1);
      break;
    case 8608:
      res = usePotion(activeChar, 2282, 1);
      break;
    case 8609:
      res = usePotion(activeChar, 2283, 1);
      break;
    case 8610:
      res = usePotion(activeChar, 2284, 1);
      break;
    case 8611:
      res = usePotion(activeChar, 2285, 1);
      break;
    case 8612:
      res = usePotion(activeChar, 2280, 1);
      res = usePotion(activeChar, 2282, 1);
      res = usePotion(activeChar, 2284, 1);
      break;
    case 8613:
      res = usePotion(activeChar, 2281, 1);
      res = usePotion(activeChar, 2283, 1);
      break;
    case 8614:
      res = usePotion(activeChar, 2278, 3);
      res = usePotion(activeChar, 2279, 3);
      break;
    case 8193:
      if (activeChar.getSkillLevel(1315) <= 3) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 1);
      break;
    case 8194:
      if (activeChar.getSkillLevel(1315) <= 6) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 2);
      break;
    case 8195:
      if (activeChar.getSkillLevel(1315) <= 9) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 3);
      break;
    case 8196:
      if (activeChar.getSkillLevel(1315) <= 12) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 4);
      break;
    case 8197:
      if (activeChar.getSkillLevel(1315) <= 15) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 5);
      break;
    case 8198:
      if (activeChar.getSkillLevel(1315) <= 18) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 6);
      break;
    case 8199:
      if (activeChar.getSkillLevel(1315) <= 21) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 7);
      break;
    case 8200:
      if (activeChar.getSkillLevel(1315) <= 24) {
        playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        playable.sendPacket(Static.NOTHING_HAPPENED);
        return;
      }
      res = usePotion(activeChar, 2274, 8);
      break;
    case 8201:
      res = usePotion(activeChar, 2274, 9);
      break;
    case 8202:
      res = usePotion(activeChar, 2275, 1);
      break;
    case 8786:
      res = usePotion(activeChar, 2305, 1);
      break;
    case 8787:
      res = usePotion(activeChar, 2305, 1);
      break;
    }

    if (res)
      activeChar.destroyItem("Consume", itmobj, 1, null, false);
  }

  private boolean isEffectReplaceable(L2PcInstance activeChar, int magicId, int itemId)
  {
    switch (magicId) {
    case 2002:
    case 2031:
    case 2032:
      if ((activeChar.getFirstEffect(2037) != null) || (activeChar.canPotion())) {
        noPotion(activeChar, itemId);
        return false;
      }
      return true;
    case 2037:
      L2Effect potion = activeChar.getFirstEffect(2037);
      if (potion != null) {
        if (potion.getTaskTime() > potion.getSkill().getBuffDuration() * 67 / 100000) {
          if (activeChar.canPotion()) {
            activeChar.clearPotions();
          }
          potion.exit();
          return true;
        }
        noPotion(activeChar, itemId);
        return false;
      }
      if (activeChar.canPotion()) {
        activeChar.clearPotions();
      }
      return true;
    }
    return true;
  }

  private void noPotion(L2PcInstance activeChar, int potion) {
    activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_PREPARED_FOR_REUSE).addItemName(potion));
  }

  public boolean usePotion(L2PcInstance activeChar, int magicId, int level) {
    if ((activeChar.isCastingNow()) && (magicId > 2277) && (magicId < 2285)) {
      _herbstask += 100;
      ThreadPoolManager.getInstance().scheduleAi(new HerbTask(activeChar, magicId, level), _herbstask, true);
    } else {
      L2Effect effect = activeChar.getFirstEffect(magicId);
      if (effect != null) {
        effect.exit();
      }

      SkillTable.getInstance().getInfo(magicId, level).getEffects(activeChar, activeChar);

      activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, magicId, level, 1, 0));
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(magicId));
      return true;
    }
    return false;
  }

  public void useElixir(L2PcInstance activeChar, int magicId, int level, int itemid, int itemobj) {
    if (activeChar.isSkillDisabled(magicId)) {
      activeChar.sendPacket(SystemMessage.id(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(magicId));
      return;
    }

    activeChar.destroyItem("Consume", itemobj, 1, null, false);
    L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
    activeChar.doCast(skill);
    activeChar.sendPacket(new ExUseSharedGroupItem(itemid, 0, 300, 300));
  }

  public void manaPotion(L2PcInstance activeChar, int itemId) {
    activeChar.destroyItemByItemId("Consume", itemId, 1, null, false);
    if (Config.MANA_RESTORE == 2005L) {
      L2Effect potion = activeChar.getFirstEffect(2005);
      if (potion != null) {
        if (potion.getTaskTime() > potion.getSkill().getBuffDuration() * 67 / 100000) {
          potion.exit();
        } else {
          noPotion(activeChar, itemId);
          return;
        }
      }
      SkillTable.getInstance().getInfo(2005, 1).getEffects(activeChar, activeChar);
    }
    else if (activeChar.getCurrentMp() != activeChar.getMaxMp()) {
      activeChar.setCurrentMp(activeChar.getCurrentMp() + Config.MANA_RESTORE);
    }

    activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, 2240, 1, 1, 0));
  }

  public void qhpPotion(L2PcInstance activeChar, int itemobj) {
    activeChar.destroyItem("Consume", itemobj, 1, null, false);
    activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, 2038, 1, 1, 0));
    if (activeChar.getCurrentHp() != activeChar.getMaxHp())
      activeChar.setCurrentHp(activeChar.getCurrentHp() + 435.0D);
  }

  public void cpPotion(L2PcInstance activeChar, int itemobj, int restore)
  {
    activeChar.destroyItem("Consume", itemobj, 1, null, false);
    activeChar.broadcastPacket(new MagicSkillUser(activeChar, activeChar, 2166, 1, 1, 0));
    if (activeChar.getCurrentCp() != activeChar.getMaxCp())
      activeChar.setCurrentCp(activeChar.getCurrentCp() + restore);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }

  private class HerbTask
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private int _magicId;
    private int _level;

    HerbTask(L2PcInstance activeChar, int magicId, int level)
    {
      _activeChar = activeChar;
      _magicId = magicId;
      _level = level;
    }

    public void run() {
      try {
        usePotion(_activeChar, _magicId, _level);
      } catch (Throwable t) {
        Potions._log.log(Level.WARNING, "", t);
      }
    }
  }
}