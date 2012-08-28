package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Macro;
import net.sf.l2j.gameserver.model.L2Macro.L2MacroCmd;

public class SendMacroList extends L2GameServerPacket
{
  private final int _rev;
  private final int _count;
  private final L2Macro _macro;

  public SendMacroList(int rev, int count, L2Macro macro)
  {
    _rev = rev;
    _count = count;
    _macro = macro;
  }

  protected final void writeImpl()
  {
    writeC(231);

    writeD(_rev);
    writeC(0);
    writeC(_count);
    writeC(_macro != null ? 1 : 0);

    if (_macro != null) {
      writeD(_macro.id);
      writeS(_macro.name);
      writeS(_macro.descr);
      writeS(_macro.acronym);
      writeC(_macro.icon);

      writeC(_macro.commands.length);

      for (int i = 0; i < _macro.commands.length; i++) {
        L2Macro.L2MacroCmd cmd = _macro.commands[i];
        writeC(i + 1);
        writeC(cmd.type);
        writeD(cmd.d1);
        writeC(cmd.d2);
        writeS(cmd.cmd);
      }
    }
  }

  public String getType()
  {
    return "S.SendMacroList";
  }
}