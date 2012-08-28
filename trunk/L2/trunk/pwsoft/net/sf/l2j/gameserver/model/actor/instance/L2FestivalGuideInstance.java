package net.sf.l2j.gameserver.model.actor.instance;

import java.util.Calendar;
import java.util.List;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.StatsSet;

public final class L2FestivalGuideInstance extends L2FolkInstance
{
  protected int _festivalType;
  protected int _festivalOracle;
  protected int _blueStonesNeeded;
  protected int _greenStonesNeeded;
  protected int _redStonesNeeded;

  public L2FestivalGuideInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);

    switch (getNpcId()) {
    case 31127:
    case 31132:
      _festivalType = 0;
      _festivalOracle = 2;
      _blueStonesNeeded = 900;
      _greenStonesNeeded = 540;
      _redStonesNeeded = 270;
      break;
    case 31128:
    case 31133:
      _festivalType = 1;
      _festivalOracle = 2;
      _blueStonesNeeded = 1500;
      _greenStonesNeeded = 900;
      _redStonesNeeded = 450;
      break;
    case 31129:
    case 31134:
      _festivalType = 2;
      _festivalOracle = 2;
      _blueStonesNeeded = 3000;
      _greenStonesNeeded = 1800;
      _redStonesNeeded = 900;
      break;
    case 31130:
    case 31135:
      _festivalType = 3;
      _festivalOracle = 2;
      _blueStonesNeeded = 4500;
      _greenStonesNeeded = 2700;
      _redStonesNeeded = 1350;
      break;
    case 31131:
    case 31136:
      _festivalType = 4;
      _festivalOracle = 2;
      _blueStonesNeeded = 6000;
      _greenStonesNeeded = 3600;
      _redStonesNeeded = 1800;
      break;
    case 31137:
    case 31142:
      _festivalType = 0;
      _festivalOracle = 1;
      _blueStonesNeeded = 900;
      _greenStonesNeeded = 540;
      _redStonesNeeded = 270;
      break;
    case 31138:
    case 31143:
      _festivalType = 1;
      _festivalOracle = 1;
      _blueStonesNeeded = 1500;
      _greenStonesNeeded = 900;
      _redStonesNeeded = 450;
      break;
    case 31139:
    case 31144:
      _festivalType = 2;
      _festivalOracle = 1;
      _blueStonesNeeded = 3000;
      _greenStonesNeeded = 1800;
      _redStonesNeeded = 900;
      break;
    case 31140:
    case 31145:
      _festivalType = 3;
      _festivalOracle = 1;
      _blueStonesNeeded = 4500;
      _greenStonesNeeded = 2700;
      _redStonesNeeded = 1350;
      break;
    case 31141:
    case 31146:
      _festivalType = 4;
      _festivalOracle = 1;
      _blueStonesNeeded = 6000;
      _greenStonesNeeded = 3600;
      _redStonesNeeded = 1800;
    }
  }

  public void onBypassFeedback(L2PcInstance player, String command)
  {
    if (command.startsWith("FestivalDesc"))
    {
      int val = Integer.parseInt(command.substring(13));

      showChatWindow(player, val, null, true);
    }
    else if (command.startsWith("Festival"))
    {
      L2Party playerParty = player.getParty();
      int val = Integer.parseInt(command.substring(9, 10));

      switch (val)
      {
      case 1:
        if (SevenSigns.getInstance().isSealValidationPeriod())
        {
          showChatWindow(player, 2, "a", false);
          return;
        }

        if (SevenSignsFestival.getInstance().isFestivalInitialized())
        {
          player.sendMessage("You cannot sign up while a festival is in progress.");
          return;
        }

        if (playerParty == null) {
          showChatWindow(player, 2, "b", false);
          return;
        }

        if (!playerParty.isLeader(player)) {
          showChatWindow(player, 2, "c", false);
          return;
        }

        if (playerParty.getMemberCount() < Config.ALT_FESTIVAL_MIN_PLAYER)
        {
          showChatWindow(player, 2, "b", false);
          return;
        }

        if (playerParty.getLevel() > SevenSignsFestival.getMaxLevelForFestival(_festivalType))
        {
          showChatWindow(player, 2, "d", false);
          return;
        }

        if (player.isFestivalParticipant()) {
          SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
          showChatWindow(player, 2, "f", false);
          return;
        }

        showChatWindow(player, 1, null, false);
        break;
      case 2:
        int stoneType = Integer.parseInt(command.substring(11));
        int stonesNeeded = 0;

        switch (stoneType) {
        case 6360:
          stonesNeeded = _blueStonesNeeded;
          break;
        case 6361:
          stonesNeeded = _greenStonesNeeded;
          break;
        case 6362:
          stonesNeeded = _redStonesNeeded;
        }

        if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true)) return;

        SevenSignsFestival.getInstance().setParticipants(_festivalOracle, _festivalType, playerParty);
        SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);

        showChatWindow(player, 2, "e", false);
        break;
      case 3:
        if (SevenSigns.getInstance().isSealValidationPeriod())
        {
          showChatWindow(player, 3, "a", false);
          return;
        }

        if (SevenSignsFestival.getInstance().isFestivalInProgress())
        {
          player.sendMessage("You cannot register a score while a festival is in progress.");
          return;
        }

        if (playerParty == null) {
          showChatWindow(player, 3, "b", false);
          return;
        }

        List prevParticipants = SevenSignsFestival.getInstance().getPreviousParticipants(_festivalOracle, _festivalType);

        if (prevParticipants == null) {
          return;
        }

        if (!prevParticipants.contains(player)) {
          showChatWindow(player, 3, "b", false);
          return;
        }

        if (player.getObjectId() != ((L2PcInstance)prevParticipants.get(0)).getObjectId()) {
          showChatWindow(player, 3, "b", false);
          return;
        }

        L2ItemInstance bloodOfferings = player.getInventory().getItemByItemId(5901);
        int offeringCount = 0;

        if (bloodOfferings == null) {
          player.sendMessage("You do not have any blood offerings to contribute.");
          return;
        }

        offeringCount = bloodOfferings.getCount();

        int offeringScore = offeringCount * 5;
        boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(player, _festivalOracle, _festivalType, offeringScore);

        player.destroyItem("SevenSigns", bloodOfferings, this, false);

        player.sendPacket(SystemMessage.id(SystemMessageId.CONTRIB_SCORE_INCREASED).addNumber(offeringScore));

        if (isHighestScore)
          showChatWindow(player, 3, "c", false);
        else
          showChatWindow(player, 3, "d", false);
        break;
      case 4:
        TextBuilder strBuffer = new TextBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");

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
          strBuffer.append(new StringBuilder().append("Dawn: ").append(calculateDate(dawnData.getString("date"))).append(". Score ").append(dawnScore).append("<br>").append(dawnData.getString("members")).append("<br>").toString());
        else {
          strBuffer.append("Dawn: No record exists. Score 0<br>");
        }
        if (duskScore > 0)
          strBuffer.append(new StringBuilder().append("Dusk: ").append(calculateDate(duskData.getString("date"))).append(". Score ").append(duskScore).append("<br>").append(duskData.getString("members")).append("<br>").toString());
        else {
          strBuffer.append("Dusk: No record exists. Score 0<br>");
        }
        if (overallScore > 0) {
          String cabalStr = "Children of Dusk";

          if (overallData.getString("cabal").equals("dawn")) {
            cabalStr = "Children of Dawn";
          }
          strBuffer.append(new StringBuilder().append("Consecutive top scores: ").append(calculateDate(overallData.getString("date"))).append(". Score ").append(overallScore).append("<br>Affilated side: ").append(cabalStr).append("<br>").append(overallData.getString("members")).append("<br>").toString());
        }
        else {
          strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
        }
        strBuffer.append(new StringBuilder().append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\">Go back.</a></body></html>").toString());

        NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
        html.setHtml(strBuffer.toString());
        player.sendPacket(html);
        break;
      case 8:
        if (playerParty == null) {
          return;
        }
        if (!SevenSignsFestival.getInstance().isFestivalInProgress()) {
          return;
        }
        if (!playerParty.isLeader(player)) {
          showChatWindow(player, 8, "a", false);
        }
        else if (SevenSignsFestival.getInstance().increaseChallenge(_festivalOracle, _festivalType))
          showChatWindow(player, 8, "b", false);
        else
          showChatWindow(player, 8, "c", false);
        break;
      case 9:
        if (playerParty == null) {
          return;
        }

        boolean isLeader = playerParty.isLeader(player);

        if (isLeader) {
          SevenSignsFestival.getInstance().updateParticipants(player, null);
        }
        else {
          SevenSignsFestival.getInstance().updateParticipants(player, playerParty);
          playerParty.removePartyMember(player);
        }
        break;
      case 0:
        if (!SevenSigns.getInstance().isSealValidationPeriod())
        {
          player.sendMessage("Bonuses cannot be paid during the competition period.");
          return;
        }

        if (SevenSignsFestival.getInstance().distribAccumulatedBonus(player) > 0)
          showChatWindow(player, 0, "a", false);
        else
          showChatWindow(player, 0, "b", false);
        break;
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

  private void showChatWindow(L2PcInstance player, int val, String suffix, boolean isDescription)
  {
    String filename = "data/html/seven_signs/festival/";
    filename = new StringBuilder().append(filename).append(isDescription ? "desc_" : "festival_").toString();
    filename = new StringBuilder().append(filename).append(suffix != null ? new StringBuilder().append(val).append(suffix).append(".htm").toString() : new StringBuilder().append(val).append(".htm").toString()).toString();

    NpcHtmlMessage html = NpcHtmlMessage.id(getObjectId());
    html.setFile(filename);
    html.replace("%objectId%", String.valueOf(getObjectId()));
    html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
    html.replace("%cycleMins%", String.valueOf(SevenSignsFestival.getInstance().getMinsToNextCycle()));
    if ((!isDescription) && ("2b".equals(new StringBuilder().append(val).append(suffix).toString()))) {
      html.replace("%minFestivalPartyMembers%", String.valueOf(Config.ALT_FESTIVAL_MIN_PLAYER));
    }

    if (val == 5) html.replace("%statsTable%", getStatsTable());
    if (val == 6) html.replace("%bonusTable%", getBonusTable());

    if (val == 1)
    {
      html.replace("%blueStoneNeeded%", String.valueOf(_blueStonesNeeded));
      html.replace("%greenStoneNeeded%", String.valueOf(_greenStonesNeeded));
      html.replace("%redStoneNeeded%", String.valueOf(_redStonesNeeded));
    }

    player.sendPacket(html);

    player.sendActionFailed();
  }

  private final String getStatsTable()
  {
    TextBuilder tableHtml = new TextBuilder();

    for (int i = 0; i < 5; i++)
    {
      int dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
      int duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
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

  private final String getBonusTable()
  {
    TextBuilder tableHtml = new TextBuilder();

    for (int i = 0; i < 5; i++)
    {
      int accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
      String festivalName = SevenSignsFestival.getFestivalName(i);

      tableHtml.append(new StringBuilder().append("<tr><td align=\"center\" width=\"150\">").append(festivalName).append("</td><td align=\"center\" width=\"150\">").append(accumScore).append("</td></tr>").toString());
    }

    return tableHtml.toString();
  }

  private final String calculateDate(String milliFromEpoch)
  {
    long numMillis = Long.valueOf(milliFromEpoch).longValue();
    Calendar calCalc = Calendar.getInstance();

    calCalc.setTimeInMillis(numMillis);

    return new StringBuilder().append(calCalc.get(1)).append("/").append(calCalc.get(2)).append("/").append(calCalc.get(5)).toString();
  }
}