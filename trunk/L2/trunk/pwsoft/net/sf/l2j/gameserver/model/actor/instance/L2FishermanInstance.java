package net.sf.l2j.gameserver.model.actor.instance;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import net.sf.l2j.gameserver.TradeController;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.datatables.SkillTreeTable;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2SkillLearn;
import net.sf.l2j.gameserver.model.L2TradeList;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList;
import net.sf.l2j.gameserver.network.serverpackets.AquireSkillList.SkillType;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;
import net.sf.l2j.gameserver.network.serverpackets.SellList;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2FishermanInstance extends L2FolkInstance
{
  public L2FishermanInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);
  }

  public String getHtmlPath(int npcId, int val)
  {
    String pom = "";

    if (val == 0)
      pom = "" + npcId;
    else {
      pom = npcId + "-" + val;
    }

    return "data/html/fisherman/" + pom + ".htm";
  }

  private void showBuyWindow(L2PcInstance player, int val) {
    double taxRate = 0.0D;
    if (getIsInTown()) {
      taxRate = getCastle().getTaxRate();
    }
    player.tempInvetoryDisable();

    L2TradeList list = TradeController.getInstance().getBuyList(val);

    if ((list != null) && (list.getNpcId().equals(String.valueOf(getNpcId())))) {
      BuyList bl = new BuyList(list, player.getAdena(), taxRate);
      player.sendPacket(bl);
    } else {
      _log.warning("possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
      _log.warning("buylist id:" + val);
    }

    player.sendActionFailed();
  }

  private void showSellWindow(L2PcInstance player)
  {
    player.sendPacket(new SellList(player));

    player.sendActionFailed();
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("FishSkillList")) {
      player.setSkillLearningClassId(player.getClassId());
      showSkillList(player);
    }

    StringTokenizer st = new StringTokenizer(command, " ");
    String command2 = st.nextToken();

    if (command2.equalsIgnoreCase("Buy")) {
      if (st.countTokens() < 1) {
        return;
      }
      int val = Integer.parseInt(st.nextToken());
      showBuyWindow(player, val);
    } else if (command2.equalsIgnoreCase("Sell")) {
      showSellWindow(player);
    } else {
      super.onBypassFeedback(player, command);
    }
  }

  public void showSkillList(L2PcInstance player)
  {
    SkillTable st = SkillTable.getInstance();
    SkillTreeTable stt = SkillTreeTable.getInstance();
    L2SkillLearn[] skills = stt.getAvailableSkills(player);
    AquireSkillList asl = new AquireSkillList(AquireSkillList.SkillType.Fishing);

    int counts = 0;
    L2Skill sk = null;
    for (L2SkillLearn s : skills) {
      sk = st.getInfo(s.getId(), s.getLevel());
      if (sk == null)
      {
        continue;
      }
      counts++;
      asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), s.getSpCost(), 1);
    }

    if (counts == 0) {
      asl = null;
      int minlevel = stt.getMinLevelForNewSkill(player);
      if (minlevel > 0)
      {
        player.sendPacket(SystemMessage.id(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN).addNumber(minlevel));
      }
      else player.sendHtmlMessage("You've learned all skills."); 
    }
    else
    {
      player.sendPacket(asl);
    }

    player.sendActionFailed();
  }

  public boolean isL2Fisherman()
  {
    return true;
  }
}