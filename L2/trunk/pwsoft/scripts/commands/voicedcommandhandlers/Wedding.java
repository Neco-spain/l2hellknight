package scripts.commands.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.util.Broadcast;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scripts.commands.IVoicedCommandHandler;

public class Wedding
  implements IVoicedCommandHandler
{
  static final Log _log = LogFactory.getLog(Wedding.class);
  private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.startsWith("gotolove"))
      return GoToLove(activeChar);
    return false;
  }

  public boolean GoToLove(L2PcInstance activeChar)
  {
    if (!activeChar.isMarried())
    {
      if (!activeChar.getAppearance().getSex())
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u0436\u0435\u043D\u0430\u0442\u044B");
      else
        activeChar.sendMessage("\u0412\u044B \u043D\u0435 \u0437\u0430\u043C\u0443\u0436\u0435\u043C");
      return false;
    }

    if (activeChar.getPartnerId() == 0)
    {
      activeChar.sendMessage("\u0423\u043F\u0441, \u0441\u043E\u043E\u0431\u0449\u0438\u0442\u0435 \u043E\u0431 \u044D\u0442\u043E\u0439 \u043E\u0448\u0438\u0431\u043A\u0435 \u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440\u0443");
      _log.error("Married but couldn't find parter for " + activeChar.getName());
      return false;
    }

    L2PcInstance partner = (L2PcInstance)L2World.getInstance().findObject(activeChar.getPartnerId());
    if (partner == null)
    {
      activeChar.sendMessage("\u0412\u0430\u0448 \u043F\u0430\u0440\u0442\u043D\u0435\u0440 \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
      return false;
    }

    Castle castleac = CastleManager.getInstance().getCastle(activeChar);
    Castle castlepn = CastleManager.getInstance().getCastle(partner);

    String Sex = "";
    if (!partner.getAppearance().getSex())
      Sex = "\u0412\u0430\u0448\u0430 \u0436\u0435\u043D\u0430";
    else {
      Sex = "\u0412\u0430\u0448 \u043C\u0443\u0436";
    }
    if (partner.isInJail())
    {
      activeChar.sendMessage("" + Sex + " \u0432 \u0442\u044E\u0440\u044C\u043C\u0435");
      return false;
    }
    if ((partner.isInOlympiadMode()) || (partner.getOlympiadSide() > 1))
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435");
      return false;
    }
    if (partner.atEvent)
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435");
      return false;
    }
    if (partner.isInDuel())
    {
      activeChar.sendMessage("" + Sex + " \u0434\u0443\u044D\u043B\u0438\u0442\u0441\u044F");
      return false;
    }
    if (partner.isFestivalParticipant())
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430 \u0444\u0435\u0441\u0442\u0438\u0432\u0430\u043B\u0435 \u0442\u044C\u043C\u044B");
      return false;
    }
    if ((partner.isInParty()) && (partner.getParty().isInDimensionalRift()))
    {
      activeChar.sendMessage("" + Sex + " \u0432 \u0440\u0438\u0444\u0442\u0435");
      return false;
    }
    if (partner.inObserverMode())
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430\u0431\u043B\u044E\u0434\u0430\u0435\u0442 \u0437\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u043E\u0439");
      return false;
    }
    if (partner.isEventWait())
    {
      partner.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u043F\u0440\u0438\u0437\u044B\u0432\u0430\u0442\u044C \u043D\u0430 \u044D\u0432\u0435\u043D\u0442\u0435");
      return false;
    }
    if ((partner.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return false;
    }
    if ((castlepn != null) && (castlepn.getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("" + Sex + " \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return false;
    }

    if (activeChar.isInJail())
    {
      activeChar.sendMessage("\u0412\u044B \u0432 \u0442\u044E\u0440\u044C\u043C\u0435");
      return false;
    }
    if ((activeChar.isInOlympiadMode()) || (activeChar.getOlympiadSide() > 1))
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435");
      return false;
    }
    if (activeChar.atEvent)
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435");
      return false;
    }
    if (activeChar.isInDuel())
    {
      activeChar.sendMessage("\u0414\u0435\u0440\u0438\u0441\u044C \u0442\u0440\u044F\u043F\u043A\u0430");
      return false;
    }
    if (activeChar.inObserverMode())
    {
      activeChar.sendMessage("\u0421\u043C\u043E\u0442\u0440\u0438\u0442\u0435 \u0434\u0430\u043B\u044C\u0448\u0435 \u0432\u0430\u0448\u0443 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0443");
      return false;
    }
    if ((activeChar.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return false;
    }
    if ((castleac != null) && (castleac.getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return false;
    }
    if (activeChar.isFestivalParticipant())
    {
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u0444\u0435\u0441\u0442\u0438\u0432\u0430\u043B\u0435");
      return false;
    }
    if ((activeChar.isInParty()) && (activeChar.getParty().isInDimensionalRift()))
    {
      activeChar.sendMessage("\u0412\u044B \u0432 \u0440\u0438\u0444\u0442\u0435");
      return false;
    }

    if (!TvTEvent.onEscapeUse(activeChar.getName()))
    {
      activeChar.sendPacket(new ActionFailed());
      activeChar.sendMessage("\u0412\u044B \u043D\u0430 \u0442\u0432\u0442");
      return false;
    }
    if (activeChar.isEventWait())
    {
      activeChar.sendMessage("\u041D\u0435\u043B\u044C\u0437\u044F \u043F\u0440\u0438\u0437\u044B\u0432\u0430\u0442\u044C \u043D\u0430 \u044D\u0432\u0435\u043D\u0442\u0435");
      return false;
    }

    int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION * 1000;

    activeChar.sendMessage("\u0412\u044B \u0432\u0441\u0442\u0440\u0435\u0442\u0438\u0442\u0435\u0441\u044C \u0447\u0435\u0440\u0435\u0437 " + teleportTimer / 60000 + " \u043C\u0438\u043D.");
    activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);

    activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    activeChar.setTarget(activeChar);
    activeChar.disableAllSkills();

    MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, teleportTimer, 0);
    Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 1200L);
    SetupGauge sg = new SetupGauge(0, teleportTimer);
    activeChar.sendPacket(sg);

    EscapeFinalizer ef = new EscapeFinalizer(activeChar, partner.getX(), partner.getY(), partner.getZ(), partner.isIn7sDungeon());

    activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(ef, teleportTimer));
    activeChar.setSkillCastEndTime(10 + GameTimeController.getGameTicks() + teleportTimer / 100);

    return true;
  }

  public String[] getVoicedCommandList()
  {
    return _voicedCommands;
  }

  static class EscapeFinalizer
    implements Runnable
  {
    private L2PcInstance _activeChar;
    private int _partnerx;
    private int _partnery;
    private int _partnerz;
    private boolean _to7sDungeon;

    EscapeFinalizer(L2PcInstance activeChar, int x, int y, int z, boolean to7sDungeon)
    {
      _activeChar = activeChar;
      _partnerx = x;
      _partnery = y;
      _partnerz = z;
      _to7sDungeon = to7sDungeon;
    }

    public void run()
    {
      if (_activeChar.isDead()) {
        return;
      }
      _activeChar.setIsIn7sDungeon(_to7sDungeon);

      _activeChar.enableAllSkills();
      try
      {
        _activeChar.teleToLocation(_partnerx, _partnery, _partnerz); } catch (Throwable e) {
        Wedding._log.error(e.getMessage(), e);
      }
    }
  }
}