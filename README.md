# SportStore — Sistema de Ventas de Productos Deportivos

Aplicación de escritorio desarrollada en **Java 17** con **Java Swing** para la gestión de inventario (CRUD de productos deportivos), punto de venta e historial de ventas, con persistencia local en archivos CSV.

## Características principales

- **CRUD de Productos Deportivos (`Productos (CRUD)`)**:
  - Crear, consultar, actualizar y eliminar productos.
  - Búsqueda en tiempo real por código SKU, nombre o marca, y filtro por categoría.
  - Validaciones de negocio (código SKU único, precio mayor a 0, stock no negativo, campos obligatorios).
  - Indicador visual de stock bajo (≤ 5 unidades) y estado activo/inactivo.
- **Punto de Venta (`Punto de Venta`)**:
  - Selección de productos activos con stock disponible.
  - Carrito de compras con cálculo automático de subtotal, IVA (16%) y total.
  - Registro de cliente y método de pago (Efectivo, Tarjeta de Débito, Tarjeta de Crédito, Transferencia).
  - Descuento automático de inventario al confirmar la venta.
- **Historial de Ventas (`Historial de Ventas`)**:
  - Consulta de ventas registradas con folio, fecha, cliente, método de pago y totales.
  - Visualización del detalle de productos por venta.
  - Anulación de ventas con restauración automática del stock.
- **Persistencia local**:
  - Almacenamiento automático en `./data/productos.csv`, `./data/ventas.csv` y `./data/venta_detalles.csv`.
  - Carga inicial de 8 productos deportivos de ejemplo si `data/productos.csv` no existe.

## Requisitos

- **Java JDK 17** o superior (`javac` y `java`).
- **Apache Maven 3.x** (opcional).

## Estructura del proyecto

```text
├── pom.xml
├── src
│   ├── main/java/com/sportstore
│   │   ├── App.java
│   │   ├── model
│   │   │   ├── CategoriaProducto.java
│   │   │   ├── DetalleVenta.java
│   │   │   ├── Producto.java
│   │   │   └── Venta.java
│   │   ├── repository
│   │   │   ├── CsvHelper.java
│   │   │   ├── ProductoRepository.java
│   │   │   └── VentaRepository.java
│   │   ├── service
│   │   │   ├── ProductoService.java
│   │   │   ├── ValidationException.java
│   │   │   └── VentaService.java
│   │   └── ui
│   │       ├── HistorialVentasPanel.java
│   │       ├── MainFrame.java
│   │       ├── ProductosPanel.java
│   │       ├── PuntoVentaPanel.java
│   │       └── Theme.java
│   └── test/java/com/sportstore
│       └── SportStoreTests.java
```

## Compilación y ejecución

### Con `javac` y `java` (PowerShell)

1. **Compilar el código fuente**:
   ```powershell
   New-Item -ItemType Directory -Force -Path target\classes | Out-Null
   $mainFiles = Get-ChildItem -Recurse -Filter *.java src\main\java | ForEach-Object { $_.FullName }
   javac --release 17 -encoding UTF-8 -d target\classes $mainFiles
   ```

2. **Ejecutar la aplicación**:
   ```powershell
   java -cp target\classes com.sportstore.App
   ```

3. **Compilar y ejecutar las pruebas**:
   ```powershell
   New-Item -ItemType Directory -Force -Path target\test-classes | Out-Null
   $testFiles = Get-ChildItem -Recurse -Filter *.java src\test\java | ForEach-Object { $_.FullName }
   javac --release 17 -encoding UTF-8 -cp target\classes -d target\test-classes $testFiles
   java -ea -cp "target\classes;target\test-classes" com.sportstore.SportStoreTests
   ```

### Con Maven

```powershell
mvn compile
mvn package
java -jar target/sistema-ventas-deportivas-1.0.0.jar
```
