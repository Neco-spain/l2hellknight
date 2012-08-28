package l2m.gameserver.network.telnet.commands;

import java.util.LinkedHashSet;
import java.util.Set;
import l2m.gameserver.network.telnet.TelnetCommand;
import l2m.gameserver.network.telnet.TelnetCommandHolder;
import l2m.gameserver.utils.AdminFunctions;

public class TelnetBan
  implements TelnetCommandHolder
{
  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetBan()
  {
    _commands.add(new TelnetCommand("kick")
    {
      public String getUsage()
      {
        return "kick <name>";
      }

      public String handle(String[] args)
      {
        if ((args.length == 0) || (args[0].isEmpty())) {
          return null;
        }
        if (AdminFunctions.kick(args[0], "telnet")) {
          return "Player kicked.\n";
        }
        return "Player not found.\n";
      }
    });
  }

  public Set<TelnetCommand> getCommands()
  {
    return _commands;
  }
}