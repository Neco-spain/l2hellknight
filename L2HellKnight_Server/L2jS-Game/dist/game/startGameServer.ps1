$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2J - Game Server Console"

do
{
    switch ($LASTEXITCODE)
    {
        -1 { cls; "Starting L2J Game Server."; break; }
        2 { cls; "Restarting L2J Game Server."; break; }
    }
    ""
    # -------------------------------------
    # Default parameters for a basic server.
    java "-Djava.util.logging.manager=com.l2js.util.L2LogManager" -Xms1024m -Xmx1024m -cp "./../libs/*;l2js-game.jar" com.l2js.gameserver.GameServer
    # -------------------------------------
}
while ($LASTEXITCODE -like 2)

if ($LASTEXITCODE -like 1)
{
    "Server Terminated Abnormally";
}
else 
{
    "Server Terminated";
}