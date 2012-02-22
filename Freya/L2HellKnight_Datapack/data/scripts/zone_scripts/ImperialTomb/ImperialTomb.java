package zone_scripts.ImperialTomb;

import l2.hellknight.Config;
import l2.hellknight.gameserver.Announcements;
import l2.hellknight.gameserver.GeoData;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.GrandBossManager;
import l2.hellknight.gameserver.instancemanager.InstanceManager;
import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.L2Effect;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2Party;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.L2Attackable;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.L2Summon;
import l2.hellknight.gameserver.model.actor.instance.L2DoorInstance;
import l2.hellknight.gameserver.model.actor.instance.L2GrandBossInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.Instance;
import l2.hellknight.gameserver.instancemanager.InstanceManager.InstanceWorld;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.zone.L2ZoneType;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo;
import l2.hellknight.gameserver.network.serverpackets.ExShowScreenMessage;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillCanceld;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.SpecialCamera;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import javolution.util.FastMap;

public class ImperialTomb extends Quest
{
	private boolean debug = true;
	private static final String qn = "FinalEmperialTomb";

    private class FrintezzaWorld extends InstanceWorld
    {
        private int _Angle = 0;
        private int _Scarlet_x = 0;
        private int _Scarlet_y = 0;
        private int _Scarlet_z = 0;
        private int _Scarlet_h = 0;
        private boolean secondMorph, thirdMorph, onMorph, changeTarget;
        private int _KillDarkChoirPlayer = 0;
        private int _KillDarkChoirCaptain = 0;
        private int droppedItems = 0;
        private int lastSkillId = 0;
        private L2Skill Song;
        private final L2ZoneType _Zone = ZoneManager.getInstance().getZoneById(12017);
        private L2Npc _frintezzaDummy, _overheadDummy, _portraitDummy1, _portraitDummy3, _scarletDummy,
                portrait1, portrait2, portrait3, portrait4,
                frintezza, weakScarlet, strongScarlet, activeScarlet;
        private ArrayList<L2PcInstance> players = new ArrayList<L2PcInstance>();
        private ArrayList<L2Npc> _RoomMobs = new ArrayList<L2Npc>();
        private ArrayList<L2Npc> Demons1 = new ArrayList<L2Npc>();
        private ArrayList<L2Npc> Demons2 = new ArrayList<L2Npc>();
        private ArrayList<L2Npc> Demons3 = new ArrayList<L2Npc>();
        private ArrayList<L2Npc> Demons4 = new ArrayList<L2Npc>();
        
        public FrintezzaWorld()
		{
			InstanceManager.getInstance();
		}
    }
    
    private static class teleCoord {int x; int y; int z;}

	private static final int[][] _mobLoc = {
            {18329, -88725, -142175, -9168, 0},
            {18329, -88239, -142613, -9168, 0},
            {18329, -88131, -142389, -9168, 0},
            {18329, -88131, -142629, -9168, 0},
            {18329, -88077, -142453, -9168, 0},
            {18329, -88641, -142469, -9168, 0},
            {18329, -88683, -142427, -9168, 0},
            {18329, -88515, -142595, -9168, 0},
            {18329, -89145, -141881, -9168, 0},
            {18329, -88515, -142343, -9168, 0},
            {18329, -89061, -142049, -9168, 0},
            {18329, -88599, -142343, -9168, 0},
            {18329, -88683, -142175, -9168, 0},
            {18329, -88977, -141881, -9168, 0},
            {18329, -89176, -141535, -9168, 0},
            {18329, -89240, -141379, -9168, 0},
            {18329, -89208, -141379, -9168, 0},
            {18329, -89192, -141431, -9168, 0},
            {18329, -89208, -141639, -9168, 0},
            {18329, -89064, -141483, -9168, 0},
            {18329, -88583, -140336, -9168, 0},
            {18329, -88667, -140292, -9168, 0},
            {18329, -88919, -140688, -9168, 0},
            {18329, -88541, -140072, -9168, 0},
            {18329, -88625, -140248, -9168, 0},
            {18329, -89129, -140732, -9168, 0},
            {18329, -88625, -140424, -9168, 0},
            {18329, -88793, -140556, -9168, 0},
            {18329, -89045, -140644, -9168, 0},
            {18329, -88793, -140644, -9168, 0},
            {18329, -88243, -140112, -9168, 0},
            {18329, -88081, -139936, -9168, 0},
            {18329, -88135, -139952, -9168, 0},
            {18329, -89000, -141067, -9168, 0},
            {18329, -89064, -141171, -9168, 0},
            {18329, -89192, -140911, -9168, 0},
            {18329, -89208, -140963, -9168, 0},
            {18329, -86793, -141539, -9168, 0},
            {18329, -86617, -141701, -9168, 0},
            {18329, -86553, -141701, -9168, 0},
            {18329, -86793, -141593, -9168, 0},
            {18329, -86825, -141539, -9168, 0},
            {18329, -86617, -141593, -9168, 0},
            {18329, -87034, -142282, -9168, 0},
            {18329, -86824, -142118, -9168, 0},
            {18329, -87034, -142159, -9168, 0},
            {18329, -87202, -142241, -9168, 0},
            {18329, -87244, -142159, -9168, 0},
            {18329, -87160, -142405, -9168, 0},
            {18329, -86992, -142118, -9168, 0},
            {18329, -86992, -142159, -9168, 0},
            {18329, -87286, -142323, -9168, 0},
            {18329, -86793, -141107, -9168, 0},
            {18329, -86745, -141107, -9168, 0},
            {18329, -86809, -141107, -9168, 0},
            {18329, -86825, -141053, -9168, 0},
            {18329, -86816, -140594, -9168, 0},
            {18329, -86690, -140637, -9168, 0},
            {18329, -86900, -140680, -9168, 0},
            {18329, -87194, -140422, -9168, 0},
            {18329, -87110, -140164, -9168, 0},
            {18329, -87320, -140293, -9168, 0},
            {18329, -87278, -140379, -9168, 0},
            {18329, -86984, -140594, -9168, 0},
            {18329, -86984, -140422, -9168, 0},
            {18329, -87068, -140465, -9168, 0},
            {18329, -87973, -139952, -9168, 0},
            {18329, -87865, -140160, -9168, 0},
            {18329, -87811, -140224, -9168, 0},
            {18329, -87757, -140224, -9168, 0},
            {18329, -87757, -140176, -9168, 0},
            {18329, -87919, -140080, -9168, 0},
            {18329, -87757, -140032, -9168, 0},
            {18329, -87861, -142677, -9168, 0},
            {18329, -87915, -142661, -9168, 0},
            {18329, -87483, -142549, -9168, 0},
            {18329, -87753, -142629, -9168, 0},
            {18329, -88023, -142597, -9168, 0},
            {18329, -87537, -142661, -9168, 0},
            {18329, -87034, -142282, -9168, 0},
            {18330, -86809, -141215, -9168, 0},
            {18330, -86697, -141215, -9168, 0},
            {18330, -86729, -140999, -9168, 0},
            {18330, -86774, -140723, -9168, 0},
            {18330, -87236, -140422, -9168, 0},
            {18330, -88027, -140160, -9168, 0},
            {18330, -88027, -140064, -9168, 0},
            {18330, -87865, -140240, -9168, 0},
            {18330, -87591, -142613, -9168, 0},
            {18330, -87915, -142485, -9168, 0},
            {18330, -87969, -142501, -9168, 0},
            {18330, -87320, -140379, -9168, 0},
            {18330, -87160, -142159, -9168, 0},
            {18330, -87076, -142036, -9168, 0},
            {18330, -87076, -142241, -9168, 0},
            {18330, -89208, -141015, -9168, 0},
            {18330, -89256, -141223, -9168, 0},
            {18330, -88583, -140468, -9168, 0},
            {18330, -88835, -140688, -9168, 0},
            {18330, -89171, -140644, -9168, 0},
            {18330, -89256, -141743, -9168, 0},
            {18330, -88809, -142217, -9168, 0},
            {18330, -88809, -142175, -9168, 0},
            {18330, -88851, -141965, -9168, 0},
            {18331, -89240, -141639, -9168, 0},
            {18331, -88347, -142677, -9168, 0},
            {18331, -88239, -142677, -9168, 0},
            {18331, -88185, -142485, -9168, 0},
            {18331, -88185, -142517, -9168, 0},
            {18331, -88131, -142629, -9168, 0},
            {18331, -88473, -142427, -9168, 0},
            {18331, -88473, -142385, -9168, 0},
            {18331, -88557, -142385, -9168, 0},
            {18331, -89061, -141881, -9168, 0},
            {18331, -88809, -142343, -9168, 0},
            {18331, -88641, -142511, -9168, 0},
            {18331, -88935, -141839, -9168, 0},
            {18331, -88683, -142427, -9168, 0},
            {18331, -88557, -142301, -9168, 0},
            {18331, -89145, -141965, -9168, 0},
            {18331, -89144, -141587, -9168, 0},
            {18331, -89096, -141535, -9168, 0},
            {18331, -89224, -141431, -9168, 0},
            {18331, -89112, -141379, -9168, 0},
            {18331, -89176, -141483, -9168, 0},
            {18331, -89128, -141483, -9168, 0},
            {18331, -88541, -140248, -9168, 0},
            {18331, -88625, -140424, -9168, 0},
            {18331, -88625, -140248, -9168, 0},
            {18331, -88583, -140336, -9168, 0},
            {18331, -88667, -140424, -9168, 0},
            {18331, -88499, -140028, -9168, 0},
            {18331, -89003, -140600, -9168, 0},
            {18331, -88835, -140732, -9168, 0},
            {18331, -88877, -140644, -9168, 0},
            {18331, -88751, -140336, -9168, 0},
            {18331, -88081, -140096, -9168, 0},
            {18331, -88135, -139984, -9168, 0},
            {18331, -88081, -140032, -9168, 0},
            {18331, -88189, -139968, -9168, 0},
            {18331, -88297, -140016, -9168, 0},
            {18331, -88189, -140048, -9168, 0},
            {18331, -88984, -141223, -9168, 0},
            {18331, -89256, -141223, -9168, 0},
            {18331, -89224, -141119, -9168, 0},
            {18331, -86649, -141593, -9168, 0},
            {18331, -86665, -141701, -9168, 0},
            {18331, -86777, -141539, -9168, 0},
            {18331, -86809, -141593, -9168, 0},
            {18331, -86681, -141485, -9168, 0},
            {18331, -87370, -142323, -9168, 0},
            {18331, -86992, -141913, -9168, 0},
            {18331, -87118, -142077, -9168, 0},
            {18331, -87370, -142323, -9168, 0},
            {18331, -86908, -141831, -9168, 0},
            {18331, -87286, -142610, -9168, 0},
            {18331, -86740, -141995, -9168, 0},
            {18331, -86782, -141954, -9168, 0},
            {18331, -86908, -141831, -9168, 0},
            {18331, -87244, -142282, -9168, 0},
            {18331, -88023, -142389, -9168, 0},
            {18331, -87969, -142565, -9168, 0},
            {18331, -87483, -142597, -9168, 0},
            {18331, -88023, -142469, -9168, 0},
            {18331, -87915, -142581, -9168, 0},
            {18331, -87649, -140192, -9168, 0},
            {18331, -87703, -139968, -9168, 0},
            {18331, -88027, -139984, -9168, 0},
            {18331, -87865, -140096, -9168, 0},
            {18331, -86984, -140594, -9168, 0},
            {18331, -87320, -140207, -9168, 0},
            {18331, -86774, -140508, -9168, 0},
            {18331, -87152, -140508, -9168, 0},
            {18331, -87194, -140336, -9168, 0},
            {18331, -86816, -140551, -9168, 0},
            {18331, -86648, -140723, -9168, 0},
            {18331, -86900, -140594, -9168, 0},
            {18331, -87152, -140508, -9168, 0},
            {18331, -86900, -140723, -9168, 0},
            {18331, -86649, -141269, -9168, 0},
            {18331, -86713, -140999, -9168, 0},
            {18331, -86633, -141107, -9168, 0},
            {18331, -86601, -141269, -9168, 0},
            {18331, -86617, -140945, -9168, 0},
    };
    private static final int[][] _mobLoc2 = {
        {18339, -87952, -147004, -9184, 17329},
        {18339, -87794, -147193, -9184, 17466},
        {18339, -88021, -147358, -9184, 0},
        {18339, -87834, -147259, -9184, 55622}
    };
    private static final int[][] _mobLoc3 = {
            //{29124, -87878, -141310, -9168, 0},
            //{29124, -87868, -141350, -9168, 0},
            {18336, -88502, -146049, -9152, 53280},
            {18336, -88478, -146133, -9136, 53730},
            {18336, -88486, -146022, -9168, 49304},
            {18336, -88463, -145971, -9168, 1613},
            {18336, -88467, -145827, -9168, 59045},
            {18335, -88831, -146233, -9168, 0},
            {18338, -89252, -147210, -9168, 0},
            {18338, -89126, -146503, -9168, 0},
            {18338, -89273, -147008, -9168, 0},
            {18338, -89336, -147311, -9168, 0},
            {18338, -89021, -147311, -9168, 0},
            {18338, -89189, -146301, -9168, 0},
            {18338, -89294, -146806, -9168, 0},
            {18338, -89210, -146402, -9168, 0},
            {18337, -89225, -147522, -9168, 46511},
            {18337, -89100, -146892, -9168, 64239},
            {18337, -89345, -147175, -9168, 54095},
            {18337, -89142, -146423, -9168, 5920},
            {18337, -89207, -147045, -9168, 40341},
            {18337, -89311, -147221, -9168, 9413},
            {18337, -89337, -147097, -9168, 27293},
            {18337, -89126, -146907, -9168, 0},
            {18335, -88851, -146518, -9160, 0},
            {18335, -88911, -146404, -9168, 0},
            {18335, -88831, -146233, -9168, 0},
            {18335, -88811, -146119, -9168, 0},
            {18335, -88891, -146461, -9168, 0},
            {18335, -88891, -146347, -9168, 0},
            {18335, -88971, -146404, -9168, 0},
            {18335, -88931, -146347, -9168, 0},
            {18335, -88871, -146518, -9168, 0},
            {18336, -88486, -146114, -9136, 53844},
            {18336, -88471, -145699, -9168, 55229},
            {18336, -88474, -146041, -9160, 49453},
            {18336, -88472, -146058, -9152, 48818},
            {18334, -88755, -146963, -9136, 0},
            {18334, -88755, -146723, -9136, 0},
            {18334, -88755, -147187, -9136, 0},
            {18334, -88755, -147395, -9136, 0},
            {18335, -87158, -146011, -9168, 0},
            {18335, -86969, -146461, -9168, 0},
            {18335, -87158, -146311, -9160, 0},
            {18335, -87158, -146011, -9168, 0},
            {18335, -87011, -146411, -9168, 0},
            {18335, -87284, -145861, -9168, 0},
            {18335, -87116, -146261, -9168, 0},
            {18335, -87326, -146011, -9168, 0},
            {18335, -86969, -146211, -9168, 0},
            {18338, -86912, -147192, -9168, 0},
            {18335, -87074, -146111, -9168, 0},
            {18338, -86786, -146471, -9168, 0},
            {18338, -86828, -146780, -9168, 0},
            {18338, -86828, -147398, -9168, 0},
            {18338, -86870, -146471, -9168, 0},
            {18338, -86744, -146471, -9168, 0},
            {18337, -86846, -146945, -9168, 22779},
            {18337, -86830, -146835, -9168, 24239},
            {18337, -86814, -146833, -9168, 39002},
            {18337, -86749, -146327, -9168, 2707},
            {18337, -86921, -147059, -9168, 36804},
            {18337, -86617, -146724, -9168, 6680},
            {18337, -86832, -146307, -9168, 25692},
            {18336, -87395, -145923, -9168, 33000},
            {18336, -87395, -145779, -9168, 33000},
            {18336, -87395, -145859, -9168, 33000},
            {18336, -87427, -145955, -9168, 33000},
            {18336, -87395, -145715, -9168, 33000},
            {18336, -87427, -145747, -9168, 33000},
            {18336, -87427, -145819, -9168, 33000},
            {18336, -87427, -145883, -9168, 33000},
            {18336, -87427, -145683, -9168, 33000},
            {18334, -87171, -146739, -9136, 33000},
            {18334, -87155, -147219, -9136, 33000},
            {18334, -87155, -146963, -9136, 33000},
            {18334, -87155, -147411, -9136, 33000},
            {18335, -88685, -148239, -9168, 0},
            {18335, -88795, -147833, -9168, 0},
            {18335, -88861, -148181, -9168, 0},
            {18335, -88883, -147949, -9168, 0},
            {18335, -88773, -148181, -9168, 0},
            {18335, -88531, -148181, -9168, 0},
            {18335, -88773, -147717, -9152, 0},
            {18335, -88949, -148007, -9168, 0},
            {18335, -88575, -148181, -9168, 0},
            {18335, -88773, -148123, -9168, 0},
            {18338, -88979, -148018, -9168, 0},
            {18338, -89168, -147715, -9168, 0},
            {18337, -89338, -147449, -9168, 53319},
            {18337, -89076, -147524, -9168, 16383},
            {18336, -88503, -148239, -9168, 0},
            {18336, -88499, -148307, -9168, 0},
            {18336, -88499, -148435, -9168, 0},
            {18336, -88503, -148375, -9168, 0},
            {18336, -88471, -148199, -9168, 0},
            {18336, -88467, -148271, -9168, 0},
            {18336, -88467, -148403, -9168, 0},
            {18336, -88467, -148339, -9168, 0},
            {18336, -88467, -148467, -9168, 0},
            {18338, -86807, -147501, -9168, 0},
            {18338, -86744, -147707, -9168, 0},
            {18338, -86849, -147810, -9168, 0},
            {18338, -86870, -147501, -9168, 0},
            {18337, -86908, -147798, -9168, 3085},
            {18337, -86891, -147604, -9168, 0},
            {18337, -86591, -147633, -9168, 12766},
            {18335, -87257, -148113, -9168, 0},
            {18335, -87086, -147663, -9152, 0},
            {18335, -87295, -148063, -9152, 0},
            {18335, -87200, -148213, -9168, 0},
            {18335, -87162, -148063, -9168, 0},
            {18335, -87333, -148263, -9168, 0},
            {18335, -87181, -147863, -9152, 0},
            {18335, -87314, -148163, -9168, 0},
            {18335, -87105, -147863, -9168, 0},
            {18335, -87352, -148263, -9168, 0},
            {18336, -87395, -148227, -9168, 33000},
            {18336, -87395, -148371, -9168, 33000},
            {18336, -87395, -148303, -9168, 33000},
            {18336, -87395, -148435, -9168, 33000},
            {18336, -87427, -148467, -9168, 33000},
            {18336, -87427, -148331, -9168, 33000},
            {18336, -87427, -148395, -9168, 33000},
            {18336, -87427, -148263, -9168, 33000},
            {18336, -87427, -148191, -9168, 33000}
 };
	private static final int scarlet1Id = 29046;
	private static final int scarlet2Id = 29047;
	private static final int frintezzaId = 29045;
	private static final int guideId = 32011;
	private static final int cubeId = 29061;
    //private static final teleCoord enterLoc = new teleCoord();
    //-88172, -141076, -9170
	//private static final int EXIT_TIME = 5;
    private static final int[] allDoors = {17130042,17130043,17130045,17130046,17130051,17130052,17130053,17130054,17130055,17130056,17130057,17130058,17130061,17130062,17130063,17130064,17130065,17130066,17130067,17130068,17130069,17130070};
    private static long nextUpdate = 0;

    private static int checkworld(L2PcInstance player)
    {
        InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
        if (checkworld != null)
        {
            if (!(checkworld instanceof FrintezzaWorld))
                return 0;
            return 1;
        }
        return 2;
    }
    private synchronized void enterInstance(L2PcInstance player)
    {
        InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
        int inst = checkworld(player);
        if (inst == 0)
        {
            player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
            return;
        }
        else if (inst == 1)
        {
        	teleCoord tc = new teleCoord();
        	tc.x = -88172;
        	tc.y = -141076;
        	tc.z = -9170;
        	teleportplayer(player,tc,(FrintezzaWorld)world);
            return;
        }
        if (checkConditions(player))
        {
            L2Party party = player.getParty();
            world = new FrintezzaWorld();
            world.instanceId = InstanceManager.getInstance().createDynamicInstance(null);
            final Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
            final int time = 1200000; // 20 minutes (only for empty instance)
            //TODO: retail time for duration 
            //instance.setDuration(EXIT_TIME * 60000);
            instance.setEmptyDestroyTime(time);
            final int[] returnLoc = {player.getX(), player.getY(), player.getZ()};
            instance.setSpawnLoc(returnLoc);
            InstanceManager.getInstance().addWorld(world);
            instance.setName("Final Emperial Tomb");
            instance.setAllowSummon(false);
            for (int door : allDoors)
                instance.addDoor(door, false);
            _log.info("Instance Final Emperial Tomb created with id "+world.instanceId+" and created by player "+player.getName());
            startInstance((FrintezzaWorld)world);
            if (debug)
            {
                if (party == null)
                {
                	QuestState qs = player.getQuestState(qn);
                	qs.takeItems(8556, -1);
                	qs.takeItems(8192, -1);
                    ((FrintezzaWorld)world).players.add(player);
                    teleCoord tc = new teleCoord();
                	tc.x = -88172;
                	tc.y = -141076;
                	tc.z = -9170;
                    teleportplayer(player,tc,(FrintezzaWorld)world);
                }
                else
                    if (party.isInCommandChannel())
                        for (L2PcInstance partyMember : party.getCommandChannel().getMembers())
                        {
                        	QuestState qs = partyMember.getQuestState(qn);
                        	qs.takeItems(8556, -1);
                        	qs.takeItems(8192, -1);
                            ((FrintezzaWorld)world).players.add(partyMember);
                            teleCoord tc = new teleCoord();
                        	tc.x = -88172;
                        	tc.y = -141076;
                        	tc.z = -9170;
                            teleportplayer(partyMember,tc,(FrintezzaWorld)world);
                        }
                    else
                        for (L2PcInstance partyMember : party.getPartyMembers())
                        {
                        	QuestState qs = partyMember.getQuestState(qn);
                        	qs.takeItems(8556, -1);
                        	qs.takeItems(8192, -1);
                            ((FrintezzaWorld)world).players.add(partyMember);
                            teleCoord tc = new teleCoord();
                        	tc.x = -88172;
                        	tc.y = -141076;
                        	tc.z = -9170;
                            teleportplayer(partyMember,tc,(FrintezzaWorld)world);
                        }
            }
            else
            {
            	if (player.destroyItemByItemId("Frintezza", 8073, 1, null, true))
            	{
	                for (L2PcInstance partyMember : party.getCommandChannel().getMembers())
	                {
	                	//qs = partyMember.getQuestState(qn);
	                	//qs.takeItems(8556, -1);
	                	//qs.takeItems(8192, -1);
	                	if (partyMember.getInventory().getItemByItemId(8556) != null && partyMember.getInventory().getItemByItemId(8556).getCount() > 0)
	                		partyMember.destroyItemByItemId("Frintezza", 8556, partyMember.getInventory().getItemByItemId(8556).getCount(), null, true);
	                	if (partyMember.getInventory().getItemByItemId(8192) != null && partyMember.getInventory().getItemByItemId(8192).getCount() > 0)
	                		partyMember.destroyItemByItemId("Frintezza", 8192, partyMember.getInventory().getItemByItemId(8192).getCount(), null, true);
	                    ((FrintezzaWorld)world).players.add(partyMember);
	                    teleCoord tc = new teleCoord();
	                	tc.x = -88172;
	                	tc.y = -141076;
	                	tc.z = -9170;
	                    teleportplayer(partyMember,tc,(FrintezzaWorld)world);
	                }
            	}
            }
        }
    }

    private static void openDoor(int doorId,int instanceId)
    {
        for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
            if (door.getDoorId() == doorId)
                door.openMe();
    }

    private static void closeDoor(int doorId,int instanceId)
    {
        for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
            if (door.getDoorId() == doorId)
                door.closeMe();
    }

    private boolean checkConditions(L2PcInstance player)
    {
        if (debug)
            return true;
        L2Party party = player.getParty();
        if (party == null || !party.isInCommandChannel())
        {
            player.sendPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
            return false;
        }
        if (party.getCommandChannel().getChannelLeader() != player)
        {
            player.sendPacket(SystemMessage.getSystemMessage(2765));
            return false;
        }
        if (party.getCommandChannel().getMembers().size() < 1)
        {
            player.sendPacket(SystemMessage.getSystemMessage(2793).addNumber(18));
            return false;
        }
        if (party.getCommandChannel().getMembers().size() > 45)
        {
            player.sendPacket(SystemMessage.getSystemMessage(2764));
            return false;
        }
        if (player.getInventory().getItemByItemId(8073) == null || player.getInventory().getItemByItemId(8073).getCount() < 1)
        {
        	player.sendPacket(SystemMessage.getSystemMessage(2099).addPcName(player));
            return false;
        }
        for (L2PcInstance plr : party.getCommandChannel().getMembers())
        {
            if (plr.getLevel() < 74)
            {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(plr));
                return false;
            }
            if (!plr.isInsideRadius(player, 1000, true, true))
            {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(plr));
                return false;
            }
            FastMap<Integer, Long> instanceTimes = (FastMap<Integer, Long>) InstanceManager.getInstance().getAllInstanceTimes(plr.getObjectId());
            if (instanceTimes != null && instanceTimes.containsKey(136))
            {
                if (System.currentTimeMillis() < instanceTimes.get(136))
                {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(plr));
                    return false;
                }
            }
        }
        return true;
    }

    private void teleportplayer(L2PcInstance player, teleCoord loc, FrintezzaWorld world)
    {
        player.setInstanceId(world.instanceId);
        player.teleToLocation(loc.x, loc.y, loc.z);
        if (player.getPet() != null)
        	player.getPet().teleToLocation(loc.x, loc.y, loc.z);
        //player.teleToLocationWithPet(loc.x, loc.y, loc.z);
        if (!world.allowed.contains(player.getObjectId()))
            world.allowed.add(player.getObjectId());
    }

    private void startInstance(FrintezzaWorld world)
    {
        L2Npc mob = addSpawn(18328, -87904, -141296, -9168, 0, false, 0, true, world.instanceId);
        mob.setIsImmobilized(true);
        for (int[] spawn : _mobLoc)
        {
            mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.instanceId);
            mob.setIsNoRndWalk(true);
            world._RoomMobs.add(mob);
        }
    }

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
        int instanceId = 0;
        if (npc != null && npc.getInstanceId() > 0)
            instanceId = npc.getInstanceId();
        else if (player != null && player.getInstanceId() > 0)
            instanceId = player.getInstanceId();
        if (instanceId < 1)
            return null;
        InstanceWorld instWorld = InstanceManager.getInstance().getWorld(instanceId);
        if (instWorld instanceof FrintezzaWorld)
        {
            final FrintezzaWorld world = (FrintezzaWorld) instWorld;
            if (event.equalsIgnoreCase("waiting"))
            {
                startQuestTimer("close", 10000, null, player);
                ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(1, world), 12000);
            }
            else if (event.equalsIgnoreCase("room_spawn"))
            {
                for (int i = 17130051; i <= 17130058; i++)
                    openDoor(i, world.instanceId);
                
                for (L2Npc mob : world._RoomMobs)
                {
                	mob.setRunning();
    				mob.setTarget(player);
    				((L2Attackable) mob).addDamageHate(player, 0, 999);
    				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                }
            }
            else if (event.equalsIgnoreCase("room2_spawn"))
            {
            	world._RoomMobs.clear();
                for (int[] spawn : _mobLoc2)
                    addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.instanceId);

                for (int[] spawn : _mobLoc3)
                {
                    L2Npc mob = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, true, world.instanceId);
                    if (mob.getNpcId() == 18334)
                        mob.setIsImmobilized(true);
                    else
                        mob.setIsNoRndWalk(true);
                    
                    world._RoomMobs.add(mob);
                }
            }
            else if (event.equalsIgnoreCase("close"))
            {
                closeDoor(17130042, world.instanceId);
                closeDoor(17130043, world.instanceId);
                closeDoor(17130045, world.instanceId);
                closeDoor(17130046, world.instanceId);
            }
            else if (event.equalsIgnoreCase("songs_play"))
            {
                if (world.frintezza != null && !world.frintezza.isDead() && !world.onMorph)
                {
                    world.Song = getRandomSong();
                    world.frintezza.doCast(world.Song);
                    world._Zone.broadcastPacket(new MagicSkillUse(world.frintezza, world.frintezza, world.Song.getId(), world.Song.getLevel(), world.Song.getHitTime(), 0));
                    startQuestTimer("songs_effect", 5000, world.frintezza, null);
                    int StringId = 0;
                    switch(world.Song.getLevel())
                    {
                        case 1:
                            StringId = 1000522;
                            break;
                        case 2:
                            StringId = 1000524;
                            break;
                        case 3:
                            StringId = 1000526;
                            break;
                        case 4:
                            StringId = 1000523;
                            break;
                        case 5:
                            StringId = 1000525;
                            break;
                    }
                    if (world.Song.getId() == 5006)
                        StringId = 1000527;
                    if (StringId != 0)
                    	//TODO: Fix Error
                    	//world._Zone.broadcastPacket(new Align(StringId, 3000, Align.ScreenMessageAlign.TOP_CENTER, true, false, -1, true));
                    startQuestTimer("songs_play", world.Song.getHitTime(), world.frintezza, null);
                }
            }
            else if (event.equalsIgnoreCase("songs_effect") && world.frintezza != null && !world.frintezza.isDead() && world.activeScarlet != null && !world.activeScarlet.isDead())
            {
                if (world.Song != null && world.Song.getId() == 5008 && world.Song.getLevel() == 1)
                    world.Song.getEffects(world.frintezza, world.activeScarlet);
            }
            else if (event.equalsIgnoreCase("spawn_minions"))
            {
                if (npc != null && !npc.isDead() && world.frintezza != null && !world.frintezza.isDead())
                {
                    L2Npc mob;
                    if (world.portrait1 != null && !world.portrait1.isDead() && world.Demons1.size() < 4)
                    {
                        mob = addSpawn(29050, -89378, -153968, -9168, 3368, false, 0, true, world.instanceId);
                        ((L2Attackable) mob).setIsRaidMinion(true);
                        world.Demons1.add(mob);
                    }
                    if (world.portrait2 != null && !world.portrait2.isDead() && world.Demons2.size() < 4)
                    {
                        mob = addSpawn(29050, -86261, -152492, -9168, 37656, false, 0, true, world.instanceId);
                        ((L2Attackable) mob).setIsRaidMinion(true);
                        world.Demons2.add(mob);
                    }
                    if (world.portrait3 != null && !world.portrait3.isDead() && world.Demons3.size() < 4)
                    {
                        mob = addSpawn(29051, -89311, -152491, -9168, 60384, false, 0, true, world.instanceId);
                        ((L2Attackable) mob).setIsRaidMinion(true);
                        world.Demons3.add(mob);
                    }
                    if (world.portrait4 != null && !world.portrait4.isDead() && world.Demons4.size() < 4)
                    {
                        mob = addSpawn(29051, -86217, -153956, -9168, 29456, false, 0, true, world.instanceId);
                        ((L2Attackable) mob).setIsRaidMinion(true);
                        world.Demons4.add(mob);
                    }
                }
            }
            else if (event.equalsIgnoreCase("callSkillAI") && npc != null && !npc.isCastingNow())
			    callSkillAI(npc, world);
        }
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
        if (npc.getInstanceId() > 0)
        {
            InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
            if (world instanceof FrintezzaWorld)
            {
                /*if (npc.getNpcId() == cubeId)
                {
                    if (world.status > 255)
                    {
                        int x = 150037 + Rnd.get(500);
                        int y = -57720 + Rnd.get(500);
                        player.setInstanceId(0);
                        player.teleToLocation(x, y, -2976);
                        return null;
                    }
                    else if (world.status == 255)
                    {
                        int x = -87700 + Rnd.get(500);
                        int y = -153300 + Rnd.get(500);
                        player.teleToLocation(x, y, -9176);
                        return null;
                    }
                }*/
            }
        }
        else
            enterInstance(player);
		return null;
	}

    private class Spawn implements Runnable
	{
		private int _taskId = 0;
        private FrintezzaWorld world = null;

		public Spawn(int taskId, FrintezzaWorld _world)
		{
			_taskId = taskId;
            world = _world;
		}

		public void run()
		{
			try
            {
                switch(_taskId)
                {
                    case 1:
			            world._overheadDummy = addSpawn(29052, -87793, -153301, -9188, 16384, false, 0, true, world.instanceId);
			            world._overheadDummy.setIsInvul(true);
			            world._overheadDummy.setIsImmobilized(true);
			            world._overheadDummy.setCollisionHeight(600);
			            world._Zone.broadcastPacket(new NpcInfo(world._overheadDummy, null));
			            world._frintezzaDummy = addSpawn(29052, -87780, -155087, -9080, 16048, false, 0, true, world.instanceId);
			            world._frintezzaDummy.setIsInvul(true);
			            world._frintezzaDummy.setIsImmobilized(true);
			            world._portraitDummy1 = addSpawn(29052, -89575, -153171, -9161, 16048, false, 0, true, world.instanceId);
			            world._portraitDummy1.setIsImmobilized(true);
			            world._portraitDummy1.setIsInvul(true);
			            world._portraitDummy3 = addSpawn(29052, -86013, -153171, -9161, 16048, false, 0, true, world.instanceId);
			            world._portraitDummy3.setIsImmobilized(true);
			            world._portraitDummy3.setIsInvul(true);
			            world._scarletDummy = addSpawn(29053, -87793, -153301, -9188, 16384, false, 0, true, world.instanceId);
			            world._scarletDummy.setIsInvul(true);
			            world._scarletDummy.setIsImmobilized(true);
			            stopPc(world);
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(2, world), 3000);
                        break;
                    case 2:
		                world._Zone.broadcastPacket(new SpecialCamera(world._overheadDummy.getObjectId(), 0, 75, -89, 0, 100, 0, 0, 1, 0));
			            world._Zone.broadcastPacket(new SpecialCamera(world._overheadDummy.getObjectId(), 300, 90, -10, 3500, 7000, 0, 0, 1, 0));
			            world.frintezza = addSpawn(frintezzaId, -87780, -155086, -9080, 16048, false, 0, true, world.instanceId);
			            GrandBossManager.getInstance().addBoss((L2GrandBossInstance)world.frintezza);
			            world.frintezza.setIsImmobilized(true);
			            world.frintezza.setIsInvul(true);
                        world.frintezza.setRHandId(500); // fake weapon
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(3, world), 3500);
                        break;
                    case 3:
		                world._Zone.broadcastPacket(new SpecialCamera(world._frintezzaDummy.getObjectId(), 1800, 90, 8, 3500, 7000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(4, world), 900);
                        break;
                    case 4:
		                world._Zone.broadcastPacket(new SpecialCamera(world._frintezzaDummy.getObjectId(), 140, 90, 10, 2500, 4500, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(5, world), 4000);
                        break;
                    case 5:
		                world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 40, 75, -10, 0, 1000, 0, 0, 1, 0));
                        world._frintezzaDummy.deleteMe();
			            world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 40, 75, -10, 0, 12000, 0, 0, 1, 0));
			            ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(6, world), 1350);
                        break;
		            case 6:
			            world._Zone.broadcastPacket(new SocialAction(world.frintezza, 2));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(7, world), 8000);
                        break;
                    case 7:
                        for (L2Character pc : world._Zone.getCharactersInsideArray())
                            if (pc instanceof L2PcInstance)
                            {
                                if (pc.getX() < 174232)
                                {
                                    pc.sendPacket(new SpecialCamera(world._portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000, 0, 0, 1, 0));
                                    pc.sendPacket(new SpecialCamera(world._portraitDummy1.getObjectId(), 1000, 118, 0, 0, 10000, 0, 0, 1, 0));
                                }
                                else
                                {
                                    pc.sendPacket(new SpecialCamera(world._portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000, 0, 0, 1, 0));
                                    pc.sendPacket(new SpecialCamera(world._portraitDummy3.getObjectId(), 1000, 62, 0, 0, 10000, 0, 0, 1, 0));
                                }
                            }
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(8, world), 400);
                        break;
                    case 8:
                        world.portrait1 = addSpawn(29048, -89381, -153981, -9168, 3368, false, 0, true, world.instanceId); // leva dal od dveri
                        world.portrait1.setIsOverloaded(true);
                        world.portrait4 = addSpawn(29049, -86189, -153968, -9168, 29456, false, 0, true, world.instanceId); // pravo dal od dveri
                        world.portrait4.setIsOverloaded(true);
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(21, world), 1000);
                        break;
                    case 9:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 240, 90, 0, 0, 1000, 0, 0, 1, 0));
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 240, 90, 25, 5500, 10000, 0, 0, 1, 0));
                        world._Zone.broadcastPacket(new SocialAction(world.frintezza, 3));
                        world._portraitDummy1.deleteMe();
                        world._portraitDummy3.deleteMe();
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(10, world), 4500);
                        break;
                    case 10:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 100, 195, 35, 0, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(11, world), 700);
                        break;
                    case 11:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 100, 195, 35, 0, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(12, world), 1300);
                        break;
                    case 12:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 120, 180, 45, 1500, 10000, 0, 0, 1, 0));
			            world._Zone.broadcastPacket(new MagicSkillUse(world.frintezza, world.frintezza, 5006, 1, 34000, 0));
			            world._Zone.broadcastPacket(new ExShowScreenMessage(1,0,5,0,1,0,0,true,3000,1,"Rondo of Solitude!"));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(13, world), 1500);
                        break;
                    case 13:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 520, 135, 45, 8000, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(14, world), 7500);
                        break;
                    case 14:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 1500, 110, 25, 8000, 13000, 0, 0, 1, 0));
			            world.weakScarlet = addSpawn(29046, -87789, -153295, -9176, 16384, false, 0, true, world.instanceId);
			            world.weakScarlet.setIsInvul(true);
			            world.weakScarlet.setIsImmobilized(true);
			            world.weakScarlet.disableAllSkills();
                        world.weakScarlet.setShowSummonAnimation(true);
                        //world.weakScarlet.getSpawn().setOnKillDelay(100);
                        world.activeScarlet = world.weakScarlet;
                        world.weakScarlet.decayMe();
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(15, world), 7500);
                        break;
                    case 15:
                        world._Zone.broadcastPacket(new SpecialCamera(world._overheadDummy.getObjectId(), 700, 160, -20, 0, 10000, 0, 0, 1, 0));
                        world._Zone.broadcastPacket(new SpecialCamera(world._overheadDummy.getObjectId(), 700, 160, -20, 0, 10000, 0, 0, 1, 0));
			            world._Zone.broadcastPacket(new MagicSkillUse(world._scarletDummy, world._overheadDummy, 5004, 1, 5800, 0));
                        world._scarletDummy.doCast(SkillTable.getInstance().getInfo(5004, 1));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(16, world), 3300);
                        break;
                    case 16:
                        world.weakScarlet.spawnMe();
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(17, world), 1800);
                        break;
                    case 17:
                        world._Zone.broadcastPacket(new SpecialCamera(world.weakScarlet.getObjectId(), 800, 160, 5, 1000, 10000, 0, 0, 1, 0));
                        for (L2PcInstance cha : world._scarletDummy.getKnownList().getKnownPlayersInRadius(225))
                            SkillTable.getInstance().getInfo(5004, 1).getEffects(world.weakScarlet, cha);
			            ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(18, world), 2100);
                        break;
                    case 18:
		                world._Zone.broadcastPacket(new SpecialCamera(world.weakScarlet.getObjectId(), 300, 60, 8, 0, 10000, 0, 0, 1, 0));
			            ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(19, world), 2000);
                        break;
                    case 19:
                        world._Zone.broadcastPacket(new SpecialCamera(world.weakScarlet.getObjectId(), 500, 90, 10, 3000, 3000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(20, world), 3000);
                        break;
                    case 20:
                        world._overheadDummy.deleteMe();
                        world._scarletDummy.deleteMe();
                        startNpc(world.weakScarlet);
                        startPc(world);
                        addSpawn(cubeId, -87904, -141296, -9168, 0, false, 0, true, world.instanceId);
                        startQuestTimer("songs_play", 200, world.frintezza, null);
                        startQuestTimer("callSkillAI", 1000, world.activeScarlet, null, true);
                        startQuestTimer("spawn_minions", 20000, world.frintezza, null, true);
                        break;
                    case 21:
                        world.portrait2 = addSpawn(29048, -86234, -152467, -9168, 37656, false, 0, true, world.instanceId); // V pravo u dveri
                        world.portrait2.setIsOverloaded(true);
                        world.portrait3 = addSpawn(29049, -89342,-152479, -9168, 60384, false, 0, true, world.instanceId); // leva u dvere
                        world.portrait3.setIsOverloaded(true);
                        ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(9, world), 1500);
                        break;
                }
            }
            catch(Exception e)
            { }
        }
    }
    private class Morph implements Runnable
    {
        private int _taskId;
        private FrintezzaWorld world;

        public Morph(int taskId, FrintezzaWorld _world)
        {
            _taskId = taskId;
            world = _world;
        }

        public void run()
        {
            try
            {
                switch(_taskId)
                {
                    case 1:
                        world._Zone.broadcastPacket(new SocialAction(world.frintezza, 4));
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 250, 120, 15, 0, 1000));
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 250, 120, 15, 0, 10000, 0, 0, 1, 0));
                        world._Scarlet_x = world.weakScarlet.getX();
                        world._Scarlet_y = world.weakScarlet.getY();
                        world._Scarlet_z = world.weakScarlet.getZ();
                        world._Scarlet_h = world.weakScarlet.getHeading();
                        world.weakScarlet.setIsImmobilized(true);
                        world.weakScarlet.disableAllSkills();
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(2, world), 7000);
                        break;
                    case 2:
                        world._Zone.broadcastPacket(new MagicSkillUse(world.frintezza, world.frintezza, 5007, 2, 32000, 0));
                        world._Zone.broadcastPacket(new ExShowScreenMessage(1,0,5,0,1,0,0,true,3000,1,"Frenetic Toccata!"));
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 900, 70, 15, 3000, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(3, world), 3000);
                        break;
                    case 3:
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 2500, 90, 12, 6000, 10000, 0, 0, 1, 0));
			            ThreadPoolManager.getInstance().scheduleGeneral(new Morph(4, world), 3000);
                        break;
                    case 4:
                        world._Zone.broadcastPacket(new SpecialCamera(world.weakScarlet.getObjectId(), 250, world._Angle, 12, 0, 10000, 0, 0, 1, 0));
			            ThreadPoolManager.getInstance().scheduleGeneral(new Morph(5, world), 500);
                        break;
                    case 5:
                        world.weakScarlet.doDie(world.weakScarlet);
                        world._Zone.broadcastPacket(new SpecialCamera(world.weakScarlet.getObjectId(), 450, world._Angle, 14, 8000, 8000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(6, world), 6250);
                        break;
                    case 6:
                        world.weakScarlet.deleteMe();
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(7, world), 950);
                        break;
                    case 7:
                        world.strongScarlet = addSpawn(scarlet2Id, world._Scarlet_x, world._Scarlet_y, world._Scarlet_z, world._Scarlet_h, false, 0, true, world.instanceId);
                        world.strongScarlet.setIsInvul(true);
                        world.strongScarlet.setIsImmobilized(true);
                        world.strongScarlet.disableAllSkills();
                        //world.strongScarlet.getSpawn().setOnKillDelay(100);
                        world.activeScarlet = world.strongScarlet;
                        world._Zone.broadcastPacket(new SpecialCamera(world.strongScarlet.getObjectId(), 450, world._Angle, 12, 500, 14000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(8, world), 8100);
                        break;
                    case 8:
                        world._Zone.broadcastPacket(new SocialAction(world.strongScarlet, 2));
                        SkillTable.getInstance().getInfo(5017, 1).getEffects(world.strongScarlet, world.strongScarlet);
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(9, world), 5900);
                        break;
                    case 9:
			            world.onMorph = false;
                        startPc(world);
                        startNpc(world.activeScarlet);
                        startQuestTimer("callSkillAI", 1000, world.activeScarlet, null, true);
                        startQuestTimer("songs_play", 200, world.frintezza, null);
                        break;
                    case 10:
                        world._Zone.broadcastPacket(new SpecialCamera(world.strongScarlet.getObjectId(), 300, world._Angle - 180, 5, 0, 7000, 0, 0, 1, 0));
                        world._Zone.broadcastPacket(new SpecialCamera(world.strongScarlet.getObjectId(), 200, world._Angle, 85, 4000, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(11, world), 7500);
                        break;
                    case 11:
                        world.frintezza.doDie(world.frintezza);
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 100, 120, 5, 0, 7000, 0, 0, 1, 0));
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 100, 90, 5, 5000, 15000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(12, world), 7000);
                        break;
                    case 12:
                        world.activeScarlet.deleteMe();
                        //addSpawn(cubeId, -87789, -153295, -9176, 16384, false, 900000, true, world.instanceId);
                        world._Zone.broadcastPacket(new SpecialCamera(world.frintezza.getObjectId(), 2500, 90, 15, 10000, 10000, 0, 0, 1, 0));
                        ThreadPoolManager.getInstance().scheduleGeneral(new Morph(13, world), 10000);
                        break;
                    case 13:
                        world.frintezza.deleteMe();
                        startPc(world);
                        Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
                        inst.setDuration(900000);
                        inst.setEmptyDestroyTime(0);
                        Announcements.getInstance().announceToInstance(SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED).addString(InstanceManager.getInstance().getInstanceIdName(136)), world.instanceId);
                        break;
                    case 15:
                        world.activeScarlet.setIsImmobilized(false);
                        world.onMorph = false;
                        startQuestTimer("callSkillAI", 1000, world.activeScarlet, null, true);
                        break;
                }
            }
            catch (Exception e)
            { }
        }
    }

	@Override
    public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
        if (npc != null && npc.getInstanceId() > 0 && InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FrintezzaWorld)
        {
            final FrintezzaWorld world = (FrintezzaWorld)InstanceManager.getInstance().getWorld(npc.getInstanceId());
            if (npc.getNpcId() == frintezzaId)
                npc.setCurrentHpMp(npc.getMaxHp(), 0);
            else if (npc.getNpcId() == scarlet1Id)
            {
                if (!world.secondMorph && !world.thirdMorph && !world.onMorph && npc.getCurrentHp() < npc.getMaxHp() * 0.75)
                {
                    world.onMorph = true;
                    if (getQuestTimer("callSkillAI", npc, null) != null)
			            getQuestTimer("callSkillAI", npc, null).cancel();
                    world.secondMorph = true;
                    npc.abortAttack();
                    npc.abortCast();
                    npc.setIsImmobilized(true);
                    world.weakScarlet.doCast(SkillTable.getInstance().getInfo(5017,1));
                    world.weakScarlet.setRHandId(7903);
                    ThreadPoolManager.getInstance().scheduleGeneral(new Morph(15, world), 10000);
                }
                else if (world.secondMorph && !world.thirdMorph && !world.onMorph && npc.getCurrentHp() < npc.getMaxHp()*0.1)
                {
                    world.onMorph = true;
                    world.thirdMorph = true;
                    npc.setIsInvul(true);
                    if (getQuestTimer("callSkillAI", npc, null) != null)
			            getQuestTimer("callSkillAI", npc, null).cancel();
                    attackStop(world);
                    stopPc(world);
                    stopNpc(npc, world);
                    ThreadPoolManager.getInstance().scheduleGeneral(new Morph(1, world), 1000);
                }
            }
        }
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
        if (npc != null && npc.getInstanceId() > 0 && InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FrintezzaWorld)
        {
            final FrintezzaWorld world = (FrintezzaWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId());
            switch(npc.getNpcId())
            {
                case scarlet2Id:
                    stopPc(world);
                    stopNpc(npc, world);
                    ThreadPoolManager.getInstance().scheduleGeneral(new Morph(10, world), 100);
                    world._Zone.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
                    if (getQuestTimer("spawn_minions", world.frintezza, null) != null)
                        getQuestTimer("spawn_minions", world.frintezza, null).cancel();
                    world.status = 300;
                    if (nextUpdate < System.currentTimeMillis())
                        nextUpdate = generateUpdateTime();
                    for (L2PcInstance plr : world.players)
                        InstanceManager.getInstance().setInstanceTime(plr.getObjectId(), 136, nextUpdate);
                    break;
                case 18328:
                    startQuestTimer("room_spawn", 100, npc, player);
                    break;
                case 18329:
                case 18330:
                case 18331:
                    if (world.droppedItems < 4 && Rnd.get(100) < 1.5)
                    {
                        ((L2Attackable)npc).dropItem(player, new L2Attackable.RewardItem(8556, 1));
                        world.droppedItems++;
                    }
                    synchronized(world._RoomMobs)
                    {
                        if (world._RoomMobs.contains(npc))
                        {
                            world._RoomMobs.remove(npc);
                            if (world._RoomMobs.isEmpty())
                            {
                                startQuestTimer("room2_spawn", 100, npc, player);
                                openDoor(17130042, world.instanceId);
                                openDoor(17130043, world.instanceId);
                                openDoor(17130045, world.instanceId);
                            }
                        }
                    }
                    break;
                case 18339:
                    world._KillDarkChoirPlayer++;
                    if (world._KillDarkChoirPlayer == 4)
                    {
                        closeDoor(17130042, world.instanceId);
                        closeDoor(17130043, world.instanceId);
                        closeDoor(17130045, world.instanceId);
                        for (int i = 17130061; i <= 17130070; i++)
                            openDoor(i, world.instanceId);
                        for (L2Npc mob : world._RoomMobs)
                        {
                        	mob.setRunning();
            				mob.setTarget(player);
            				((L2Attackable) mob).addDamageHate(player, 0, 999);
            				mob.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
                        }
                        world._RoomMobs.clear();
                    }
                    break;
                case 18334:
                    world._KillDarkChoirCaptain++;
                    if (world._KillDarkChoirCaptain >= 8)
                    {
                        openDoor(17130045, world.instanceId);
                        openDoor(17130046, world.instanceId);
                    }
                    break;
                case 29051:
                case 29050:
                    if (world.Demons1.contains(npc) && !world.portrait1.isDead())
                        world.Demons1.remove(npc);
                    else if (world.Demons2.contains(npc) && !world.portrait2.isDead())
                        world.Demons2.remove(npc);
                    else if (world.Demons3.contains(npc) && !world.portrait3.isDead())
                        world.Demons3.remove(npc);
                    else if (world.Demons4.contains(npc) && !world.portrait4.isDead())
                        world.Demons4.remove(npc);
                    break;
            }
        }
		return super.onKill(npc, player, isPet);
	}

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        if (character instanceof L2PcInstance && character.getInstanceId() > 0)
        {
            InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld((L2PcInstance) character);
            if (tmpworld instanceof FrintezzaWorld)
            {
            	final FrintezzaWorld world = (FrintezzaWorld) tmpworld;
	            if (world.status < 255)
	            {
	                startQuestTimer("waiting", 600000, null, character.getActingPlayer()); //180000
	                world.status = 255;
	            }
            }
        }
        return null;
    }

    @Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
        if (npc != null && npc.getInstanceId() > 0 && InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FrintezzaWorld)
        {
            if (skill.getId() == 2276 && (npc.getNpcId() == 29048 || npc.getNpcId() == 29049) && Arrays.asList(targets).contains(npc))
                npc.doDie(caster);
        }
        return null;
    }

    @Override
    public final String onFirstTalk(L2Npc npc, L2PcInstance player)
    {
        if (npc.getInstanceId() > 0)
        {
            InstanceWorld world = InstanceManager.getInstance().getWorld(npc.getInstanceId());
            if (world instanceof FrintezzaWorld)
            {
                if (world.status > 255)
                    return "29061.htm";
                else if (world.status == 255)
                    return "29061-1.htm";
            }
        }
        return null;
    }

    @Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if (npc.getInstanceId() > 0 && !npc.isCastingNow() && InstanceManager.getInstance().getWorld(npc.getInstanceId()) instanceof FrintezzaWorld)
			callSkillAI(npc, (FrintezzaWorld) InstanceManager.getInstance().getWorld(npc.getInstanceId()));
		return super.onSpellFinished(npc, player, skill);
	}

    @Override
    public final String onSpawn(L2Npc npc)
    {
        if (npc.getInstanceId() > 0)
            npc.disableCoreAI(true);
        return super.onSpawn(npc);
    }

    private L2Skill getRandomSong()
    {
        int rnd = Rnd.get(1000);
        if (rnd < 150)
            return SkillTable.getInstance().getInfo(5007, 2);
        else if (rnd < 250)
            return SkillTable.getInstance().getInfo(5007, 3);
        else if (rnd < 350)
            return SkillTable.getInstance().getInfo(5007, 4);
        else if (rnd < 500)
            return SkillTable.getInstance().getInfo(5007, 1);
        else
            return SkillTable.getInstance().getInfo(5006, 1);
    }

    private void stopPc(FrintezzaWorld world)
    {
        for (L2Character cha : world._Zone.getCharactersInsideArray())
        {
			cha.abortAttack();
			cha.abortCast();
			cha.disableAllSkills();
			cha.setTarget(null);
			cha.stopMove(null);
			cha.setIsImmobilized(true);
			cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
    }
    private void stopNpc(L2Npc npc, FrintezzaWorld world)
    {
        if (npc == null) return;
        int _Heading = npc.getHeading();
	    if (_Heading < 32768)
		    world._Angle = Math.abs(180 - (int) (_Heading / 182.044444444));
		else
		    world._Angle = Math.abs(540 - (int) (_Heading / 182.044444444));
    }
    private void startPc(FrintezzaWorld world)
    {
        for (L2Character cha : world._Zone.getCharactersInsideArray())
			if (cha != world.frintezza)
			{
				cha.enableAllSkills();
				cha.setIsImmobilized(false);
			}
    }
    private void startNpc(L2Npc npc)
    {
        if (npc == null) return;
        npc.setRunning();
		npc.setIsInvul(false);
    }
    private void attackStop(FrintezzaWorld world)
    {
		cancelQuestTimers("songs_play");
		cancelQuestTimers("songs_effect");
	    if (world.frintezza != null)
	    {
			world.frintezza.abortCast();
			world._Zone.broadcastPacket(new MagicSkillCanceld(world.frintezza.getObjectId()));
	    }
        stopSongEffects(world);
    }

    private void stopSongEffects(FrintezzaWorld world)
    {
        if (world.activeScarlet != null)
            for (L2Effect e : world.activeScarlet.getAllEffects())
                if (e.getSkill().getId() == 5008)
                    e.exit();

        for (L2Character cha : world.players)
        {
            if (cha != null)
                for (L2Effect e : cha.getAllEffects())
                    if (e.getSkill().getId() == 5008)
                        e.exit();
        }
    }

    private long generateUpdateTime()
    {
        Calendar result = Calendar.getInstance();
	    if (result.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY)
        {
            if (result.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                result.add(Calendar.DATE,1);
            result.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
        }
        else if (result.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY && result.get(Calendar.DAY_OF_WEEK) > Calendar.WEDNESDAY)
            result.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        else
        {
            if (result.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
            {
                if (result.get(Calendar.HOUR_OF_DAY) < 6 || result.get(Calendar.HOUR_OF_DAY) == 6 && result.get(Calendar.MINUTE) < 30)
                    result.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
                else
                    result.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
            }
            else if (result.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            {
                if (result.get(Calendar.HOUR_OF_DAY) < 6 || result.get(Calendar.HOUR_OF_DAY) == 6 && result.get(Calendar.MINUTE) < 30)
                    result.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
                else
                {
                    result.add(Calendar.DATE,2);
                    result.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
                }
            }
        }
		result.set(Calendar.HOUR_OF_DAY,6);
        result.set(Calendar.MINUTE,30);
        result.set(Calendar.SECOND,0);
        return result.getTimeInMillis();
    }

    private synchronized void callSkillAI(final L2Npc npc, final FrintezzaWorld world)
	{
		if (npc.isCastingNow())
			return;
        L2Character _target = (L2Character)npc.getTarget();
        L2Skill _skill = null;
		if (_target == null || _target.isDead() || (!world.secondMorph && world.lastSkillId == 5016 && Rnd.get(100) < 25))
		{
			_target = getRandomTarget(npc);
			if (_target != null)
				_skill = getRandomSkill(npc, world);
		}
		if (_skill == null)
			_skill = getRandomSkill(npc, world);
        if (world.changeTarget)
        {
            world.changeTarget = false;
            _target = getRandomTarget(npc);
        }
        npc.setIsCastingNow(true);
        final L2Character target = _target;
        final L2Skill skill = _skill;
        ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
        {
            public void run()
            {
                if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
                {
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                    npc.setTarget(target);
                    world.lastSkillId = skill.getId();
                    npc.doCast(skill);
                }
                else
                {
                    world.lastSkillId = 0;
                    npc.setIsCastingNow(false);
                    npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, null);
                }
            }
        }, 600);
	}

    public L2Skill getRandomSkill(L2Npc npc, FrintezzaWorld world)
	{
        if (!world.secondMorph)
        {
            if (world.lastSkillId == 5014)
            {
                if (Rnd.get(100) < 15)
                    return SkillTable.getInstance().getInfo(5016,1);
                else
                    return SkillTable.getInstance().getInfo(5014,1);
            }
            else
                return SkillTable.getInstance().getInfo(5014,1);
        }
        else if (world.secondMorph && !world.thirdMorph)
        {
            if (world.lastSkillId == 5014)
            {
                int rnd = Rnd.get(100);
                if (rnd < 20)
                    return SkillTable.getInstance().getInfo(5016,1);
                else if (rnd < 30)
                {
                    world.changeTarget = true;
                    return SkillTable.getInstance().getInfo(5015,2);
                }
                else
                    return SkillTable.getInstance().getInfo(5014,2);
            }
            else if (world.lastSkillId == 5016)
            {
                if (Rnd.get(100) < 25)
                    world.changeTarget = true;
                return SkillTable.getInstance().getInfo(5014,2);
            }
            else if (world.lastSkillId == 5015)
            {
                if (Rnd.get(100) < 30)
                    return SkillTable.getInstance().getInfo(5018,1);
                else
                    return SkillTable.getInstance().getInfo(5014,2);
            }
            else
                return SkillTable.getInstance().getInfo(5014,2);
        }
        else if (world.thirdMorph)
        {
            if (npc.getCurrentHp() < npc.getMaxHp()*0.75)
            {
                if (world.lastSkillId == 5014)
                {
                    int rnd = Rnd.get(100);
                    if (rnd < 40)
                        return SkillTable.getInstance().getInfo(5018,2);
                    else if (rnd < 60)
                        return SkillTable.getInstance().getInfo(5016,1);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else if (world.lastSkillId == 5016)
                {
                    if (Rnd.get(100) < 15)
                        return SkillTable.getInstance().getInfo(5015,3);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else if (world.lastSkillId == 5015)
                {
                    if (Rnd.get(100) < 30)
                        return SkillTable.getInstance().getInfo(5018,2);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else
                    return SkillTable.getInstance().getInfo(5014,3);
            }
            else
            {
                if (world.lastSkillId == 5014)
                {
                    if (Rnd.get(100) < 20)
                        return SkillTable.getInstance().getInfo(5016,1);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else if (world.lastSkillId == 5016)
                {
                    if (Rnd.get(100) < 15)
                        return SkillTable.getInstance().getInfo(5015,3);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else if (world.lastSkillId == 5015)
                {
                    if (Rnd.get(100) < 30)
                        return SkillTable.getInstance().getInfo(5019,3);
                    else
                        return SkillTable.getInstance().getInfo(5014,3);
                }
                else
                    return SkillTable.getInstance().getInfo(5014,3);
            }

        }
        return null;
	}

    public L2Character getRandomTarget(L2Npc npc)
	{
		ArrayList<L2Character> result = new ArrayList<L2Character>();
		for (L2Object obj : npc.getKnownList().getKnownObjects().values())
		    if (obj instanceof L2PcInstance || obj instanceof L2Summon)
                if (GeoData.getInstance().canSeeTarget(obj.getX(), obj.getY(), obj.getZ(), npc.getX(), npc.getY(), npc.getZ()) && !((L2Character) obj).isDead())
                    result.add((L2Character) obj);
		if (!result.isEmpty() && result.size() != 0)
            return result.get(Rnd.get(result.size()));
		return null;
	}

	public ImperialTomb(int id, String name, String descr)
	{
		super(id, name, descr);
        for (int i : new int[] {scarlet1Id, scarlet2Id, frintezzaId, 18328, 18329, 18330, 18331, 18332, 18333, 18334, 18335, 18336, 18337, 18338, 18339, 29048, 29049, 29050, 29051})
        {
            addKillId(i);
            addAttackId(i);
        }
        addSkillSeeId(29048);
        addSkillSeeId(29049);
        addSpellFinishedId(scarlet1Id);
        addSpellFinishedId(scarlet2Id);
        addSpawnId(scarlet1Id);
        addSpawnId(scarlet2Id);
		addStartNpc(guideId);
		addTalkId(guideId);
		addStartNpc(cubeId);
		addTalkId(cubeId);
        addFirstTalkId(cubeId);
        addEnterZoneId(12017);
        nextUpdate = generateUpdateTime();
	}

    public static void main(String[] args)
	{
		new ImperialTomb(-1, qn, "instances");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Imperial Tomb: Frintezza Instance");
	}
}