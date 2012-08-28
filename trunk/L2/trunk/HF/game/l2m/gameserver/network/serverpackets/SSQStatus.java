package l2m.gameserver.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2m.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket
{
  private Player _player;
  private int _page;
  private int period;

  public SSQStatus(Player player, int recordPage)
  {
    _player = player;
    _page = recordPage;
    period = SevenSigns.getInstance().getCurrentPeriod();
  }

  protected final void writeImpl()
  {
    writeC(251);

    writeC(_page);
    writeC(period);

    switch (_page)
    {
    case 1:
      writeD(SevenSigns.getInstance().getCurrentCycle());

      switch (period)
      {
      case 0:
        writeD(1183);
        break;
      case 1:
        writeD(1176);
        break;
      case 2:
        writeD(1184);
        break;
      case 3:
        writeD(1177);
      }

      switch (period)
      {
      case 0:
      case 2:
        writeD(1287);
        break;
      case 1:
      case 3:
        writeD(1286);
      }

      writeC(SevenSigns.getInstance().getPlayerCabal(_player));
      writeC(SevenSigns.getInstance().getPlayerSeal(_player));

      writeQ(SevenSigns.getInstance().getPlayerStoneContrib(_player));
      writeQ(SevenSigns.getInstance().getPlayerAdenaCollect(_player));

      long dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
      long dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);
      long dawnTotalScore = SevenSigns.getInstance().getCurrentScore(2);

      long duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
      long duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);
      long duskTotalScore = SevenSigns.getInstance().getCurrentScore(1);

      long totalStoneScore = duskStoneScore + dawnStoneScore;
      totalStoneScore = totalStoneScore == 0L ? 1L : totalStoneScore;

      long duskStoneScoreProp = Math.round(duskStoneScore * 500.0D / totalStoneScore);
      long dawnStoneScoreProp = Math.round(dawnStoneScore * 500.0D / totalStoneScore);

      long totalOverallScore = duskTotalScore + dawnTotalScore;
      totalOverallScore = totalOverallScore == 0L ? 1L : totalOverallScore;

      long dawnPercent = Math.round(dawnTotalScore * 110.0D / totalOverallScore);
      long duskPercent = Math.round(duskTotalScore * 110.0D / totalOverallScore);

      writeQ(duskStoneScoreProp);
      writeQ(duskFestivalScore);
      writeQ(duskTotalScore);

      writeC((int)duskPercent);

      writeQ(dawnStoneScoreProp);
      writeQ(dawnFestivalScore);
      writeQ(dawnTotalScore);

      writeC((int)dawnPercent);
      break;
    case 2:
      writeH(1);
      writeC(5);

      for (int i = 0; i < 5; i++)
      {
        writeC(i + 1);
        writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);

        long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
        long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);

        writeQ(duskScore);

        StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);

        if (duskScore > 0L)
        {
          String[] partyMembers = highScoreData.getString("names").split(",");
          writeC(partyMembers.length);
          for (String partyMember : partyMembers)
            writeS(partyMember);
        }
        else {
          writeC(0);
        }

        writeQ(dawnScore);

        highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);

        if (dawnScore > 0L)
        {
          String[] partyMembers = highScoreData.getString("names").split(",");
          writeC(partyMembers.length);
          for (String partyMember : partyMembers)
            writeS(partyMember);
        }
        else {
          writeC(0);
        }
      }
      break;
    case 3:
      writeC(10);
      writeC(35);
      writeC(3);

      int totalDawnProportion = 1;
      int totalDuskProportion = 1;

      for (int i = 1; i <= 3; i++)
      {
        totalDawnProportion += SevenSigns.getInstance().getSealProportion(i, 2);
        totalDuskProportion += SevenSigns.getInstance().getSealProportion(i, 1);
      }

      totalDawnProportion = Math.max(1, totalDawnProportion);
      totalDuskProportion = Math.max(1, totalDuskProportion);

      for (int i = 1; i <= 3; i++)
      {
        int dawnProportion = SevenSigns.getInstance().getSealProportion(i, 2);
        int duskProportion = SevenSigns.getInstance().getSealProportion(i, 1);

        writeC(i);
        writeC(SevenSigns.getInstance().getSealOwner(i));
        writeC(duskProportion * 100 / totalDuskProportion);
        writeC(dawnProportion * 100 / totalDawnProportion);
      }
      break;
    case 4:
      int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
      writeC(winningCabal);
      writeC(3);

      int dawnTotalPlayers = SevenSigns.getInstance().getTotalMembers(2);
      int duskTotalPlayers = SevenSigns.getInstance().getTotalMembers(1);

      for (int i = 1; i < 4; i++)
      {
        writeC(i);

        int dawnSealPlayers = SevenSigns.getInstance().getSealProportion(i, 2);
        int duskSealPlayers = SevenSigns.getInstance().getSealProportion(i, 1);
        int dawnProp = dawnTotalPlayers > 0 ? dawnSealPlayers * 100 / dawnTotalPlayers : 0;
        int duskProp = duskTotalPlayers > 0 ? duskSealPlayers * 100 / duskTotalPlayers : 0;
        int curSealOwner = SevenSigns.getInstance().getSealOwner(i);

        if (Math.max(dawnProp, duskProp) < 10)
        {
          writeC(0);
          if (curSealOwner == 0) {
            writeD(1292);
          }
          else
            writeD(1291);
        }
        else if (Math.max(dawnProp, duskProp) < 35)
        {
          writeC(curSealOwner);
          if (curSealOwner == 0) {
            writeD(1292);
          }
          else
            writeD(1289);
        }
        else if (dawnProp == duskProp)
        {
          writeC(0);
          writeD(1293);
        }
        else
        {
          int sealWinning = dawnProp > duskProp ? 2 : 1;
          writeC(sealWinning);
          if (sealWinning == curSealOwner) {
            writeD(1289);
          }
          else {
            writeD(1290);
          }
        }
      }
    }

    _player = null;
  }
}