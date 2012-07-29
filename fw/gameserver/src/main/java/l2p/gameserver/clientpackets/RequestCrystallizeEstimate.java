package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.items.CrystallizationItem;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.serverpackets.ExGetCrystalizingEstimation;
import l2p.gameserver.templates.item.ItemTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 30.05.12
 * Time: 23:17
 */
public class RequestCrystallizeEstimate extends L2GameClientPacket {

    private int _objectId;
    /*private long _count;*/

    @Override
    protected void readImpl() throws Exception
    {
        _objectId = readD();
        /*_count = */readQ();
    }

    @Override
    protected void runImpl() throws Exception
    {
        Player player = getClient().getActiveChar();
        if (player == null)
            return;

        if(player.isActionsDisabled())
        {
            player.sendActionFailed();
            return;
        }

        if(player.isInStoreMode())
        {
            player.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }

        if(player.isInTrade())
        {
            player.sendActionFailed();
            return;
        }

        ItemInstance item = player.getInventory().getItemByObjectId(_objectId);
        if(item == null)
        {
            player.sendActionFailed();
            return;
        }

        if(item.isHeroWeapon())
        {
            player.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
            return;
        }

        if(!item.canBeCrystallized(player))
        {
            player.sendActionFailed();
            return;
        }

        if(player.isInStoreMode())
        {
            player.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
            return;
        }

        if(player.isFishing())
        {
            player.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
            return;
        }

        if(player.isInTrade())
        {
            player.sendActionFailed();
            return;
        }

        int crystalAmount = item.getTemplate().getCrystalCount();
        int crystalId = item.getTemplate().getCrystalType().cry;

        //can player crystallize?
        int level = player.getSkillLevel(Skill.SKILL_CRYSTALLIZE);
        if(level < 1 || crystalId - ItemTemplate.CRYSTAL_D + 1 > level)
        {
            player.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
            player.sendActionFailed();
            return;
        }


        ExGetCrystalizingEstimation CE = new ExGetCrystalizingEstimation();
        CE.addCrystallizationItem(new CrystallizationItem(crystalId, crystalAmount, 100));
        //[TODO]: Cain Шанс получить заточки, части редкого СА и т д
        // http://l2central.info/wiki/%D0%9A%D1%80%D0%B8%D1%81%D1%82%D0%B0%D0%BB%D0%BB%D0%B8%D0%B7%D0%B0%D1%86%D0%B8%D1%8F
        player.sendPacket(CE);
    }
}
