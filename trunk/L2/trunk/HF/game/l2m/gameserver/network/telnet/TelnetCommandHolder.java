package l2m.gameserver.network.telnet;

import java.util.Set;

public abstract interface TelnetCommandHolder
{
  public abstract Set<TelnetCommand> getCommands();
}