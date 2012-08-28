package l2p.gameserver.data.xml;

import l2p.gameserver.data.StringHolder;
import l2p.gameserver.data.htm.HtmCache;
import l2p.gameserver.data.xml.holder.BuyListHolder;
import l2p.gameserver.data.xml.holder.MultiSellHolder;
import l2p.gameserver.data.xml.holder.ProductHolder;
import l2p.gameserver.data.xml.holder.RecipeHolder;
import l2p.gameserver.data.xml.parser.AirshipDockParser;
import l2p.gameserver.data.xml.parser.ArmorSetsParser;
import l2p.gameserver.data.xml.parser.CubicParser;
import l2p.gameserver.data.xml.parser.DomainParser;
import l2p.gameserver.data.xml.parser.DoorParser;
import l2p.gameserver.data.xml.parser.EnchantItemParser;
import l2p.gameserver.data.xml.parser.EventParser;
import l2p.gameserver.data.xml.parser.FishDataParser;
import l2p.gameserver.data.xml.parser.HennaParser;
import l2p.gameserver.data.xml.parser.InstantZoneParser;
import l2p.gameserver.data.xml.parser.ItemParser;
import l2p.gameserver.data.xml.parser.NpcParser;
import l2p.gameserver.data.xml.parser.OptionDataParser;
import l2p.gameserver.data.xml.parser.PetitionGroupParser;
import l2p.gameserver.data.xml.parser.ResidenceParser;
import l2p.gameserver.data.xml.parser.RestartPointParser;
import l2p.gameserver.data.xml.parser.SkillAcquireParser;
import l2p.gameserver.data.xml.parser.SoulCrystalParser;
import l2p.gameserver.data.xml.parser.SpawnParser;
import l2p.gameserver.data.xml.parser.StaticObjectParser;
import l2p.gameserver.data.xml.parser.ZoneParser;
import l2p.gameserver.instancemanager.ReflectionManager;
import l2p.gameserver.tables.SkillTable;

public abstract class Parsers
{
  public static void parseAll()
  {
    HtmCache.getInstance().reload();
    StringHolder.getInstance().load();

    SkillTable.getInstance().load();
    OptionDataParser.getInstance().load();
    ItemParser.getInstance().load();

    NpcParser.getInstance().load();

    DomainParser.getInstance().load();
    RestartPointParser.getInstance().load();

    StaticObjectParser.getInstance().load();
    DoorParser.getInstance().load();
    ZoneParser.getInstance().load();
    SpawnParser.getInstance().load();
    InstantZoneParser.getInstance().load();

    ReflectionManager.getInstance();

    AirshipDockParser.getInstance().load();
    SkillAcquireParser.getInstance().load();

    ResidenceParser.getInstance().load();
    EventParser.getInstance().load();

    CubicParser.getInstance().load();

    BuyListHolder.getInstance();
    RecipeHolder.getInstance();
    MultiSellHolder.getInstance();
    ProductHolder.getInstance();

    HennaParser.getInstance().load();
    EnchantItemParser.getInstance().load();
    SoulCrystalParser.getInstance().load();
    ArmorSetsParser.getInstance().load();
    FishDataParser.getInstance().load();

    PetitionGroupParser.getInstance().load();
  }
}