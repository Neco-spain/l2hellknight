package other.NpcLocationInfo;

import l2.brick.gameserver.model.Location;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.quest.QuestState;
import gnu.trove.TIntObjectHashMap;

public class NpcLocationInfo extends Quest
{
    private static final String qn = "NpcLocationInfo";
    private static final TIntObjectHashMap<Location> Npcs = new TIntObjectHashMap<Location>();
    
    public NpcLocationInfo(int id, String name, String descr)
    {
        super(id, name, descr);

        for (int npcId : new int[] {30598, 30599, 30600, 30601, 30602, 32135})
        {
            addStartNpc(npcId);
            addTalkId(npcId);
        }
        
        // Talking Island
        Npcs.put(30006, new Location(-84108, 244604, -3729)); //Gatekeeper Roxxy
        Npcs.put(30039, new Location(-82236, 241573, -3728)); //Captain Gilbert
        Npcs.put(30040, new Location(-82515, 241221, -3728)); //Guard Leon
        Npcs.put(30041, new Location(-82319, 244709, -3727)); //Guard Arnold
        Npcs.put(30042, new Location(-82659, 244992, -3717)); //Guard Abellos
        Npcs.put(30043, new Location(-86114, 244682, -3727)); //Guard Johnstone
        Npcs.put(30044, new Location(-86328, 244448, -3724)); //Guard Chiperan
        Npcs.put(30045, new Location(-86322, 241215, -3727)); //Guard Kenyos
        Npcs.put(30046, new Location(-85964, 240947, -3727)); //Guard Hanks
        Npcs.put(30283, new Location(-85026, 242689, -3729)); //Blacksmith Altran
        Npcs.put(30003, new Location(-83789, 240799, -3717)); //Trader Silvia
        Npcs.put(30004, new Location(-84204, 240403, -3717)); //Trader Katerina
        Npcs.put(30001, new Location(-86385, 243267, -3717)); //Trader Lector
        Npcs.put(30002, new Location(-86733, 242918, -3717)); //Trader Jackson
        Npcs.put(30031, new Location(-84516, 245449, -3714)); //High Priest Biotin
        Npcs.put(30033, new Location(-84729, 245001, -3726)); //Magister Baulro
        Npcs.put(30035, new Location(-84965, 245222, -3726)); //Magister Harrys
        Npcs.put(30032, new Location(-84981, 244764, -3726)); //Priest Yohanes
        Npcs.put(30036, new Location(-85186, 245001, -3726)); //Priest Petron
        Npcs.put(30026, new Location(-83326, 242964, -3718)); //Grand Master Bitz
        Npcs.put(30027, new Location(-83020, 242553, -3718)); //Master Gwinter
        Npcs.put(30029, new Location(-83175, 243065, -3718)); //Master Minia
        Npcs.put(30028, new Location(-82809, 242751, -3718)); //Master Pintage
        Npcs.put(30054, new Location(-81895, 243917, -3721)); //Warehouse Keeper Rant
        Npcs.put(30055, new Location(-81840, 243534, -3721)); //Warehouse Keeper Rolfe
        Npcs.put(30005, new Location(-81512, 243424, -3720)); //Warehouse Keeper Wilford
        Npcs.put(30048, new Location(-84436, 242793, -3729)); //Darin
        Npcs.put(30312, new Location(-78939, 240305, -3443)); //Lighthouse Keeper Rockswell
        Npcs.put(30368, new Location(-85301, 244587, -3725)); //Lilith
        Npcs.put(30049, new Location(-83163, 243560, -3728)); //Bonnie
        Npcs.put(30047, new Location(-97131, 258946, -3622)); //Wharf Manager Firon
        Npcs.put(30497, new Location(-114685, 222291, -2925)); //Edmond
        Npcs.put(30050, new Location(-84057, 242832, -3729)); //Elias
        Npcs.put(30311, new Location(-100332, 238019, -3573)); //Sir Collin Windawood
        Npcs.put(30051, new Location(-82041, 242718, -3725)); //Cristel
        
        // Dark Elf Village
        Npcs.put(30134, new Location(9670, 15537, -4499)); //Gatekeeper Jasmine
        Npcs.put(30224, new Location(15120, 15656, -4301)); //Sentry Knight Rayla
        Npcs.put(30348, new Location(17306, 13592, -3649)); //Sentry Nelsya
        Npcs.put(30355, new Location(15272, 16310, -4302)); //Sentry Roselyn
        Npcs.put(30347, new Location(6449, 19619, -3619)); //Sentry Marion
        Npcs.put(30432, new Location(-15404, 71131, -3370)); //Sentry Irene
        Npcs.put(30356, new Location(7490, 17397, -4378)); //Sentry Altima
        Npcs.put(30349, new Location(17102, 13002, -3668)); //Sentry Jenna
        Npcs.put(30346, new Location(6532, 19903, -3618)); //Sentry Kayleen
        Npcs.put(30433, new Location(-15648, 71405, -3376)); //Sentry Kathaway
        Npcs.put(30357, new Location(7634, 18047, -4378)); //Sentry Kristin
        Npcs.put(30431, new Location(-1301, 75883, -3491)); //Sentry Eriel
        Npcs.put(30430, new Location(-1152, 76125, -3491)); //Sentry Trionell
        Npcs.put(30307, new Location(10584, 17574, -4557)); //Blacksmith Karrod
        Npcs.put(30138, new Location(12009, 15704, -4555)); //Trader Minaless
        Npcs.put(30137, new Location(11951, 15661, -4555)); //Trader Vollodos
        Npcs.put(30135, new Location(10761, 17970, -4558)); //Trader Iria
        Npcs.put(30136, new Location(10823, 18013, -4558)); //Trader Payne
        Npcs.put(30143, new Location(11283, 14226, -4167)); //Master Trudy
        Npcs.put(30360, new Location(10447, 14620, -4167)); //Master Harant
        Npcs.put(30145, new Location(11258, 14431, -4167)); //Master Vlasty
        Npcs.put(30135, new Location(10761, 17970, -4558)); //Magister Harne
        Npcs.put(30144, new Location(10344, 14445, -4167)); //Tetrarch Vellior
        Npcs.put(30358, new Location(10775, 14190, -4167)); //Tetrarch Thifiell
        Npcs.put(30359, new Location(11235, 14078, -4167)); //Tetrarch Kaitar
        Npcs.put(30141, new Location(11012, 14128, -4167)); //Tetrarch Talloth
        Npcs.put(30139, new Location(13380, 17430, -4544)); //Warehouse Keeper Dorankus 
        Npcs.put(30140, new Location(13464, 17751, -4544)); //Warehouse Keeper Erviante 
        Npcs.put(30350, new Location(13763, 17501, -4544)); //Warehouse Freightman Carlon
        Npcs.put(30421, new Location(-44225, 79721, -3577)); //Varika
        Npcs.put(30419, new Location(-44015, 79683, -3577)); //Arkenia
        Npcs.put(30130, new Location(25856, 10832, -3649)); //Abyssal Celebrant Undrias
        Npcs.put(30351, new Location(12328, 14947, -4499)); //Astaron
        Npcs.put(30353, new Location(13081, 18444, -4498)); //Jughead
        Npcs.put(30354, new Location(12311, 17470, -4499)); //Jewel
        
        // Elven Village
        Npcs.put(30146, new Location(46926, 51511, -2977)); //Gatekeeper Mirabel
        Npcs.put(30285, new Location(44995, 51706, -2803)); //Sentinel Gartrandell
        Npcs.put(30284, new Location(45727, 51721, -2803)); //Sentinel Knight Alberius
        Npcs.put(30221, new Location(42812, 51138, -2996)); //Sentinel Rayen
        Npcs.put(30217, new Location(45487, 46511, -2996)); //Sentinel Berros
        Npcs.put(30219, new Location(47401, 51764, -2996)); //Sentinel Veltress
        Npcs.put(30220, new Location(42971, 51372, -2996)); //Sentinel Starden
        Npcs.put(30218, new Location(47595, 51569, -2996)); //Sentinel Kendell
        Npcs.put(30216, new Location(45778, 46534, -2996)); //Sentinel Wheeler
        Npcs.put(30363, new Location(44476, 47153, -2984)); //Blacksmith Aios
        Npcs.put(30149, new Location(42700, 50057, -2984)); //Trader Creamees
        Npcs.put(30150, new Location(42766, 50037, -2984)); //Trader Herbiel
        Npcs.put(30148, new Location(44683, 46952, -2981)); //Trader Ariel
        Npcs.put(30147, new Location(44667, 46896, -2982)); //Trader Unoren
        Npcs.put(30155, new Location(45725, 52105, -2795)); //Master Ellenia
        Npcs.put(30156, new Location(44823, 52414, -2795)); //Master Cobendell
        Npcs.put(30157, new Location(45000, 52101, -2795)); //Magister Greenis
        Npcs.put(30158, new Location(45919, 52414, -2795)); //Magister Esrandell
        Npcs.put(30154, new Location(44692, 52261, -2795)); //Hierarch Asterios
        Npcs.put(30153, new Location(47780, 49568, -2983)); //Warehouse Keeper Markius
        Npcs.put(30152, new Location(47912, 50170, -2983)); //Warehouse Keeper Julia
        Npcs.put(30151, new Location(47868, 50167, -2983)); //Warehouse Freightman Chad
        Npcs.put(30423, new Location(28928, 74248, -3773)); //Northwind
        Npcs.put(30414, new Location(43673, 49683, -3046)); //Rosella
        Npcs.put(31853, new Location(50592, 54896, -3376)); //Treant Bremec
        Npcs.put(30223, new Location(42978, 49115, -2994)); //Arujien
        Npcs.put(30362, new Location(46475, 50495, -3058)); //Andellia
        Npcs.put(30222, new Location(45859, 50827, -3058)); //Alshupes
        Npcs.put(30371, new Location(51210, 82474, -3283)); //Thalia
        Npcs.put(31852, new Location(49262, 53607, -3216)); //Pixy Murika
        
        // Dwarven Village
        Npcs.put(30540, new Location(115072, -178176, -906)); //Gatekeeper Wirphy
        Npcs.put(30541, new Location(117847, -182339, -1537)); //Protector Paion
        Npcs.put(30542, new Location(116617, -184308, -1569)); //Defender Runant
        Npcs.put(30543, new Location(117826, -182576, -1537)); //Defender Ethan
        Npcs.put(30544, new Location(116378, -184308, -1571)); //Defender Cromwell
        Npcs.put(30545, new Location(115183, -176728, -791)); //Defender Proton
        Npcs.put(30546, new Location(114969, -176752, -790)); //Defender Dinkey
        Npcs.put(30547, new Location(117366, -178725, -1118)); //Defender Tardyon
        Npcs.put(30548, new Location(117378, -178914, -1120)); //Defender Nathan
        Npcs.put(30531, new Location(116226, -178529, -948)); //Iron Gate's Lockirin
        Npcs.put(30532, new Location(116190, -178441, -948)); //Golden Wheel's Spiron
        Npcs.put(30533, new Location(116016, -178615, -948)); //Silver Scale's Balanki
        Npcs.put(30534, new Location(116190, -178615, -948)); //Bronze Key's Keef
        Npcs.put(30535, new Location(116103, -178407, -948)); //Filaur of the Gray Pillar
        Npcs.put(30536, new Location(116103, -178653, -948)); //Black Anvil's Arin
        Npcs.put(30525, new Location(115468, -182446, -1434)); //Head Blacksmith Bronk
        Npcs.put(30526, new Location(115315, -182155, -1444)); //Blacksmith Brunon
        Npcs.put(30527, new Location(115271, -182692, -1445)); //Blacksmith Silvera
        Npcs.put(30518, new Location(115900, -177316, -915)); //Trader Garita
        Npcs.put(30519, new Location(116268, -177524, -914)); //Trader Mion
        Npcs.put(30516, new Location(115741, -181645, -1344)); //Trader Reep
        Npcs.put(30517, new Location(116192, -181072, -1344)); //Trader Shari
        Npcs.put(30520, new Location(115205, -180024, -870)); //Warehouse Chief Reed
        Npcs.put(30521, new Location(114716, -180018, -871)); //Warehouse Freightman Murdoc
        Npcs.put(30522, new Location(114832, -179520, -871)); //Warehouse Keeper Airy
        Npcs.put(30523, new Location(115717, -183488, -1483)); //Collector Gouph
        Npcs.put(30524, new Location(115618, -183265, -1483)); //Collector Pippi
        Npcs.put(30537, new Location(114348, -178537, -813)); //Daichir, Priest of the Eart
        Npcs.put(30650, new Location(114990, -177294, -854)); //Priest of the Earth Gerald
        Npcs.put(30538, new Location(114426, -178672, -812)); //Priest of the Earth Zimenf
        Npcs.put(30539, new Location(114409, -178415, -812)); //Priestess of the Earth Chichirin
        Npcs.put(30671, new Location(117061, -181867, -1413)); //Captain Croto
        Npcs.put(30651, new Location(116164, -184029, -1507)); //Wanderer Dorf
        Npcs.put(30550, new Location(115563, -182923, -1448)); //Gauri Twinklerock
        Npcs.put(30554, new Location(112656, -174864, -611)); //Miner Bolter
        Npcs.put(30553, new Location(116852, -183595, -1566)); //Maryse Redbonnet
        
        // Orc Village
        Npcs.put(30576, new Location(-45264, -112512, -235)); //Gatekeeper Tamil
        Npcs.put(30577, new Location(-46576, -117311, -242)); //Praetorian Rukain
        Npcs.put(30578, new Location(-47360, -113791, -237)); //Centurion Nakusin
        Npcs.put(30579, new Location(-47360, -113424, -235)); //Centurion Tamai
        Npcs.put(30580, new Location(-45744, -117165, -236)); //Centurion Parugon
        Npcs.put(30581, new Location(-46528, -109968, -250)); //Centurion Orinak
        Npcs.put(30582, new Location(-45808, -110055, -255)); //Centurion Tiku
        Npcs.put(30583, new Location(-45731, -113844, -237)); //Centurion Petukai
        Npcs.put(30584, new Location(-45728, -113360, -237)); //Centurion Vapook
        Npcs.put(30569, new Location(-45952, -114784, -199)); //Prefect Brukurse
        Npcs.put(30570, new Location(-45952, -114496, -199)); //Prefect Karukia
        Npcs.put(30571, new Location(-45863, -112621, -200)); //Seer Tanapi
        Npcs.put(30572, new Location(-45864, -112540, -199)); //Seer Livina
        Npcs.put(30564, new Location(-43264, -112532, -220)); //Blacksmith Sumari
        Npcs.put(30560, new Location(-43910, -115518, -194)); //Trader Uska
        Npcs.put(30561, new Location(-43950, -115457, -194)); //Trader Papuma
        Npcs.put(30558, new Location(-44416, -111486, -222)); //Trader Jakal
        Npcs.put(30559, new Location(-43926, -111794, -222)); //Trader Kunai
        Npcs.put(30562, new Location(-43109, -113770, -221)); //Warehouse Keeper Grookin
        Npcs.put(30563, new Location(-43114, -113404, -221)); //Warehouse Keeper Imantu
        Npcs.put(30565, new Location(-46768, -113610, -3)); //Flame Lord Kakai
        Npcs.put(30566, new Location(-46802, -114011, -112)); //Atuba Chief Varkees
        Npcs.put(30567, new Location(-46247, -113866, -21)); //Neruga Chief Tantus
        Npcs.put(30568, new Location(-46808, -113184, -112)); //Urutu Chief Hatos
        Npcs.put(30585, new Location(-45328, -114736, -237)); //Tataru Zu Hestui
        Npcs.put(30587, new Location(-44624, -111873, -238)); //Gantaki Zu Urutu
        
        // Kamael Village
        Npcs.put(32163, new Location(-116879, 46591, 360)); //Gatekeeper Ragara
        Npcs.put(32173, new Location(-119378, 49242, 8)); //Zerstorer Marcela
        Npcs.put(32174, new Location(-119774, 49245, 8)); //Marksman Maddy
        Npcs.put(32175, new Location(-119830, 51860, -792)); //Marksman Bixon
        Npcs.put(32176, new Location(-119362, 51862, -792)); //Marksman Ambra
        Npcs.put(32177, new Location(-112872, 46850, 48)); //Marksman Syzar
        Npcs.put(32178, new Location(-112352, 47392, 48)); //Guard Karba
        Npcs.put(32179, new Location(-110544, 49040, -1128)); //Marksman Putin
        Npcs.put(32180, new Location(-110536, 45162, -1128)); //Marksman Kato
        Npcs.put(32164, new Location(-115888, 43568, 524)); //Weapons Trader Erinu
        Npcs.put(32165, new Location(-115486, 43567, 525)); //Armor Trader Zacon
        Npcs.put(32168, new Location(-116920, 47792, 456)); //Magic Trader Janis
        Npcs.put(32166, new Location(-116749, 48077, 462)); //Accessory Trader Treavi
        Npcs.put(32167, new Location(-117153, 48075, 456)); //Consumption Goods Trader Neazel
        Npcs.put(32141, new Location(-119104, 43280, 544)); //Master Nerga
        Npcs.put(32142, new Location(-119104, 43152, 544)); //Master Tenor
        Npcs.put(32143, new Location(-117056, 43168, 544)); //Master Belkis
        Npcs.put(32144, new Location(-117060, 43296, 544)); //Master Sonya
        Npcs.put(32145, new Location(-118192, 42384, 824)); //Grand Master Maynard
        Npcs.put(32146, new Location(-117968, 42384, 824)); //Grand Master Valpor
        Npcs.put(32139, new Location(-118132, 42788, 712)); //Hierarch Casca
        Npcs.put(32140, new Location(-118028, 42778, 712)); //Hierarch Zenya
        Npcs.put(32138, new Location(-118080, 42835, 712)); //Hierarch Kekropus
        Npcs.put(32171, new Location(-114802, 44821, 524)); //Warehouse Chief Hoffa
        Npcs.put(32170, new Location(-114975, 44658, 512)); //Warehouse Keeper Benis
        Npcs.put(32172, new Location(-114801, 45031, 525)); //Warehouse Freightman Saylem
        Npcs.put(32153, new Location(-120432, 45296, 408)); //High Priest Prana
        Npcs.put(32154, new Location(-120706, 45079, 408)); //Grand Master Aldenia
        Npcs.put(32155, new Location(-120356, 45293, 408)); //Priest Nabot
        Npcs.put(32156, new Location(-120604, 44960, 408)); //Master Talbot
        Npcs.put(32150, new Location(-120294, 46013, 384)); //Hight Prefect Took
        Npcs.put(32151, new Location(-120157, 45813, 344)); //Prefect Harz
        Npcs.put(32152, new Location(-120158, 46221, 344)); //Seer Henri
        Npcs.put(32147, new Location(-120400, 46921, 400)); //Grand Master Libian
        Npcs.put(32148, new Location(-120407, 46755, 408)); //Master Sydnet
        Npcs.put(32149, new Location(-120442, 47125, 408)); //Magister Enea
        Npcs.put(32160, new Location(-118720, 48062, 464)); //Grand Magister Devon
        Npcs.put(32162, new Location(-118918, 47956, 464)); //Magister Martika
        Npcs.put(32161, new Location(-118527, 47955, 464)); //Master Black
        Npcs.put(32158, new Location(-117605, 48079, 456)); //Warehouse Chief Fisler
        Npcs.put(32157, new Location(-117824, 48080, 464)); //Head Blacksmith Moka
        Npcs.put(32159, new Location(-118030, 47930, 456)); //Blacksmith Kincaid
        Npcs.put(32169, new Location(-119237, 46587, 360));   //Spellbook Trader Mifren
    }

    public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
    {
        String htmltext = event;
        QuestState st = player.getQuestState(qn);
        if (st == null) return getNoQuestMsg(player);
        if (isDigit(event))
        {
            htmltext = "";
            int npcId = Integer.parseInt(event);
            if (Npcs.containsKey(npcId))
            {
                Location loc = Npcs.get(npcId);
                st.addRadar(loc.getX(), loc.getY(), loc.getY());
                htmltext = "MoveToLoc.htm";
            }
            st.exitQuest(true);
        }
        return htmltext;
    }

    public String onTalk(L2Npc npc, L2PcInstance player)
    {
        return npc.getNpcId()+".htm";
    }

    public static void main(String[] args)
    {
        new NpcLocationInfo(-1, qn, "other");
    }
}
