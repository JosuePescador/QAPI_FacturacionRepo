# 📈 Flujo de CI/CD - Diagrama Completo

## Flujo General

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    GitHub - Eventos del Repositorio                     │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
         ┌───────▼────────┐ ┌───────▼────────┐ ┌───────▼────────┐
         │ PUSH a main    │ │ PUSH a develop │ │ PULL REQUEST   │
         └────────────────┘ └────────────────┘ └────────────────┘
                 │                  │                  │
                 └──────────────────┼──────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
        ┌───────────▼────────────┐      ┌──────────▼──────────┐
        │   test.yml             │      │  code-quality.yml   │
        │ (Test Environment CI)  │      │  (Build Verify)     │
        └────────────────────────┘      └─────────────────────┘
                    │                               │
        ┌───────────▼────────────┐      ┌──────────▼──────────┐
        │ 1. Inicia LocalStack   │      │ 1. mvn clean verify │
        │ 2. Compila proyectos   │      │ 2. Genera reportes  │
        │ 3. Ejecuta E2E tests   │      │ 3. Uploads results  │
        │ 4. Verifica BD         │      └─────────────────────┘
        │ 5. Carga artifacts     │              │
        └────────────────────────┘              │
                    │                           │
        ┌───────────▼─────────┐         ┌───────▼────────┐
        │ ✅ Pass / ❌ Fail   │         │ ✅ Pass / ❌ Fail
        └───────┬─────────────┘         └────────┬────────┘
                │ (develop branch)              │
                │                              │
        ┌───────▼──────────────────┐  (todos los branches)
        │                          │           │
        │  deploy-test.yml         │  ┌────────▼─────────┐
        │  SOLO en develop         │  │  security.yml    │
        │                          │  │ (cada domingo +  │
        │  1. Build test JARs      │  │  push/PR)        │
        │  2. Deploy vía SSH       │  │                  │
        │  3. Ejecuta smoke tests  │  │ - OWASP check    │
        │  4. Notifica resultado   │  │ - SonarCloud     │
        │                          │  │ - Reportes       │
        └───────┬──────────────────┘  └────────┬─────────┘
                │                              │
        ┌───────▼──────────────┐       ┌────────▼────────┐
        │ Test Server Actualizado     │ ✅ / ❌ Seguridad │
        │ Services Running ✅         │ Reporte generado │
        └──────────────────────┘      └─────────────────┘
```

---

## Flujo Detallado por Rama

### Branch: `main` (Producción)
```
Push a main
    │
    ├─→ test.yml               ✅ Debe pasar
    ├─→ code-quality.yml       ✅ Debe pasar
    └─→ security.yml           ⚠️ Informativo
        │
        └─→ Si TODO ✅
            └─→ Pronto: Deploy a producción
```

### Branch: `develop` (Test)
```
Push a develop
    │
    ├─→ test.yml               ✅ Debe pasar
    ├─→ code-quality.yml       ✅ Debe pasar
    ├─→ security.yml           ⚠️ Informativo
    └─→ deploy-test.yml        🚀 Deploy automático
        │
        └─→ Si TODO ✅
            └─→ Test Server actualizado ✅
```

### Branch: `scripts` (Experimental)
```
Push a scripts
    │
    ├─→ test.yml               ✅ Validation
    ├─→ code-quality.yml       ✅ Validation
    └─→ security.yml           ⚠️ Informativo
        │
        └─→ Sin deploy automático
```

### Pull Request
```
Abrir PR hacía main/develop
    │
    ├─→ test.yml               ✅ Validación requerida
    ├─→ code-quality.yml       ✅ Validación requerida
    └─→ security.yml           ⚠️ Informativo
        │
        └─→ Si TODO ✅
            └─→ "Ready to merge" ✅
```

---

## Matriz de Triggers

| Evento | test.yml | code-quality.yml | deploy-test.yml | security.yml |
|--------|----------|------------------|-----------------|--------------|
| Push main | ✅ | ✅ | ❌ | ✅ |
| Push develop | ✅ | ✅ | ✅ | ✅ |
| Push scripts | ✅ | ✅ | ❌ | ✅ |
| Pull Request | ✅ | ✅ | ❌ | ✅ |
| Manual (workflow_dispatch) | ❌ | ❌ | ✅ | ❌ |
| Programado (cron) | ❌ | ❌ | ❌ | ✅ |

---

## Duración Aproximada de Workflows

```
test.yml (Test Environment CI)
├─ Setup + Checkout:           ~30s
├─ Java Setup + Dependencies:  ~45s
├─ LocalStack init:            ~30s
├─ Resource initialization:    ~15s
├─ Build QAPI_masiva:          ~90s
├─ Build QAPI_worker:          ~90s
├─ Start Services:             ~30s
├─ E2E Tests:                  ~45s
├─ DynamoDB Verification:      ~15s
└─ TOTAL:                      ~6-8 minutos

code-quality.yml (Code Quality)
├─ Setup:                      ~30s
├─ Java Setup + Dependencies:  ~45s
├─ QAPI_masiva verify:         ~120s
├─ QAPI_worker verify:         ~120s
└─ TOTAL:                      ~5-7 minutos

deploy-test.yml (Deploy Test)
├─ Setup + Build:              ~3 minutos
├─ Create artifacts:           ~30s
├─ SSH transfer:               ~45s
├─ Docker pull/compose:        ~2 minutos
├─ Init resources:             ~30s
├─ Smoke tests:                ~30s
└─ TOTAL:                      ~8-10 minutos

security.yml (Security)
├─ Setup:                      ~30s
├─ OWASP Check QAPI_masiva:    ~2 minutos
├─ OWASP Check QAPI_worker:    ~2 minutos
├─ SonarCloud (opcional):      ~2-3 minutos
└─ TOTAL:                      ~8-12 minutos (sin SonarCloud: 5-6m)
```

---

## Flujo de Datos en test.yml

```
┌─────────────────────────────────────────────────────────┐
│                  GitHub Actions Runner                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ LocalStack Container                           │    │
│  │ ┌────────┐ ┌─────────┐ ┌───┐                 │    │
│  │ │  SQS   │ │DynamoDB │ │ S3│                 │    │
│  │ └────────┘ └─────────┘ └───┘                 │    │
│  └────────────────────────────────────────────────┘    │
│              ▲            ▲      ▲                     │
│              │            │      │                     │
│  ┌───────────┴────────────┴──────┴─────────────────┐  │
│  │ QAPI_masiva (8080) ←→ QAPI_worker (8081)       │  │
│  │                                                 │  │
│  │ 1. Recibe solicitud                             │  │
│  │ 2. Envia mensaje a SQS                          │  │
│  │ 3. Guarda en DynamoDB #1 (InfoRequest)         │  │
│  │ 4. Worker procesa (lee SQS)                     │  │
│  │ 5. Guarda en DynamoDB #2 (InfoRequestProcessing)│ │
│  │ 6. Tests verifican todo                         │  │
│  └─────────────────────────────────────────────────┘  │
│                                                         │
│  ┌────────────────────────────────────────────────┐    │
│  │ JUnit + Verificaciones en test-e2e.sh         │    │
│  └────────────────────────────────────────────────┘    │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## Estados Posibles

### ✅ Success
- Todos los tests pasaron
- Build exitoso
- Datos correctos en BD
- Artifacts guardados

### ❌ Failure
- Test falló
- Build error
- Keycloak no accesible
- LocalStack no inició
- BD sin datos

### ⏳ In Progress
- Ejecutándose
- Esperando recursos
- Compilando

### ⏭️ Skipped
- Condición no cumplida
- Workflow deshabilitado

### 🟡 Neutral
- Warnings (no bloqueante)
- Tests con falsos positivos

---

## Protecciones Recomendadas

Para **rama main**:

```yaml
# Settings → Branches → main → Edit

✅ Require a pull request before merging
   └─ Require approvals: 1
   └─ Dismiss stale pull request reviews

✅ Require status checks to pass before merging
   ├─ Test Environment CI
   └─ Code Quality & Build

✅ Require branches to be up to date before merging

✅ Include administrators: ✓

✅ Restrict who can push to matching branches
   └─ Only admins
```

---

## Dashboard Recomendado

Para monitorear en tiempo real:

```
https://github.com/JosuePescador/QAPI_FacturacionRepo/actions

Personaliza el dashboard con:
├─ Filter by status (success/failure/in progress)
├─ Filter by workflow
├─ Sort by date/status
└─ View detailed logs
```

---

## Mejoras Futuras

Consideraciones para después:

- [ ] Análisis de performance (tests lentos)
- [ ] Notificaciones a Slack
- [ ] Deploy automático a staging
- [ ] Code coverage mínimo requerido
- [ ] Automated versioning/tagging
- [ ] Release automation
- [ ] Docker image push a registry
- [ ] Kubernetes deployment
- [ ] Load testing
- [ ] Rollback automático

