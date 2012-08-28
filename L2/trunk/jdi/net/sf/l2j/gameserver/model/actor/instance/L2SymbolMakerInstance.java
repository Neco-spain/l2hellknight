package net.sf.l2j.gameserver.model.actor.instance;

import javolution.text.TextBuilder;
import net.sf.l2j.gameserver.datatables.HennaTreeTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2HennaInstance;
import net.sf.l2j.gameserver.network.serverpackets.HennaEquipList;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2SymbolMakerInstance extends L2FolkInstance
{
  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.equals("Draw"))
    {
      L2HennaInstance[] henna = HennaTreeTable.getInstance().getAvailableHenna(player.getClassId());
      HennaEquipList hel = new HennaEquipList(player, henna);
      player.sendPacket(hel);
    }
    else if (command.equals("RemoveList"))
    {
      showRemoveChat(player);
    }
    else if (command.startsWith("Remove "))
    {
      int slot = Integer.parseInt(command.substring(7));
      player.removeHenna(slot);
    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  private void showRemoveChat(L2PcInstance player)
  {
    TextBuilder html1 = new TextBuilder("<html><body>");
    html1.append("Select symbol you would like to remove:<br><br>");
    boolean hasHennas = false;

    for (int i = 1; i <= 3; i++)
    {
      L2HennaInstance henna = player.getHenna(i);

      if (henna == null)
        continue;
      hasHennas = true;
      html1.append("<a action=\"bypass -h npc_%objectId%_Remove " + i + "\">" + henna.getName() + "</a><br>");
    }

    if (!hasHennas)
      html1.append("You don't have any symbol to remove!");
    html1.append("</body></html>");
    insertObjectIdAndShowChatWindow(player, html1.toString());
  }

  public L2SymbolMakerInstance(int objectID, L2NpcTemplate template)
  {
    super(objectID, template);
  }

  public String getHtmlPath(int npcId, int val)
  {
    return "data/html/symbolmaker/SymbolMaker.htm";
  }

  public boolean isAutoAttackable(L2Character attacker)
  {
    return false;
  }
}