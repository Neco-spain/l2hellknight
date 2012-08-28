package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.SevenSignsFestival;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
  private static Logger _log = Logger.getLogger(SSQStatus.class.getName());
  private static final String _S__F5_SSQStatus = "[S] F5 RecordUpdate";
  private L2PcInstance _activevChar;
  private int _page;

  public SSQStatus(L2PcInstance player, int recordPage)
  {
    _activevChar = player;
    _page = recordPage;
  }

  protected final void writeImpl()
  {
    int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
    int totalDawnMembers = SevenSigns.getInstance().getTotalMembers(2);
    int totalDuskMembers = SevenSigns.getInstance().getTotalMembers(1);

    writeC(245);

    writeC(_page);
    writeC(SevenSigns.getInstance().getCurrentPeriod());

    int dawnPercent = 0;
    int duskPercent = 0;

    switch (_page)
    {
    case 1:
      writeD(SevenSigns.getInstance().getCurrentCycle());

      int currentPeriod = SevenSigns.getInstance().getCurrentPeriod();

      switch (currentPeriod)
      {
      case 0:
        writeD(SystemMessageId.INITIAL_PERIOD.getId());
        break;
      case 1:
        writeD(SystemMessageId.QUEST_EVENT_PERIOD.getId());
        break;
      case 2:
        writeD(SystemMessageId.RESULTS_PERIOD.getId());
        break;
      case 3:
        writeD(SystemMessageId.VALIDATION_PERIOD.getId());
      }

      switch (currentPeriod)
      {
      case 0:
      case 2:
        writeD(SystemMessageId.UNTIL_TODAY_6PM.getId());
        break;
      case 1:
      case 3:
        writeD(SystemMessageId.UNTIL_MONDAY_6PM.getId());
      }

      writeC(SevenSigns.getInstance().getPlayerCabal(_activevChar));
      writeC(SevenSigns.getInstance().getPlayerSeal(_activevChar));

      writeD(SevenSigns.getInstance().getPlayerStoneContrib(_activevChar));
      writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_activevChar));

      double dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
      int dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);

      double duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
      int duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);

      double totalStoneScore = duskStoneScore + dawnStoneScore;

      int duskStoneScoreProp = 0;
      int dawnStoneScoreProp = 0;

      if (totalStoneScore != 0.0D)
      {
        duskStoneScoreProp = Math.round((float)duskStoneScore / (float)totalStoneScore * 500.0F);
        dawnStoneScoreProp = Math.round((float)dawnStoneScore / (float)totalStoneScore * 500.0F);
      }

      int duskTotalScore = SevenSigns.getInstance().getCurrentScore(1);
      int dawnTotalScore = SevenSigns.getInstance().getCurrentScore(2);

      int totalOverallScore = duskTotalScore + dawnTotalScore;

      if (totalOverallScore != 0)
      {
        dawnPercent = Math.round(dawnTotalScore / totalOverallScore * 100.0F);
        duskPercent = Math.round(duskTotalScore / totalOverallScore * 100.0F);
      }

      if (Config.DEBUG) {
        _log.info("Dusk Stone Score: " + duskStoneScore + " - Dawn Stone Score: " + dawnStoneScore);

        _log.info("Dusk Festival Score: " + duskFestivalScore + " - Dawn Festival Score: " + dawnFestivalScore);

        _log.info("Dusk Score: " + duskTotalScore + " - Dawn Score: " + dawnTotalScore);
        _log.info("Overall Score: " + totalOverallScore);
        _log.info("");
        if (totalStoneScore == 0.0D)
          _log.info("Dusk Prop: 0 - Dawn Prop: 0");
        else {
          _log.info("Dusk Prop: " + duskStoneScore / totalStoneScore * 500.0D + " - Dawn Prop: " + dawnStoneScore / totalStoneScore * 500.0D);
        }
        _log.info("Dusk %: " + duskPercent + " - Dawn %: " + dawnPercent);
      }

      writeD(duskStoneScoreProp);
      writeD(duskFestivalScore);
      writeD(duskTotalScore);

      writeC(duskPercent);

      writeD(dawnStoneScoreProp);
      writeD(dawnFestivalScore);
      writeD(dawnTotalScore);

      writeC(dawnPercent);
      break;
    case 2:
      writeH(1);

      writeC(5);

      for (int i = 0; i < 5; i++)
      {
        writeC(i + 1);
        writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);

        int duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
        int dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);

        writeD(duskScore);

        StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);
        String[] partyMembers = highScoreData.getString("members").split(",");

        if (partyMembers != null)
        {
          writeC(partyMembers.length);

          for (String partyMember : partyMembers)
            writeS(partyMember);
        }
        else
        {
          writeC(0);
        }

        writeD(dawnScore);

        highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);
        partyMembers = highScoreData.getString("members").split(",");

        if (partyMembers != null)
        {
          writeC(partyMembers.length);

          for (String partyMember : partyMembers)
            writeS(partyMember);
        }
        else
        {
          writeC(0);
        }
      }
      break;
    case 3:
      writeC(10);
      writeC(35);
      writeC(3);

      for (int i = 1; i < 4; i++)
      {
        int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
        int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);

        if (Config.DEBUG) {
          _log.info(SevenSigns.getSealName(i, true) + " = Dawn Prop: " + dawnProportion + "(" + dawnProportion / totalDawnMembers * 100 + "%)" + ", Dusk Prop: " + duskProportion + "(" + duskProportion / totalDuskMembers * 100 + "%)");
        }

        writeC(i);
        writeC(SevenSigns.getInstance().getSealOwner(i));

        if (totalDuskMembers == 0)
        {
          if (totalDawnMembers == 0)
          {
            writeC(0);
            writeC(0);
          }
          else
          {
            writeC(0);
            writeC(Math.round(dawnProportion / totalDawnMembers * 100.0F));
          }

        }
        else if (totalDawnMembers == 0)
        {
          writeC(Math.round(duskProportion / totalDuskMembers * 100.0F));
          writeC(0);
        }
        else
        {
          writeC(Math.round(duskProportion / totalDuskMembers * 100.0F));
          writeC(Math.round(dawnProportion / totalDawnMembers * 100.0F));
        }
      }

      break;
    case 4:
      writeC(winningCabal);
      writeC(3);

      for (int i = 1; i < 4; i++)
      {
        int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
        int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);
        dawnPercent = Math.round(dawnProportion / totalDawnMembers * 100.0F);
        duskPercent = Math.round(duskProportion / totalDuskMembers * 100.0F);
        int sealOwner = SevenSigns.getInstance().getSealOwner(i);

        writeC(i);

        switch (sealOwner)
        {
        case 0:
          switch (winningCabal)
          {
          case 0:
            writeC(0);
            writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
            break;
          case 2:
            if (dawnPercent >= 35)
            {
              writeC(2);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
            }
            break;
          case 1:
            if (duskPercent >= 35)
            {
              writeC(1);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.getId());
            }
          }

          break;
        case 2:
          switch (winningCabal)
          {
          case 0:
            if (dawnPercent >= 10)
            {
              writeC(2);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
            }break;
          case 2:
            if (dawnPercent >= 10)
            {
              writeC(sealOwner);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
            }
            break;
          case 1:
            if (duskPercent >= 35)
            {
              writeC(1);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
            }
            else if (dawnPercent >= 10)
            {
              writeC(2);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
            }
          }

          break;
        case 1:
          switch (winningCabal)
          {
          case 0:
            if (duskPercent >= 10)
            {
              writeC(1);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.getId());
            }break;
          case 2:
            if (dawnPercent >= 35)
            {
              writeC(2);
              writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.getId());
            }
            else if (duskPercent >= 10)
            {
              writeC(sealOwner);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
            }
            break;
          case 1:
            if (duskPercent >= 10)
            {
              writeC(sealOwner);
              writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.getId());
            }
            else
            {
              writeC(0);
              writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.getId());
            }
          }

        }

        writeH(0);
      }
    }
  }

  public String getType()
  {
    return "[S] F5 RecordUpdate";
  }
}