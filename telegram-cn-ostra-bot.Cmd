@Echo Off
SetLocal EnableDelayedExpansion
Title %~n0
CD /D "%~dp0"

If /I "%1"=="Minimized" (
    Start /Min Cmd /C "%~dpnx0 PauseError"
    Exit
)

Java -jar %~dp0.jar
Set ExitCode=!ErrorLevel!
Echo.
If "!ExitCode!"=="0" (
    If Not "%1"=="PauseError" (
        Pause
    )
) Else (
    Echo Exit Code: !ExitCode! 1>&2
    Echo.
    Pause
)
Exit !ExitCode!