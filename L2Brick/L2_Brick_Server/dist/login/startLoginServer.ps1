$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2 Brick - Login Server Console"
$LASTEXITCODE = -1
do
{
    switch ($LASTEXITCODE)
    {
        -1 { cls; "Starting L2 Brick Login Server."; break; }
        2 { cls; "Restarting L2 Brick Login Server."; break; }
    }
    java -Xms128m -Xmx256m  -cp "./../libs/*;l2brick_login.jar" l2.brick.loginserver.L2LoginServer
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