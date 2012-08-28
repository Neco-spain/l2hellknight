package l2m.gameserver.model.instances;

import java.util.Calendar;
import l2m.gameserver.Config;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.Reflection;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import l2m.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.network.serverpackets.NpcHtmlMessage;
import l2m.gameserver.network.serverpackets.SystemMessage2;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.templates.StatsSet;
import l2m.gameserver.templates.npc.NpcTemplate;

public final class FestivalGuideInstance extends NpcInstance
{
  public static final long serialVersionUID = 1L;
  protected int _festivalType;
  protected int _festivalOracle;

  public FestivalGuideInstance(int objectId, NpcTemplate template)
  {
    super(objectId, template);

    switch (getNpcId())
    {
    case 31127:
    case 31132:
      _festivalType = 0;
      _festivalOracle = 2;
      break;
    case 31128:
    case 31133:
      _festivalType = 1;
      _festivalOracle = 2;
      break;
    case 31129:
    case 31134:
      _festivalType = 2;
      _festivalOracle = 2;
      break;
    case 31130:
    case 31135:
      _festivalType = 3;
      _festivalOracle = 2;
      break;
    case 31131:
    case 31136:
      _festivalType = 4;
      _festivalOracle = 2;
      break;
    case 31137:
    case 31142:
      _festivalType = 0;
      _festivalOracle = 1;
      break;
    case 31138:
    case 31143:
      _festivalType = 1;
      _festivalOracle = 1;
      break;
    case 31139:
    case 31144:
      _festivalType = 2;
      _festivalOracle = 1;
      break;
    case 31140:
    case 31145:
      _festivalType = 3;
      _festivalOracle = 1;
      break;
    case 31141:
    case 31146:
      _festivalType = 4;
      _festivalOracle = 1;
    }
  }

  public void onBypassFeedback(Player player, String command)
  {
    if (!canBypassCheck(player, this)) {
      return;
    }
    if (SevenSigns.getInstance().getPlayerCabal(player) == 0)
    {
      player.sendMessage("You must be Seven Signs participant.");
      return;
    }

    if (command.startsWith("FestivalDesc"))
    {
      int val = Integer.parseInt(command.substring(13));
      showChatWindow(player, val, null, true);
    }
    else if (command.startsWith("Festival"))
    {
      Party playerParty = player.getParty();
      int val = Integer.parseInt(command.substring(9, 10));
      Reflection r;
      switch (val)
      {
      case 1:
        showChatWindow(player, 1, null, false);
        break;
      case 2:
        if (SevenSigns.getInstance().getCurrentPeriod() != 1)
        {
          showChatWindow(player, 2, "a", false);
          return;
        }

        if (SevenSignsFestival.getInstance().isFestivalInitialized())
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.InProgress", player, new Object[0]));
          return;
        }

        if ((playerParty == null) || (playerParty.getMemberCount() < Config.FESTIVAL_MIN_PARTY_SIZE))
        {
          showChatWindow(player, 2, "b", false);
          return;
        }

        if (!playerParty.isLeader(player))
        {
          showChatWindow(player, 2, "c", false);
          return;
        }

        int maxlevel = SevenSignsFestival.getMaxLevelForFestival(_festivalType);
        for (Player p : playerParty.getPartyMembers()) {
          if (p.getLevel() > maxlevel)
          {
            showChatWindow(player, 2, "d", false);
            return;
          }
          if (SevenSigns.getInstance().getPlayerCabal(p) == 0)
          {
            showChatWindow(player, 2, "g", false);
            return;
          }
        }
        if (player.isFestivalParticipant())
        {
          showChatWindow(player, 2, "f", false);
          return;
        }

        int stoneType = Integer.parseInt(command.substring(11));
        long stonesNeeded = ()Math.floor(SevenSignsFestival.getStoneCount(_festivalType, stoneType) * Config.FESTIVAL_RATE_PRICE);

        if (!player.getInventory().destroyItemByItemId(stoneType, stonesNeeded))
        {
          player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.NotEnoughSSType", player, new Object[0]));
          return;
        }

        player.sendPacket(SystemMessage2.removeItems(stoneType, stonesNeeded));
        SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);

        new DarknessFestival(player.getParty(), SevenSigns.getInstance().getPlayerCabal(player), _festivalType);

        showChatWindow(player, 2, "e", false);
        break;
      case 4:
        StringBuilder strBuffer = new StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");

        StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(2, _festivalType);
        StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(1, _festivalType);
        StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(_festivalType);

        int dawnScore = dawnData.getInteger("score");
        int duskScore = duskData.getInteger("score");
        int overallScore = 0;

        if (overallData != null) {
          overallScore = overallData.getInteger("score");
        }
        strBuffer.append(new StringBuilder().append(SevenSignsFestival.getFestivalName(_festivalType)).append(" festival.<br>").toString());

        if (dawnScore > 0)
          strBuffer.append(new StringBuilder().append("Dawn: ").append(calculateDate(dawnData.getString("date"))).append(". Score ").append(dawnScore).append("<br>").append(dawnData.getString("names").replaceAll(",", ", ")).append("<br>").toString());
        else {
          strBuffer.append("Dawn: No record exists. Score 0<br>");
        }
        if (duskScore > 0)
          strBuffer.append(new StringBuilder().append("Dusk: ").append(calculateDate(duskData.getString("date"))).append(". Score ").append(duskScore).append("<br>").append(duskData.getString("names").replaceAll(",", ", ")).append("<br>").toString());
        else {
          strBuffer.append("Dusk: No record exists. Score 0<br>");
        }
        if ((overallScore > 0) && (overallData != null))
        {
          String cabalStr = "Children of Dusk";
          if (overallData.getInteger("cabal") == 2)
            cabalStr = "Children of Dawn";
          strBuffer.append(new StringBuilder().append("Consecutive top scores: ").append(calculateDate(overallData.getString("date"))).append(". Score ").append(overallScore).append("<br>Affilated side: ").append(cabalStr).append("<br>").append(overallData.getString("names").replaceAll(",", ", ")).append("<br>").toString());
        }
        else {
          strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
        }
        strBuffer.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\">Go back.</a></body></html>").toString());

        NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setHtml(strBuffer.toString());
        player.sendPacket(html);
        break;
      case 8:
        if (playerParty == null) {
          return;
        }
        if (!playerParty.isLeader(player))
        {
          showChatWindow(player, 8, "a", false);
        }
        else
        {
          r = getReflection();
          if (!(r instanceof DarknessFestival)) break;
          if (((DarknessFestival)r).increaseChallenge())
            showChatWindow(player, 8, "b", false);
          else
            showChatWindow(player, 8, "c", false); 
        }break;
      case 9:
        if (playerParty == null) {
          return;
        }
        r = getReflection();
        if (!(r instanceof DarknessFestival)) {
          return;
        }
        if (playerParty.isLeader(player))
          ((DarknessFestival)r).collapse();
        else if (playerParty.getMemberCount() > Config.FESTIVAL_MIN_PARTY_SIZE)
          player.leaveParty();
        else
          player.sendMessage("Only party leader can leave festival, if minmum party member is reached.");
        break;
      case 3:
      case 5:
      case 6:
      case 7:
      default:
        showChatWindow(player, val, null, false);
      }

    }
    else
    {
      super.onBypassFeedback(player, command);
    }
  }

  private void showChatWindow(Player player, int val, String suffix, boolean isDescription) {
    String filename = "seven_signs/festival/";
    filename = new StringBuilder().append(filename).append(isDescription ? "desc_" : "festival_").toString();
    filename = new StringBuilder().append(filename).append(suffix != null ? new StringBuilder().append(val).append(suffix).append(".htm").toString() : new StringBuilder().append(val).append(".htm").toString()).toString();
    NpcHtmlMessage html = new NpcHtmlMessage(player, this);
    html.setFile(filename);
    html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
    html.replace("%min%", String.valueOf(Config.FESTIVAL_MIN_PARTY_SIZE));

    if (val == 1)
    {
      html.replace("%price1%", String.valueOf(()Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6362) * Config.FESTIVAL_RATE_PRICE)));
      html.replace("%price2%", String.valueOf(()Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6361) * Config.FESTIVAL_RATE_PRICE)));
      html.replace("%price3%", String.valueOf(()Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6360) * Config.FESTIVAL_RATE_PRICE)));
    }
    if (val == 5)
      html.replace("%statsTable%", getStatsTable());
    if (val == 6)
      html.replace("%bonusTable%", getBonusTable());
    player.sendPacket(html);
    player.sendActionFailed();
  }

  public void showChatWindow(Player player, int val, Object[] arg)
  {
    String filename = "seven_signs/";

    switch (getNpcId())
    {
    case 31127:
    case 31128:
    case 31129:
    case 31130:
    case 31131:
      filename = new StringBuilder().append(filename).append("festival/dawn_guide.htm").toString();
      break;
    case 31137:
    case 31138:
    case 31139:
    case 31140:
    case 31141:
      filename = new StringBuilder().append(filename).append("festival/dusk_guide.htm").toString();
      break;
    case 31132:
    case 31133:
    case 31134:
    case 31135:
    case 31136:
    case 31142:
    case 31143:
    case 31144:
    case 31145:
    case 31146:
      filename = new StringBuilder().append(filename).append("festival/festival_witch.htm").toString();
      break;
    default:
      filename = getHtmlPath(getNpcId(), val, player);
    }

    player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
  }

  private String getStatsTable()
  {
    StringBuilder tableHtml = new StringBuilder();

    for (int i = 0; i < 5; i++)
    {
      long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
      long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
      String festivalName = SevenSignsFestival.getFestivalName(i);
      String winningCabal = "Children of Dusk";

      if (dawnScore > duskScore)
        winningCabal = "Children of Dawn";
      else if (dawnScore == duskScore) {
        winningCabal = "None";
      }
      tableHtml.append(new StringBuilder().append("<tr><td width=\"100\" align=\"center\">").append(festivalName).append("</td><td align=\"center\" width=\"35\">").append(duskScore).append("</td><td align=\"center\" width=\"35\">").append(dawnScore).append("</td><td align=\"center\" width=\"130\">").append(winningCabal).append("</td></tr>").toString());
    }

    return tableHtml.toString();
  }

  private String getBonusTable()
  {
    StringBuilder tableHtml = new StringBuilder();

    for (int i = 0; i < 5; i++)
    {
      long accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
      String festivalName = SevenSignsFestival.getFestivalName(i);

      tableHtml.append(new StringBuilder().append("<tr><td align=\"center\" width=\"150\">").append(festivalName).append("</td><td align=\"center\" width=\"150\">").append(accumScore).append("</td></tr>").toString());
    }

    return tableHtml.toString();
  }

  private String calculateDate(String milliFromEpoch)
  {
    long numMillis = Long.valueOf(milliFromEpoch).longValue();
    Calendar calCalc = Calendar.getInstance();

    calCalc.setTimeInMillis(numMillis);

    return new StringBuilder().append(calCalc.get(1)).append("/").append(calCalc.get(2)).append("/").append(calCalc.get(5)).toString();
  }
}