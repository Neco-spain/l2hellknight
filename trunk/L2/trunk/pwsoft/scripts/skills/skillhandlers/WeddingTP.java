package scripts.skills.skillhandlers;

import javolution.util.FastList;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.L2Party;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scripts.skills.ISkillHandler;

public class WeddingTP
  implements ISkillHandler
{
  static final Log _log = LogFactory.getLog(WeddingTP.class);
  private static final L2Skill.SkillType[] SKILL_IDS = { L2Skill.SkillType.WEDDINGTP };

  public void useSkill(L2Character activeChar, L2Skill skill, FastList<L2Object> targets)
  {
    L2PcInstance player = (L2PcInstance)activeChar;

    if ((player.getX() >= -150000) && (player.getX() <= -60000) && (player.getY() >= -250000) && (player.getY() <= -180000)) {
      return;
    }
    if (!player.isMarried())
    {
      if (!player.getAppearance().getSex())
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0436\u0435\u043D\u0430\u0442\u044B");
      else
        player.sendMessage("\u0412\u044B \u043D\u0435 \u0437\u0430\u043C\u0443\u0436\u0435\u043C");
      return;
    }

    if (player.getPartnerId() == 0)
    {
      player.sendMessage("\u0423\u043F\u0441, \u0441\u043E\u043E\u0431\u0449\u0438\u0442\u0435 \u043E\u0431 \u044D\u0442\u043E\u0439 \u043E\u0448\u0438\u0431\u043A\u0435 \u0410\u0434\u043C\u0438\u043D\u0438\u0441\u0442\u0440\u0430\u0442\u043E\u0440\u0443");
      return;
    }

    L2PcInstance partner = (L2PcInstance)L2World.getInstance().findObject(player.getPartnerId());

    if (partner == null)
    {
      player.sendMessage("\u0412\u0430\u0448\u0430 \u043F\u043E\u043B\u043E\u0432\u0438\u043D\u043A\u0430 \u043D\u0435 \u0432 \u0438\u0433\u0440\u0435");
      return;
    }

    String Sex = "";
    if (!partner.getAppearance().getSex())
      Sex = "\u0412\u0430\u0448\u0430 \u0436\u0435\u043D\u0430";
    else {
      Sex = "\u0412\u0430\u0448 \u043C\u0443\u0436";
    }
    Castle castleac = CastleManager.getInstance().getCastle(player);
    Castle castlepn = CastleManager.getInstance().getCastle(partner);

    if (player.isInJail())
    {
      player.sendMessage("\u0412\u044B \u0432 \u0442\u044E\u0440\u044C\u043C\u0435");
      return;
    }
    if ((player.isInOlympiadMode()) || (player.getOlympiadSide() > 1))
    {
      player.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435");
      return;
    }
    if (player.atEvent)
    {
      player.sendMessage("\u0412\u044B \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435");
      return;
    }
    if (player.isInDuel())
    {
      player.sendMessage("\u0414\u0435\u0440\u0438\u0441\u044C \u0442\u0440\u044F\u043F\u043A\u0430");
      return;
    }
    if (player.inObserverMode())
    {
      player.sendMessage("\u0421\u043C\u043E\u0442\u0440\u0438\u0442\u0435 \u0434\u0430\u043B\u044C\u0448\u0435 \u0432\u0430\u0448\u0443 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0443");
      return;
    }
    if ((player.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(player.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(player.getClan()).getSiege().getIsInProgress()))
    {
      player.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return;
    }
    if ((castleac != null) && (castleac.getSiege().getIsInProgress()))
    {
      player.sendMessage("\u0412\u044B \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return;
    }
    if (player.isFestivalParticipant())
    {
      player.sendMessage("\u0412\u044B \u043D\u0430 \u0444\u0435\u0441\u0442\u0438\u0432\u0430\u043B\u0435");
      return;
    }
    if ((player.isInParty()) && (player.getParty().isInDimensionalRift()))
    {
      player.sendMessage("\u0412\u044B \u0432 \u0440\u0438\u0444\u0442\u0435");
      return;
    }
    if (player.isInsideSilenceZone())
    {
      player.sendMessage("\u0417\u0434\u0435\u0441\u044C \u043D\u0435\u043B\u044C\u0437\u044F");
      return;
    }

    if (!TvTEvent.onEscapeUse(player.getName()))
    {
      player.sendPacket(new ActionFailed());
      player.sendMessage("\u0412\u044B \u043D\u0430 \u0442\u0432\u0442");
      return;
    }
    if ((player.isEventWait()) || (player.getChannel() > 1))
    {
      player.sendMessage("\u0417\u0434\u0435\u0441\u044C \u043D\u0435\u043B\u044C\u0437\u044F");
      return;
    }

    if (partner.isInJail())
    {
      player.sendMessage("" + Sex + " \u0432 \u0442\u044E\u0440\u044C\u043C\u0435");
      return;
    }
    if (partner.isDead())
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u0442\u043E\u043C \u0441\u0432\u0435\u0442\u0435");
      return;
    }
    if ((partner.isInOlympiadMode()) || (partner.getOlympiadSide() > 1))
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u0435");
      return;
    }
    if (partner.atEvent)
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u0435\u0432\u0435\u043D\u0442\u0435");
      return;
    }
    if (partner.isInDuel())
    {
      player.sendMessage("" + Sex + " \u0434\u0443\u044D\u043B\u0438\u0442\u0441\u044F");
      return;
    }
    if (partner.isFestivalParticipant())
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u0444\u0435\u0441\u0442\u0438\u0432\u0430\u043B\u0435 \u0442\u044C\u043C\u044B");
      return;
    }
    if ((partner.isInParty()) && (partner.getParty().isInDimensionalRift()))
    {
      player.sendMessage("" + Sex + " \u0432 \u0440\u0438\u0444\u0442\u0435");
      return;
    }
    if (partner.inObserverMode())
    {
      player.sendMessage("" + Sex + " \u043D\u0430\u0431\u043B\u044E\u0434\u0430\u0435\u0442 \u0437\u0430 \u043E\u043B\u0438\u043C\u043F\u0438\u0430\u0434\u043E\u0439");
      return;
    }
    if (partner.isInsideSilenceZone())
    {
      player.sendMessage("\u0417\u0434\u0435\u0441\u044C \u043D\u0435\u043B\u044C\u0437\u044F");
      return;
    }
    if ((partner.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()) != null) && (CastleManager.getInstance().getCastleByOwner(partner.getClan()).getSiege().getIsInProgress()))
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return;
    }
    if ((castlepn != null) && (castlepn.getSiege().getIsInProgress()))
    {
      player.sendMessage("" + Sex + " \u043D\u0430 \u043E\u0441\u0430\u0434\u0435");
      return;
    }
    if ((partner.isEventWait()) || (player.getChannel() > 1))
    {
      player.sendMessage("\u0417\u0434\u0435\u0441\u044C \u043D\u0435\u043B\u044C\u0437\u044F");
      return;
    }

    player.teleToLocation(partner.getX(), partner.getY(), partner.getZ());
  }

  public L2Skill.SkillType[] getSkillIds()
  {
    return SKILL_IDS;
  }
}