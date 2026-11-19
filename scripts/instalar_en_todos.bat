@echo off
REM Instala y abre la app en TODOS los dispositivos conectados (USB, WiFi o emuladores).
REM Antes: Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s)
set APK=app\build\outputs\apk\debug\app-debug.apk
set PKG=com.antojitos.app

if not exist %APK% (
  echo No existe %APK%. Genera el APK primero en Android Studio.
  exit /b 1
)

adb start-server
echo Dispositivos detectados:
adb devices

for /f "skip=1 tokens=1,2" %%a in ('adb devices') do (
  if "%%b"=="device" (
    echo ^>^>^> Instalando en %%a
    adb -s %%a install -r %APK%
    adb -s %%a shell am start -n %PKG%/.MainActivity
  )
)
