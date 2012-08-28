package scripts.handler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastMap;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AutoAnnouncementHandler
{
  protected static Log _log = LogFactory.getLog(AutoAnnouncementHandler.class.getName());
  private static AutoAnnouncementHandler _instance;
  private static final long DEFAULT_ANNOUNCEMENT_DELAY = 180000L;
  protected Map<Integer, AutoAnnouncementInstance> _registeredAnnouncements;

  protected AutoAnnouncementHandler()
  {
    _registeredAnnouncements = new FastMap();
    restoreAnnouncementData();
  }

  private void restoreAnnouncementData() {
    int numLoaded = 0;

    Connect con = null;
    ResultSet rs = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT * FROM auto_announcements ORDER BY id");
      rs = st.executeQuery();

      while (rs.next()) {
        numLoaded++;

        registerGlobalAnnouncement(rs.getInt("id"), rs.getString("announcement"), rs.getLong("delay"));
      }

      _log.info("GameServer: Loaded " + numLoaded + " Auto Announcements.");
    } catch (Exception e) {
    } finally {
      Close.CSR(con, st, rs);
    }
  }

  public void listAutoAnnouncements(L2PcInstance activeChar) {
    NpcHtmlMessage adminReply = NpcHtmlMessage.id(5);

    TextBuilder replyMSG = new TextBuilder("<html><body>");
    replyMSG.append("<table width=260><tr>");
    replyMSG.append("<td width=40></td>");
    replyMSG.append("<button value=\"Main\" action=\"bypass -h admin_admin\" width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");
    replyMSG.append("<td width=180><center>Auto Announcement Menu</center></td>");
    replyMSG.append("<td width=40></td>");
    replyMSG.append("</tr></table>");
    replyMSG.append("<br><br>");
    replyMSG.append("<center>Add new auto announcement:</center>");
    replyMSG.append("<center><multiedit var=\"new_autoannouncement\" width=240 height=30></center><br>");
    replyMSG.append("<br><br>");
    replyMSG.append("<center>Delay: <edit var=\"delay\" width=70></center>");
    replyMSG.append("<center>Note: Time in Seconds 60s = 1 min.</center>");
    replyMSG.append("<br><br>");
    replyMSG.append("<center><table><tr><td>");
    replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_autoannouncement $delay $new_autoannouncement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
    replyMSG.append("</td></tr></table></center>");
    replyMSG.append("<br>");

    for (AutoAnnouncementInstance announcementInst : getInstance().values()) {
      replyMSG.append("<table width=260><tr><td width=220>[" + announcementInst.getDefaultDelay() + "s] " + announcementInst.getDefaultTexts().toString() + "</td><td width=40>");
      replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_autoannouncement " + announcementInst.getDefaultId() + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
    }

    replyMSG.append("</body></html>");

    adminReply.setHtml(replyMSG.toString());
    activeChar.sendPacket(adminReply);
  }

  public static AutoAnnouncementHandler getInstance() {
    if (_instance == null) {
      _instance = new AutoAnnouncementHandler();
    }

    return _instance;
  }

  public int size() {
    return _registeredAnnouncements.size();
  }

  public AutoAnnouncementInstance registerGlobalAnnouncement(int id, String announcementTexts, long announcementDelay)
  {
    return registerAnnouncement(id, announcementTexts, announcementDelay);
  }

  public AutoAnnouncementInstance registerAnnouncment(int id, String announcementTexts, long announcementDelay)
  {
    return registerAnnouncement(id, announcementTexts, announcementDelay);
  }

  public AutoAnnouncementInstance registerAnnouncment(String announcementTexts, long announcementDelay) {
    int nextId = nextAutoAnnouncmentId();

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("INSERT INTO auto_announcements (id,announcement,delay) VALUES (?,?,?)");
      st.setInt(1, nextId);
      st.setString(2, announcementTexts);
      st.setLong(3, announcementDelay);
      st.executeUpdate();
    } catch (Exception e) {
      _log.fatal("System: Could Not Insert Auto Announcment into DataBase: Reason: Duplicate Id");
    } finally {
      Close.CS(con, st);
    }

    return registerAnnouncement(nextId, announcementTexts, announcementDelay);
  }

  public int nextAutoAnnouncmentId()
  {
    int nextId = 0;

    Connect con = null;
    ResultSet rs = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      st = con.prepareStatement("SELECT id FROM auto_announcements ORDER BY id");
      rs = st.executeQuery();

      while (rs.next()) {
        if (rs.getInt("id") > nextId) {
          nextId = rs.getInt("id");
        }
      }
      nextId++;
    } catch (Exception e) {
    } finally {
      Close.CSR(con, st, rs);
    }

    return nextId;
  }

  private final AutoAnnouncementInstance registerAnnouncement(int id, String announcementTexts, long chatDelay) {
    AutoAnnouncementInstance announcementInst = null;

    if (chatDelay < 0L) {
      chatDelay = 180000L;
    }

    if (_registeredAnnouncements.containsKey(Integer.valueOf(id)))
      announcementInst = (AutoAnnouncementInstance)_registeredAnnouncements.get(Integer.valueOf(id));
    else {
      announcementInst = new AutoAnnouncementInstance(id, announcementTexts, chatDelay);
    }

    _registeredAnnouncements.put(Integer.valueOf(id), announcementInst);

    return announcementInst;
  }

  public Collection<AutoAnnouncementInstance> values() {
    return _registeredAnnouncements.values();
  }

  public boolean removeAnnouncement(int id)
  {
    AutoAnnouncementInstance announcementInst = (AutoAnnouncementInstance)_registeredAnnouncements.get(Integer.valueOf(id));

    Connect con = null;
    PreparedStatement st = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("DELETE FROM auto_announcements WHERE id=?");
      st.setInt(1, announcementInst.getDefaultId().intValue());
      st.executeUpdate();
    } catch (Exception e) {
      _log.fatal("Could not Delete Auto Announcement in Database, Reason:", e);
    } finally {
      Close.CS(con, st);
    }

    return removeAnnouncement(announcementInst);
  }

  public boolean removeAnnouncement(AutoAnnouncementInstance announcementInst)
  {
    if (announcementInst == null) {
      return false;
    }

    _registeredAnnouncements.remove(announcementInst.getDefaultId());
    announcementInst.setActive(false);

    return true;
  }

  public AutoAnnouncementInstance getAutoAnnouncementInstance(int id)
  {
    return (AutoAnnouncementInstance)_registeredAnnouncements.get(Integer.valueOf(id));
  }

  public void setAutoAnnouncementActive(boolean isActive)
  {
    for (AutoAnnouncementInstance announcementInst : _registeredAnnouncements.values())
      announcementInst.setActive(isActive);  } 
  public class AutoAnnouncementInstance { private long _defaultDelay = 180000L;
    private String _defaultTexts;
    private boolean _defaultRandom = false;
    private Integer _defaultId;
    private boolean _isActive;
    public ScheduledFuture<?> _chatTask;

    protected AutoAnnouncementInstance(int id, String announcementTexts, long announcementDelay) { _defaultId = Integer.valueOf(id);
      _defaultTexts = announcementTexts;
      _defaultDelay = (announcementDelay * 1000L);

      setActive(true); }

    public boolean isActive()
    {
      return _isActive;
    }

    public boolean isDefaultRandom() {
      return _defaultRandom;
    }

    public long getDefaultDelay() {
      return _defaultDelay;
    }

    public String getDefaultTexts() {
      return _defaultTexts;
    }

    public Integer getDefaultId() {
      return _defaultId;
    }

    public void setDefaultChatDelay(long delayValue) {
      _defaultDelay = delayValue;
    }

    public void setDefaultChatTexts(String textsValue) {
      _defaultTexts = textsValue;
    }

    public void setDefaultRandom(boolean randValue) {
      _defaultRandom = randValue;
    }

    public void setActive(boolean activeValue) {
      if (_isActive == activeValue) {
        return;
      }

      _isActive = activeValue;

      if (isActive()) {
        AutoAnnouncementRunner acr = new AutoAnnouncementRunner(_defaultId.intValue());
        _chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
      }
      else
      {
        _chatTask.cancel(false);
      }
    }

    private class AutoAnnouncementRunner
      implements Runnable
    {
      protected int id;

      protected AutoAnnouncementRunner(int pId)
      {
        id = pId;
      }

      public synchronized void run() {
        AutoAnnouncementHandler.AutoAnnouncementInstance announcementInst = (AutoAnnouncementHandler.AutoAnnouncementInstance)_registeredAnnouncements.get(Integer.valueOf(id));

        String text = announcementInst.getDefaultTexts();

        if (text == null) {
          return;
        }

        Announcements.getInstance().announceToAll(text);
      }
    }
  }
}