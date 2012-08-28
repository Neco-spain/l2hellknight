package l2m.gameserver.network.telnet.commands;

import java.util.LinkedHashSet;
import java.util.Set;
import l2m.gameserver.Announcements;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.World;
import l2m.gameserver.network.telnet.TelnetCommand;
import l2m.gameserver.network.telnet.TelnetCommandHolder;
import l2m.gameserver.network.serverpackets.Say2;
import l2m.gameserver.network.serverpackets.components.ChatType;

public class TelnetSay
  implements TelnetCommandHolder
{
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetSay()
  {
    _commands.add(new TelnetCommand("announce", new String[] { "ann" })
    {
      public String getUsage()
      {
        return "announce <text>";
      }

      public String handle(String[] args)
      {
        if (args.length == 0) {
          return null;
        }
        Announcements.getInstance().announceToAll(args[0]);

        return "Announcement sent.\n";
      }
    });
    _commands.add(new TelnetCommand("message", new String[] { "msg" })
    {
      public String getUsage()
      {
        return "message <player> <text>";
      }

      public String handle(String[] args)
      {
        if (args.length < 2) {
          return null;
        }
        Player player = World.getPlayer(args[0]);
        if (player == null) {
          return "Player not found.\n";
        }
        Say2 cs = new Say2(0, ChatType.TELL, "[Admin]", args[1]);
        player.sendPacket(cs);

        return "Message sent.\n";
      }
    });
  }

  public Set<TelnetCommand> getCommands()
  {
    return _commands;
  }
}