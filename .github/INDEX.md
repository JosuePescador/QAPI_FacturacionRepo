# 📑 Índice de GitHub Actions - QAPI Facturación Masiva

```
.github/
│
├── 📂 workflows/                          Workflows de CI/CD
│   ├── test.yml                           ✅ Test Environment (Main)
│   ├── code-quality.yml                   ✅ Code Quality Check
│   ├── deploy-test.yml                    🚀 Deploy Automático
│   └── security.yml                       🔒 Seguridad & Dependencias
│
├── 📘 QUICK_START.md                      ⚡ COMIENZA AQUÍ (30 seg)
├── 📘 CHECKLIST.md                        ✅ Checklist de setup
├── 📘 README.md                           📚 Documentación completa
├── 📘 GITHUB_ACTIONS_SETUP.md             🔐 Guía de secretos
├── 📘 RESUMEN.md                          📊 Resumen visual rápido
├── 📘 FLUJO_CI_CD.md                      📈 Diagramas y flujos
└── 📘 INDEX.md                            📑 Este archivo
```

---

## 🎯 Por Dónde Empezar

### Si tienes 30 segundos ⚡
→ Lee: `QUICK_START.md`

### Si tienes 5 minutos 📝
→ Lee: `CHECKLIST.md`

### Si quieres entenderlo todo 📚
→ Lee en orden:
1. `README.md` - Visión general
2. `FLUJO_CI_CD.md` - Diagramas
3. `GITHUB_ACTIONS_SETUP.md` - Configuración
4. `RESUMEN.md` - Referencia

### Si necesitas resolver un problema 🔧
→ Busca en: `README.md` → Troubleshooting

---

## 📋 Contenido de Cada Archivo

### `QUICK_START.md` (1 min de lectura)
- 3 pasos para activar
- Qué hace cada workflow
- Comandos útiles
- Links rápidos

### `CHECKLIST.md` (5 min de lectura)
- Verificación de archivos
- Configuración en GitHub
- Commit & push
- Troubleshooting paso a paso

### `README.md` (15-20 min de lectura)
- Descripción completa de cada workflow
- Cómo configurar GitHub Actions
- Monitoreo avanzado
- Best practices

### `GITHUB_ACTIONS_SETUP.md` (10 min de lectura)
- Cómo agregar secretos
- Variables de entorno
- Configuración de branches
- Troubleshooting común

### `RESUMEN.md` (5 min de lectura)
- Estructura visual
- Qué hace cada workflow
- Próximos pasos
- Badges para README

### `FLUJO_CI_CD.md` (10 min de lectura)
- Diagramas del flujo
- Matriz de triggers
- Duración de workflows
- Estados posibles

---

## 🚀 Workflow Rápido

```
1. Abre QUICK_START.md
   ↓
2. Agrega 3 secretos en GitHub
   ↓
3. git push
   ↓
4. Verifica en GitHub Actions
   ↓
5. ✅ ¡Listo!
```

---

## 📱 Archivos de Workflow

### `test.yml` - Pipeline Principal
```
Se ejecuta en: push + pull_request (main/develop/scripts)

¿Qué hace?
1. Inicia LocalStack (SQS, DynamoDB, S3)
2. Compila ambos proyectos
3. Ejecuta pruebas E2E
4. Verifica datos en BD
5. Carga artifacts

Tiempo: 6-8 minutos
Criticidad: ⭐⭐⭐⭐⭐
```

### `code-quality.yml` - Calidad
```
Se ejecuta en: push + pull_request (main/develop/scripts)

¿Qué hace?
1. mvn clean verify en QAPI_masiva
2. mvn clean verify en QAPI_worker
3. Genera reportes de cobertura

Tiempo: 5-7 minutos
Criticidad: ⭐⭐⭐⭐
```

### `deploy-test.yml` - Deploy
```
Se ejecuta en: push a develop (o manual)

¿Qué hace?
1. Build JARs
2. Transfiere vía SSH
3. Ejecuta docker-compose
4. Inicializa recursos
5. Smoke tests

Tiempo: 8-10 minutos
Criticidad: ⭐⭐⭐⭐⭐ (si necesitas deploy automático)
```

### `security.yml` - Seguridad
```
Se ejecuta en: Semanal + push + pull_request

¿Qué hace?
1. OWASP Dependency Check
2. SonarCloud Analysis (opcional)
3. Genera reportes

Tiempo: 8-12 minutos
Criticidad: ⭐⭐⭐
```

---

## 🔧 Configuración

### Secretos Requeridos (3)
```
✅ KEYCLOAK_URL
✅ KEYCLOAK_CLIENT_ID
✅ KEYCLOAK_CLIENT_SECRET
```

Ver: `GITHUB_ACTIONS_SETUP.md`

### Secretos Opcionales
```
⭕ TEST_SERVER_HOST (para deploy)
⭕ TEST_SERVER_USER (para deploy)
⭕ TEST_SERVER_KEY (para deploy)
⭕ SONAR_TOKEN (para análisis)
⭕ SONAR_ORGANIZATION (para análisis)
```

---

## 📊 Matriz de Triggers

| Evento | test.yml | code-quality | deploy-test | security |
|--------|:--------:|:------------:|:-----------:|:--------:|
| Push main | ✅ | ✅ | ❌ | ✅ |
| Push develop | ✅ | ✅ | ✅ | ✅ |
| Push scripts | ✅ | ✅ | ❌ | ✅ |
| Pull Request | ✅ | ✅ | ❌ | ✅ |
| Manual | ❌ | ❌ | ✅ | ❌ |
| Semanal | ❌ | ❌ | ❌ | ✅ |

---

## 🆘 Necesito...

### Activar GitHub Actions
→ Lee: `QUICK_START.md`

### Configurar secretos
→ Lee: `GITHUB_ACTIONS_SETUP.md`

### Entender qué hace cada workflow
→ Lee: `README.md` + `FLUJO_CI_CD.md`

### Resolver un problema
→ Lee: `README.md` → Troubleshooting

### Ver toda la estructura
→ Lee: `RESUMEN.md` + `FLUJO_CI_CD.md`

### Chequear que esté todo bien
→ Usa: `CHECKLIST.md`

---

## 📞 Documentación Externa

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [Security Best Practices](https://docs.github.com/en/actions/security-guides)
- [Environment Variables](https://docs.github.com/en/actions/learn-github-actions/environment-variables)

---

## ✨ Características

✅ Test automático en cada push/PR
✅ Compilación paralela
✅ LocalStack simulado (sin AWS real)
✅ E2E tests completos
✅ Verificación de código
✅ Deploy automático (opcional)
✅ Análisis de seguridad
✅ Reportes de cobertura
✅ Artifacts guardados
✅ Notificaciones automáticas

---

## 📈 Estado del Proyecto

```
Workflows:        ✅ 4 workflows
Documentación:    ✅ 7 archivos .md
Configuración:    ⏳ Necesita secretos en GitHub
Status:           🟡 Listo para usar (falta secretos)
```

---

## 🎯 Resumen

```
┌─────────────────────────────────────────┐
│ Paso 1: Leer QUICK_START.md (30 seg)   │
├─────────────────────────────────────────┤
│ Paso 2: Agregar secretos (2 min)        │
├─────────────────────────────────────────┤
│ Paso 3: git push (1 min)                │
├─────────────────────────────────────────┤
│ Paso 4: Verificar en GitHub (1 min)     │
├─────────────────────────────────────────┤
│ ✅ LISTO - Los workflows funcionan      │
└─────────────────────────────────────────┘
```

---

**Última actualización:** 19 de noviembre de 2025
**Status:** ✅ Completado y listo
**Próximo:** Agregar secretos en GitHub

