# Antojitos 💜 — bloc de notas de antojos y control de gastos

App nativa Android con vistas XML (plantilla *Empty Views Activity*). **Java** para la interfaz (Activities, Fragments, Adapter) y **Kotlin** para los datos (Room, repositorio, utilidades). minSdk 24 (Android 7.0) / targetSdk 34.

## Modalidades
1. **Antojos**: guardar nombre, precio estimado, prioridad, categoría, foto y enlace (opcionales). Marcar como comprado.
2. **Ahorros**: total consumido de lo ya comprado, filtrable por categoría y rango de precios.

## Abrir el proyecto
`File > Open` en Android Studio (Koala o superior) y elegir la carpeta `Antojitos`. Esperar el *Gradle Sync*.

## Probar en 3 emuladores
`Tools > Device Manager > Create Device`:
| # | Dispositivo | API |
|---|-------------|-----|
| 1 | Pixel 4 (pantalla pequeña) | 30 (Android 11) |
| 2 | Pixel 7 (pantalla media) | 34 (Android 14) |
| 3 | Pixel Tablet o Pixel Fold (pantalla grande) | 34 |

Con los tres encendidos, en la barra superior elige cada uno y pulsa ▶ Run, o usa el script `scripts/instalar_en_todos`.

## Conectar un teléfono físico por USB
1. Teléfono: *Ajustes > Acerca del teléfono > Número de compilación* (tocar 7 veces) > activar **Depuración USB** en *Opciones de desarrollador*.
2. Conectar el cable y aceptar "¿Permitir depuración USB?".
3. En la terminal (Android Studio: *View > Tool Windows > Terminal*):
```
adb devices                 # debe aparecer el teléfono como "device"
adb -s <SERIAL> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <SERIAL> shell am start -n com.antojitos.app/.MainActivity
```
Si `adb` no se reconoce, está en `<Android SDK>/platform-tools/` (Windows: `%LOCALAPPDATA%\Android\Sdk\platform-tools`).

### Por WiFi (Android 11+, sin cable)
```
adb pair <IP>:<PUERTO_EMPAREJAMIENTO>     # Opciones de desarrollador > Depuración inalámbrica > Vincular
adb connect <IP>:<PUERTO>
```
### Por WiFi (Android 10 o menos, empezando con cable)
```
adb tcpip 5555
adb connect <IP_DEL_TELEFONO>:5555
```

## Generar el APK
`Build > Build Bundle(s) / APK(s) > Build APK(s)` → `app/build/outputs/apk/debug/app-debug.apk`

## Subir a GitHub
```
git init
git add .
git commit -m "Antojitos v1.0"
git branch -M main
git remote add origin https://github.com/<usuario>/Antojitos.git
git push -u origin main
```

## Estructura
```
app/src/main/java/com/antojitos/app/
├── MainActivity.java        (Java)   barra inferior Antojos / Ahorros
├── WishesFragment.java      (Java)   modalidad 1: registro de antojos
├── SavingsFragment.java     (Java)   modalidad 2: control de ahorros + filtros
├── WishFormActivity.java    (Java)   crear / editar antojo (foto, enlace, prioridad)
├── WishAdapter.java         (Java)   lista RecyclerView
├── UiUtils.java             (Java)
└── data/
    ├── Wish.kt              (Kotlin) entidad Room
    ├── WishDao.kt           (Kotlin) consultas
    ├── AppDatabase.kt       (Kotlin) base de datos
    ├── WishRepository.kt    (Kotlin) acceso a datos
    └── Formats.kt           (Kotlin) categorías, prioridades, formato de moneda
app/src/main/res/layout/     (XML)    pantallas
```
