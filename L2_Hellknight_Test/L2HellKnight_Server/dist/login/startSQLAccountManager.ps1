$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2 HellKnight - SQL Account Manager"
java "-Djava.util.logging.config.file=console.cfg" -cp "./../libs/*;hellknight_login.jar" l2.hellknight.tools.accountmanager.SQLAccountManager 2> $null
if ($LASTEXITCODE -like 0)
{
    ""
    "Execution succesful"
    ""
}
else
{
    ""
    "An error has ocurred while running the L2 HellKnight Account Manager!"
    ""
    "Possible reasons for this to happen:"
    ""
    "Missing .jar files or ../libs directory."
    "- MySQL server not running or incorrect MySQL settings:"
    "   check ./config/loginserver.properties"
    "- Wrong data types or values out of range were provided:"
    "   specify correct values for each required field"
    ""
}
