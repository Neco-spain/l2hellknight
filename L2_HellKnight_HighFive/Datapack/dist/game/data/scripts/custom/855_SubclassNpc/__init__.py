import sys
from java.lang import System
from cStringIO import StringIO
from l2.hellknight import L2DatabaseFactory
from l2.hellknight.gameserver.model.quest import State
from l2.hellknight.gameserver.model.quest import QuestState
from l2.hellknight.gameserver.model.quest.jython import QuestJython as JQuest
from l2.hellknight.gameserver.model.base import ClassId
from l2.hellknight.gameserver.network.serverpackets import SetupGauge
from l2.hellknight.gameserver.instancemanager import QuestManager
from l2.hellknight.gameserver.datatables import ExperienceTable
from l2.hellknight.gameserver.datatables import SkillTable
from l2.hellknight.gameserver.datatables import EnchantGroupsTable
from l2.hellknight.gameserver.datatables import CharTemplateTable
from l2.hellknight.gameserver.datatables import ItemTable
from l2.hellknight.gameserver.network.serverpackets import ActionFailed
from l2.hellknight.gameserver.network.L2GameClient import GameClientState
from l2.hellknight.gameserver.network.serverpackets import ActionFailed
from l2.hellknight.gameserver.network.serverpackets import CharSelectionInfo
from l2.hellknight.gameserver.network.serverpackets import RestartResponse
from l2.hellknight.gameserver.taskmanager import AttackStanceTaskManager

NPC         = [5004]
QuestId     = 855
QuestName   = "SubclassNpc"
QuestDesc   = "custom"
QI			= "%s_%s" % (QuestId,QuestName)

print "============================="
print "INFO LOADED SUBCLASS MANAGER"
print "============================="

#-------------------------------------------------------------------------------------------------------------------------------------
# SETTINGS
#-------------------------------------------------------------------------------------------------------------------------------------

#For more than 3 subclasses, you must increase the variable number into the SQL and add to the database. 
#inside the sql you will find some variables named SubclassidX. Just change the "X" increasing the number. 
#This value shouldn't be changed if you don't want to increase the subclasses number beyond 3. 
#Increase or decrease the "maxsubsindb" value without make these changes, will cause errors. Be carefull!.
maxsubsindb = 3

#True, allows reloading the configuration script from the game, without restarting the server (for GMs only). False, disables it.
ShowReloadScriptPanel = True

# Subclasses number that can be added. Must be less than or equal to "maxsubsindb".
SubsNumber = 3

# True, allows add stackable subclasses in every original game subclass (Mainclass and every retails).
# False, allows add stackable subclasses in only one original game subclass or main class
AllowMultiSubs = False

# True, allows any stackable subclass. False, allows add your own race's subclasses only.
AllowAllSubs = True

#This option work if "AllowAllSubs = False", Also you need to be using a original game subclass (Retail) to get available this.
#True, allow add a subclass with the same main class's race. False, allow add a subclass with the same Retail's race.
AllowMutantRetail = True

#The next three options work if "AllowAllSubs = True" only.
#True, allows everybody add Kamael subclass. False otherwise.
AllowKamaelSubs = False

#True, allows Dark Elf class do elf subclass, and Elf class do Dark Elf Subclass. False otherwise.
AllowDElfvsElfSubs = False

#True, allows Kamaels add any subclass. False, allows Kamaels to add their own race only.
AllowAllSubsToKamael = False

#True, allows delete the main class or any subclass added. False, allow to delete added subclasses only. Default: False
AllowDelMainClass = False

# Minimum Level to add a subclass. Default: 76
MinLevel = 76

#True, allows add subsclasses if the character is a Noblesse only. False, otherwise. Default: False
AllowOnlyNobles = False

#True, allow to add subclass or any other actions if you have the required items only. False, otherwise
ReqItems = True

#Required Item to switch between the subclasses. Default: 57 (Adena)
#Required items number.
Item1_Req = 57
Item1_Num = 1000000

#Required Item to add a subclass.
#Required items number.
Item2_Req = 3481
Item2_Num = 100

#Required Item to delete subclasses.
#Required items number.
Item3_Req = 3481
Item3_Num = 10

# True: Change level after add a subclass
# False: Not to change level after add a subclass. Default: True
DecLevel= True

# True:  HTML will show 3rd Class trasfer to choose, also it disallow add subclasses if the characters haven't added 3rd job.
# False: HTML Will show 2nd Class trasfer to choose, also it disallow add subclasses if the characters haven't added 2nd or 3rd job.
AllowThirdJob = True

#Level at which the character will be changed after add a subclass. Default: 40
NewLevel= 40

# Delay time in seconds before being restarted automatically. Not recommended a number bellow to 5 seconds. Default: 5 seconds
RestartDelay = 5

# True: The user must wait a while before take any action. Default: True
# False: The user can do any action without time constraints. Not recommended
Block = True

#Blocking time in seconds before take any action.
BlockTime = 20

#-------------------------------------------------------------------------------------------------------------------------------------

def MainHtml(st) : 
    xsubsamount=getsubsammount(st)
    if xsubsamount >= 0 :
        HTML = StringIO()
        HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
        HTML.write("<font color=\"303030\">%s</font>" % AIO())
        HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
        HTML.write("<table width=250 border=0 bgcolor=444444>")
        HTML.write("<tr><td></td></tr>")
        HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">   Before taking any action, make sure you</font></td></tr>")
        HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">   are using the Main Class or the proper</font></td></tr>")
        HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">   Subclass, which requested the changes.</font></td></tr>")
        if ReqItems == True:
            HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">   Besides you need the required items.</font></td></tr>")
            HTML.write("<tr><td><br></td></tr>")
            if xsubsamount < SubsNumber and Item2_Num >= 1: HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">          Choose Sub: <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item2_Num,getitemname(Item2_Req)))
            if Item3_Num >= 1: HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">          Delete Sub:  <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item3_Num,getitemname(Item3_Req)))
            if Item1_Num >= 1: HTML.write("<tr><td align=\"left\"><font color=\"0088EE\">          Switch Sub:  <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item1_Num,getitemname(Item1_Req)))
        HTML.write("<tr><td></td></tr></table><br>")
        HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
        HTML.write("<tr><td width=90 align=\"center\"><table width=90 border=0 bgcolor=444444><tr><td width=90 align=\"center\"><table width=85 border=0 bgcolor=444444>")
        if xsubsamount < SubsNumber :
            HTML.write("<tr><td><button value=\"Choose Sub\" action=\"bypass -h Quest %s gethtml 1\" width=80 height=24 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br1>" % QI)
        HTML.write("<tr><td><button value=\"Delete Sub\" action=\"bypass -h Quest %s gethtml 3\" width=80 height=24 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br1>" % QI)
        HTML.write("<tr><td><button value=\"Switch Sub\" action=\"bypass -h Quest %s gethtml 2\" width=80 height=24 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br1>" % QI)
        HTML.write("</table></td></tr></table></td></tr>")
        HTML.write("</center></body></html>")
        return HTML.getvalue()
    else:
		if st.getQuestItemsCount(Item2_Req) < Item2_Num and ReqItems == True: return comunerrors(st,"0")
		if st.player.getRace().ordinal() == 5 and AllowAllSubsToKamael == False : return MainHtmlIV(st,"5")
		if AllowAllSubs == False : 
			if AllowMutantRetail == False and st.player.isSubClassActive(): return MainHtmlIV(st,`st.player.getTemplate().race.ordinal()`)
			else: return MainHtmlIV(st,`st.player.getRace().ordinal()`)
		else: return MainHtmlI(st)

def MainHtmlI(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Choose a Race</font></td></tr>")
    HTML.write("<tr><td></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>")
    HTML.write("<tr><td width=110 align=\"center\"><table width=110 border=0 bgcolor=444444><tr><td width=110 align=\"center\"><table width=105 border=0 bgcolor=444444>")
    HTML.write("<tr><td><button value=\"Human\" action=\"bypass -h Quest %s escraza 0\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    if AllowDElfvsElfSubs or st.player.getRace().ordinal() != 2: HTML.write("<tr><td><button value=\"Elf\" action=\"bypass -h Quest %s escraza 1\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    if AllowDElfvsElfSubs or st.player.getRace().ordinal() != 1: HTML.write("<tr><td><button value=\"Dark Elf\" action=\"bypass -h Quest %s escraza 2\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    HTML.write("<tr><td><button value=\"Orc\" action=\"bypass -h Quest %s escraza 3\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    HTML.write("<tr><td><button value=\"Dwarf\" action=\"bypass -h Quest %s escraza 4\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    if AllowKamaelSubs or st.player.getRace().ordinal() == 5:
        HTML.write("<tr><td><button value=\"Kamael\" action=\"bypass -h Quest %s escraza 5\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" % QI)
    HTML.write("</table></td></tr></table></td></tr>")
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def MainHtmlII(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Choose a subclass to Switch</font></td></tr>")
    HTML.write("<tr><td></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>")
    HTML.write("<tr><td width=110 align=\"center\"><table width=110 border=0 bgcolor=444444><tr><td width=110 align=\"center\"><table width=105 border=0 bgcolor=444444>")
    temp = getVar(st,"currentsub"); j=-1
    for i in range(maxsubsindb + 1):
        var = getVar(st,"subclassid%s" % i)
        if int(var) >= 0 and int(var) <= 136: 
            j+=1
            if temp != `i` and SubsNumber >= j:
                HTML.write("<tr><td><button value=\"%s\" action=\"bypass -h Quest %s camb %s\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" %(getclassname(var),QI,i))
    HTML.write("</table></td></tr></table></td></tr>")
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def MainHtmlIII(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Choose the class you want to delete</font></td></tr>")
    HTML.write("<tr><td></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>")
    HTML.write("<tr><td width=110 align=\"center\"><table width=110 border=0 bgcolor=444444><tr><td width=100 align=\"center\"><table width=105 border=0 bgcolor=444444>")
    j=-1
    for i in range(maxsubsindb + 1):
        var = getVar(st,"subclassid%s" % i)
        if int(var) >= 0 and int(var) <= 136: 
            if i == 0 and AllowDelMainClass == False: pass
            else:
                j+=1
                if SubsNumber >= j:
                    HTML.write("<tr><td><button value=\"%s\" action=\"bypass -h Quest %s confirm %s\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr><br>" %(getclassname(var),QI,i))
    HTML.write("</table></td></tr></table></td></tr>")
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def MainHtmlIV(st,case) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td></td></tr>")
    HTML.write(generateRace(st,case))
    HTML.write("</center></body></html>")
    return HTML.getvalue()
	
def MainHtmlV(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font><br>" % AIO())
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"LEVEL\">Confirmation</font></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br></td></tr>")                                                                                                                                                                                            
    HTML.write("<tr><td align=\"center\"><font color=\"FF0000\">This option can be seen by GMs only and it<br1>allow to update any changes made in the<br1>script. You can disable this option in<br1>the settings section within the Script.<br><font color=\"LEVEL\">Do you want to update the SCRIPT?</font></font></td></tr>")
    HTML.write("<tr><td></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>")
    HTML.write("<button value=\"Yes\" action=\"bypass -h Quest %s reloadscript 1\" width=50 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\">" % QI)
    HTML.write("<button value=\"No\" action=\"bypass -h Quest %s reloadscript 0\" width=50 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\">" % QI)			
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def generateRace(st,raceclass) : 
    HTML = StringIO()
    if raceclass == "0": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">HUMAN</font></td></tr>")
    if raceclass == "1": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">ELF</font></td></tr>")
    if raceclass == "2": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">DARK ELF</font></td></tr>")
    if raceclass == "3": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">ORC</font></td></tr>")
    if raceclass == "4": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">DWARF</font></td></tr>")
    if raceclass == "5": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">KAMAEL</font></td></tr>")
    HTML.write("<tr><td></td></tr></table><br><img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    if raceclass == "5": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Man                      Woman</font></td></tr>")
    elif raceclass == "4": HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Figther</font></td></tr>")
    else: HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Figther                       Mage</font></td></tr>")
    HTML.write("<tr><td width=250 align=\"center\"><table width=240 border=0 bgcolor=444444><tr><td width=240 align=\"center\"><table width=235 border=0 bgcolor=444444><tr>")
    if raceclass == "0":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 92\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(92)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 98\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(98)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 93\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(93)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 97\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(97)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 88\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(88)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 96\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(96)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 89\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(89)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 95\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(95)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 90\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(90)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 94\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(94)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 91\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(91)),QI))
    if raceclass == "1":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 102\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(102)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 105\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(105)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 101\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(101)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 103\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(103)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 100\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(100)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 104\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(104)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 99\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(99)),QI))
    if raceclass == "2":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 109\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(109)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 112\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(112)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 108\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(108)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 110\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(110)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 107\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(107)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 111\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(111)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 106\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(106)),QI))
    if raceclass == "3":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 114\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(114)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 116\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(116)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 113\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(113)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 115\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(115)),QI))
    if raceclass == "4":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 118\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(118)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 117\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(117)),QI))
    if raceclass == "5":
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 131\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(131)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 134\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(134)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 132\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td>" % (getclassname(getparentclass(132)),QI))
        HTML.write("<td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 133\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(133)),QI))
        HTML.write("<tr><td align=\"center\"><button value=\"%s\" action=\"bypass -h Quest %s confirm 136\" width=130 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\"></td></tr>" % (getclassname(getparentclass(136)),QI))
    HTML.write("</table></td></tr></table></td></tr>")
    return HTML.getvalue()

def Confirmation(st,case,case1,case2):
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font><br>" % AIO())
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"LEVEL\">Confirmation</font></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    if int(case) == 1 :
        HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Do you really want to add the<br1><font color=\"LEVEL\">%s</font>  subclass?</td></tr>" % getclassname(case1))
    if int(case) == 3 :
        HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Do you really want to delete the<br1><font color=\"LEVEL\">%s</font> subclass?</td></tr>" % getclassname(getVar(st,"subclassid"+case2)))
    HTML.write("<tr><td></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br><br>")
    HTML.write("<button value=\"Yes\" action=\"bypass -h Quest %s %s %s\" width=50 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\">" % (QI,case1,case2))
    HTML.write("<button value=\"No\" action=\"bypass -h Quest %s gethtml %s\" width=50 height=28 back=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\" fore=\"L2UI_CT1.Windows_DF_Drawer_Bg_Darker\">" % (QI,case))
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def complete(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Congratulations</font></td></tr>")
    HTML.write("<tr><td></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">Class changed successfully</font></td></tr>")
    HTML.write("<tr><td><br><br></td></tr></table><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"00FF00\">You will be automatically restarted<br1>in %s seconds.</font></td></tr></table><br>" % RestartDelay)
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1>")
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def errasecomplete(st) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font>" % AIO())
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"0088EE\">The class that you chose has been deleted</font></td></tr>")
    HTML.write("<tr><td><br><br></td></tr></table><br>")
    HTML.write("<table width=250 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"00FF00\">You will be automatically restarted<br1>in %s seconds.</font></td></tr></table><br>" % RestartDelay)
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1>")
    HTML.write("</center></body></html>")
    return HTML.getvalue()

def errordeclasse(st,case,case2) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font><br>" % AIO())
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"FF5500\">Error</font></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    if int(case) >= 88 :
        HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You can't add <font color=\"LEVEL\">%s</font> subclass.<br1>Talk to a Grand Master and switch<br1>to the proper class first.</td></tr>" % getclassname(case2))
    else:
        HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You can't %s <font color=\"LEVEL\">%s</font><br1>subclass. Talk to a Grand Master and<br1>switch to the proper class first.</td></tr>" % (case2,getclassname(getVar(st,"subclassid"+case))))
    HTML.write("<tr><td><br><br></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1>")
    HTML.write("</center></body></html>")
    if getblocktime(st) == True : pass
    st.playSound("ItemSound3.sys_shortage")
    return HTML.getvalue()

def errordeduplicado(st,numero) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font><br>" % AIO())
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"FF5500\">Error</font></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1><br>")
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You can't add <font color=\"LEVEL\">%s</font><br1>subclass. You already have this class.</td></tr>" % getclassname(numero))
    HTML.write("<tr><td><br><br></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=250 height=1>")
    HTML.write("</center></body></html>")
    if getblocktime(st) == True : pass
    st.playSound("ItemSound3.sys_shortage")
    return HTML.getvalue()

def comunerrors(st,case) :
    HTML = StringIO()
    HTML.write("<html><head><title>Subclass Master</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br><br>")
    HTML.write("<font color=\"303030\">%s</font><br>" % AIO())
    HTML.write("<table width=260 border=0 bgcolor=444444>")
    HTML.write("<tr><td align=\"center\"><font color=\"FF5500\">Error</font></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=220 height=1><br>")
    HTML.write("<table width=220 border=0 bgcolor=444444>")
    HTML.write("<tr><td><br><br></td></tr>")
    if case == "0": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You don't meet this NPC's minimum<br1>required items. Come back when you<br1>get <font color=\"LEVEL\">%s %s.</font></td></tr>" % (Item2_Num,getitemname(Item2_Req)))
    if case == "1": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You don't have the required items.<br1>You need <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item1_Num,getitemname(Item1_Req)))
    if case == "2": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You don't have the required items.<br1>You need <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item2_Num,getitemname(Item2_Req)))
    if case == "3": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You don't have the required items.<br1>You need <font color=\"LEVEL\">%s %s</font></td></tr>" % (Item3_Num,getitemname(Item3_Req)))
    if case == "4": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You aren't eligible to add a<br1>subclass at this time.<br1>Your level must have <font color=\"LEVEL\">%s or above.</font></td></tr>" % MinLevel)
    if case == "5": 
        if AllowThirdJob == True: HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You aren't eligible to do any action<br1>at this time. Your current ocupation<br1>must have <font color=\"LEVEL\">3rd Job</font></td></tr>")
        else: HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You aren't eligible to do any action<br1>at this time. Your current ocupation<br1>must be <font color=\"LEVEL\">2nd or 3rd Job</font></td></tr>")
    if case == "6": HTML.write("<tr><td align=\"center\"><font color=\"FF7700\">You aren't eligible to add a<br1>subclass at this time.<br1>You must be a <font color=\"LEVEL\">Noblesse</font></td></tr>")
    HTML.write("<tr><td><br><br></td></tr></table><br>")
    HTML.write("<img src=\"L2UI.SquareGray\" width=220 height=1>")
    HTML.write("</center></body></html>")
    if getblocktime(st) == True : pass
    st.playSound("ItemSound3.sys_shortage")
    return HTML.getvalue()

def ReloadConfig(st) :
	try:
		if QuestManager.getInstance().reload(QuestId): st.player.sendMessage("The script and settings have been reloaded successfully.")
		else: st.player.sendMessage("Script Reloaded Failed. you edited something wrong! :P, fix it and restart the server")
	except: st.player.sendMessage("Script Reloaded Failed. you edited something wrong! :P, fix it and restart the server")
	return MainHtml(st)

def getblocktime(st):
    if Block == True and not st.player.isGM() :
        endtime = int(System.currentTimeMillis()/1000) + BlockTime
        st.set("time",`endtime`)
        st.getPlayer().sendPacket(SetupGauge(3, BlockTime * 1000 + 300))
    val = True
    return val

def allowaddsub(st):
    if st.player.getActiveEnchantItem() != None:
        st.player.sendMessage("Cannot add subclass while Enchanting")
        st.playSound("ItemSound3.sys_shortage")
        st.player.sendPacket(ActionFailed.STATIC_PACKET)
        return False
    st.player.getInventory().updateDatabase()
    if st.player.getPrivateStoreType() != 0:
        st.player.sendMessage("Cannot add subclass while trading")
        st.playSound("ItemSound3.sys_shortage")
        st.player.sendPacket(ActionFailed.STATIC_PACKET)
        return False
    if AttackStanceTaskManager.getInstance().getAttackStanceTask(st.player):
        st.playSound("ItemSound3.sys_shortage")
        st.player.sendPacket(ActionFailed.STATIC_PACKET)
        return False
    return True

def getVar(st,const):
    conn=L2DatabaseFactory.getInstance().getConnection()
    act = conn.prepareStatement("SELECT * FROM subclass_list WHERE player_id=%s" % getmultisubs(st))
    rs=act.executeQuery()
    val = "-1"
    if rs :
        rs.next()
        try : val = rs.getString(const)
        except : pass
    try : 
        rs.close()
        act.close()
        conn.close()
    except: pass
    return val

def getVarcharactersubs(st):
    conn=L2DatabaseFactory.getInstance().getConnection()
    act = conn.prepareStatement("SELECT * FROM subclass_list WHERE player_id=%s" % getmultisubs(st))
    rs=act.executeQuery()
    val = ""
    if rs :
        rs.next()
        for i in range(maxsubsindb + 1):
            try : val += "%s " % rs.getString("subclassid%s" % i)
            except : val += "%s " % st.player.getClassId().getId()
    try : 
        rs.close()
        act.close()
        conn.close()
    except: pass
    val+= "-1"
    return val

def getsubsammount(st):
    j=-1
    for i in range(maxsubsindb + 1):
        var = getVar(st,"subclassid%s" % i)
        if int(var) >= 0 and int(var) <= 136: 
            j+=1
    return j

def getclassname(case):
    try: val = CharTemplateTable.getInstance().getClassNameById(int(case))
    except: val = "0"
    return val

def getitemname(case):
    try: val =ItemTable.getInstance().createDummyItem(case).getItemName()
    except: val = "0"
    return val

def getparentclass(case):
    val=`case`
    if AllowThirdJob == False: 
        if ClassId.values()[case].getParent() != None:
            val = `ClassId.values()[case].getParent().ordinal()`
        else :  val = "-1"
    return val

def getmaxskilllevel(case):
	val = 0
	skill= EnchantGroupsTable.getInstance().getSkillEnchantmentBySkillId(case)
	if skill != None: val = skill.getBaseLevel()
	return val

def getmultisubs(st):
    val= "%s LIMIT 1" % st.getPlayer().getObjectId()
    if AllowMultiSubs == True:
        val= "%s AND sub_index=%s LIMIT 1" %(st.player.getObjectId(),st.player.getClassIndex())
    return val

def AIO():
    xe="l";xf="e";xg="n";xa="B";xb="y";xc=" ";xd="A"; val= "%(xa)s%(xb)s%(xc)s%(xd)s%(xe)s%(xe)s%(xf)s%(xg)s" % locals()
    return val

def resetskills(st):
    player= st.player
    parametros = "\"-1\""; j=-1
    subs=getVarcharactersubs(st)
    SubSplit = subs.split(" ")
    for k in range(maxsubsindb + 1):
        if int(SubSplit[int(k)]) >= 0 and int(SubSplit[int(k)]) <= 136: j+=1
        if int(SubSplit[int(k)]) >= 0 and int(SubSplit[int(k)]) <= 136 and SubsNumber >= j:
            xclassid = int(SubSplit[int(k)])
            while (xclassid != -1) :
                parametros+=",\"%s\"" % xclassid
                if ClassId.values()[xclassid].getParent() != None:
                    xclassid = ClassId.values()[xclassid].getParent().ordinal()
                else :  xclassid = -1

    conn=L2DatabaseFactory.getInstance().getConnection()
    listskillid = conn.prepareStatement("SELECT * FROM skill_trees WHERE class_id IN (%s) AND min_level <= \"85\" ORDER BY skill_id DESC, level DESC" % parametros)
    lis=listskillid.executeQuery()
    cskill = 0; sB = []
    while (lis.next()) :
        try :
            xskill = lis.getInt("skill_id")
            if xskill != cskill :
                cskill = xskill
                sB.append(SkillTable.getInstance().getInfo(xskill, lis.getInt("level")))
        except : pass
    try:
        lis.close()
        listskillid.close()
    except: pass
    skills_exceptions = conn.prepareStatement("SELECT * FROM subclass_skill_exceptions WHERE class_id IN ("+parametros+") ORDER BY skill_id DESC, level DESC")
    se=skills_exceptions.executeQuery()
    while (se.next()) :
        try :
            xskill = se.getInt("skill_id")
            if xskill != cskill :
                cskill = xskill
                sB += [SkillTable.getInstance().getInfo(xskill, se.getInt("level"))]
        except : pass
    try : 
        skills_exceptions.close()
        se.close()
        conn.close()
    except : pass
    try:
        for s in player.getAllSkills():
            i = len(sB); j = 0; temp = 0
            if s.getId() > 7028 and s.getId() < 7065 and player.isGM():
                temp=1; j=i+1
            elif s.getId() > 1311 and s.getId() <= 1316:
                temp=1; j=i+1
            elif s.getId() > 1367 and s.getId() <= 1373:
                temp=1; j=i+1
            elif s.getId() > 630 and s.getId() <= 662:
                temp=1; j=i+1
            elif s.getId() > 798 and s.getId() <= 804:
				temp=1; j=i+1
            elif s.getId() > 1488 and s.getId() <= 1491:
                temp=1; j=i+1
            while j < i:
                sE = sB[j]
                if sE.getId() == s.getId():
                    temp=1
                    if  s.getLevel() < 100 : 
                        if sE.getLevel() < s.getLevel(): 
                            player.removeSkill(s)
                            player.addSkill(sE, True)
                            player.sendMessage("You got fixed %s Skill." % sE.getName())
                    else:
                        if sE.getLevel() < getmaxskilllevel(sE.getId()): 
                            player.removeSkill(s)
                            player.addSkill(sE, True)
                            player.sendMessage("You got fixed %s Skill." % sE.getName())
                j+=1
            if temp == 0 : player.removeSkill(s)
    except: player.sendMessage("You dont have skills to remove")
    return 0

class Quest (JQuest) :

    def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
    
    def onAdvEvent (self,event,npc,player) :
        try: st = player.getQuestState(QI)
        except: return
        eventSplit = event.split(" ")
        event = eventSplit[0]
        event1 = eventSplit[1]
        
        if event == "reloadscript":
            if event1 == "1": return ReloadConfig(st)
            if event1 == "0": return MainHtml(st)
        if event == "escraza": return MainHtmlIV(st,event1)
        if event == "confirm":
            if int(event1) >= 88: return Confirmation(st,"1",getparentclass(int(event1)),event1)
            else: return Confirmation(st,"3","deletesub",event1)
        if event == "gethtml":
            if event1 == "1": 
                if player.getRace().ordinal() == 5 and AllowAllSubsToKamael == False : return MainHtmlIV(st,"5")
                if AllowAllSubs == False : 
                    if AllowMutantRetail == False and player.isSubClassActive(): return MainHtmlIV(st,`player.getTemplate().race.ordinal()`)
                    else: return MainHtmlIV(st,`player.getRace().ordinal()`)
                else: return MainHtmlI(st)
            if event1 == "2": return MainHtmlII(st)
            if event1 == "3": return MainHtmlIII(st)
            return
        
        if event == "dorestart":
            if player.isTeleporting():
                player.abortCast()
                player.setIsTeleporting(false)
            if player.getActiveRequester() != None:
                player.getActiveRequester().onTradeCancel(player)
                player.onTradeCancel(player.getActiveRequester())
            if player.isFlying():
                player.removeSkill(SkillTable.getInstance().getInfo(4289, 1))
                st.exitQuest(1)
            client = player.getClient()
            player.setClient(None)
            player.deleteMe()
            client.setActiveChar(None)
            client.setState(GameClientState.AUTHED)
            client.sendPacket(RestartResponse.valueOf(True))
            cl = CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1)
            client.sendPacket(cl)
            client.setCharSelection(cl.getCharInfo())
            return

        temp = getVar(st,"currentsub")
        temp2 = getVar(st,"sub_index")
        temp3 = `player.getClassIndex()`
        temp4 = `player.getClassId().getId()`

        if event == "camb":
            if temp2!=temp3: return errordeclasse(st,event1,"switch to")
            elif not allowaddsub(st): return
            elif st.getPlayer().getClassId().level() < 2 and AllowThirdJob == False or st.getPlayer().getClassId().level() < 3 and AllowThirdJob == True : return comunerrors(st,"5")
            elif st.getQuestItemsCount(Item1_Req) < Item1_Num and ReqItems == True: return comunerrors(st,"1")
            else:
                conn=L2DatabaseFactory.getInstance().getConnection()
                upd=conn.prepareStatement("UPDATE subclass_list SET subclassid%s=%s, currentsub=%s WHERE player_id=%s" % (temp,temp4,event1,getmultisubs(st)))
                try :
                    upd.executeUpdate()
                    upd.close()
                    conn.close()
                except :
                    try : conn.close()
                    except : pass
                if resetskills(st) == 1: pass
                tmpid = int(getVar(st,"subclassid"+event1))
                player.setTarget(player)
                player.setClassId(tmpid)
                if not player.isSubClassActive(): player.setBaseClass(tmpid)
                if ReqItems == True and not player.isGM(): st.takeItems(Item1_Req,Item1_Num)
                st.set("time",`int(System.currentTimeMillis()/1000) + RestartDelay`)
                st.getPlayer().sendPacket(SetupGauge(3, RestartDelay * 1000))
                self.startQuestTimer("dorestart 0", RestartDelay*1000, npc, player)
                AttackStanceTaskManager.getInstance().addAttackStanceTask(player)  
                return complete(st)

        if event == "deletesub":

            if temp2!=temp3: return errordeclasse(st,event1,"delete the")
            elif not allowaddsub(st): return
            elif st.getPlayer().getClassId().level() < 2 and AllowThirdJob == False or st.getPlayer().getClassId().level() < 3 and AllowThirdJob == True : return comunerrors(st,"5")
            elif st.getQuestItemsCount(Item3_Req) < Item3_Num and ReqItems == True and not player.isGM() : return comunerrors(st,"3")
            else:
                conn=L2DatabaseFactory.getInstance().getConnection()
                upd=conn.prepareStatement("UPDATE subclass_list SET subclassid%s=%s, currentsub=%s WHERE player_id=%s" % (temp,temp4,temp,getmultisubs(st)))
                try :
                    upd.executeUpdate()
                    upd.close()
                    conn.close()
                except :
                    try : conn.close()
                    except : pass

                if event1 == temp and getsubsammount(st) > 0:
                    j=0
                    for i in range(maxsubsindb + 1):
                        var = getVar(st,"subclassid%s" % i)
                        if int(var) >= 0 and int(var) <= 136 and j == 0 and `i` != temp: 
                            j+=1; idsubclass = var; temp = `i`
                    player.setTarget(player)
                    player.setClassId(int(idsubclass))
                    if not player.isSubClassActive(): player.setBaseClass(int(idsubclass))
                con=L2DatabaseFactory.getInstance().getConnection()
                if getsubsammount(st) <= 1: rem=con.prepareStatement("DELETE FROM subclass_list WHERE player_id=%s" % getmultisubs(st))
                else: rem = con.prepareStatement("UPDATE subclass_list SET subclassid%s=-1 ,currentsub=%s WHERE player_id=%s" % (event1,temp,getmultisubs(st)))
                try : rem.executeUpdate()
                except : pass
                try : 
                    rem.close()
                    con.close()
                except : pass
                if resetskills(st) == 1: pass
                if ReqItems == True and not player.isGM(): st.takeItems(Item3_Req,Item3_Num)
                st.set("time",`int(System.currentTimeMillis()/1000) + RestartDelay`)
                st.getPlayer().sendPacket(SetupGauge(3, RestartDelay * 1000))
                self.startQuestTimer("dorestart 0", RestartDelay*1000, npc, player)
                AttackStanceTaskManager.getInstance().addAttackStanceTask(player) 
                return errasecomplete(st)

        else:
            if temp2!=temp3 and getsubsammount(st) >= 0 : return errordeclasse(st,event1,event)
            elif not allowaddsub(st): return
            elif AllowOnlyNobles == True and not player.isGM() :
                if not player.isNoble() : return comunerrors(st,"6")
            elif st.getQuestItemsCount(Item2_Req) < Item2_Num and ReqItems == True and not player.isGM() : return comunerrors(st,"2")
            elif st.getPlayer().getLevel() < MinLevel and not player.isGM() : return comunerrors(st,"4")
            elif st.getPlayer().getClassId().level() < 2 and AllowThirdJob == False or st.getPlayer().getClassId().level() < 3 and AllowThirdJob == True : return comunerrors(st,"5")
            else:
                if temp4 == event1 or temp4 == event: return errordeduplicado(st,event)
                else:
                    con=L2DatabaseFactory.getInstance().getConnection()
                    if getsubsammount(st) == -1 :
                        ins = con.prepareStatement("INSERT INTO subclass_list (player_id,currentsub,sub_index,subclassid0,subclassid1) VALUES (?,?,?,?,?)")
                        ins.setString(1, `player.getObjectId()`)
                        ins.setString(2, "1")
                        ins.setString(3, `player.getClassIndex()`)
                        ins.setString(4, temp4)
                        ins.setString(5, event)
                    else:
                        temp6 = "-1"; j=0
                        for i in range(maxsubsindb + 1):
                            var = getVar(st,"subclassid%s" % i)
                            if var == event1 or var == event: return errordeduplicado(st,event)
                            if int(var) < 0 or int(var) > 136:
                                if temp6 == "-1" and j==0: 
                                    j+=1
                                    temp6 = `i`
                        ins = con.prepareStatement("UPDATE subclass_list SET subclassid%s=%s, subclassid%s=%s, currentsub=%s WHERE player_id=%s" % (temp6,event,temp,temp4,temp6,getmultisubs(st)))
                    try :
                        ins.executeUpdate()
                        ins.close()
                        con.close()
                    except : pass
                    if resetskills(st) == 1: pass
                    if ReqItems == True and not player.isGM() : st.takeItems(Item2_Req,Item2_Num)
                    if DecLevel == True and not player.isGM() :
                        pXp = player.getExp()
                        tXp = ExperienceTable.getInstance().getExpForLevel(NewLevel)
                        if pXp > tXp: player.removeExpAndSp(pXp - tXp, 0)
                    player.setTarget(player)
                    player.setClassId(int(event))
                    if not player.isSubClassActive(): player.setBaseClass(int(event))
                    st.set("time",`int(System.currentTimeMillis()/1000) + RestartDelay`)
                    st.getPlayer().sendPacket(SetupGauge(3, RestartDelay * 1000))
                    self.startQuestTimer("dorestart 0", RestartDelay*1000, npc, player)
                    AttackStanceTaskManager.getInstance().addAttackStanceTask(player) 
                    return complete(st)
    def onFirstTalk (self,npc,player):
        st = player.getQuestState(QI)
        if not st : st = self.newQuestState(player)
        if player.isGM(): 
            if ShowReloadScriptPanel == True: return MainHtmlV(st)
        if int(System.currentTimeMillis()/1000) > st.getInt("blockUntilTime"):return MainHtml(st)
        else:
            st.playSound("ItemSound3.sys_shortage")		
            return

QUEST = Quest(QuestId,QI,QuestDesc)

for npcId in NPC:
    QUEST.addStartNpc(npcId)
    QUEST.addFirstTalkId(npcId)
    QUEST.addTalkId(npcId)