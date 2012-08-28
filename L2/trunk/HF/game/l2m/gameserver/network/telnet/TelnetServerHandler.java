package l2m.gameserver.network.telnet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import l2m.gameserver.Config;
import l2m.gameserver.network.telnet.commands.TelnetBan;
import l2m.gameserver.network.telnet.commands.TelnetConfig;
import l2m.gameserver.network.telnet.commands.TelnetDebug;
import l2m.gameserver.network.telnet.commands.TelnetPerfomance;
import l2m.gameserver.network.telnet.commands.TelnetSay;
import l2m.gameserver.network.telnet.commands.TelnetServer;
import l2m.gameserver.network.telnet.commands.TelnetStatus;
import l2m.gameserver.network.telnet.commands.TelnetWorld;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelnetServerHandler extends SimpleChannelUpstreamHandler
  implements TelnetCommandHolder
{
  private static final Logger _log = LoggerFactory.getLogger(TelnetServerHandler.class);

  private static final Pattern COMMAND_ARGS_PATTERN = Pattern.compile("\"([^\"]*)\"|([^\\s]+)");

  private Set<TelnetCommand> _commands = new LinkedHashSet();

  public TelnetServerHandler()
  {
    _commands.add(new TelnetCommand("help", new String[] { "h" })
    {
      public String getUsage()
      {
        return "help [command]";
      }

      public String handle(String[] args)
      {
        if (args.length == 0)
        {
          StringBuilder sb = new StringBuilder();
          sb.append("Available commands:\n");
          for (TelnetCommand cmd : _commands)
          {
            sb.append(cmd.getCommand()).append("\n");
          }

          return sb.toString();
        }

        TelnetCommand cmd = TelnetServerHandler.this.getCommand(args[0]);
        if (cmd == null) {
          return "Unknown command.\n";
        }
        return new StringBuilder().append("usage:\n").append(cmd.getUsage()).append("\n").toString();
      }
    });
    addHandler(new TelnetBan());
    addHandler(new TelnetConfig());
    addHandler(new TelnetDebug());
    addHandler(new TelnetPerfomance());
    addHandler(new TelnetSay());
    addHandler(new TelnetServer());
    addHandler(new TelnetStatus());
    addHandler(new TelnetWorld());
  }

  public void addHandler(TelnetCommandHolder handler)
  {
    for (TelnetCommand cmd : handler.getCommands())
      _commands.add(cmd);
  }

  public Set<TelnetCommand> getCommands()
  {
    return _commands;
  }

  private TelnetCommand getCommand(String command)
  {
    for (TelnetCommand cmd : _commands) {
      if (cmd.equals(command))
        return cmd;
    }
    return null;
  }

  private String tryHandleCommand(String command, String[] args)
  {
    TelnetCommand cmd = getCommand(command);

    if (cmd == null) {
      return "Unknown command.\n";
    }
    String response = cmd.handle(args);
    if (response == null) {
      response = "usage:\n" + cmd.getUsage() + "\n";
    }
    return response;
  }

  public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    throws Exception
  {
    e.getChannel().write("Welcome to L2 GameServer telnet console.\n");
    e.getChannel().write("It is " + new Date() + " now.\n");
    if (!Config.TELNET_PASSWORD.isEmpty())
    {
      e.getChannel().write("Password:");
      ctx.setAttachment(Boolean.FALSE);
    }
    else
    {
      e.getChannel().write("Type 'help' to see all available commands.\n");
      ctx.setAttachment(Boolean.TRUE);
    }
  }

  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
  {
    String request = (String)e.getMessage();

    String response = null;
    boolean close = false;

    if (Boolean.FALSE.equals(ctx.getAttachment())) {
      if (Config.TELNET_PASSWORD.equals(request))
      {
        ctx.setAttachment(Boolean.TRUE);
        request = "";
      }
      else
      {
        response = "Wrong password!\n";
        close = true;
      }
    }
    if (Boolean.TRUE.equals(ctx.getAttachment())) {
      if (request.isEmpty()) {
        response = "Type 'help' to see all available commands.\n";
      } else if (request.toLowerCase().equals("exit"))
      {
        response = "Have a good day!\n";
        close = true;
      }
      else
      {
        Matcher m = COMMAND_ARGS_PATTERN.matcher(request);

        m.find();
        String command = m.group();

        List args = new ArrayList();

        while (m.find())
        {
          String arg = m.group(1);
          if (arg == null)
            arg = m.group(0);
          args.add(arg);
        }

        response = tryHandleCommand(command, (String[])args.toArray(new String[args.size()]));
      }

    }

    ChannelFuture future = e.getChannel().write(response);

    if (close)
      future.addListener(ChannelFutureListener.CLOSE);
  }

  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
  {
    if ((e.getCause() instanceof IOException))
      e.getChannel().close();
    else
      _log.error("", e.getCause());
  }
}