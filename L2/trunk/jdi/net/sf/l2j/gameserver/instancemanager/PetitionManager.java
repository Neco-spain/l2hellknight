package net.sf.l2j.gameserver.instancemanager;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class PetitionManager
{
  protected static final Logger _log = Logger.getLogger(PetitionManager.class.getName());
  private static PetitionManager _instance;
  private Map<Integer, Petition> _pendingPetitions;
  private Map<Integer, Petition> _completedPetitions;

  public static PetitionManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing PetitionManager");
      _instance = new PetitionManager();
    }

    return _instance;
  }

  private PetitionManager()
  {
    _pendingPetitions = new FastMap();
    _completedPetitions = new FastMap();
  }

  public void clearCompletedPetitions()
  {
    int numPetitions = getPendingPetitionCount();

    getCompletedPetitions().clear();
    _log.info("PetitionManager: Completed petition data cleared. " + numPetitions + " petition(s) removed.");
  }

  public void clearPendingPetitions()
  {
    int numPetitions = getPendingPetitionCount();

    getPendingPetitions().clear();
    _log.info("PetitionManager: Pending petition queue cleared. " + numPetitions + " petition(s) removed.");
  }

  public boolean acceptPetition(L2PcInstance respondingAdmin, int petitionId)
  {
    if (!isValidPetition(petitionId)) {
      return false;
    }
    Petition currPetition = (Petition)getPendingPetitions().get(Integer.valueOf(petitionId));

    if (currPetition.getResponder() != null) {
      return false;
    }
    currPetition.setResponder(respondingAdmin);
    currPetition.setState(PetitionState.In_Process);

    currPetition.sendPetitionerPacket(new SystemMessage(SystemMessageId.PETITION_APP_ACCEPTED));

    SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1);
    sm.addNumber(currPetition.getId());
    currPetition.sendResponderPacket(sm);

    sm = new SystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY);
    sm.addString(currPetition.getPetitioner().getName());
    currPetition.sendResponderPacket(sm);
    return true;
  }

  public boolean cancelActivePetition(L2PcInstance player)
  {
    for (Petition currPetition : getPendingPetitions().values())
    {
      if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId())) {
        return currPetition.endPetitionConsultation(PetitionState.Petitioner_Cancel);
      }
      if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId())) {
        return currPetition.endPetitionConsultation(PetitionState.Responder_Cancel);
      }
    }
    return false;
  }

  public void checkPetitionMessages(L2PcInstance petitioner)
  {
    if (petitioner != null)
      for (Petition currPetition : getPendingPetitions().values())
      {
        if (currPetition == null) {
          continue;
        }
        if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()))
        {
          for (CreatureSay logMessage : currPetition.getLogMessages()) {
            petitioner.sendPacket(logMessage);
          }
          return;
        }
      }
  }

  public boolean endActivePetition(L2PcInstance player)
  {
    if (!player.isGM()) {
      return false;
    }
    for (Petition currPetition : getPendingPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId())) {
        return currPetition.endPetitionConsultation(PetitionState.Completed);
      }
    }
    return false;
  }

  protected Map<Integer, Petition> getCompletedPetitions()
  {
    return _completedPetitions;
  }

  protected Map<Integer, Petition> getPendingPetitions()
  {
    return _pendingPetitions;
  }

  public int getPendingPetitionCount()
  {
    return getPendingPetitions().size();
  }

  public int getPlayerTotalPetitionCount(L2PcInstance player)
  {
    if (player == null) {
      return 0;
    }
    int petitionCount = 0;

    for (Petition currPetition : getPendingPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId())) {
        petitionCount++;
      }
    }
    for (Petition currPetition : getCompletedPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId())) {
        petitionCount++;
      }
    }
    return petitionCount;
  }

  public boolean isPetitionInProcess()
  {
    for (Petition currPetition : getPendingPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      if (currPetition.getState() == PetitionState.In_Process) {
        return true;
      }
    }
    return false;
  }

  public boolean isPetitionInProcess(int petitionId)
  {
    if (!isValidPetition(petitionId)) {
      return false;
    }
    Petition currPetition = (Petition)getPendingPetitions().get(Integer.valueOf(petitionId));
    return currPetition.getState() == PetitionState.In_Process;
  }

  public boolean isPlayerInConsultation(L2PcInstance player)
  {
    if (player != null)
      for (Petition currPetition : getPendingPetitions().values())
      {
        if ((currPetition == null) || 
          (currPetition.getState() != PetitionState.In_Process)) {
          continue;
        }
        if (((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId())) || ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId())))
        {
          return true;
        }
      }
    return false;
  }

  public boolean isPetitioningAllowed()
  {
    return Config.PETITIONING_ALLOWED;
  }

  public boolean isPlayerPetitionPending(L2PcInstance petitioner)
  {
    if (petitioner != null) {
      for (Petition currPetition : getPendingPetitions().values())
      {
        if (currPetition == null) {
          continue;
        }
        if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == petitioner.getObjectId()))
          return true;
      }
    }
    return false;
  }

  private boolean isValidPetition(int petitionId)
  {
    return getPendingPetitions().containsKey(Integer.valueOf(petitionId));
  }

  public boolean rejectPetition(L2PcInstance respondingAdmin, int petitionId)
  {
    if (!isValidPetition(petitionId)) {
      return false;
    }
    Petition currPetition = (Petition)getPendingPetitions().get(Integer.valueOf(petitionId));

    if (currPetition.getResponder() != null) {
      return false;
    }
    currPetition.setResponder(respondingAdmin);
    return currPetition.endPetitionConsultation(PetitionState.Responder_Reject);
  }

  public boolean sendActivePetitionMessage(L2PcInstance player, String messageText)
  {
    for (Petition currPetition : getPendingPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      if ((currPetition.getPetitioner() != null) && (currPetition.getPetitioner().getObjectId() == player.getObjectId()))
      {
        CreatureSay cs = new CreatureSay(player.getObjectId(), 6, player.getName(), messageText);
        currPetition.addLogMessage(cs);

        currPetition.sendResponderPacket(cs);
        currPetition.sendPetitionerPacket(cs);
        return true;
      }

      if ((currPetition.getResponder() != null) && (currPetition.getResponder().getObjectId() == player.getObjectId()))
      {
        CreatureSay cs = new CreatureSay(player.getObjectId(), 7, player.getName(), messageText);
        currPetition.addLogMessage(cs);

        currPetition.sendResponderPacket(cs);
        currPetition.sendPetitionerPacket(cs);
        return true;
      }
    }

    return false;
  }

  public void sendPendingPetitionList(L2PcInstance activeChar)
  {
    TextBuilder htmlContent = new TextBuilder("<html><body><center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">");

    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm z");

    if (getPendingPetitionCount() == 0)
      htmlContent.append("<tr><td colspan=\"4\">There are no currently pending petitions.</td></tr>");
    else {
      htmlContent.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td><td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>");
    }

    for (Petition currPetition : getPendingPetitions().values())
    {
      if (currPetition == null) {
        continue;
      }
      htmlContent.append("<tr><td>");

      if (currPetition.getState() != PetitionState.In_Process) {
        htmlContent.append("<button value=\"View\" action=\"bypass -h admin_view_petition " + currPetition.getId() + "\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">");
      }
      else {
        htmlContent.append("<font color=\"999999\">In Process</font>");
      }
      htmlContent.append("</td><td>" + currPetition.getPetitioner().getName() + "</td><td>" + currPetition.getTypeAsString() + "</td><td>" + dateFormat.format(new Date(currPetition.getSubmitTime())) + "</td></tr>");
    }

    htmlContent.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><button value=\"Back\" action=\"bypass -h admin_admin\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");

    NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
    htmlMsg.setHtml(htmlContent.toString());
    activeChar.sendPacket(htmlMsg);
  }

  public int submitPetition(L2PcInstance petitioner, String petitionText, int petitionType)
  {
    Petition newPetition = new Petition(petitioner, petitionText, petitionType);
    int newPetitionId = newPetition.getId();
    getPendingPetitions().put(Integer.valueOf(newPetitionId), newPetition);

    String msgContent = petitioner.getName() + " has submitted a new petition.";
    GmListTable.broadcastToGMs(new CreatureSay(petitioner.getObjectId(), 17, "Petition System", msgContent));

    return newPetitionId;
  }

  public void viewPetition(L2PcInstance activeChar, int petitionId)
  {
    if (!activeChar.isGM()) {
      return;
    }
    if (!isValidPetition(petitionId)) {
      return;
    }
    Petition currPetition = (Petition)getPendingPetitions().get(Integer.valueOf(petitionId));
    TextBuilder htmlContent = new TextBuilder("<html><body>");
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd MMM HH:mm z");

    htmlContent.append("<center><br><font color=\"LEVEL\">Petition #" + currPetition.getId() + "</font><br1>");
    htmlContent.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>");
    htmlContent.append("Submit Time: " + dateFormat.format(new Date(currPetition.getSubmitTime())) + "<br1>");
    htmlContent.append("Petitioner: " + currPetition.getPetitioner().getName() + "<br1>");
    htmlContent.append("Petition Type: " + currPetition.getTypeAsString() + "<br>" + currPetition.getContent() + "<br>");
    htmlContent.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition " + currPetition.getId() + "\"" + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>");

    htmlContent.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition " + currPetition.getId() + "\" " + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>");

    htmlContent.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center>");

    htmlContent.append("</body></html>");

    NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
    htmlMsg.setHtml(htmlContent.toString());
    activeChar.sendPacket(htmlMsg);
  }

  private class Petition
  {
    private long _submitTime = System.currentTimeMillis();
    private long _endTime = -1L;
    private int _id;
    private PetitionManager.PetitionType _type;
    private PetitionManager.PetitionState _state = PetitionManager.PetitionState.Pending;
    private String _content;
    private List<CreatureSay> _messageLog = new FastList();
    private L2PcInstance _petitioner;
    private L2PcInstance _responder;

    public Petition(L2PcInstance petitioner, String petitionText, int petitionType)
    {
      petitionType--;
      _id = IdFactory.getInstance().getNextId();
      if (petitionType >= PetitionManager.PetitionType.values().length)
      {
        PetitionManager._log.warning("PetitionManager:Petition : invalid petition type (received type was +1) : " + petitionType);
      }
      _type = PetitionManager.PetitionType.values()[petitionType];
      _content = petitionText;

      _petitioner = petitioner;
    }

    protected boolean addLogMessage(CreatureSay cs)
    {
      return _messageLog.add(cs);
    }

    protected List<CreatureSay> getLogMessages()
    {
      return _messageLog;
    }

    public boolean endPetitionConsultation(PetitionManager.PetitionState endState)
    {
      setState(endState);
      _endTime = System.currentTimeMillis();

      if ((getResponder() != null) && (getResponder().isOnline() == 1))
      {
        if (endState == PetitionManager.PetitionState.Responder_Reject)
        {
          getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
        }
        else
        {
          SystemMessage sm = new SystemMessage(SystemMessageId.PETITION_ENDED_WITH_S1);
          sm.addString(getPetitioner().getName());
          getResponder().sendPacket(sm);

          if (endState == PetitionManager.PetitionState.Petitioner_Cancel)
          {
            sm = new SystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED);
            sm.addNumber(getId());
            getResponder().sendPacket(sm);
          }
        }

      }

      if ((getPetitioner() != null) && (getPetitioner().isOnline() == 1)) {
        getPetitioner().sendPacket(new SystemMessage(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK));
      }
      getCompletedPetitions().put(Integer.valueOf(getId()), this);
      return getPendingPetitions().remove(Integer.valueOf(getId())) != null;
    }

    public String getContent()
    {
      return _content;
    }

    public int getId()
    {
      return _id;
    }

    public L2PcInstance getPetitioner()
    {
      return _petitioner;
    }

    public L2PcInstance getResponder()
    {
      return _responder;
    }

    public long getEndTime()
    {
      return _endTime;
    }

    public long getSubmitTime()
    {
      return _submitTime;
    }

    public PetitionManager.PetitionState getState()
    {
      return _state;
    }

    public String getTypeAsString()
    {
      return _type.toString().replace("_", " ");
    }

    public void sendPetitionerPacket(L2GameServerPacket responsePacket)
    {
      if ((getPetitioner() == null) || (getPetitioner().isOnline() == 0))
      {
        return;
      }

      getPetitioner().sendPacket(responsePacket);
    }

    public void sendResponderPacket(L2GameServerPacket responsePacket)
    {
      if ((getResponder() == null) || (getResponder().isOnline() == 0))
      {
        endPetitionConsultation(PetitionManager.PetitionState.Responder_Missing);
        return;
      }

      getResponder().sendPacket(responsePacket);
    }

    public void setState(PetitionManager.PetitionState state)
    {
      _state = state;
    }

    public void setResponder(L2PcInstance respondingAdmin)
    {
      if (getResponder() != null) {
        return;
      }
      _responder = respondingAdmin;
    }
  }

  private static enum PetitionType
  {
    Immobility, 
    Recovery_Related, 
    Bug_Report, 
    Quest_Related, 
    Bad_User, 
    Suggestions, 
    Game_Tip, 
    Operation_Related, 
    Other;
  }

  private static enum PetitionState
  {
    Pending, 
    Responder_Cancel, 
    Responder_Missing, 
    Responder_Reject, 
    Responder_Complete, 
    Petitioner_Cancel, 
    Petitioner_Missing, 
    In_Process, 
    Completed;
  }
}