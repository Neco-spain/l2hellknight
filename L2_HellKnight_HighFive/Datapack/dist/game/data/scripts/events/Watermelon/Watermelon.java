/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.Watermelon;

import java.util.Arrays;
import java.util.Comparator;

import javolution.util.FastMap;

import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.instancemanager.QuestManager;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2ChronoMonsterInstance;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.quest.QuestState;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.clientpackets.Say2;
import l2.hellknight.gameserver.network.serverpackets.CreatureSay;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.model.items.L2Weapon;
import l2.hellknight.util.Rnd;

/**
 * @author JOJO
 *
 * Original: "SquashEvent" Gnacik version 1.0
 * Update By pmq 04-09-2010
 */
public class Watermelon extends Quest
{
	private static final String qn = "Watermelon";
/**
 * 活動執行者 普斯貓 Npc ID (31227) Original: ID (32727) For Freya New Npc
 */
	private static final int MANAGER = 31227;
/**
 * 神酒 Skill ID (2005)
 */
	private static final int NECTAR_SKILL = 2005;

	private static final long DESPAWN_FIRST = 180000;
	private static final long DESPAWN_NEXT = 90000;

	private static final int DAMAGE_MAX = 12;
	private static final int DAMAGE_DEFAULT = 5;
/**
 * 初章紀念樂器 克魯諾齊塔納   Item ID (4202) * 貳章紀念樂器 克魯諾鈴鼓   Item ID (5133)
 * 參章紀念樂器 克魯諾杖鐘     Item ID (5817) * 肆章紀念樂器 克魯諾達布卡 Item ID (7058)
 * 伍章紀念樂器 克魯諾瑪拉卡斯 Item ID (8350)
 */
	private static final int[] CHRONO_LIST =
	{
		4202,5133,5817,7058,8350
	};
/**
 * 未成熟的西瓜     Mob ID (13271) * 優良的西瓜       Mob ID (13273) * 不良的西瓜     Mob ID (13272)
 * 未成熟的香甜西瓜 Mob ID (13275) * 優良的香甜西瓜   Mob ID (13277) * 不良的香甜西瓜 Mob ID (13276)
 * 優良的西瓜王     Mob ID (13274) * 優良的香甜西瓜王 Mob ID (13278)
 */
	private static final int[] WATERMELON_LIST =
	{
		13271,13273,13272,
		13275,13277,13276,
		13274,13278
	};

/*
1800903	咦~！這是哪裡啊~ 是哪位叫我的呀~？
1800904	嘿咻~！西瓜登場啦~！
1800905	>_<...叫我了嗎...？
1800906	西瓜登場了~！現在開始會逐漸成長的！
1800907	唉喲，這是多久沒見面的人啊？
1800908	恭喜發財！夏天最棒的莫屬於西瓜呀~！
1800909	叫我啦~？！呼！想說會有什麼好東西嗎~？
1800910	想看我美麗的姿態嗎~？
1800911	呼呼呼！我們一起來吧~！
1800912	好好栽培的話，會是個大西瓜~ 否則會是個大爛瓜~！
1800913	嘿咻~ 變~ 身~ 了~！
1800914	您猜我是優良西瓜？還是不良西瓜？
1800915	剛剛！長大囉~！全都放馬過來！
1800916	要變大~ 還是要變強~？！說出您的願望吧~~！
1800917	期盼爛西瓜！...不過，已經成熟了耶？
1800918	期盼大西瓜！...好好打開看看吧！
1800919	已經成熟了~！我要逃走囉~^^
1800920	放我走的話，我會給您一千萬金幣的！
1800921	那麼~！您覺得我裡面裝著什麼？？
1800922	很好~ 很好~ 做得~ 很好。接下來知道要做什麼了吧？
1800923	喂！用點心做！都流出來了嘛！竟然把這麼珍貴的...
1800924	啊~ 真舒服！再~澆澆看~！
1800925	咕嚕咕嚕~ 很好！還有沒有啊？
1800926	瞄準都不會嗎？當過兵沒有啊~？
1800927	這是不是摻水啦~？味道怎麼會這樣呢~？
1800928	噢！很好~ 繼續，真舒服~
1800929	呵呵！不是那裡~ 是這裡啦~ 這裡！嫌我小，澆水澆不準啊？
1800930	呃，呸呸！這是什麼~？！這確實是神酒嗎？
1800931	好好栽培~！只要餵滿五瓶的話，就會成為大西瓜了~！
1800932	果然~！神酒中最好的果然是西瓜神酒！哈哈哈~！
1800933	呀！很好~ >_< 請輕輕地拍打我~
1800934	哇！竟然現在給我亂打？給我神酒啦~
1800935	喂~！！這樣是會被打壞的~！
1800936	您這是！還沒栽培就想吃啦？好吧~ 隨便好了~ 不給我神酒的話，我就會討厭您喔！
1800937	再打呀！再打呀！
1800938	這樣是會枯死的~ 我可不管喔~！
1800939	喂~ 如果我就這麼枯死了，那就得不到寶物囉！神酒有那麼珍貴嗎？
1800940	再接再厲！！！加油啊~！
1800941	救救我吧~ 連一滴神酒都沒嚐過就要死啦~~！
1800942	如果我就這麼死了，那就會成爛西瓜了~！
1800943	開始有精神了~？！再等30秒，就可以開溜了~ 呼呼！
1800944	20秒後，就要說拜拜了~！
1800945	哈哈，就剩10秒了~！9...8...7...！
1800946	要是不給神酒的話，2分鐘後就會跑掉囉~！
1800947	要是不給神酒的話，1分鐘後就說拜拜囉~！
1800948	我要走了~！那麼，保重啊~！
1800949	很可惜，但我這個大西瓜就先告辭了~！
1800950	為時已晚~！大家好好玩吧~！
1800951	噹噹噹噹~ 結束了。收起所有武器，挑戰下個機會吧~！
1800952	真可惜~！辛苦栽培了，呿~！T.T
1800953	喔~ 聲音不錯喔？！
1800954	樂器不錯，但沒有歌曲耶。要我來唱一首嗎？
1800955	真棒的音樂！
1800956	感覺真好~ 繼續敲敲看！
1800957	克魯諾的旋律真令人傾心！
1800958	好好配配音~！那邊那個大叔~ 您錯了！
1800959	非常好~！
1800960	啊啊~~ 身子就要敞開了！
1800961	哦，這合音！真是美呆了！再敲敲看！
1800962	就是這個！這就是我要的旋律！您想不想當歌星啊？
1800963	再怎麼敲這玩意兒，也得不到好東西的啦~！
1800964	很痛耶！求你只用樂器來敲打！
1800965	只有好音樂才能敞開我的身子！
1800966	這個不對，不是有那個嗎？就是拿來當每一章紀念品的，就用那個敲吧！
1800967	為何~ 沒有音樂？好無聊喔...我要走囉~
1800968	不是這種刺耳的噪音~！用美妙的旋律啦！
1800969	這個大西瓜只能用音樂才打得開~ 用武器是不行的~！
1800970	用樂器敲啦~！就跟你說不是這個，用樂器敲啦~！
1800971	很有實力哦！但這全都是白費工夫~！
1800972	那是到野外狩獵怪物時使用的嗎？！我要的是克魯諾~！
1800973	各位，西瓜要爆開啦！！！
1800974	我看您是會收到爛西瓜了~！
1800975	從裡面應該只會爆出些妖怪吧（？）！
1800976	大西瓜！！恭喜發財！！
1800977	別把我的死訊流傳於外~
1800978	呃啊~ 有紅色的水在流呢！
1800979	這些是全部嗎？
1800980	呃啊啊啊！！！真讓我氣憤...！！
1800981	各位~ 這裡的西瓜要爆開啦！！有寶物掉下來哦！
1800982	哎喲！爆開啦！裡面的東西嘩啦嘩啦~
1800983	呵呵呵~ 敲好一點啦！
1800984	哇！！您真是一位具有實力的人啊~？！
1800985	繼續敲啊！繼續！繼續！繼續！
1800986	我是要受打擊才能成長的！
1800987	加油啊~ 沒時間了~
1800988	才這麼點力就以為我會爆開嗎~？
1800989	不錯的攻擊~ 還算能打死隻蒼蠅~
1800990	那裡那裡！右邊一點~！啊~ 好舒服。
1800991	那也能算是敲打嗎？找個更有實力的朋友來吧！
1800992	不用去想！只管打！打吧！
1800993	需要神酒~ 西瓜神酒！
1800994	只有喝神酒，我才能長大哦~
1800995	來，趕快栽培看看吧！養得好就變大西瓜，否則就變爛西瓜~！
1800996	給我一點神酒吧~ 肚子好餓~
1800997	請趕快拿神酒給我吧...！而不是啤酒~...（抱歉！）
1800998	若拿神酒來的話，喝了之後，我就為您快速成長！
1800999	這樣的小西瓜也要吃？給我點神酒吧，我可以長更大的~！
1801000	嘻嘻嘻嘻，養好了就會發財~ 養不好的話（？）也不關我的事喔~
1801001	想要大西瓜嗎？可我喜歡的是爛西瓜~
1801002	相信我，儘管將神酒澆上！！我會讓您發大財的~！
*/
	private static final String[] SPAWN_TEXT =
	{
		"唉喲，這是多久沒見面的人啊？",
		"恭喜發財！夏天最棒的莫屬於西瓜呀~！",
		"想看我美麗的姿態嗎~？",
		"咦~！這是哪裡啊~是哪位叫我的呀~？",
		"嘿咻~！西瓜登場啦~！"
	};
	private static final String[] GROWUP_TEXT =
	{
		"要變大~還是要變強~？！說出您的願望吧~~！",
		"期盼爛西瓜！...不過，已經成熟了耶？",
		"剛剛！長大囉~！全部放馬過來！",
		"嘿咻~變~身~了~！",
		"很好~很好~做得~很好。接下來知道要做什麼了吧？",
		"已經成熟了~！我要逃走囉~^^"
	};
	// 打爆葫蘆的對話
	private static final String[] KILL_TEXT =
	{
		"呃啊~有紅色的水在流呢！",
		"這些是全部嗎？",
		"哎喲！爆開啦！裡面的東西嘩啦嘩啦~",
		"各位~這裡的西瓜要爆開啦！！有寶物掉下來哦！",
		"各位，西瓜要爆開啦！！！",
		"別把我的死訊流傳於外~",
		"呃啊啊啊！！！真讓我氣憤...！！",
		"恭喜發財！",
		"拿著這個給我滾蛋~",
		"你去給我變乞丐吧！"
	};
	// 沒用指定武器打怪的對話
	private static final String[] NOCHRONO_TEXT =
	{
		"這個大西瓜只能用音樂才打得開~用武器是不行的~！",
		"不是這種刺耳的噪音~！用美妙的旋律啦！",
		"再打呀！再打呀！",
		"唉呀？打我？打我是嗎？",
		"喂喂，夠了吧？這樣會枯哦？",
		"這樣會枯的~我不管囉~",
		"你就好好~的白費工夫吧！",
		"唉喲！竟然現在給我亂打？不是要你澆神酒嗎？",
		"喂，我這樣死了，什麼寶物都沒有了哦？神酒有那麼珍貴嗎？",
		"這樣死了就變成乞丐啦~",
		"不養我反而想吃掉我是嗎？好，隨你便~不給神酒就死給你看！"
	};
	// 用指定武器打怪的對話
	private static final String[] CHRONO_TEXT =
	{
		"好好配配音~！那邊那個大叔~您錯了！",
		"就是這個！這就是我要的旋律！您想不想當歌星啊？",
		"感覺真好~繼續敲敲看！",
		"克魯諾的旋律真令人傾心！",
		"哦，這合音！真是美呆了！再敲敲看！",
		"啊啊~~身子就要敞開了！",
		"非常好~！",
		"喔~聲音不錯喔？！",
		"真棒的音樂！",
		"樂器不錯，但沒有歌曲耶。要我來唱一首嗎？",
		"那裡那裡！右一點~！啊~好舒服。",
		"哇！！您真是一位具有實力的人啊~？！",
		"那也能算是敲打嗎？找個更有實力的朋友來吧！",
		"我是要受打擊才能成長的！",
		"加油啊~沒時間了~",
		"繼續敲啊！繼續！繼續！繼續！",
		"不用去想！只管打！打吧！"
	};
	// 用神酒餵食葫蘆的對話
	private static final String[] NECTAR_TEXT =
	{
		"噢！很好~繼續，真舒服~",
		"呃，呸呸！這是什麼~？！這確實是神酒嗎？",
		"喂！用點心做！都流出來了嘛！竟然把這麼珍貴的...",
		"相信我，儘管將神酒澆上！！我會讓您發大財的~！",
		"來，趕快栽培看看吧！養得好就變大西瓜，否則就變爛西瓜~！",
		"瞄準都不會嗎？當過兵沒有啊？",
		"咕嚕咕嚕~！很好！還有沒有啊？",
		"好好栽培~！只要餵滿五瓶的話，就會成為大西瓜了~！",
		"呵呵！不是那裡~是這裡啦~這裡！嫌我小，澆水澆不準啊？",
		"需要神酒~西瓜神酒！",
		"啊~真舒服！再~澆澆看~！",
		"這樣的小西瓜也要吃？給我點神酒吧，我可以長更大的~！",
		"嘻嘻嘻嘻，,養好了就會發財~養不好的話頭（？）也不關我的事喔~",
		"果然~！神酒中最好的果然是西瓜神酒！哈哈哈~！",
		"很好~很好~做得~很好。接下來知道要做什麼了吧？",
		"請趕快拿神酒給我吧...！而不是啤酒~...（抱歉！）",
		"這是不是摻水啦~？味道怎麼會這樣呢~？",
		"好好栽培的話，會夫個大西瓜~否則會是個大爛瓜~！",
		"想要大西瓜嗎？可我喜歡的是爛西瓜~",
		"給我一點神酒吧~肚子好餓~"
	};
	// 獎品 【 ID 怪物 , NO 機率 , ID 物品, NO 數量 】
	private static final int[][] DROPLIST =
	{
		/**
		 * must be sorted by npcId !
		 * npcId, chance, itemId,qty [,itemId,qty...]
		 *
		 * Young Watermelon
		 * 未成熟的西瓜
		 */
		{ 13271,100,  6391,2 },		// Nectar
		/**
		 * Defective Watermelon
		 * 不良的西瓜
		 */
		{ 13272,100,  6391,10 },	// Nectar
		/**
		 * Rain Watermelon
		 * 優良的西瓜
		 */
		{ 13273,100,  6391,30 },	// Nectar
		/**
		 * Large Rain Watermelon
		 * 優良的西瓜王
		 */
		{ 13274,100,  6391,50 },	// Nectar
		/**
		 * Young Honey Watermelon
		 * 未成熟的香甜西瓜
		 */
		{ 13275,100, 14701,2,		// 超強力瞬間體力治癒藥水
		             14700,2 },		// 超強力體力治癒藥水
		/**
		 * Defective Honey Watermelon
		 * 不良的香甜西瓜
		 */
		{ 13276, 50,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2 },		// 祝福的防具強化卷軸-A級
		{ 13276, 30,  6622,1 },		// 巨人的經典
		{ 13276, 10,  8750,1 },		// 高級生命石-67級
		{ 13276, 10,  8751,1 },		// 高級生命石-70級
		{ 13276, 99, 14701,4,		// 超強力瞬間體力治癒藥水
		             14700,4 },		// 超強力體力治癒藥水
		{ 13276, 50,  1461,4 },		// 結晶-A級
		{ 13276, 30,  1462,3 },		// 結晶-S級
		{ 13276, 50,  2133,4 },		// 寶石-A級
		{ 13276, 30,  2134,3 },		// 寶石-S級
		/**
		 * Rain Honey Watermelon
		 * 優良的香甜西瓜
		 */
		{ 13277,  7,  9570,1,		// 紅色搜魂石-階段14
		              9571,1,		// 藍色搜魂石-階段14
		              9572,1,		// 綠色搜魂石-階段14
		             10480,1,		// 紅色搜魂石-階段15
		             10481,1,		// 藍色搜魂石-階段15
		             10482,1,		// 綠色搜魂石-階段15
		             13071,1,		// 紅色搜魂石-階段16
		             13072,1,		// 藍色搜魂石-階段16
		             13073,1 },		// 綠色搜魂石-階段16
		{ 13277, 35,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		               959,3,		// 武器強化卷軸-S級
		               960,3,		// 防具強化卷軸-S級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2,		// 祝福的防具強化卷軸-A級
		              6577,1,		// 祝福的武器強化卷軸-S級
		              6578,1 },		// 祝福的防具強化卷軸-S級
		{ 13277, 28,  6622,3,		// 巨人的經典
		              9625,2,		// 巨人的經典-遺忘篇
		              9626,2,		// 巨人的經典-訓練篇
		              9627,2 },		// 巨人的經典-熟練篇
		{ 13277, 14,  8750,10 },	// 高級生命石-67級
		{ 13277, 14,  8751,8 },		// 高級生命石-70級
		{ 13277, 14,  8752,6 },		// 高級生命石-76級
		{ 13277, 14,  9575,4 },		// 高級生命石-80級
		{ 13277, 14, 10485,2 },		// 高級生命石-82級
		{ 13277, 14, 14168,1 },		// 高級生命石-84級
		{ 13277, 21,  8760,1,		// 特級生命石-67級
		              8761,1,		// 特級生命石-70級
		              8762,1,		// 特級生命石-76級
		              9576,1,		// 特級生命石-80級
		             10486,1,		// 特級生命石-82級
		             14169,1 },		// 特級生命石-84級
		{ 13277, 21, 14683,1,		// 超強力生命靈藥-D級
		             14684,1,		// 超強力生命靈藥-C級
		             14685,1,		// 超強力生命靈藥-B級
		             14686,1,		// 超強力生命靈藥-A級
		             14687,1,		// 超強力生命靈藥-S級
		             14689,1,		// 超強力精神靈藥-D級
		             14690,1,		// 超強力精神靈藥-C級
		             14691,1,		// 超強力精神靈藥-B級
		             14692,1,		// 超強力精神靈藥-A級
		             14693,1,		// 超強力精神靈藥-S級
		             14695,1,		// 超強力鬥志靈藥-D級
		             14696,1,		// 超強力鬥志靈藥-C級
		             14697,1,		// 超強力鬥志靈藥-B級
		             14698,1,		// 超強力鬥志靈藥-A級
		             14699,1 },		// 超強力鬥志靈藥-S級
		{ 13277, 99, 14701,9,		// 超強力瞬間體力治癒藥水
		             14700,9 },		// 超強力體力治癒藥水
		{ 13277, 63,  1461,8 },		// 結晶-A級
		{ 13277, 49,  1462,5 },		// 結晶-S級
		{ 13277, 63,  2133,6 },		// 寶石-A級
		{ 13277, 49,  2134,4 },		// 寶石-S級
		/**
		 * Large Rain Honey Watermelon
		 * 優良的香甜西瓜王
		 */
		{ 13278, 10,  9570,1,		// 紅色搜魂石-階段14
		              9571,1,		// 藍色搜魂石-階段14
		              9572,1,		// 綠色搜魂石-階段14
		             10480,1,		// 紅色搜魂石-階段15
		             10481,1,		// 藍色搜魂石-階段15
		             10482,1,		// 綠色搜魂石-階段15
		             13071,1,		// 紅色搜魂石-階段16
		             13072,1,		// 藍色搜魂石-階段16
		             13073,1 },		// 綠色搜魂石-階段16
		{ 13278, 50,   729,4,		// 武器強化卷軸-A級
		               730,4,		// 防具強化卷軸-A級
		               959,3,		// 武器強化卷軸-S級
		               960,3,		// 防具強化卷軸-S級
		              6569,2,		// 祝福的武器強化卷軸-A級
		              6570,2,		// 祝福的防具強化卷軸-A級
		              6577,1,		// 祝福的武器強化卷軸-S級
		              6578,1 },		// 祝福的防具強化卷軸-S級
		{ 13278, 40,  6622,3,		// 巨人的經典
		              9625,2,		// 巨人的經典-遺忘篇
		              9626,2,		// 巨人的經典-訓練篇
		              9627,2 },		// 巨人的經典-熟練篇
		{ 13278, 20,  8750,10 },	// 高級生命石-67級
		{ 13278, 20,  8751,8 },		// 高級生命石-70級
		{ 13278, 20,  8752,6 },		// 高級生命石-76級
		{ 13278, 20,  9575,4 },		// 高級生命石-80級
		{ 13278, 20, 10485,2 },		// 高級生命石-82級
		{ 13278, 20, 14168,1 },		// 高級生命石-84級
		{ 13278, 30,  8760,1,		// 特級生命石-67級
		              8761,1,		// 特級生命石-70級
		              8762,1,		// 特級生命石-76級
		              9576,1,		// 特級生命石-80級80
		             10486,1,		// 特級生命石-82級
		             14169,1 },		// 特級生命石-84級
		{ 13278, 30, 14683,1,		// 超強力生命靈藥-D級
		             14684,1,		// 超強力生命靈藥-C級
		             14685,1,		// 超強力生命靈藥-B級
		             14686,1,		// 超強力生命靈藥-A級
		             14687,1,		// 超強力生命靈藥-S級
		             14689,1,		// 超強力精神靈藥-D級
		             14690,1,		// 超強力精神靈藥-C級
		             14691,1,		// 超強力精神靈藥-B級
		             14692,1,		// 超強力精神靈藥-A級
		             14693,1,		// 超強力精神靈藥-S級
		             14695,1,		// 超強力鬥志靈藥-D級
		             14696,1,		// 超強力鬥志靈藥-C級
		             14697,1,		// 超強力鬥志靈藥-B級
		             14698,1,		// 超強力鬥志靈藥-A級
		             14699,1 },		// 超強力鬥志靈藥-S級
		{ 13278, 99, 14701,12,		// 超強力瞬間體力治癒藥水
		             14700,12 },	// 超強力體力治癒藥水
		{ 13278, 90,  1461,8 },		// 結晶-A級
		{ 13278, 70,  1462,5 },		// 結晶-S級
		{ 13278, 90,  2133,6 },		// 寶石-A級
		{ 13278, 70,  2134,4 },		// 寶石-S級
	};

	private int _numAtk = 0;
	private int w_nectar = 0;
	
	class TheInstance
	{
		int nectar;
		//int numatk;
		//int tmpatk;
		long despawnTime;
	}
	FastMap<L2ChronoMonsterInstance, TheInstance> _monsterInstances = new FastMap<L2ChronoMonsterInstance, TheInstance>().shared();
	private TheInstance create(L2ChronoMonsterInstance mob)
	{
		TheInstance mons = new TheInstance();
		_monsterInstances.put(mob, mons);
		return mons;
	}
	private TheInstance get(L2ChronoMonsterInstance mob)
	{
		return _monsterInstances.get(mob);
	}
	private void remove(L2ChronoMonsterInstance mob)
	{
		cancelQuestTimer("countdown", mob, null);
		cancelQuestTimer("despawn", mob, null);
		_monsterInstances.remove(mob);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event == "countdown")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			final TheInstance self = get(mob);
			int timeLeft = (int)((self.despawnTime - System.currentTimeMillis()) / 1000);
			if (timeLeft == 30)
				autoChat(mob, "開始有精神了~？！再等30秒，就可以開溜了~呼呼！");
			else if (timeLeft == 20)
				autoChat(mob, "開始有精神了~？！再等20秒，就可以開溜了~呼呼！");
			else if (timeLeft == 10)
				autoChat(mob, "哦，10秒時間！ 9 ... 8 ... 7 ...");
			else if (timeLeft == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "喂，我這樣死了，什麼寶物都沒有了哦？神酒有那麼珍貴嗎？");
				else
					autoChat(mob, "主人救救我吧~連一滴神酒都沒嚐過就要死啦~");
			}
			else if ((timeLeft % 60) == 0)
			{
				if (self.nectar == 0)
					autoChat(mob, "要是不給神酒的話" + timeLeft / 60 + "分鐘後就會跑掉囉~");
			}
		}
		else if (event == "despawn")
		{
			remove((L2ChronoMonsterInstance)npc);
			npc.deleteMe();
		}
		else if (event == "sound")
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			mob.broadcastPacket(new PlaySound(0, "ItemSound3.sys_sow_success", 0, 0, 0, 0, 0));
		}
		return "";
		//return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		L2Weapon weapon;
		final boolean isChronoAttack
			= !isPet
			&& (weapon = attacker.getActiveWeaponItem()) != null && contains(CHRONO_LIST, weapon.getItemId());
		switch (mob.getNpcId())
		{
			case 13271:
			case 13273:
			case 13272:
			case 13274:
				if (isChronoAttack)
				{
					chronoText(mob);
				}
				else
				{
					noChronoText(mob);
				}
				break;
			case 13275:
			case 13277:
			case 13276:
			case 13278:
				if (isChronoAttack)
				{
					mob.setIsInvul(false);
					if (damage == 0)
						mob.getStatus().reduceHp(DAMAGE_DEFAULT, attacker);
					else if (damage > DAMAGE_MAX)
						mob.getStatus().setCurrentHp(mob.getStatus().getCurrentHp() + damage - DAMAGE_MAX);
					chronoText(mob);
				}
				else
				{
					mob.setIsInvul(true);
					mob.setCurrentHp(mob.getMaxHp());
					noChronoText(mob);
				}
				break;
			default:
				throw new RuntimeException();
		}
		mob.getStatus().stopHpMpRegeneration();
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.getId() == NECTAR_SKILL && targets[0] == npc)
		{
			final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
			switch(mob.getNpcId())
			{
				case 13271:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(13273, 13273, 13274, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(13272, 13272, 13272, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(13272, 13273, 13274, mob);
					break;
				case 13275:
					if (w_nectar == 0 || w_nectar == 1 || w_nectar == 2 || w_nectar == 3 || w_nectar == 4)
					{
						if(Rnd.get(100) < 50)
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4514, 1));
							w_nectar++;
						}
						else
						{
							nectarText(mob);
							npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
							w_nectar++;
							_numAtk++;
						}
					}
					else if (w_nectar >= 4)
					{
						if (_numAtk >= 4)
						{
							randomSpawn(13277, 13277, 13278, mob);
							w_nectar++;
							_numAtk = 0;
						}
						else 
						{
							randomSpawn(13276, 13276, 13276, mob);
							_numAtk = 0;
						}
					}
					//randomSpawn(13276, 13277, 13278, mob);
					break;
				case 13273:
					npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13274, mob);
					break;
				case 13277:
					npc.doCast(SkillTable.getInstance().getInfo(4513, 1));
					randomSpawn(13278, mob);
					break;
				case 13272:
				case 13276:
					autoChat(mob, "都長大了！那就來走人囉~");
					break;
			}
		}
		return null;
		//return super.onSkillSee(npc,caster,skill,targets,isPet);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		remove(mob);
		autoChat(mob, KILL_TEXT[Rnd.get(KILL_TEXT.length)]);
		dropItem(mob, killer);
		w_nectar = 0;
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		assert npc instanceof L2ChronoMonsterInstance;
		
		final L2ChronoMonsterInstance mob = (L2ChronoMonsterInstance)npc;
		mob.setOnKillDelay(1500);	//Default 5000ms.
		final TheInstance self = create(mob);
		switch(mob.getNpcId())
		{
			case 13271:
			case 13275:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_FIRST, mob, null);
				self.nectar = 0;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_FIRST;
				autoChat(mob, SPAWN_TEXT[Rnd.get(SPAWN_TEXT.length)]);
				break;
			case 13272:
			case 13273:
			case 13274:
			case 13276:
			case 13277:
			case 13278:
				startQuestTimer("countdown", 10000, mob, null, true);
				startQuestTimer("despawn", DESPAWN_NEXT, mob, null);
				startQuestTimer("sound",100, mob, null);
				self.nectar = 5;
				self.despawnTime = System.currentTimeMillis() + DESPAWN_NEXT;
				autoChat(mob, GROWUP_TEXT[Rnd.get(GROWUP_TEXT.length)]);
				break;
			default:
				throw new RuntimeException();
		}
		return super.onSpawn(npc);
	}

	static {
		Arrays.sort(DROPLIST, new Comparator<int[]>() {
			@Override
			public int compare(int[] a, int[] b) { return a[0] - b[0]; }
		});
	}
	private static final void dropItem(L2ChronoMonsterInstance mob, L2PcInstance player)
	{
		final int npcId = mob.getNpcId();
		for (int[] drop : DROPLIST)
		{
			/**
			 * npcId   = drop[0]
			 * chance  = drop[1]
			 * itemId  = drop[2,4,6,8...]
			 * itemQty = drop[3,5,7,9...]
			 */
			if (npcId == drop[0])
			{
				final int chance = Rnd.get(100);
				if (chance < drop[1])
				{
					int i = 2 + 2 * Rnd.get((drop.length - 2) / 2);
					int itemId = drop[i + 0];
					int itemQty = drop[i + 1];
					if (itemQty > 1) itemQty = Rnd.get(1, itemQty);
					mob.dropItem(mob.getOwner(), itemId, itemQty);
					continue;
				}
			}
			if (npcId < drop[0])
				return; // not found
		}
	}

	private void randomSpawn(int bad, int good, int king, L2ChronoMonsterInstance mob)
	{
		//final TheInstance self = get(mob);
		if (w_nectar >= 5)
		{
			w_nectar = 0;
			int _random = Rnd.get(100);
			if ((_random -= 10) < 0)		// 10% 
				spawnNext(king, mob);
			else if ((_random -= 40) < 0)	// 40% 
				spawnNext(good, mob);
			else							// 50% 
				spawnNext(bad, mob);
		}
		else
		{
			nectarText(mob);
		}
	}

	private void randomSpawn(int king, L2ChronoMonsterInstance mob)
	{
		final TheInstance self = get(mob);
		if (++self.nectar > 5 && self.nectar <= 15 && Rnd.get(100) < 10)	// 10% 
			spawnNext(king, mob);
		else
			nectarText(mob);
	}

	private void autoChat(L2ChronoMonsterInstance mob, String text)
	{
		mob.broadcastPacket(new CreatureSay(mob.getObjectId(), Say2.ALL, mob.getName(), text));
	}
	private void chronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, CHRONO_TEXT[Rnd.get(CHRONO_TEXT.length)]);
	}
	private void noChronoText(L2ChronoMonsterInstance mob)
	{
		if (Rnd.get(100) < 20)
			autoChat(mob, NOCHRONO_TEXT[Rnd.get(NOCHRONO_TEXT.length)]);
	}
	private void nectarText(L2ChronoMonsterInstance mob)
	{
	/*	if (Rnd.get(100) < 30)	*/
		autoChat(mob, NECTAR_TEXT[Rnd.get(NECTAR_TEXT.length)]);
	}

	private void spawnNext(int npcId, L2ChronoMonsterInstance oldMob)
	{
		remove(oldMob);
		L2ChronoMonsterInstance newMob = (L2ChronoMonsterInstance)addSpawn(npcId, oldMob.getX(), oldMob.getY(), oldMob.getZ(), oldMob.getHeading(), false, 0);
		newMob.setOwner(oldMob.getOwner());
		newMob.setTitle(oldMob.getTitle());
		oldMob.deleteMe();
	}

	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public Watermelon(int questId, String name, String descr)
	{
		super(questId, name, descr);

		for (int mob : WATERMELON_LIST)
		{
			addAttackId(mob);
			addKillId(mob);
			addSpawnId(mob);
			addSkillSeeId(mob);
		}

		addStartNpc(MANAGER);
		addFirstTalkId(MANAGER);
		addTalkId(MANAGER);
		
		//addSpawn(MANAGER, 83063, 148843, -3477, 32219, false, 0);
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		//String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			Quest q = QuestManager.getInstance().getQuest(getName());
			st = q.newQuestState(player);
		}
		switch (npc.getNpcId())
		{
			case MANAGER: return "31227.htm";
		}
		throw new RuntimeException();
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		// 31227-1.htm
		return event;
	}

	public static void main(String[] args)
	{
		new Watermelon(-1, qn, "events");
	}
}