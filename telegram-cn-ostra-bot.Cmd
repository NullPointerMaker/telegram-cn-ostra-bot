@Echo Off
@Rem Command script launcher with pause at exit
SetLocal EnableDelayedExpansion
Title %~n0
CD /D "%~dp0"

If /I "%1"=="Minimized" (
    Start /Min Cmd /C "%~dpnx0 PauseErrorOnly"
    Exit
)

If Exist %~dp0.Bat (
    Rem Batch
    %~dp0.Bat
) Else If %~dp0.Exe (
    Rem Executable
    %~dp0.Exe
) Else If %~dp0.JAr (
    Rem Java
    Java -jar %~dp0.JAr
) Else If %~dp0.Py (
    Rem Python
    Python -u %~dp0.Py
) Else (
    Echo Program file not found!
    Set ErrorLevel=1
)

Set ExitCode=!ErrorLevel!
Echo.
If /I "!ExitCode!"=="0" (
    If Not "%1"=="PauseErrorOnly" (
        Pause
    )
) Else (
    Echo Exit Code: !ExitCode! 1>&2
    Echo.
    Pause
)
Exit !ExitCode!