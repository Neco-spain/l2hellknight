$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2 Brick - Register Game Server"
java "-Djava.util.logging.config.file=console.cfg" -cp "./../libs/*;l2brick_login.jar" l2.brick.tools.gsregistering.BaseGameServerRegister -c