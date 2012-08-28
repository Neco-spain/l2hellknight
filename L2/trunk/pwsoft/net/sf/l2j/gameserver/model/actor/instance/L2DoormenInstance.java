package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.datatables.DoorTable;
import net.sf.l2j.gameserver.instancemanager.ClanHallManager;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.ClanHall;
import net.sf.l2j.gameserver.network.serverpackets.MyTargetSelected;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2DoormenInstance extends L2FolkInstance
{
  private ClanHall _clanHall;
  private static int COND_ALL_FALSE = 0;
  private static int COND_BUSY_BECAUSE_OF_SIEGE = 1;
  private static int COND_CASTLE_OWNER = 2;
  private static int COND_HALL_OWNER = 3;
  private static DoorTable _dt = DoorTable.getInstance();

  public L2DoormenInstance(int objectID, L2NpcTemplate template)
  {
    super(objectID, template);
  }

  public final ClanHall getClanHall()
  {
    switch (getTemplate().npcId)
    {
    case 35433:
    case 35434:
    case 35435:
    case 35436:
      _clanHall = ClanHallManager.getInstance().getClanHallById(35);
      break;
    case 35641:
    case 35642:
      _clanHall = ClanHallManager.getInstance().getClanHallById(64);
    }

    if (_clanHall == null)
      _clanHall = ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
    return _clanHall;
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    int condition = validateCondition(player);
    if (condition <= COND_ALL_FALSE) return;
    if (condition == COND_BUSY_BECAUSE_OF_SIEGE) return;
    if ((condition == COND_CASTLE_OWNER) || (condition == COND_HALL_OWNER))
    {
      if (command.startsWith("Chat"))
      {
        showMessageWindow(player);
        return;
      }
      if (command.startsWith("open_doors"))
      {
        if (condition == COND_HALL_OWNER)
        {
          switch (getTemplate().npcId)
          {
          case 35433:
          case 35434:
            _dt.getDoor(Integer.valueOf(22170003)).openMe();
            _dt.getDoor(Integer.valueOf(22170004)).openMe();
            break;
          case 35435:
          case 35436:
            _dt.getDoor(Integer.valueOf(22170001)).openMe();
            _dt.getDoor(Integer.valueOf(22170002)).openMe();
            break;
          case 35641:
            _dt.getDoor(Integer.valueOf(21170001)).openMe();
            _dt.getDoor(Integer.valueOf(21170002)).openMe();
            break;
          case 35642:
            _dt.getDoor(Integer.valueOf(21170003)).openMe();
            _dt.getDoor(Integer.valueOf(21170004)).openMe();
            _dt.getDoor(Integer.valueOf(21170005)).openMe();
            _dt.getDoor(Integer.valueOf(21170006)).openMe();
            break;
          default:
            getClanHall().openCloseDoors(true);
          }
          player.sendPacket(NpcHtmlMessage.id(getObjectId(), "<html><body>\u0414\u0432\u0435\u0440\u0438 \u043E\u0442\u043A\u0440\u044B\u0442\u044B.<br>\u041D\u0435 \u0437\u0430\u0431\u0443\u0434\u044C\u0442\u0435 \u0437\u0430\u043A\u0440\u044B\u0442\u044C \u0434\u0432\u0435\u0440\u0438, \u0438\u043D\u0430\u0447\u0435 \u043F\u043E\u0441\u0442\u043E\u0440\u043E\u043D\u043D\u0438\u0435 \u0441\u043C\u043E\u0433\u0443\u0442 \u0432\u043E\u0439\u0442\u0438.<br><center><a action=\"bypass -h npc_" + getObjectId() + "_close_doors\">\u0417\u0430\u043A\u0440\u043E\u0439 \u0434\u0432\u0435\u0440\u0438.</a></center></body></html>"));
        }
        else
        {
          StringTokenizer st = new StringTokenizer(command.substring(10), ", ");
          st.nextToken();

          if (condition == 2)
          {
            while (st.hasMoreTokens())
            {
              getCastle().openDoor(player, Integer.parseInt(st.nextToken()));
            }
            return;
          }
        }

      }
      else if (command.startsWith("close_doors"))
      {
        if (condition == COND_HALL_OWNER)
        {
          switch (getTemplate().npcId)
          {
          case 35433:
          case 35434:
            _dt.getDoor(Integer.valueOf(22170003)).closeMe();
            _dt.getDoor(Integer.valueOf(22170004)).closeMe();
            break;
          case 35435:
          case 35436:
            _dt.getDoor(Integer.valueOf(22170001)).closeMe();
            _dt.getDoor(Integer.valueOf(22170002)).closeMe();
            break;
          case 35641:
            _dt.getDoor(Integer.valueOf(21170001)).closeMe();
            _dt.getDoor(Integer.valueOf(21170002)).closeMe();
            break;
          case 35642:
            _dt.getDoor(Integer.valueOf(21170003)).closeMe();
            _dt.getDoor(Integer.valueOf(21170004)).closeMe();
            _dt.getDoor(Integer.valueOf(21170005)).closeMe();
            _dt.getDoor(Integer.valueOf(21170006)).closeMe();
            break;
          default:
            getClanHall().openCloseDoors(false);
          }
          player.sendPacket(NpcHtmlMessage.id(getObjectId(), "<html><body>\u0414\u0432\u0435\u0440\u0438 \u0437\u0430\u043A\u0440\u044B\u0442\u044B.<br>\u0412\u0441\u0435\u0433\u043E \u0445\u043E\u0440\u043E\u0448\u0435\u0433\u043E!<br><center><a action=\"bypass -h npc_" + getObjectId() + "_open_doors\">\u041E\u0442\u043A\u0440\u043E\u0439 \u0434\u0432\u0435\u0440\u0438.</a></center></body></html>"));
        }
        else
        {
          StringTokenizer st = new StringTokenizer(command.substring(11), ", ");
          st.nextToken();

          if (condition == 2)
          {
            while (st.hasMoreTokens())
            {
              getCastle().closeDoor(player, Integer.parseInt(st.nextToken()));
            }
            return;
          }
        }
      }
    }

    super.onBypassFeedback(player, command);
  }

  public void onAction(L2PcInstance player)
  {
    if (!canTarget(player)) return;

    if (this != player.getTarget())
    {
      player.setTarget(this);

      MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
      player.sendPacket(my);

      player.sendPacket(new ValidateLocation(this));
    }
    else if (!canInteract(player))
    {
      player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
    }
    else
    {
      showMessageWindow(player);
    }

    player.sendActionFailed();
  }

  public void showMessageWindow(L2PcInstance player)
  {
    player.sendActionFailed();
    String filename = "data/html/doormen/" + getTemplate().npcId + "-no.htm";

    int condition = validateCondition(player);
    if (condition == COND_BUSY_BECAUSE_OF_SIEGE) filename = "data/html/doormen/" + getTemplate().npcId + "-busy.htm";
    else if (condition == COND_CASTLE_OWNER) {
      filename = "data/html/doormen/" + getTemplate().npcId + ".htm";
    }

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    if (getClanHall() != null)
    {
      TextBuilder tb = new TextBuilder("<html><body>");
      if (condition == COND_HALL_OWNER)
      {
        tb.append("\u0427\u0442\u043E \u044F \u043C\u043E\u0433\u0443 \u0441\u0434\u0435\u043B\u0430\u0442\u044C \u0434\u043B\u044F \u0432\u0430\u0441?<br><br>");
        tb.append("<center><a action=\"bypass -h npc_%objectId%_open_doors\">\u041E\u0442\u043A\u0440\u043E\u0439 \u0434\u0432\u0435\u0440\u0438.</a><br>");
        tb.append("<a action=\"bypass -h npc_%objectId%_close_doors\">\u0417\u0430\u043A\u0440\u043E\u0439 \u0434\u0432\u0435\u0440\u0438.</a></center></body></html>");
      }
      else
      {
        L2Clan owner = ClanTable.getInstance().getClan(getClanHall().getOwnerId());
        if ((owner != null) && (owner.getLeader() != null))
        {
          tb.append("\u042F \u043D\u0435 \u0434\u043E\u043B\u0436\u0435\u043D \u0441 \u0432\u0430\u043C\u0438 \u0440\u0430\u0437\u0433\u043E\u0432\u0430\u0440\u0438\u0432\u0430\u0442\u044C, \u0438\u043D\u0430\u0447\u0435 \u0443 \u043C\u0435\u043D\u044F \u043C\u043E\u0433\u0443\u0442 \u0431\u044B\u0442\u044C \u043D\u0435\u043F\u0440\u0438\u044F\u0442\u043D\u043E\u0441\u0442\u0438!<br>");
          tb.append("\u0412\u0441\u0435, \u0447\u0442\u043E \u044F \u043C\u043E\u0433\u0443 \u0441\u043A\u0430\u0437\u0430\u0442\u044C, \u044D\u0442\u043E \u0442\u043E, \u0447\u0442\u043E \u043A\u043B\u0430\u043D\u0445\u043E\u043B\u043B \u043F\u0440\u0435\u043D\u0430\u0434\u043B\u0435\u0436\u0438\u0442 \u043A\u043B\u0430\u043D\u0443<br> <font color=\"55FFFF\">" + owner.getName() + "</font>");
        }
        else {
          tb.append("\u0412 \u0434\u0430\u043D\u043D\u044B\u0439 \u043C\u043E\u043C\u0435\u043D\u0442 \u0443 \u043A\u043B\u0430\u043D\u0445\u043E\u043B\u043B\u0430 <font color=\"LEVEL\">" + getClanHall().getName() + "</font> \u043D\u0435\u0442 \u0432\u043B\u0430\u0434\u0435\u043B\u044C\u0446\u0430.<br><br></body></html>");
        }
      }
      html.setHtml(tb.toString());
      tb.clear();
      tb = null;
    }
    else {
      html.setFile(filename);
    }
    html.replace("%objectId%", String.valueOf(getObjectId()));
    player.sendPacket(html);
  }

  private int validateCondition(L2PcInstance player)
  {
    if (player.getClan() != null)
    {
      if (getClanHall() != null)
      {
        if (player.getClanId() == getClanHall().getOwnerId()) return COND_HALL_OWNER;
        return COND_ALL_FALSE;
      }
      if ((getCastle() != null) && (getCastle().getCastleId() > 0))
      {
        if (getCastle().getOwnerId() == player.getClanId()) {
          return COND_CASTLE_OWNER;
        }
      }
    }
    return COND_ALL_FALSE;
  }
}