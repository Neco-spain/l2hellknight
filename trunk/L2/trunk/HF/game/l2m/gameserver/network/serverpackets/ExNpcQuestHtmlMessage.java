package l2m.gameserver.network.serverpackets;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2m.gameserver.data.htm.HtmCache;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.scripts.Functions;

public class ExNpcQuestHtmlMessage extends NpcHtmlMessage
{
  private int _questId;

  public ExNpcQuestHtmlMessage(int npcObjId, int questId)
  {
    super(npcObjId);
    _questId = questId;
  }

  protected void writeImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (_file != null)
    {
      if (player.isGM())
        Functions.sendDebugMessage(player, "HTML: " + _file);
      String content = HtmCache.getInstance().getNotNull(_file, player);
      String content2 = HtmCache.getInstance().getNullable(_file, player);
      if (content2 == null)
        setHtml((have_appends) && (_file.endsWith(".htm")) ? "" : content);
      else {
        setHtml(content);
      }
    }
    for (int i = 0; i < _replaces.size(); i += 2) {
      _html = _html.replaceAll((String)_replaces.get(i), (String)_replaces.get(i + 1));
    }
    if (_html == null) {
      return;
    }
    Matcher m = objectId.matcher(_html);
    if (m != null) {
      _html = m.replaceAll(String.valueOf(_npcObjId));
    }
    _html = playername.matcher(_html).replaceAll(player.getName());

    player.cleanBypasses(false);
    _html = player.encodeBypasses(_html, false);

    writeEx(141);
    writeD(_npcObjId);
    writeS(_html);
    writeD(_questId);
  }
}