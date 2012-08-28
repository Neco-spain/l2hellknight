package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.poly.ObjectPoly;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class AdminPolymorph
  implements IAdminCommandHandler
{
  private static final String[] ADMIN_COMMANDS = { "admin_polymorph", "admin_unpolymorph", "admin_polymorph_menu", "admin_unpolymorph_menu" };

  private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;

  public boolean useAdminCommand(String command, L2PcInstance activeChar)
  {
    if ((!Config.ALT_PRIVILEGES_ADMIN) && (
      (!checkLevel(activeChar.getAccessLevel())) || (!activeChar.isGM()))) {
      return false;
    }
    if (command.startsWith("admin_polymorph"))
    {
      StringTokenizer st = new StringTokenizer(command);
      L2Object target = activeChar.getTarget();
      try
      {
        st.nextToken();
        String p1 = st.nextToken();
        if (st.hasMoreTokens())
        {
          String p2 = st.nextToken();
          doPolymorph(activeChar, target, p2, p1);
        }
        else {
          doPolymorph(activeChar, target, p1, "npc");
        }
      }
      catch (Exception e) {
        activeChar.sendMessage("Usage: //polymorph [type] <id>");
      }
    }
    else if (command.equals("admin_unpolymorph"))
    {
      doUnpoly(activeChar, activeChar.getTarget());
    }
    if (command.contains("menu"))
      showMainPage(activeChar);
    return true;
  }

  public String[] getAdminCommandList()
  {
    return ADMIN_COMMANDS;
  }

  private boolean checkLevel(int level)
  {
    return level >= REQUIRED_LEVEL;
  }

  private void doPolymorph(L2PcInstance activeChar, L2Object obj, String id, String type)
  {
    if (obj != null)
    {
      obj.getPoly().setPolyInfo(type, id);

      if ((obj instanceof L2Character))
      {
        L2Character Char = (L2Character)obj;
        MagicSkillUser msk = new MagicSkillUser(Char, 1008, 1, 4000, 0);
        Char.broadcastPacket(msk);
        SetupGauge sg = new SetupGauge(0, 4000);
        Char.sendPacket(sg);
      }

      obj.decayMe();
      obj.spawnMe(obj.getX(), obj.getY(), obj.getZ());
      activeChar.sendMessage("Polymorph succeed");
    }
    else {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
    }
  }

  private void doUnpoly(L2PcInstance activeChar, L2Object target)
  {
    if (target != null)
    {
      target.getPoly().setPolyInfo(null, "1");
      target.decayMe();
      target.spawnMe(target.getX(), target.getY(), target.getZ());
      activeChar.sendMessage("Unpolymorph succeed");
    }
    else {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
    }
  }

  private void showMainPage(L2PcInstance activeChar) {
    AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
  }
}