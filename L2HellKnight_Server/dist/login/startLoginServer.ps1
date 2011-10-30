$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2HellKnight - Login Server Console"
$LASTEXITCODE = -1
do
{
    switch ($LASTEXITCODE)
    {
        -1 { cls; "Starting L2HellKnight Login Server."; break; }
        2 { cls; "Restarting L2HellKnight Login Server."; break; }
    }
    java -Xms128m -Xmx128m  -cp "./../libs/*;l2hellknight_login.jar" l2.hellknight.loginserver.L2LoginServer
}
while ($LASTEXITCODE -like 2)

if ($LASTEXITCODE -like 1)
{
    "Server Terminate Abnormally";
}
else 
{
    "Server Terminated";
}