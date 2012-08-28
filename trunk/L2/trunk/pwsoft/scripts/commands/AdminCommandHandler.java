package scripts.commands;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.util.log.AbstractLogger;
import scripts.commands.admincommandhandlers.AdminAdmin;
import scripts.commands.admincommandhandlers.AdminAnnouncements;
import scripts.commands.admincommandhandlers.AdminBBS;
import scripts.commands.admincommandhandlers.AdminBan;
import scripts.commands.admincommandhandlers.AdminBanChat;
import scripts.commands.admincommandhandlers.AdminCache;
import scripts.commands.admincommandhandlers.AdminChangeAccessLevel;
import scripts.commands.admincommandhandlers.AdminCountdown;
import scripts.commands.admincommandhandlers.AdminCreateItem;
import scripts.commands.admincommandhandlers.AdminCursedWeapons;
import scripts.commands.admincommandhandlers.AdminDelete;
import scripts.commands.admincommandhandlers.AdminDonator;
import scripts.commands.admincommandhandlers.AdminDoorControl;
import scripts.commands.admincommandhandlers.AdminEditChar;
import scripts.commands.admincommandhandlers.AdminEditNpc;
import scripts.commands.admincommandhandlers.AdminEffects;
import scripts.commands.admincommandhandlers.AdminEnchant;
import scripts.commands.admincommandhandlers.AdminEvent;
import scripts.commands.admincommandhandlers.AdminEventEngine;
import scripts.commands.admincommandhandlers.AdminExpSp;
import scripts.commands.admincommandhandlers.AdminFightCalculator;
import scripts.commands.admincommandhandlers.AdminGeoEditor;
import scripts.commands.admincommandhandlers.AdminGeodata;
import scripts.commands.admincommandhandlers.AdminGm;
import scripts.commands.admincommandhandlers.AdminGmChat;
import scripts.commands.admincommandhandlers.AdminHeal;
import scripts.commands.admincommandhandlers.AdminHelpPage;
import scripts.commands.admincommandhandlers.AdminHero;
import scripts.commands.admincommandhandlers.AdminInvul;
import scripts.commands.admincommandhandlers.AdminKick;
import scripts.commands.admincommandhandlers.AdminKill;
import scripts.commands.admincommandhandlers.AdminLevel;
import scripts.commands.admincommandhandlers.AdminLoc;
import scripts.commands.admincommandhandlers.AdminLogin;
import scripts.commands.admincommandhandlers.AdminMammon;
import scripts.commands.admincommandhandlers.AdminManor;
import scripts.commands.admincommandhandlers.AdminMenu;
import scripts.commands.admincommandhandlers.AdminMobGroup;
import scripts.commands.admincommandhandlers.AdminMonsterRace;
import scripts.commands.admincommandhandlers.AdminNoble;
import scripts.commands.admincommandhandlers.AdminPForge;
import scripts.commands.admincommandhandlers.AdminPathNode;
import scripts.commands.admincommandhandlers.AdminPetition;
import scripts.commands.admincommandhandlers.AdminPledge;
import scripts.commands.admincommandhandlers.AdminPolymorph;
import scripts.commands.admincommandhandlers.AdminQuest;
import scripts.commands.admincommandhandlers.AdminReload;
import scripts.commands.admincommandhandlers.AdminRepairChar;
import scripts.commands.admincommandhandlers.AdminRes;
import scripts.commands.admincommandhandlers.AdminRideWyvern;
import scripts.commands.admincommandhandlers.AdminShop;
import scripts.commands.admincommandhandlers.AdminShutdown;
import scripts.commands.admincommandhandlers.AdminSiege;
import scripts.commands.admincommandhandlers.AdminSkill;
import scripts.commands.admincommandhandlers.AdminSpawn;
import scripts.commands.admincommandhandlers.AdminTarget;
import scripts.commands.admincommandhandlers.AdminTeleport;
import scripts.commands.admincommandhandlers.AdminTest;
import scripts.commands.admincommandhandlers.AdminTvTEvent;
import scripts.commands.admincommandhandlers.AdminUnblockIp;
import scripts.commands.admincommandhandlers.AdminZone;

public final class AdminCommandHandler
{
  private static final Logger _log = AbstractLogger.getLogger(AdminCommandHandler.class.getName());
  private static AdminCommandHandler _instance;
  private Map<String, IAdminCommandHandler> _datatable;
  private static final Logger _priviLog = Logger.getLogger("AltPrivilegesAdmin");
  private static FastMap<String, Integer> _privileges;

  public static AdminCommandHandler getInstance()
  {
    if (_instance == null) {
      _instance = new AdminCommandHandler();
    }
    return _instance;
  }

  private AdminCommandHandler() {
    _datatable = new FastMap();
    registerAdminCommandHandler(new AdminAdmin());
    registerAdminCommandHandler(new AdminInvul());
    registerAdminCommandHandler(new AdminDelete());
    registerAdminCommandHandler(new AdminKill());
    registerAdminCommandHandler(new AdminTarget());
    registerAdminCommandHandler(new AdminShop());
    registerAdminCommandHandler(new AdminAnnouncements());
    registerAdminCommandHandler(new AdminCreateItem());
    registerAdminCommandHandler(new AdminHeal());
    registerAdminCommandHandler(new AdminHelpPage());
    registerAdminCommandHandler(new AdminShutdown());
    registerAdminCommandHandler(new AdminSpawn());
    registerAdminCommandHandler(new AdminSkill());
    registerAdminCommandHandler(new AdminExpSp());
    registerAdminCommandHandler(new AdminEventEngine());
    registerAdminCommandHandler(new AdminGmChat());
    registerAdminCommandHandler(new AdminEditChar());
    registerAdminCommandHandler(new AdminGm());
    registerAdminCommandHandler(new AdminTeleport());
    registerAdminCommandHandler(new AdminRepairChar());
    registerAdminCommandHandler(new AdminChangeAccessLevel());
    registerAdminCommandHandler(new AdminBan());
    registerAdminCommandHandler(new AdminPolymorph());
    registerAdminCommandHandler(new AdminBanChat());
    registerAdminCommandHandler(new AdminKick());
    registerAdminCommandHandler(new AdminMonsterRace());
    registerAdminCommandHandler(new AdminEditNpc());
    registerAdminCommandHandler(new AdminFightCalculator());
    registerAdminCommandHandler(new AdminMenu());
    registerAdminCommandHandler(new AdminSiege());
    registerAdminCommandHandler(new AdminPathNode());
    registerAdminCommandHandler(new AdminPetition());
    registerAdminCommandHandler(new AdminPForge());
    registerAdminCommandHandler(new AdminBBS());
    registerAdminCommandHandler(new AdminEffects());
    registerAdminCommandHandler(new AdminDoorControl());
    registerAdminCommandHandler(new AdminTest());
    registerAdminCommandHandler(new AdminEnchant());
    registerAdminCommandHandler(new AdminMobGroup());
    registerAdminCommandHandler(new AdminRes());
    registerAdminCommandHandler(new AdminMammon());
    registerAdminCommandHandler(new AdminUnblockIp());
    registerAdminCommandHandler(new AdminPledge());
    registerAdminCommandHandler(new AdminRideWyvern());
    registerAdminCommandHandler(new AdminLoc());
    registerAdminCommandHandler(new AdminLogin());
    registerAdminCommandHandler(new AdminCache());
    registerAdminCommandHandler(new AdminLevel());
    registerAdminCommandHandler(new AdminQuest());
    registerAdminCommandHandler(new AdminZone());
    registerAdminCommandHandler(new AdminCursedWeapons());
    registerAdminCommandHandler(new AdminGeodata());
    registerAdminCommandHandler(new AdminGeoEditor());
    registerAdminCommandHandler(new AdminManor());
    registerAdminCommandHandler(new AdminTvTEvent());
    registerAdminCommandHandler(new AdminReload());
    registerAdminCommandHandler(new AdminDonator());
    registerAdminCommandHandler(new AdminHero());
    registerAdminCommandHandler(new AdminNoble());
    registerAdminCommandHandler(new AdminCountdown());
    registerAdminCommandHandler(new AdminEvent());
    _log.config("AdminCommandHandler: Loaded " + _datatable.size() + " handlers.");
  }

  public void registerAdminCommandHandler(IAdminCommandHandler handler)
  {
    String[] ids = handler.getAdminCommandList();
    for (int i = 0; i < ids.length; i++)
    {
      _datatable.put(ids[i], handler);
    }
  }

  public IAdminCommandHandler getAdminCommandHandler(String adminCommand) {
    String command = adminCommand;
    if (adminCommand.indexOf(" ") != -1) {
      command = adminCommand.substring(0, adminCommand.indexOf(" "));
    }

    return (IAdminCommandHandler)_datatable.get(command);
  }

  public IAdminCommandHandler getEGMCommandHandler(L2PcInstance player, String adminCommand) {
    System.out.println("###" + adminCommand);
    String command = adminCommand;
    if (adminCommand.indexOf(" ") != -1) {
      command = adminCommand.substring(0, adminCommand.indexOf(" "));
    }

    System.out.println("#####" + command);

    if ((command.startsWith("admin_announce")) || (command.startsWith("admin_banchat")) || (command.startsWith("admin_jail")) || (command.startsWith("admin_kick")) || (command.startsWith("admin_admin_heal")) || (command.startsWith("admin_admin_kill")) || (command.startsWith("admin_admin_res")) || (command.startsWith("admin_silence")) || (command.startsWith("admin_unbanchat")) || (command.startsWith("admin_unjail")) || (command.startsWith("admin_open")) || (command.startsWith("admin_close")) || (command.startsWith("admin_set_level")) || (command.startsWith("admin_rec")) || (command.startsWith("admin_vis")) || (command.startsWith("admin_invis")) || (command.startsWith("admin_recall")) || (command.startsWith("admin_recall_char_menu")) || (command.startsWith("admin_teleportto")) || (command.startsWith("admin_move_to")) || (command.startsWith("admin_teleport_character")) || (command.startsWith("admin_tele")) || (command.startsWith("admin_teleto")) || (command.startsWith("admin_setnoble")))
    {
      return (IAdminCommandHandler)_datatable.get(command);
    }

    return null;
  }

  public int size()
  {
    return _datatable.size();
  }

  public final boolean checkPrivileges(L2PcInstance player, String adminCommand)
  {
    if (!player.isGM()) {
      return false;
    }

    String command = adminCommand;
    if (adminCommand.indexOf(" ") != -1) {
      command = adminCommand.substring(0, adminCommand.indexOf(" "));
    }
    if (player.getAccessLevel() == 75)
    {
      return (command.startsWith("announce")) || (command.startsWith("banchat")) || (command.startsWith("jail")) || (command.startsWith("kick")) || (command.startsWith("heal")) || (command.startsWith("kill")) || (command.startsWith("res")) || (command.startsWith("silence")) || (command.startsWith("unbanchat")) || (command.startsWith("unjail")) || (command.startsWith("open")) || (command.startsWith("close")) || (command.startsWith("set_level")) || (command.startsWith("rec")) || (command.startsWith("vis")) || (command.startsWith("invis")) || (command.startsWith("recall")) || (command.startsWith("recall_char_menu")) || (command.startsWith("teleportto")) || (command.startsWith("move_to")) || (command.startsWith("teleport_character")) || (command.startsWith("tele")) || (command.startsWith("teleto")) || (command.startsWith("setnoble"));
    }

    if ((!Config.ALT_PRIVILEGES_ADMIN) || (Config.EVERYBODY_HAS_ADMIN_RIGHTS)) {
      return true;
    }

    if (_privileges == null) {
      _privileges = new FastMap();
    }

    if (!_datatable.containsKey(command)) {
      return false;
    }

    int requireLevel = 0;

    if (!_privileges.containsKey(command))
    {
      boolean isLoaded = false;

      InputStream is = null;
      try {
        Properties Settings = new Properties();
        is = new FileInputStream("./config/command-privileges.cfg");
        Settings.load(is);

        String stringLevel = Settings.getProperty(command);

        if (stringLevel != null) {
          isLoaded = true;
          requireLevel = Integer.parseInt(stringLevel);
        }
      } catch (Exception ignored) {
      } finally {
        try {
          if (is != null) {
            is.close();
          }
        }
        catch (Exception ignored)
        {
        }
      }

      if (!isLoaded) {
        if (Config.ALT_PRIVILEGES_SECURE_CHECK) {
          _priviLog.info("The command '" + command + "' haven't got a entry in the configuration file. The command cannot be executed!!");
          return false;
        }

        requireLevel = Config.ALT_PRIVILEGES_DEFAULT_LEVEL;
      }

      _privileges.put(command, Integer.valueOf(requireLevel));
    } else {
      requireLevel = ((Integer)_privileges.get(command)).intValue();
    }

    if (player.getAccessLevel() < requireLevel) {
      _priviLog.warning("<GM>" + player.getName() + ": have not access level to execute the command '" + command + "'");
      return false;
    }

    return true;
  }
}