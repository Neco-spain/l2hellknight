package l2m.gameserver.network.telnet;

import org.apache.commons.lang3.ArrayUtils;

public abstract class TelnetCommand
  implements Comparable<TelnetCommand>
{
  private final String command;
  private final String[] acronyms;

  public TelnetCommand(String command)
  {
    this(command, ArrayUtils.EMPTY_STRING_ARRAY);
  }

  public TelnetCommand(String command, String[] acronyms)
  {
    this.command = command;
    this.acronyms = acronyms;
  }

  public String getCommand()
  {
    return command;
  }

  public String[] getAcronyms()
  {
    return acronyms;
  }

  public abstract String getUsage();

  public abstract String handle(String[] paramArrayOfString);

  public boolean equals(String command)
  {
    for (String acronym : acronyms)
      if (command.equals(acronym))
        return true;
    return this.command.equalsIgnoreCase(command);
  }

  public String toString()
  {
    return command;
  }

  public boolean equals(Object o)
  {
    if (o == this)
      return true;
    if (o == null)
      return true;
    if ((o instanceof TelnetCommand))
      return command.equals(((TelnetCommand)o).command);
    return false;
  }

  public int compareTo(TelnetCommand o)
  {
    return command.compareTo(o.command);
  }
}