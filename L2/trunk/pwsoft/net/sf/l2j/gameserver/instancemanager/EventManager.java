package net.sf.l2j.gameserver.instancemanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.FightClub;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.model.entity.olympiad.Olympiad;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Location;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.autoevents.anarchy.Anarchy;
import scripts.autoevents.basecapture.BaseCapture;
import scripts.autoevents.encounter.Encounter;
import scripts.autoevents.fighting.Fighting;
import scripts.autoevents.lasthero.LastHero;
import scripts.autoevents.masspvp.massPvp;
import scripts.autoevents.schuttgart.Schuttgart;

public class EventManager
{
  private static final Logger _log = AbstractLogger.getLogger(EventManager.class.getName());
  private static EventManager _instance;

  public static EventManager getInstance()
  {
    return _instance;
  }

  public static void init() {
    _log.info(" ");
    _instance = new EventManager();
    _instance.loadEvents();
  }

  public void loadEvents() {
    if (Config.MASS_PVP)
      massPvp.getEvent().load();
    else {
      _log.info("EventManager: MassPvp, off.");
    }

    if (Config.ALLOW_SCH)
      Schuttgart.init();
    else {
      _log.info("EventManager: Schuttgart, off.");
    }

    if (Config.ELH_ENABLE)
      LastHero.init();
    else {
      _log.info("EventManager: Last Hero, off.");
    }

    if (Config.EBC_ENABLE)
      BaseCapture.init();
    else {
      _log.info("EventManager: Base Capture, off.");
    }

    if (Config.EENC_ENABLE)
      Encounter.init();
    else {
      _log.info("EventManager: Encounter, off.");
    }

    if (Config.ALLOW_MEDAL_EVENT) {
      manageMedalsEvent();
      _log.info("EventManager: Medals, on.");
    } else {
      _log.info("EventManager: Medals, off.");
    }

    if (Config.ANARCHY_ENABLE)
      Anarchy.init();
    else {
      _log.info("EventManager: Anarchy, off.");
    }

    if (Config.FIGHTING_ENABLE)
      Fighting.init();
    else {
      _log.info("EventManager: Fighting, off.");
    }

    _log.info(" ");
  }

  public boolean isReg(L2PcInstance player) {
    if ((Config.MASS_PVP) && 
      (massPvp.getEvent().isReg(player))) {
      return true;
    }

    return (player.inObserverMode()) || (player.inFClub()) || (player.inFightClub()) || (player.isEventWait());
  }

  public boolean checkPlayer(L2PcInstance player)
  {
    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode())) {
      return false;
    }

    if ((Config.TVT_EVENT_ENABLED) && (TvTEvent.isPlayerParticipant(player.getName()))) {
      return false;
    }

    if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player))) {
      return false;
    }

    if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player))) {
      return false;
    }

    return (!Config.EBC_ENABLE) || (!BaseCapture.getEvent().isRegged(player));
  }

  public boolean onEvent(L2PcInstance player)
  {
    if ((Olympiad.isRegisteredInComp(player)) || (player.isInOlympiadMode())) {
      return true;
    }

    if ((Config.TVT_EVENT_ENABLED) && (TvTEvent.isPlayerParticipant(player.getName()))) {
      return true;
    }

    if ((Config.ELH_ENABLE) && (LastHero.getEvent().isRegged(player))) {
      return true;
    }

    if ((Config.MASS_PVP) && (massPvp.getEvent().isReg(player))) {
      return true;
    }

    return (Config.EBC_ENABLE) && (BaseCapture.getEvent().isRegged(player));
  }

  public boolean isRegAndBattle(L2PcInstance player)
  {
    return (Config.MASS_PVP) && 
      (massPvp.getEvent().isRegAndBattle(player));
  }

  public void doDie(L2PcInstance player, L2Character killer)
  {
    if ((killer.isPlayer()) || (killer.isL2Summon()))
    {
      player.setFightClub(false);
      player.setEventWait(false);
      FightClub.unReg(player.getObjectId(), player.inFightClub());

      if ((Config.MASS_PVP) && 
        (massPvp.getEvent().isReg(player))) {
        massPvp.getEvent().doDie(player, killer);
      }

      if (Config.ELH_ENABLE)
        LastHero.getEvent().notifyDeath(player);
    }
  }

  public void onLogin(L2PcInstance player)
  {
    if ((Config.ANARCHY_ENABLE) && (Anarchy.getEvent().isInBattle())) {
      player.sendPacket(Static.ANARCHY_EVENT);
    }

    if (Config.ALLOW_MEDAL_EVENT) {
      player.sendPacket(Static.MEDALS_EVENT);
    }

    if (Config.EVENT_SPECIAL_DROP)
      CustomServerData.getInstance().showSpecialDropWelcome(player);
  }

  public void onExit(L2PcInstance player)
  {
    player.setChannel(1);

    player.setEventWait(false);

    if ((Config.MASS_PVP) && 
      (massPvp.getEvent().isReg(player))) {
      massPvp.getEvent().onExit(player);
    }

    if (Config.ELH_ENABLE) {
      LastHero.getEvent().notifyFail(player);
    }

    if (Config.EBC_ENABLE) {
      BaseCapture.getEvent().notifyFail(player);
    }

    if (Config.TVT_EVENT_ENABLED)
      TvTEvent.onLogout(player);
  }

  public void onTexture(L2PcInstance player)
  {
    onExit(player);

    player.setChannel(1);
    player.setTeam(0);
    player.teleToLocation(116530, 76141, -2730);
  }

  public void SetDBValue(String name, String var, String value) {
    if ((name == null) || (var == null) || (value == null)) {
      return;
    }

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
      statement.setString(1, name);
      statement.setString(2, var);
      statement.setString(3, value);
      statement.executeUpdate();
    } catch (Exception e) {
      _log.warning("EventManager: could not save " + name + "; info" + e);
    } finally {
      Close.CS(con, statement);
    }
  }

  public long GetDBValue(String name, String var) {
    if ((name == null) || (var == null)) {
      return 0L;
    }

    long result = 0L;
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet rset = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT value FROM quest_global_data WHERE quest_name = ? AND var = ?");
      statement.setString(1, name);
      statement.setString(2, var);
      rset = statement.executeQuery();
      if (rset.first())
        result = rset.getLong(1);
    }
    catch (Exception e) {
      _log.warning("EventManager: could not load " + name + "; info" + e);
    } finally {
      Close.CSR(con, statement, rset);
    }
    return result;
  }

  public L2NpcInstance doSpawn(int npcId, Location loc, long unspawn) {
    L2NpcInstance result = null;
    try {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      L2Spawn spawn = new L2Spawn(template);
      spawn.setHeading(loc.h);
      spawn.setLocx(loc.x);
      spawn.setLocy(loc.y);
      spawn.setLocz(loc.z + 20);
      spawn.stopRespawn();
      result = spawn.spawnOne();
      return result;
    } catch (Exception e1) {
      _log.warning("EventManager: Could not spawn Npc " + npcId);
    }
    return null;
  }

  public void announce(String text) {
    Announcements.getInstance().announceToAll(text);
  }

  public static String getNameById(int charId) {
    Connect con = null;
    PreparedStatement statement = null;
    ResultSet result = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      statement = con.prepareStatement("SELECT char_name FROM `characters` WHERE `obj_Id`=? LIMIT 1");
      statement.setInt(1, charId);
      result = statement.executeQuery();

      if (result.next()) {
        String str = result.getString("char_name");
        return str;
      }
    }
    catch (Exception e)
    {
      _log.warning("EventManager: getSellerName() error: " + e);
    } finally {
      Close.CSR(con, statement, result);
    }
    return "";
  }

  private void manageMedalsEvent()
  {
  }
}