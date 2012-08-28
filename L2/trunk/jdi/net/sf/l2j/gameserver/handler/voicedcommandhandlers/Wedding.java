package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CoupleManager;
import net.sf.l2j.gameserver.instancemanager.GrandBossManager;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ConfirmDlg;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.Broadcast;

public class Wedding
  implements IVoicedCommandHandler
{
  static final Logger _log = Logger.getLogger(Wedding.class.getName());
  private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

  public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
  {
    if (command.startsWith("engage"))
      return Engage(activeChar);
    if (command.startsWith("divorce"))
      return Divorce(activeChar);
    if (command.startsWith("gotolove"))
      return GoToLove(activeChar);
    return false;
  }

  public boolean Divorce(L2PcInstance activeChar)
  {
    if (activeChar.getPartnerId() == 0) {
      return false;
    }
    int _partnerId = activeChar.getPartnerId();
    int _coupleId = activeChar.getCoupleId();
    int AdenaAmount = 0;

    if (activeChar.isMarried())
    {
      activeChar.sendMessage("You are now divorced.");

      AdenaAmount = activeChar.getAdena() / 100 * Config.L2JMOD_WEDDING_DIVORCE_COSTS;
      activeChar.getInventory().reduceAdena("Wedding", AdenaAmount, activeChar, null);
    }
    else
    {
      activeChar.sendMessage("You have broken up as a couple.");
    }

    L2PcInstance partner = (L2PcInstance)L2World.getInstance().findObject(_partnerId);

    if (partner != null)
    {
      partner.setPartnerId(0);
      if (partner.isMarried())
        partner.sendMessage("Your spouse has decided to divorce you.");
      else {
        partner.sendMessage("Your fiance has decided to break the engagement with you.");
      }

      if (AdenaAmount > 0) {
        partner.addAdena("WEDDING", AdenaAmount, null, false);
      }
      CoupleManager.getInstance().deleteCouple(_coupleId);
      CoupleManager.getInstance().checkCouple(activeChar);
      CoupleManager.getInstance().checkCouple(partner);

      return true;
    }

    _log.warning("Divorce partner = NULL");

    return false;
  }

  public boolean Engage(L2PcInstance activeChar)
  {
    if (activeChar.getTarget() == null)
    {
      activeChar.sendMessage("You have no one targeted.");
      return false;
    }

    if (!(activeChar.getTarget() instanceof L2PcInstance))
    {
      activeChar.sendMessage("You can only ask another player to engage you.");

      return false;
    }

    if (activeChar.getPartnerId() != 0)
    {
      activeChar.sendMessage("You are already engaged.");
      if (Config.L2JMOD_WEDDING_PUNISH_INFIDELITY)
      {
        activeChar.startAbnormalEffect(8192);

        int skillLevel = 1;

        if (activeChar.getLevel() > 40)
          skillLevel = 2;
        int skillId;
        int skillId;
        if (activeChar.isMageClass())
          skillId = 4361;
        else {
          skillId = 4362;
        }
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

        if (activeChar.getFirstEffect(skill) == null)
        {
          skill.getEffects(activeChar, activeChar);
          SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
          sm.addSkillName(skillId);
          activeChar.sendPacket(sm);
        }
      }
      return false;
    }

    L2PcInstance ptarget = (L2PcInstance)activeChar.getTarget();

    if (ptarget.getObjectId() == activeChar.getObjectId())
    {
      activeChar.sendMessage("Is there something wrong with you, are you trying to go out with youself?");
      return false;
    }

    if (ptarget.isMarried())
    {
      activeChar.sendMessage("Player already married.");
      return false;
    }

    if (ptarget.isEngageRequest())
    {
      activeChar.sendMessage("Player already asked by someone else.");
      return false;
    }

    if (ptarget.getPartnerId() != 0)
    {
      activeChar.sendMessage("Player already engaged with someone else.");
      return false;
    }

    if ((ptarget.getAppearance().getSex() == activeChar.getAppearance().getSex()) && (!Config.L2JMOD_WEDDING_SAMESEX))
    {
      activeChar.sendMessage("Gay marriage is not allowed on this server!");
      return false;
    }

    boolean FoundOnFriendList = false;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();

      PreparedStatement statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
      statement.setInt(1, ptarget.getObjectId());
      ResultSet rset = statement.executeQuery();

      while (rset.next())
      {
        int objectId = rset.getInt("friend_id");
        if (objectId == activeChar.getObjectId())
          FoundOnFriendList = true;
      }
    }
    catch (Exception e)
    {
      _log.warning("could not read friend data:" + e);
    }
    finally {
      try {
        con.close(); } catch (Exception e) {
      }
    }
    if (!FoundOnFriendList)
    {
      activeChar.sendMessage("The player you want to ask is not on your friends list, you must first be on each others friends list before you choose to engage.");
      return false;
    }

    ptarget.setEngageRequest(true, activeChar.getObjectId());

    ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.S1_S2.getId()).addString(activeChar.getName() + " is asking to engage you. Do you want to start a new relationship?");
    ptarget.sendPacket(dlg);
    return true;
  }

  public boolean GoToLove(L2PcInstance activeChar)
  {
    if (!activeChar.isMarried())
    {
      activeChar.sendMessage("You're not married.");
      return false;
    }

    if (activeChar.getPartnerId() == 0)
    {
      activeChar.sendMessage("Couldn't find your fiance in the Database - Inform a Gamemaster.");
      _log.severe("Married but couldn't find parter for " + activeChar.getName());
      return false;
    }

    if (GrandBossManager.getInstance().getZone(activeChar) != null)
    {
      activeChar.sendMessage("You are inside a Boss Zone.");
      return false;
    }

    L2PcInstance partner = (L2PcInstance)L2World.getInstance().findObject(activeChar.getPartnerId());
    if (partner == null)
    {
      activeChar.sendMessage("Your partner is not online.");
      return false;
    }
    if (partner.isInJail())
    {
      activeChar.sendMessage("Your partner is in Jail.");
      return false;
    }
    if (GrandBossManager.getInstance().getZone(partner) != null)
    {
      activeChar.sendMessage("Your partner is inside a Boss Zone.");
      return false;
    }
    if (partner.isInOlympiadMode())
    {
      activeChar.sendMessage("Your partner is in the Olympiad now.");
      return false;
    }
    if (partner.isInsideZone(4))
    {
      activeChar.sendMessage("Your partner is inside Siege.");
      return false;
    }
    if (partner.atEvent)
    {
      activeChar.sendMessage("Your partner is in an event.");
      return false;
    }
    if (partner.isInDuel())
    {
      activeChar.sendMessage("Your partner is in a duel.");
      return false;
    }
    if (partner.isFestivalParticipant())
    {
      activeChar.sendMessage("Your partner is in a festival.");
      return false;
    }
    if ((partner.isInParty()) && (partner.getParty().isInDimensionalRift()))
    {
      activeChar.sendMessage("Your partner is in dimensional rift.");
      return false;
    }
    if (partner.inObserverMode())
    {
      activeChar.sendMessage("Your partner is in the observation.");
      return false;
    }
    if (activeChar.isInFunEvent())
    {
      activeChar.sendMessage("You are in event now.");
      return false;
    }
    if (partner.isInFunEvent())
    {
      activeChar.sendMessage("Your partner is in an event.");
      return false;
    }
    if ((partner.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("Your partner is in siege, you can't go to your partner.");
      return false;
    }

    if (activeChar.isInJail())
    {
      activeChar.sendMessage("You are in Jail!");
      return false;
    }
    if (activeChar.isInOlympiadMode())
    {
      activeChar.sendMessage("You are in the Olympiad now.");
      return false;
    }
    if (activeChar.atEvent)
    {
      activeChar.sendMessage("You are in an event.");
      return false;
    }
    if (activeChar.isInDuel())
    {
      activeChar.sendMessage("You are in a duel!");
      return false;
    }
    if (activeChar.inObserverMode())
    {
      activeChar.sendMessage("You are in the observation.");
      return false;
    }
    if ((activeChar.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).getSiege().getIsInProgress()))
    {
      activeChar.sendMessage("You are in siege, you can't go to your partner.");
      return false;
    }
    if (activeChar.isFestivalParticipant())
    {
      activeChar.sendMessage("You are in a festival.");
      return false;
    }
    if ((activeChar.isInParty()) && (activeChar.getParty().isInDimensionalRift()))
    {
      activeChar.sendMessage("You are in the dimensional rift.");
      return false;
    }

    if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
    {
      activeChar.sendPacket(new ActionFailed());
      return false;
    }
    if (activeChar.isCursedWeaponEquiped())
    {
      activeChar.sendMessage("You Cannot Teleport To Your Partner When You Have a Cursed Weapon Equipped.");
      activeChar.sendPacket(new ActionFailed());
      return false;
    }
    if (partner.isCursedWeaponEquiped())
    {
      activeChar.sendMessage("You Cannot Teleport To Your Partner When You Have a Cursed Weapon Equipped.");
      activeChar.sendPacket(new ActionFailed());
      return false;
    }
    if (!TvTEvent.onEscapeUse(activeChar.getObjectId()))
    {
      activeChar.sendPacket(new ActionFailed());
      return false;
    }

    int teleportTimer = Config.L2JMOD_WEDDING_TELEPORT_DURATION * 1000;

    activeChar.sendMessage("After " + teleportTimer / 60000 + " min. you will be teleported to your fiance.");
    activeChar.getInventory().reduceAdena("Wedding", Config.L2JMOD_WEDDING_TELEPORT_PRICE, activeChar, null);

    activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

    activeChar.setTarget(activeChar);
    activeChar.disableAllSkills();

    MagicSkillUser msk = new MagicSkillUser(activeChar, 1050, 1, teleportTimer, 0);
    Broadcast.toSelfAndKnownPlayersInRadius(activeChar, msk, 810000L);
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
        Wedding._log.severe(e.toString());
      }
    }
  }
}