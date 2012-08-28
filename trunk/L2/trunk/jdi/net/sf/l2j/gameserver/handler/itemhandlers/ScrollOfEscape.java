package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.MapRegionTable.TeleportWhereType;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.instancemanager.CustomZoneManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ScrollOfEscape
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 736, 1830, 1829, 1538, 3958, 5858, 5859, 7117, 7118, 7119, 7120, 7121, 7122, 7123, 7124, 7125, 7126, 7127, 7128, 7129, 7130, 7131, 7132, 7133, 7134, 7135, 7554, 7555, 7556, 7557, 7558, 7559, 7618, 7619 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;
    L2PcInstance activeChar = (L2PcInstance)playable;

    if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if ((Olympiad.getInstance().isRegisteredInComp(activeChar)) || (activeChar.getOlympiadGameId() > 0))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT));
      return;
    }

    if ((activeChar.isMovementDisabled()) || (activeChar.isAlikeDead()) || (activeChar.isAllSkillsDisabled())) {
      return;
    }
    if (CustomZoneManager.getInstance().checkIfInZone("NoEscape", activeChar))
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isSitting())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
      return;
    }

    if ((GrandBossManager.getInstance().getZone(activeChar) != null) && (!activeChar.isGM()))
    {
      activeChar.sendPacket(new ActionFailed());
      activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a boss zone."));
      return;
    }

    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
      return;
    }

    if (activeChar.isAfraid())
    {
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    if (activeChar.isFestivalParticipant())
    {
      activeChar.sendPacket(SystemMessage.sendString("You may not use an escape skill in a festival."));
      return;
    }

    if (activeChar._inEventCTF)
    {
      activeChar.sendMessage("You may not use an escape skill in a Event.");
      return;
    }

    if (activeChar.isInJail())
    {
      activeChar.sendPacket(SystemMessage.sendString("You can not escape from jail."));
      return;
    }

    if (activeChar.isInDuel())
    {
      activeChar.sendPacket(SystemMessage.sendString("You cannot use escape skills during a duel."));
      return;
    }

    activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
    activeChar.setTarget(activeChar);
    int itemId = item.getItemId();
    int escapeSkill = (itemId == 1538) || (itemId == 5858) || (itemId == 5859) || (itemId == 3958) ? 2036 : 2013;

    if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
      return;
    }
    activeChar.disableAllSkills();

    L2Object oldtarget = activeChar.getTarget();
    activeChar.setTarget(activeChar);

    L2Skill skill = SkillTable.getInstance().getInfo(escapeSkill, 1);
    MagicSkillUser msu = new MagicSkillUser(activeChar, escapeSkill, 1, skill.getHitTime(), 0);
    activeChar.broadcastPacket(msu);
    activeChar.setTarget(oldtarget);
    SetupGauge sg = new SetupGauge(0, skill.getHitTime());
    activeChar.sendPacket(sg);

    SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
    sm.addItemName(itemId);
    activeChar.sendPacket(sm);

    EscapeFinalizer ef = new EscapeFinalizer(activeChar, itemId);
    activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleEffect(ef, skill.getHitTime()));
    activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + skill.getHitTime() / 100);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }

  static class EscapeFinalizer
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private int _itemId;

    EscapeFinalizer(L2PcInstance activeChar, int itemId)
    {
      _activeChar = activeChar;
      _itemId = itemId;
    }

    public void run()
    {
      if (_activeChar.isDead()) return;
      _activeChar.enableAllSkills();

      _activeChar.setIsIn7sDungeon(false);
      try
      {
        if (((_itemId == 1830) || (_itemId == 5859)) && (CastleManager.getInstance().getCastleByOwner(_activeChar.getClan()) != null)) {
          _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Castle);
        } else if (((_itemId == 1829) || (_itemId == 5858)) && (_activeChar.getClan() != null) && (ClanHallManager.getInstance().getClanHallByOwner(_activeChar.getClan()) != null))
        {
          _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.ClanHall);
        } else {
          if (_itemId == 5858)
          {
            _activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_HAS_NO_CLAN_HALL));
            return;
          }
          if (_itemId == 5859)
          {
            _activeChar.sendPacket(SystemMessage.sendString("Your clan does not own a castle."));
            return;
          }

          if (_itemId < 7117) {
            _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
          }
          else
            switch (_itemId)
            {
            case 7117:
              _activeChar.teleToLocation(-84318, 244579, -3730, true);
              break;
            case 7554:
              _activeChar.teleToLocation(-84318, 244579, -3730, true);
              break;
            case 7118:
              _activeChar.teleToLocation(46934, 51467, -2977, true);
              break;
            case 7555:
              _activeChar.teleToLocation(46934, 51467, -2977, true);
              break;
            case 7119:
              _activeChar.teleToLocation(9745, 15606, -4574, true);
              break;
            case 7556:
              _activeChar.teleToLocation(9745, 15606, -4574, true);
              break;
            case 7120:
              _activeChar.teleToLocation(-44836, -112524, -235, true);
              break;
            case 7557:
              _activeChar.teleToLocation(-44836, -112524, -235, true);
              break;
            case 7121:
              _activeChar.teleToLocation(115113, -178212, -901, true);
              break;
            case 7558:
              _activeChar.teleToLocation(115113, -178212, -901, true);
              break;
            case 7122:
              _activeChar.teleToLocation(-80826, 149775, -3043, true);
              break;
            case 7123:
              _activeChar.teleToLocation(-12678, 122776, -3116, true);
              break;
            case 7124:
              _activeChar.teleToLocation(15670, 142983, -2705, true);
              break;
            case 7125:
              _activeChar.teleToLocation(17836, 170178, -3507, true);
              break;
            case 7126:
              _activeChar.teleToLocation(83400, 147943, -3404, true);
              break;
            case 7559:
              _activeChar.teleToLocation(83400, 147943, -3404, true);
              break;
            case 7127:
              _activeChar.teleToLocation(105918, 109759, -3207, true);
              break;
            case 7128:
              _activeChar.teleToLocation(111409, 219364, -3545, true);
              break;
            case 7129:
              _activeChar.teleToLocation(82956, 53162, -1495, true);
              break;
            case 7130:
              _activeChar.teleToLocation(85348, 16142, -3699, true);
              break;
            case 7131:
              _activeChar.teleToLocation(116819, 76994, -2714, true);
              break;
            case 7132:
              _activeChar.teleToLocation(146331, 25762, -2018, true);
              break;
            case 7133:
              _activeChar.teleToLocation(147928, -55273, -2734, true);
              break;
            case 7134:
              _activeChar.teleToLocation(43799, -47727, -798, true);
              break;
            case 7135:
              _activeChar.teleToLocation(87331, -142842, -1317, true);
              break;
            case 7618:
              _activeChar.teleToLocation(149864, -81062, -5618, true);
              break;
            case 7619:
              _activeChar.teleToLocation(108275, -53785, -2524, true);
              break;
            default:
              _activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
            }
        }
      }
      catch (Throwable e)
      {
        if (Config.DEBUG) e.printStackTrace();
      }
    }
  }
}