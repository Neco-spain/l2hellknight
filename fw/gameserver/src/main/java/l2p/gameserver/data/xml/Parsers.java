package l2p.gameserver.data.xml;

import l2p.gameserver.data.StringHolder;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.data.xml.holder.ProductHolder;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.data.xml.parser.*;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.tables.SkillTable;

/**
 * @author VISTALL
 * @date 20:55/30.11.2010
 */
public abstract class Parsers {
    public static void parseAll() {
        HtmCache.getInstance().reload();
        StringHolder.getInstance().load();
        //
        SkillsParser.getInstance().load();
        SkillTable.getInstance().load(); // - SkillParser.getInstance();

        OptionDataParser.getInstance().load();
        ItemParser.getInstance().load();
        //
        NpcParser.getInstance().load();

        DomainParser.getInstance().load();
        RestartPointParser.getInstance().load();

        StaticObjectParser.getInstance().load();
        DoorParser.getInstance().load();
        ZoneParser.getInstance().load();
        SpawnParser.getInstance().load();
        InstantZoneParser.getInstance().load();

        ReflectionManager.getInstance();
        //
        AirshipDockParser.getInstance().load();
        SkillAcquireParser.getInstance().load();
        //
        ResidenceParser.getInstance().load();
        ShuttleTemplateParser.getInstance().load();
        EventParser.getInstance().load();
        // support(cubic & agathion)
        CubicParser.getInstance().load();
        //
        BuyListHolder.getInstance();
        RecipeHolder.getInstance();
        MultiSellHolder.getInstance();
        ProductHolder.getInstance();
        // AgathionParser.getInstance();
        // item support
        HennaParser.getInstance().load();
        EnchantItemParser.getInstance().load();
        SoulCrystalParser.getInstance().load();
        ArmorSetsParser.getInstance().load();
        FishDataParser.getInstance().load();

        // etc
        PetitionGroupParser.getInstance().load();
        JumpParser.getInstance().load();
    }
}
