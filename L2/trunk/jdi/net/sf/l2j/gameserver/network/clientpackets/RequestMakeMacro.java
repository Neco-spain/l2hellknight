package net.sf.l2j.gameserver.network.clientpackets;

import java.io.PrintStream;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2Macro.L2MacroCmd;
import net.sf.l2j.gameserver.model.MacroList;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestMakeMacro extends L2GameClientPacket
{
  private L2Macro _macro;
  private int _commandsLenght = 0;
  private static final String _C__C1_REQUESTMAKEMACRO = "[C] C1 RequestMakeMacro";
  private static final int MAX_MACRO_LENGTH = 12;

  protected void readImpl()
  {
    int _id = readD();
    String _name = readS();
    String _desc = readS();
    String _acronym = readS();
    int _icon = readC();
    int _count = readC();
    if (_count > 12) _count = 12;

    L2Macro.L2MacroCmd[] commands = new L2Macro.L2MacroCmd[_count];
    if (Config.DEBUG) System.out.println("Make macro id:" + _id + "\tname:" + _name + "\tdesc:" + _desc + "\tacronym:" + _acronym + "\ticon:" + _icon + "\tcount:" + _count);
    for (int i = 0; i < _count; i++)
    {
      int entry = readC();
      int type = readC();
      int d1 = readD();
      int d2 = readC();
      String command = readS();
      _commandsLenght += command.length();
      commands[i] = new L2Macro.L2MacroCmd(entry, type, d1, d2, command);
      if (!Config.DEBUG) continue; System.out.println("entry:" + entry + "\ttype:" + type + "\td1:" + d1 + "\td2:" + d2 + "\tcommand:" + command);
    }
    _macro = new L2Macro(_id, _icon, _name, _desc, _acronym, commands);
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    if (_commandsLenght > 255)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.INVALID_MACRO));
      return;
    }
    if (player.getMacroses().getAllMacroses().length > 24)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS));
      return;
    }
    if (_macro.name.length() == 0)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.ENTER_THE_MACRO_NAME));
      return;
    }
    if (_macro.descr.length() > 32)
    {
      player.sendPacket(new SystemMessage(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS));
      return;
    }
    player.registerMacro(_macro);
  }

  public String getType()
  {
    return "[C] C1 RequestMakeMacro";
  }
}