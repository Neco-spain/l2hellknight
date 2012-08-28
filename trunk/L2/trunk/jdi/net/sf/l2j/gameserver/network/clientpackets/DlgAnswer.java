package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class DlgAnswer extends L2GameClientPacket
{
  private static final String _C__C5_DLGANSWER = "[C] C5 DlgAnswer";
  private static Logger _log = Logger.getLogger(DlgAnswer.class.getName());
  private int _messageId;
  private int _answer;
  private int _requesterId;

  protected void readImpl()
  {
    _messageId = readD();
    _answer = readD();
    _requesterId = readD();
  }

  public void runImpl()
  {
    if (Config.DEBUG)
      _log.fine(getType() + ": Answer accepted. Message ID " + _messageId + ", answer " + _answer + ", Requester ID " + _requesterId);
    if (_messageId == SystemMessageId.RESSURECTION_REQUEST.getId())
      ((L2GameClient)getClient()).getActiveChar().reviveAnswer(_answer);
    else if (_messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
      ((L2GameClient)getClient()).getActiveChar().teleportAnswer(_answer, _requesterId);
    else if ((_messageId == SystemMessageId.S1_S2.getId()) && (Config.L2JMOD_ALLOW_WEDDING))
      ((L2GameClient)getClient()).getActiveChar().EngageAnswer(_answer);
    else if (_messageId == 1140)
      ((L2GameClient)getClient()).getActiveChar().gatesAnswer(_answer, 1);
    else if (_messageId == 1141)
      ((L2GameClient)getClient()).getActiveChar().gatesAnswer(_answer, 0);
  }

  public String getType()
  {
    return "[C] C5 DlgAnswer";
  }
}