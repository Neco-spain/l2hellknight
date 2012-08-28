package net.sf.l2j.gameserver.network.serverpackets;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.clientpackets.RequestBypassToServer;

public class NpcHtmlMessage extends L2GameServerPacket
{
  private static final String _S__1B_NPCHTMLMESSAGE = "[S] 0f NpcHtmlMessage";
  private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
  private int _npcObjId;
  private String _html;

  public NpcHtmlMessage(int npcObjId, String text)
  {
    _npcObjId = npcObjId;
    setHtml(text);
  }

  public NpcHtmlMessage(int npcObjId)
  {
    _npcObjId = npcObjId;
  }

  public void runImpl()
  {
    if (Config.BYPASS_VALIDATION)
      buildBypassCache(((L2GameClient)getClient()).getActiveChar());
  }

  public void setHtml(String text)
  {
    if (text.length() > 16384)
    {
      _log.warning("Html is too long! this will crash the client!");
      _html = "<html><body>Html was too long</body></html>";
      return;
    }
    _html = text;
  }

  public boolean setFile(String path)
  {
    String content = HtmCache.getInstance().getHtm(path);

    if (content == null)
    {
      setHtml("<html><body>My Text is missing:<br>" + path + "</body></html>");
      _log.warning("missing html page " + path);
      return false;
    }

    setHtml(content);
    return true;
  }

  public void replace(String pattern, String value)
  {
    String temp_text = Matcher.quoteReplacement(value);
    _html = _html.replaceAll(pattern, temp_text);
  }

  private final void buildBypassCache(L2PcInstance activeChar)
  {
    if (activeChar == null) {
      return;
    }
    activeChar.clearBypass();
    int len = _html.length();
    for (int i = 0; i < len; i++)
    {
      int start = _html.indexOf("bypass -h", i);
      int finish = _html.indexOf("\"", start);

      if ((start < 0) || (finish < 0)) {
        break;
      }
      start += 10;
      i = start;
      int finish2 = _html.indexOf("$", start);
      if ((finish2 < finish) && (finish2 > 0))
        activeChar.addBypass2(_html.substring(start, finish2));
      else
        activeChar.addBypass(_html.substring(start, finish));
    }
  }

  protected final void writeImpl()
  {
    writeC(15);

    writeD(_npcObjId);
    writeS(_html);
    writeD(0);
  }

  public String getType()
  {
    return "[S] 0f NpcHtmlMessage";
  }
}