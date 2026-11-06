# Saludablemente Mobile

Angular + Capacitor, conectado al mismo backend JWT que saludablemente-frontend.

## Desarrollo

```powershell
npm ci
npm start
```

Vista previa: http://localhost:4201. Tanto desarrollo como producción y el APK debug usan https://saludablemente-erp.onrender.com, igual que el frontend Angular. No hace falta un backend local ni ADB reverse. La URL pública de MinIO debe configurarse si se necesita convertir rutas relativas de medios.

## Autenticación

El formulario envía identificador y password a POST /api/v1/auth/login.
La respuesta contiene token, email, nombre, roles y expiraEn (segundos Unix).
La sesión se conserva en localStorage como en el frontend y el JWT se envía como Bearer únicamente a la API configurada. Al vencer, se requiere iniciar sesión de nuevo. El backend no ofrece renovación de tokens.
Cerrar sesión llama POST /api/v1/auth/logout, descarta la sesión local y limpia exportaciones temporales. No utiliza proveedores externos de identidad.

## Android

Requiere JDK 21 y SDK Android 36 con build-tools 35.0.0 y licencias instaladas.
Los scripts validan JAVA_HOME y las instalaciones habituales; el SDK se busca en ANDROID_HOME, ANDROID_SDK_ROOT, android/local.properties y carpetas del usuario.

```powershell
npm run android:debug
```

Genera android/app/build/outputs/apk/debug/app-debug.apk conectado a Render. Ejecuta sincronización y Gradle en secuencia para evitar bloqueos de archivos.
Para recursos de producción: npm run android:sync.

## Verificación

```powershell
npm test
npm run build
```

La compilación no sustituye las pruebas de acceso con una cuenta real y el backend disponible.
