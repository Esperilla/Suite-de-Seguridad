# 🔐 Suite de Seguridad - Java POO

[![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk)](https://openjdk.org/)
[![Estado](https://img.shields.io/badge/Estado-En%20Desarrollo-yellow.svg)]()

Una aplicación de consola en Java que proporciona herramientas de seguridad para la gestión de contraseñas y cifrado de archivos, desarrollada aplicando principios de **Programación Orientada a Objetos**.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Requisitos](#-requisitos)
- [Instalación](#-instalación)
- [Uso](#-uso)
- [Arquitectura](#-arquitectura)
- [Seguridad](#-seguridad)
- [Estructura del Proyecto](#-estructura-del-proyecto)

---

## ✨ Características

### 🔑 Gestor de Contraseñas (Bóveda)
- **Almacenamiento seguro** de secretos cifrados con AES-256-GCM
- **Búsqueda inteligente** con algoritmo de Levenshtein (tolerancia a errores tipográficos)
- Operaciones CRUD completas (Crear, Leer, Actualizar, Eliminar)
- **Política de contraseñas robusta** (mínimo 8 caracteres, mayúsculas, números y símbolos)
- Cambio de contraseña maestra con re-cifrado automático

### 🔒 Cifrador de Archivos
- Cifrado/descifrado de archivos individuales usando la contraseña maestra
- Extensión `.locked` para archivos cifrados
- Eliminación automática del archivo original por seguridad

### 📊 Sistema de Auditoría
- **Bitácora de eventos** con registro de todas las operaciones
- Niveles de severidad: INFO, WARN, ERROR
- Retención automática de logs (7 días)
- Sin registro de valores sensibles (solo nombres de secretos)

### 🛡️ Seguridad Implementada
- **AES-256-GCM** con IV aleatorio por operación
- Derivación de clave con SHA-256
- Entrada de contraseñas oculta en terminal
- Limpieza de contraseñas en memoria (`char[]`)
- Límite de intentos de autenticación

---

## 📦 Requisitos

- **Java 17** o superior
- **Terminal real** (PowerShell, CMD, Bash)
  > ⚠️ **No funciona desde terminales integradas de IDEs** debido al uso de `System.console()` para ocultar contraseñas

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/Esperilla/Suite-de-Seguridad.git
cd Suite-de-Seguridad
```

### 2. Compilar el proyecto

```powershell
# Windows (PowerShell)
javac -d out src/modelos/*.java src/modulos/*.java src/main/*.java
```

```bash
# Linux/Mac
javac -d out src/modelos/*.java src/modulos/*.java src/main/*.java
```

### 3. Ejecutar

```powershell
# Windows (PowerShell)
java -cp out main.Main
```

```bash
# Linux/Mac
java -cp out main.Main
```

---

## 💻 Uso

### Primera Ejecución

Al ejecutar por primera vez, se te pedirá crear una **contraseña maestra**:

```
--- SUITE DE SEGURIDAD (JAVA POO) ---

--- Configuración Inicial ---
Bienvenido. Crea una contraseña maestra.

Requisitos de la contraseña:
  • Mínimo 8 caracteres
  • Al menos una letra mayúscula
  • Al menos un número
  • Al menos un carácter especial (!@#$%^&* etc.)

Nueva contraseña: ********
Confirma contraseña: ********
¡Sistema configurado correctamente!
```

### Menú Principal

```
--- Menú Principal ---
1. Gestor de Contraseñas
2. Cifrador de Archivos
3. Salir
>
```

### Gestor de Contraseñas

```
--- Gestor de Contraseñas ---
1. Agregar secreto
2. Ver secreto
3. Listar secretos
4. Eliminar secreto
5. Actualizar secreto
6. Cambiar contraseña maestra
7. Guardar y Regresar
>
```

**Búsqueda inteligente**: No necesitas recordar el nombre exacto del secreto. El sistema encuentra coincidencias aunque cometas errores tipográficos:

```
Buscar secreto: gmai

✔ Secreto encontrado: Gmail
Valor: mi_contraseña_segura
```

### Cifrador de Archivos

```
--- Cifrador de Archivos ---
1. Cifrar archivo
2. Descifrar archivo
3. Regresar
>
```

> 💡 **Tip**: Usa `/` en lugar de `\` para las rutas de archivos:
> ```
> Ruta del archivo: C:/Users/usuario/documento.txt
> ```

---

## 🏗️ Arquitectura

El proyecto implementa una arquitectura modular basada en **Programación Orientada a Objetos**:

```
┌─────────────────────────────────────────────────────────┐
│                      Main.java                          │
│                   (Punto de entrada)                    │
└──────────────────────────┬──────────────────────────────┘
                           │
            ┌──────────────┴──────────────┐
            ▼                             ▼
┌───────────────────────┐     ┌───────────────────────┐
│    ModuloBoveda       │     │    ModuloArchivos     │
│  (Gestor Contraseñas) │     │  (Cifrador Archivos)  │
└───────────┬───────────┘     └───────────┬───────────┘
            │                             │
            └──────────────┬──────────────┘
                           ▼
              ┌─────────────────────────┐
              │      ModuloBase         │
              │   (Clase Abstracta)     │
              │   - Polimorfismo        │
              │   - Herencia            │
              └─────────────────────────┘
                           │
                           ▼
              ┌─────────────────────────┐
              │   UtilidadesCifrado     │
              │   - AES-GCM 256 bits    │
              │   - IV aleatorio        │
              └─────────────────────────┘
```

### Conceptos POO Aplicados

| Concepto | Implementación |
|----------|----------------|
| **Herencia** | `ModuloBase` → `ModuloBoveda`, `ModuloArchivos` |
| **Polimorfismo** | Método `ejecutar()` en cada módulo |
| **Abstracción** | Clase abstracta `ModuloBase` |
| **Encapsulación** | Atributos privados con getters |
| **Composición** | `Main` usa `Boveda` y `AlmacenamientoBoveda` |
| **Serialización** | `Boveda` implementa `Serializable` |

---

## 🔒 Seguridad

### Cifrado AES-GCM

El sistema utiliza **AES-256-GCM** (Galois/Counter Mode) para el cifrado:

| Característica | Descripción |
|----------------|-------------|
| **Algoritmo** | AES-256 |
| **Modo** | GCM (autenticación integrada) |
| **IV** | 12 bytes aleatorios por operación |
| **Tag de autenticación** | 128 bits |

### Política de Contraseñas

Las contraseñas maestras deben cumplir:
- ✅ Mínimo 8 caracteres
- ✅ Al menos una letra mayúscula
- ✅ Al menos un número
- ✅ Al menos un carácter especial (`!@#$%^&*()-_=+[]{}|;:',.<>?/~``)

### Protección contra Ataques

- **Fuerza bruta**: Límite de 3 intentos para cambio de contraseña
- **Memoria**: Contraseñas limpiadas con `Arrays.fill(arr, '\0')`
- **Replay**: IV único por cada operación de cifrado

---

## 📁 Estructura del Proyecto

```
Suite-de-Seguridad/
├── 📄 README.md                    # Este archivo
├── 📄 Suite-de-Seguridad.iml      # Configuración de IntelliJ IDEA
├── 📁 src/
│   ├── 📁 main/
│   │   └── 📄 Main.java            # Punto de entrada de la aplicación
│   ├── 📁 modelos/
│   │   ├── 📄 Boveda.java              # Almacén de secretos en memoria
│   │   ├── 📄 AlmacenamientoBoveda.java # Persistencia cifrada
│   │   ├── 📄 UtilidadesCifrado.java   # Funciones criptográficas
│   │   └── 📄 RegistroBitacora.java    # Sistema de logging
│   └── 📁 modulos/
│       ├── 📄 ModuloBase.java          # Clase abstracta base
│       ├── 📄 ModuloBoveda.java        # Gestor de contraseñas
│       └── 📄 ModuloArchivos.java      # Cifrador de archivos
├── 📁 out/                         # Archivos compilados
├── 📁 logs/                        # Archivos de bitácora
│   └── 📄 audit.log
└── 📄 boveda.dat                   # Bóveda cifrada (se genera al usar)
```

---

## 🎓 Contexto Académico

Este proyecto fue desarrollado como parte del curso de **Paradigmas de Programación**, demostrando:

- Aplicación práctica de POO en Java
- Implementación de criptografía moderna
- Diseño de software modular
- Buenas prácticas de programación segura

---

## 👤 Autor

- GitHub: [@Esperilla](https://github.com/Esperilla)
- GitHub: [@Yaz1621](https://github.com/Yaz1621)
- GitHub: [@JesusRodriguezCortes](https://github.com/JesusRodriguezCortes)

---
