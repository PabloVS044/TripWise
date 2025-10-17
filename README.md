# TripWise (Android) – Descubrimiento y reservas de alojamientos

> Aplicación Android nativa (Kotlin + Jetpack Compose) para **descubrir** alojamientos sobre mapa, **filtrar** en tiempo real, **gestionar** propiedades (admin/host) y **iniciar** el flujo de reserva.

---

## Tabla de contenidos
- [Resumen](#resumen)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura de actividades y navegación](#estructura-de-actividades-y-navegación)
- [Funciones principales](#funciones-principales)
    - [Descubrimiento y filtros](#descubrimiento-y-filtros)
    - [Autenticación y roles](#autenticación-y-roles)
    - [Gestión de propiedades (admin)](#gestión-de-propiedades-admin)
    - [Reservas](#reservas)
- [Modelos de datos y repositorios](#modelos-de-datos-y-repositorios)
- [Capa de red (API Services)](#capa-de-red-api-services)
- [Configuración & permisos](#configuración--permisos)
- [Requisitos y preparación](#requisitos-y-preparación)
- [Estructura de paquetes (alto nivel)](#estructura-de-paquetes-alto-nivel)
- [Estados y ViewModels](#estados-y-viewmodels)
- [Recursos de UI](#recursos-de-ui)
- [Roadmap](#roadmap)
- [Contribución](#contribución)
- [Licencia](#licencia)

---

## Resumen
TripWise es una app móvil que combina **búsqueda geográfica de alojamientos**, **filtros en tiempo real** y un **flujo básico de reservas**, con **autenticación** y **navegación basada en roles** (usuario, anfitrión y administrador). Está construida con **MVVM**, **Jetpack Compose** y **StateFlow** para estado reactivo.

---

## Arquitectura
- **MVVM + Repository Pattern** para desacoplar UI, lógica y acceso a datos.
- **StateFlow** en los ViewModels para exponer estado inmutable a la UI.
- **Retrofit** + **OkHttp** para comunicación HTTP y **Gson** para (de)serialización.
- **Google Maps** (Compose) para visualización geográfica.
- Capas y entidades principales:
    - **Modelos**: `ApiProperty` (red), `Property` (interno), `Post` (UI).
    - **Network Layer**: `RetrofitInstance`, `UserApiService`, `PropertyApiService`.
    - **Repository**: `propertyRepository`.
    - **ViewModel**: `PropertyViewModel`.
    - **UI**: Activities con Compose (mapas, tarjetas, formularios).

Diagrama lógico (alto nivel):

```
Data Models      Network Layer            Repository            ViewModel              UI Layer
(ApiProperty,    RetrofitInstance   ->    propertyRepository -> PropertyViewModel ->  Activities/Compose
 Property, Post) UserApi/PropertyApi                                                    (Map, Cards, Forms)
```

---

## Tecnologías
- **Kotlin**, **Jetpack Compose (Material 3)**, **Kotlin Coroutines**, **StateFlow**.
- **Retrofit**, **OkHttp**, **Gson**.
- **Google Maps Compose** para mapa y marcadores.
- **Coil** (AsyncImage) para carga de imágenes remotas.
- **Android Manifest** con navegación segura (exported) y **Google Maps API Key** externalizada.

---

## Estructura de actividades y navegación
Patrón **hub-and-spoke**: tras autenticación, el usuario es enrutado según su rol.

**Activities registradas (extracto):**
- **Launcher**: `MainActivity` (exported=true).
- **Auth**: `LoginActivity`, `RegisterActivity`, `ForgotPasswordActivity`.
- **User**: `DiscoverActivity`, `FilterActivity`, `ReservationPage1Activity`.
- **Admin**: `UsersActivity`, `PropertiesActivity`.
- **Owner/Host**: `MainHostActivity`.

**Reglas de seguridad**: Solo `MainActivity` está exportada; el resto usan `android:exported="false"` para impedir lanzamientos externos.

---

## Funciones principales

### Descubrimiento y filtros
- **DiscoverActivity** muestra un **GoogleMap** con marcadores por propiedad; al tocar un marcador se carga el detalle y se muestra una **PropertyCard**.
- **Filtros avanzados** mediante `FilterActivity` → `Intent extras` a `DiscoverActivity`:
    - `name`, `location`, `minPrice`, `maxPrice`, `capacity`, `propertyType`, `approved`.
- **Lógica de filtrado (cliente)**: coincidencia por nombre/ubicación (case-insensitive), rango de precios, capacidad mínima, tipo y estado de aprobación.
- **Galería de imágenes** en la tarjeta usando **Coil**.
- **BottomNavigationBar** en Discover: Buscar (seleccionado), Reserva, Filtros, Perfil (pendiente/disabled).

### Autenticación y roles
- **Entradas** desde `MainActivity`: botones *Iniciar Sesión* y *Comenzar Aventura* (registro).
- **Login** vía API con manejo de errores estándar (401/404 y errores de red).
- **Navegación por rol** tras login:
    - `user` → `DiscoverActivity`
    - `owner` → `MainHostActivity`
    - `admin` → `UsersActivity`
- **Registro multistep**: incluye selección de intereses y configuración de propiedad (para propietarios).

### Gestión de propiedades (admin)
- `PropertiesActivity` con `PropertiesScreen` (Compose):
    - Estados: `isLoading`, `isError`, `isRefreshing`, `properties`, `filteredProperties`.
    - **Búsqueda** en tiempo real (LaunchedEffect sobre query).
    - **Acciones**: Deshabilitar/Eliminar propiedad (soft delete) y ver detalles.
    - **Chip de estado**: Approved (azul), Pending (naranja), Rejected (rojo).

### Reservas
- `ReservationPage1Activity` con `ReservaScreen` (Compose + Scaffold).
- Captura **fechas** (check-in/out) y **número de viajeros** (+/–), muestra tarjeta con datos del viaje/propiedad.
- Navegación inferior con accesos a Discover y (placeholder) Perfil.

---

## Modelos de datos y repositorios

### Modelos principales
- **`Post`** (UI): representación completa de la propiedad. Campos frecuentes (ejemplo no exhaustivo):
    - `_id: String` (Mongo ObjectId), `name: String`, `description: String`, `location: String`,
    - `pricePerNight: Int/Double`, `capacity: Int`, `pictures: List<String>`, `amenities: List<String>`,
    - `propertyType: String`, `owner: String`, `approved: String`, `reviews: List<String>`,
    - `latitude: Double`, `longitude: Double`, `createdAt: String`, `isDeleted: Boolean`.
- **`PropertyDeleted`**: soft delete (`is`, `at`).

> Nota: El proyecto utiliza también `ApiProperty` (modelo de red) y `Property` (modelo interno).

### Repository Pattern
- `propertyRepository` abstrae llamadas a `PropertyApiService` y ofrece:
    - `suspend fun getProperties(): List<Post>`
    - `suspend fun getPropertyById(id: String): Post`
- Sienta las bases para caching, transformación, validación y manejo de errores centralizado.

---

## Capa de red (API Services)
- **RetrofitInstance** mantiene la configuración HTTP (OkHttp + LoggingInterceptor + Gson).
- **Servicios**:
    - `UserApiService` (usuarios, login y algunas operaciones de propiedades).
    - `PropertyApiService` (propiedades orientadas a UI → devuelve `Post`).

**Base URL (backend TripWise)**  
Configurada en `RetrofitInstance` (ver código del proyecto).

> Sugerencia: ajusta la `baseUrl` si apuntas a otra instancia del backend.

---

## Configuración & permisos
**AndroidManifest**:
- **Permisos**: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`.
- **Google Maps API Key** externalizada (sin hardcode):
  ```xml
  <meta-data
      android:name="com.google.android.geo.API_KEY"
      android:value="${MAPS_API_KEY}" />
  ```
- **Seguridad**: solo `MainActivity` `exported=true`. El resto `exported=false`.
- **Tema**: `@style/Theme.TripWise` para consistencia visual.
- **Target API**: 31 (Android 12).

---

## Requisitos y preparación
1. **Android Studio** (versión reciente recomendada).
2. **Clonar** el repositorio y **abrir** el proyecto.
3. **Configurar Google Maps**: define `MAPS_API_KEY` como variable de build (por ejemplo, en `gradle.properties` o variables de entorno). El Manifest ya referencia `${MAPS_API_KEY}`.
4. **Sincronizar Gradle** y **ejecutar** en emulador o dispositivo físico.
5. (Opcional) **Cambiar Base URL** del backend en `RetrofitInstance` si corresponde.

---

## Estructura de paquetes (alto nivel)
```
uvg.edu.tripwise/
  auth/                 # Login, registro, recuperación y steps (intereses, propiedad)
  discover/             # Discover + filtros + mapa + tarjeta de propiedad
  reservation/          # Flujo de reserva (pantalla 1)
  user/, host/          # Dashboards por rol
  data/                 # Modelos y repositorios
  network/              # Retrofit y servicios API
  viewModel/            # PropertyViewModel y estado
  ui/                   # Componentes/temas (según organización)
  MainActivity.kt       # Landing y navegación a auth
  PropertiesActivity.kt # Admin: gestión de propiedades
  UsersActivity.kt      # Admin: gestión de usuarios
```
*(La organización exacta puede variar según el commit actual.)*

---

## Estados y ViewModels
- **`PropertyViewModel`** expone:
    - `properties: StateFlow<List<Post>>`
    - `selectedProperty: StateFlow<Post?>`
- Operaciones:
    - `loadProperties()` → carga lista.
    - `getPropertyById(id)` → detalla y selecciona propiedad.
    - `clearSelectedProperty()` → limpia selección.
- Integración con Compose mediante `collectAsState()` para recomposición automática.

---

## Recursos de UI
- **Material 3** (temas, tipografía, colores).
- **Iconos** principales (Search, Luggage, FilterAlt, Person) para navegación inferior.
- **Coil** para imágenes (galería en `PropertyCard`).
- **Asset Wizard** de Android Studio para gestionar drawables y recursos.

---

## Roadmap
- Completar pantalla **Perfil** y navegación asociada.
- Extender flujo de **Reserva** (confirmación, integración con backend, pagos, etc.).
- Habilitar **My Location** y permisos en mapa cuando aplique.
- Añadir **tests** de UI y unitarios para filtros y navegación por rol.
- Dashboard de **admin** pendiente (p. ej., item “Dashboard” en bottom navigation de propiedades).

---

## Contribución
- Estándar de PRs y ramas: abrir **feature branches** y describir alcance y motivación del cambio.
- Mantener consistencia con la arquitectura (MVVM + Repository) y patrones de estado (StateFlow).
- Documentar endpoints nuevos en `ApiService.kt` y reflejar cambios en el README si aplica.


