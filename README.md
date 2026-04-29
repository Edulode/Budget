# Budget App 

Una aplicación de gestión financiera personal moderna, minimalista y potente, diseñada para ayudarte a tomar el control total de tus ingresos y gastos con facilidad y estilo.

## Características Principales

### Análisis y Estadísticas Avanzadas
*   **Distribución por Categorías:** Gráficos circulares vibrantes que muestran exactamente en qué gastas o de dónde provienen tus ingresos.
*   **Historial Mensual:** Gráficas de barras comparativas (Ingresos vs. Gastos) de los últimos 6 meses para visualizar tu capacidad de ahorro.
*   **Tendencias Inteligentes:** Análisis automático del balance mensual con indicadores de salud financiera.

### Escaneo de Tickets (OCR)
*   **Registro Inteligente:** Utiliza la cámara para escanear tus tickets físicos.
*   **Extracción Automática:** Detección inteligente del nombre del comercio y el monto total real (distinguiendo entre total, pago y cambio) mediante Google ML Kit.

### Presupuestos Mensuales
*   **Límites por Categoría:** Define cuánto quieres gastar al mes en alimentación, entretenimiento, etc.
*   **Seguimiento en Tiempo Real:** Barras de progreso visuales que cambian de color (Verde/Naranja/Rojo) según tu nivel de gasto.

### Gestión Integral
*   **Transacciones:** Registro rápido de ingresos y gastos con soporte para notas y fechas personalizadas.
*   **Categorías Dinámicas:** Sistema de categorías predefinidas y personalizables separadas por tipo de transacción.
*   **Formato de Moneda:** Entrada de datos profesional con formato de dinero en tiempo real.

### Diseño Premium y Minimalista
*   **Interfaz Neutra:** Colores elegantes y tipografía clara para una experiencia libre de distracciones.
*   **Modo Oscuro:** Soporte nativo completo para una visualización cómoda en ambientes de poca luz.
*   **Adaptabilidad:** Diseño optimizado para pantallas con notch y diferentes relaciones de aspecto.

## Stack Tecnológico

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/)
*   **Arquitectura:** MVVM (Model-View-ViewModel) con Repository Pattern.
*   **Base de Datos:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
*   **UI:** Material Design 3, View Binding, ConstraintLayout.
*   **Navegación:** Jetpack Navigation Component.
*   **OCR & Cámara:** Google ML Kit Text Recognition + CameraX.
*   **Gráficas:** [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
*   **Async:** Kotlin Coroutines & Flow.

## Estructura

*   **Principal:** Resumen de balance y lista de transacciones recientes.
*   **Estadísticas:** Panel de control visual con filtros de ingresos/gastos.
*   **Presupuesto:** Monitor de límites mensuales y ahorro.
*   **Escáner:** Interfaz de captura rápida de tickets.

## ⚙️ Instalación y Requisitos

1.  Clonar el repositorio.
2.  Abrir el proyecto en **Android Studio (Iguana o superior)**.
3.  Sincronizar Gradle (Versión 8.10.2).
4.  Ejecutar en un dispositivo con Android 7.0 (API 24) o superior.

---
Desarrollado  para el control financiero inteligente.
