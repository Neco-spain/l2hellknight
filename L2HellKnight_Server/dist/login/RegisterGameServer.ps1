$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2 HellKnight - Register Game Server"
java "-Djava.util.logging.config.file=console.cfg" -cp "./../libs/*;l2hellknight_login.jar" l2.hellknight.tools.gsregistering.BaseGameServerRegister -c