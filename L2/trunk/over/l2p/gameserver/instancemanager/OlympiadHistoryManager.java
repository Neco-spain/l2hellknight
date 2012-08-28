package l2p.gameserver.instancemanager;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import l2p.gameserver.dao.OlympiadHistoryDAO;
import l2p.gameserver.data.StringHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.Hero;
import l2p.gameserver.model.entity.olympiad.OlympiadHistory;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

public class OlympiadHistoryManager
{
  private static final OlympiadHistoryManager _instance = new OlympiadHistoryManager();

  private IntObjectMap<List<OlympiadHistory>> _historyNew = new CHashIntObjectMap();
  private IntObjectMap<List<OlympiadHistory>> _historyOld = new CHashIntObjectMap();

  public static OlympiadHistoryManager getInstance()
  {
    return _instance;
  }

  OlympiadHistoryManager()
  {
    Map historyList = OlympiadHistoryDAO.getInstance().select();
    for (Iterator i$ = historyList.entrySet().iterator(); i$.hasNext(); ) { entry = (Map.Entry)i$.next();
      for (OlympiadHistory history : (List)entry.getValue())
        addHistory(((Boolean)entry.getKey()).booleanValue(), history);
    }
    Map.Entry entry;
  }

  public void switchData()
  {
    _historyOld.clear();

    _historyOld.putAll(_historyNew);

    _historyNew.clear();

    OlympiadHistoryDAO.getInstance().switchData();
  }

  public void saveHistory(OlympiadHistory history)
  {
    addHistory(false, history);

    OlympiadHistoryDAO.getInstance().insert(history);
  }

  public void addHistory(boolean old, OlympiadHistory history)
  {
    IntObjectMap map = old ? _historyOld : _historyNew;

    addHistory0(map, history.getObjectId1(), history);
    addHistory0(map, history.getObjectId2(), history);
  }

  private void addHistory0(IntObjectMap<List<OlympiadHistory>> map, int objectId, OlympiadHistory history)
  {
    List historySet = (List)map.get(objectId);
    if (historySet == null) {
      map.put(objectId, historySet = new CopyOnWriteArrayList());
    }
    historySet.add(history);
  }

  public void showHistory(Player player, int targetClassId, int page)
  {
    int perpage = 15;

    Map.Entry entry = Hero.getInstance().getHeroStats(targetClassId);
    if (entry == null) {
      return;
    }
    List historyList = (List)_historyOld.get(((Integer)entry.getKey()).intValue());
    if (historyList == null) {
      historyList = Collections.emptyList();
    }
    NpcHtmlMessage html = new NpcHtmlMessage(player, null);
    html.setFile("olympiad/monument_hero_info.htm");
    html.replace("%title%", StringHolder.getInstance().getNotNull(player, "hero.history"));

    int allStatWinner = 0;
    int allStatLoss = 0;
    int allStatTie = 0;
    for (OlympiadHistory h : historyList)
    {
      if (h.getGameStatus() == 0) {
        allStatTie++;
      }
      else {
        int team = ((Integer)entry.getKey()).intValue() == h.getObjectId1() ? 1 : 2;
        if (h.getGameStatus() == team)
          allStatWinner++;
        else
          allStatLoss++;
      }
    }
    html.replace("%wins%", String.valueOf(allStatWinner));
    html.replace("%ties%", String.valueOf(allStatTie));
    html.replace("%losses%", String.valueOf(allStatLoss));

    int min = 15 * (page - 1);
    int max = 15 * page;

    int currentWinner = 0;
    int currentLoss = 0;
    int currentTie = 0;

    StringBuilder b = new StringBuilder(500);

    for (int i = 0; i < historyList.size(); i++)
    {
      OlympiadHistory history = (OlympiadHistory)historyList.get(i);
      if (history.getGameStatus() == 0) {
        currentTie++;
      }
      else {
        int team = ((Integer)entry.getKey()).intValue() == history.getObjectId1() ? 1 : 2;
        if (history.getGameStatus() == team)
          currentWinner++;
        else {
          currentLoss++;
        }
      }
      if (i < min) {
        continue;
      }
      if (i >= max) {
        break;
      }
      b.append("<tr><td>");
      b.append(history.toString(player, ((Integer)entry.getKey()).intValue(), currentWinner, currentLoss, currentTie));
      b.append("</td></tr");
    }

    if (min > 0)
    {
      html.replace("%buttprev%", "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
      html.replace("%prev_bypass%", new StringBuilder().append("_match?class=").append(targetClassId).append("&page=").append(page - 1).toString());
    }
    else {
      html.replace("%buttprev%", "");
    }
    if (historyList.size() > max)
    {
      html.replace("%buttnext%", "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=60 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
      html.replace("%prev_bypass%", new StringBuilder().append("_match?class=").append(targetClassId).append("&page=").append(page + 1).toString());
    }
    else {
      html.replace("%buttnext%", "");
    }
    html.replace("%list%", b.toString());

    player.sendPacket(html);
  }
}