package l2m.gameserver.handler.voicecommands.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.lang.reference.HardReference;
import l2p.commons.threading.RunnableImpl;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.ai.CtrlIntention;
import l2m.gameserver.ai.PlayerAI;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2m.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2m.gameserver.instancemanager.CoupleManager;
import l2m.gameserver.instancemanager.ReflectionManager;
import l2m.gameserver.listener.actor.player.OnAnswerListener;
import l2m.gameserver.model.EffectList;
import l2m.gameserver.model.GameObject;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Party;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.model.Zone.ZoneType;
import l2m.gameserver.model.entity.Couple;
import l2m.gameserver.network.serverpackets.ConfirmDlg;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.MagicSkillUse;
import l2m.gameserver.network.serverpackets.SetupGauge;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.network.serverpackets.components.SystemMsg;
import l2m.gameserver.skills.AbnormalEffect;
import l2m.gameserver.data.tables.SkillTable;
import l2m.gameserver.utils.Location;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wedding
  implements IVoicedCommandHandler
{
  private static final Logger _log = LoggerFactory.getLogger(Wedding.class);

  private static String[] _voicedCommands = { "divorce", "engage", "gotolove" };

  public boolean useVoicedCommand(String command, Player activeChar, String target)
  {
    if (!Config.ALLOW_WEDDING) {
      return false;
    }
    if (command.startsWith("engage"))
      return engage(activeChar);
    if (command.startsWith("divorce"))
      return divorce(activeChar);
    if (command.startsWith("gotolove"))
      return goToLove(activeChar);
    return false;
  }

  public boolean divorce(Player activeChar)
  {
    if (activeChar.getPartnerId() == 0) {
      return false;
    }
    int _partnerId = activeChar.getPartnerId();
    long AdenaAmount = 0L;

    if (activeChar.isMaried())
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Divorced", activeChar, new Object[0]));
      AdenaAmount = Math.abs(activeChar.getAdena() / 100L * Config.WEDDING_DIVORCE_COSTS - 10L);
      activeChar.reduceAdena(AdenaAmount, true);
    }
    else {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Disengaged", activeChar, new Object[0]));
    }
    activeChar.setMaried(false);
    activeChar.setPartnerId(0);
    Couple couple = CoupleManager.getInstance().getCouple(activeChar.getCoupleId());
    couple.divorce();
    couple = null;

    Player partner = GameObjectsStorage.getPlayer(_partnerId);

    if (partner != null)
    {
      partner.setPartnerId(0);
      if (partner.isMaried())
        partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDivorce", partner, new Object[0]));
      else
        partner.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerDisengage", partner, new Object[0]));
      partner.setMaried(false);

      if (AdenaAmount > 0L)
        partner.addAdena(AdenaAmount);
    }
    return true;
  }

  public boolean engage(Player activeChar)
  {
    if (activeChar.getTarget() == null)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NoneTargeted", activeChar, new Object[0]));
      return false;
    }

    if (!activeChar.getTarget().isPlayer())
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.OnlyAnotherPlayer", activeChar, new Object[0]));
      return false;
    }

    if (activeChar.getPartnerId() != 0)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.AlreadyEngaged", activeChar, new Object[0]));
      if (Config.WEDDING_PUNISH_INFIDELITY)
      {
        activeChar.startAbnormalEffect(AbnormalEffect.BIG_HEAD);

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
        Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);

        if (activeChar.getEffectList().getEffectsBySkill(skill) == null)
        {
          skill.getEffects(activeChar, activeChar, false, false);
          activeChar.sendPacket(new SystemMessage(110).addSkillName(skillId, skillLevel));
        }
      }
      return false;
    }

    Player ptarget = (Player)activeChar.getTarget();

    if (ptarget.getObjectId() == activeChar.getObjectId())
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.EngagingYourself", activeChar, new Object[0]));
      return false;
    }

    if (ptarget.isMaried())
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyMarried", activeChar, new Object[0]));
      return false;
    }

    if (ptarget.getPartnerId() != 0)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged", activeChar, new Object[0]));
      return false;
    }

    Pair entry = ptarget.getAskListener(false);
    if ((entry != null) && ((entry.getValue() instanceof CoupleAnswerListener)))
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyAsked", activeChar, new Object[0]));
      return false;
    }

    if (ptarget.getPartnerId() != 0)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PlayerAlreadyEngaged", activeChar, new Object[0]));
      return false;
    }

    if ((ptarget.getSex() == activeChar.getSex()) && (!Config.WEDDING_SAMESEX))
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.SameSex", activeChar, new Object[0]));
      return false;
    }

    boolean FoundOnFriendList = false;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();

      statement = con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id=?");
      statement.setInt(1, ptarget.getObjectId());
      rset = statement.executeQuery();

      while (rset.next())
      {
        int objectId = rset.getInt("friend_id");
        if (objectId != activeChar.getObjectId())
          continue;
        FoundOnFriendList = true;
      }

    }
    catch (Exception e)
    {
      _log.error("", e);
    }
    finally
    {
      DbUtils.closeQuietly(con, statement, rset);
    }

    if (!FoundOnFriendList)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.NotInFriendlist", activeChar, new Object[0]));
      return false;
    }

    ConfirmDlg packet = (ConfirmDlg)new ConfirmDlg(SystemMsg.S1, 60000).addString("Player " + activeChar.getName() + " asking you to engage. Do you want to start new relationship?");
    ptarget.ask(packet, new CoupleAnswerListener(activeChar, ptarget));
    return true;
  }

  public boolean goToLove(Player activeChar)
  {
    if (!activeChar.isMaried())
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.YoureNotMarried", activeChar, new Object[0]));
      return false;
    }

    if (activeChar.getPartnerId() == 0)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerNotInDB", activeChar, new Object[0]));
      return false;
    }

    Player partner = GameObjectsStorage.getPlayer(activeChar.getPartnerId());
    if (partner == null)
    {
      activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.PartnerOffline", activeChar, new Object[0]));
      return false;
    }

    if ((partner.isInOlympiadMode()) || (partner.isFestivalParticipant()) || (activeChar.isMovementDisabled()) || (activeChar.isMuted(null)) || (activeChar.isInOlympiadMode()) || (activeChar.isInDuel()) || (activeChar.isFestivalParticipant()) || (activeChar.getPlayer().isTerritoryFlagEquipped()) || (partner.isInZone(Zone.ZoneType.no_summon)))
    {
      activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar, new Object[0]));
      return false;
    }

    if (((activeChar.isInParty()) && (activeChar.getParty().isInDimensionalRift())) || ((partner.isInParty()) && (partner.getParty().isInDimensionalRift())))
    {
      activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar, new Object[0]));
      return false;
    }

    if ((activeChar.getTeleMode() != 0) || (activeChar.getReflection() != ReflectionManager.DEFAULT))
    {
      activeChar.sendMessage(new CustomMessage("common.TryLater", activeChar, new Object[0]));
      return false;
    }

    if ((partner.isInZoneBattle()) || (partner.isInZone(Zone.ZoneType.SIEGE)) || (partner.isInZone(Zone.ZoneType.no_restart)) || (partner.isInOlympiadMode()) || (activeChar.isInZoneBattle()) || (activeChar.isInZone(Zone.ZoneType.SIEGE)) || (activeChar.isInZone(Zone.ZoneType.no_restart)) || (activeChar.isInOlympiadMode()) || (partner.getReflection() != ReflectionManager.DEFAULT) || (partner.isInZone(Zone.ZoneType.no_summon)) || (activeChar.isInObserverMode()) || (partner.isInObserverMode()))
    {
      activeChar.sendPacket(Msg.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
      return false;
    }

    if (!activeChar.reduceAdena(Config.WEDDING_TELEPORT_PRICE, true))
    {
      activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
      return false;
    }

    int teleportTimer = Config.WEDDING_TELEPORT_INTERVAL;

    activeChar.abortAttack(true, true);
    activeChar.abortCast(true, true);
    activeChar.sendActionFailed();
    activeChar.stopMove();
    activeChar.startParalyzed();

    activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Wedding.Teleport", activeChar, new Object[0]).addNumber(teleportTimer / 60));
    activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);

    activeChar.broadcastPacket(new L2GameServerPacket[] { new MagicSkillUse(activeChar, activeChar, 1050, 1, teleportTimer, 0L) });
    activeChar.sendPacket(new SetupGauge(activeChar, 0, teleportTimer));

    ThreadPoolManager.getInstance().schedule(new EscapeFinalizer(activeChar, partner.getLoc()), teleportTimer * 1000L);
    return true;
  }

  public String[] getVoicedCommandList()
  {
    return _voicedCommands;
  }

  public void onLoad()
  {
    VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
  }

  public void onReload()
  {
  }

  public void onShutdown()
  {
  }

  static class EscapeFinalizer extends RunnableImpl
  {
    private Player _activeChar;
    private Location _loc;

    EscapeFinalizer(Player activeChar, Location loc)
    {
      _activeChar = activeChar;
      _loc = loc;
    }

    public void runImpl()
      throws Exception
    {
      _activeChar.stopParalyzed();
      if (_activeChar.isDead())
        return;
      _activeChar.teleToLocation(_loc);
    }
  }

  private static class CoupleAnswerListener
    implements OnAnswerListener
  {
    private HardReference<Player> _playerRef1;
    private HardReference<Player> _playerRef2;

    public CoupleAnswerListener(Player player1, Player player2)
    {
      _playerRef1 = player1.getRef();
      _playerRef2 = player2.getRef();
    }

    public void sayYes()
    {
      Player player1;
      Player player2;
      if (((player1 = (Player)_playerRef1.get()) == null) || ((player2 = (Player)_playerRef2.get()) == null))
        return;
      Player player2;
      CoupleManager.getInstance().createCouple(player1, player2);
      player1.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerYes", player2, new Object[0]));
    }

    public void sayNo()
    {
      Player player1;
      Player player2;
      if (((player1 = (Player)_playerRef1.get()) == null) || ((player2 = (Player)_playerRef2.get()) == null))
        return;
      Player player2;
      player1.sendMessage(new CustomMessage("l2p.gameserver.model.L2Player.EngageAnswerNo", player2, new Object[0]));
    }
  }
}