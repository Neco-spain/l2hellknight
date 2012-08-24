$OutputEncoding = New-Object -typename System.Text.UTF8Encoding
$a = (Get-Host).UI.RawUI
$a.WindowTitle = "L2J - Register Game Server"
java "-Djava.util.logging.config.file=console.cfg" -cp "./../libs/*;l2hellknightloghin.jar" l2.hellknight.tools.gsregistering.BaseGameServerRegister -c