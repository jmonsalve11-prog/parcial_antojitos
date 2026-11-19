#!/usr/bin/env bash
# Instala y abre la app en TODOS los dispositivos conectados (USB, WiFi o emuladores).
# Uso:  ./scripts/instalar_en_todos.sh
# Antes: Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s)
APK="app/build/outputs/apk/debug/app-debug.apk"
PKG="com.antojitos.app"

if [ ! -f "$APK" ]; then echo "No existe $APK. Genera el APK primero en Android Studio."; exit 1; fi

adb start-server
echo "Dispositivos detectados:"; adb devices

for d in $(adb devices | awk 'NR>1 && $2=="device"{print $1}'); do
  echo ">>> Instalando en $d"
  adb -s "$d" install -r "$APK" && adb -s "$d" shell am start -n "$PKG/.MainActivity"
done
