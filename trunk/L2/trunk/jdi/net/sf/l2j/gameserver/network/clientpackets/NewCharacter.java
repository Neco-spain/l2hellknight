package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.CharTemplateTable;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.serverpackets.CharTemplates;
import net.sf.l2j.gameserver.templates.L2PcTemplate;

public final class NewCharacter extends L2GameClientPacket
{
  private static final String _C__0E_NEWCHARACTER = "[C] 0E NewCharacter";
  private static Logger _log = Logger.getLogger(NewCharacter.class.getName());

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    if (Config.DEBUG) _log.fine("CreateNewChar");

    CharTemplates ct = new CharTemplates();

    L2PcTemplate template = CharTemplateTable.getInstance().getTemplate(0);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.fighter);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.mage);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenFighter);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.elvenMage);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.darkFighter);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.darkMage);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.orcFighter);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.orcMage);
    ct.addChar(template);

    template = CharTemplateTable.getInstance().getTemplate(ClassId.dwarvenFighter);
    ct.addChar(template);

    sendPacket(ct);
  }

  public String getType()
  {
    return "[C] 0E NewCharacter";
  }
}