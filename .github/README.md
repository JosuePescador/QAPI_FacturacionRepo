# 🚀 GitHub Actions - QAPI Facturación Masiva

Este proyecto cuenta con pipelines de CI/CD automatizados usando GitHub Actions.

## 📋 Workflows Disponibles

### 1. **Test Environment CI** (`test.yml`)
**Trigger:** `push` en main/develop/scripts y `pull_request`

Ejecuta:
- ✅ Build de QAPI_masiva y QAPI_worker
- ✅ Inicia LocalStack (SQS, DynamoDB, S3)
- ✅ Ejecuta pruebas End-to-End
- ✅ Verifica datos en DynamoDB
- ✅ Carga artifacts

**Duración:** ~5-10 minutos

### 2. **Code Quality & Build** (`code-quality.yml`)
**Trigger:** `push` en main/develop/scripts y `pull_request`

Ejecuta:
- ✅ `mvn clean verify` en ambos módulos
- ✅ Análisis de código
- ✅ Sube reportes de cobertura

**Duración:** ~5-8 minutos

### 3. **Deploy to Test Environment** (`deploy-test.yml`)
**Trigger:** `push` en develop (manual disponible)

Ejecuta:
- ✅ Build para test
- ✅ Crea artifacts
- ✅ Despliega a servidor de test
- ✅ Ejecuta smoke tests

**Duración:** ~10-15 minutos

### 4. **Security & Dependency Check** (`security.yml`)
**Trigger:** Semanal + push + pull_request

Ejecuta:
- ✅ Análisis de vulnerabilidades OWASP
- ✅ Análisis con SonarCloud (opcional)
- ✅ Verificación de dependencias

**Duración:** ~10-15 minutos

---

## 🔐 Configuración Inicial

### Paso 1: Agregar Secretos

Ve a **GitHub → Settings → Secrets and variables → Actions**

Agrega estos secretos:

#### Requeridos (para test.yml)
```
KEYCLOAK_URL=https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token
KEYCLOAK_CLIENT_ID=apifactmasiva
KEYCLOAK_CLIENT_SECRET=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV
```

#### Opcionales (para deploy-test.yml)
```
TEST_SERVER_HOST=tu-servidor-test.com
TEST_SERVER_USER=deploy-user
TEST_SERVER_KEY=<private-key-ssh>
```

#### Opcionales (para security.yml con SonarCloud)
```
SONAR_TOKEN=token-de-sonarcloud
SONAR_ORGANIZATION=tu-organizacion-sonar
```

---

## 📊 Monitoreo

Para ver los workflows en ejecución:

1. **GitHub.com** → Tu repositorio → **Actions**
2. Verás:
   - ✅ Ejecuciones completadas
   - ❌ Ejecuciones fallidas
   - ⏳ En progreso
   - ⏸️ Pausadas

### Ver detalles de una ejecución

Haz clic en el workflow para ver:
- Logs de cada step
- Artifacts generados
- Resultados de tests
- Cobertura de código

---

## 🎯 Casos de Uso

### Caso 1: Mergear PR a main
```
1. Haces push a tu rama
2. GitHub Actions ejecuta test.yml + code-quality.yml automáticamente
3. Si pasan ✅, puedes hacer merge
4. Al hacer merge a main → se ejecuta todo nuevamente
```

### Caso 2: Desplegar a test
```
1. Haces push a develop
2. GitHub Actions ejecuta deploy-test.yml automáticamente
3. Se despliega a tu servidor de test
4. Se ejecutan smoke tests
```

### Caso 3: Chequeo semanal de seguridad
```
1. Cada domingo se ejecuta security.yml automáticamente
2. Verifica vulnerabilidades de dependencias
3. Genera reporte (si SonarCloud está configurado)
```

---

## 🛠️ Personalización

### Cambiar branches que triggean workflows

En cualquier archivo `.yml` en `.github/workflows/`:

```yaml
on:
  push:
    branches:
      - main          # Agregar/quitar branches
      - develop
  pull_request:
    branches:
      - main
```

### Agregar más steps

Ejemplo: Enviar notificación a Slack

```yaml
- name: Notify Slack on failure
  if: failure()
  uses: slackapi/slack-github-action@v1
  with:
    webhook-url: ${{ secrets.SLACK_WEBHOOK }}
    payload: |
      {
        "text": "❌ Pipeline failed on ${{ github.repository }}"
      }
```

### Ejecutar workflow manualmente

Los workflows con `workflow_dispatch` se pueden ejecutar manualmente:

1. Ve a **Actions**
2. Selecciona el workflow
3. Haz clic en **Run workflow**
4. Elige la rama y parámetros (si aplica)

---

## 🔍 Troubleshooting

### LocalStack no inicia
```yaml
# Aumenta timeout en test.yml
- name: Wait for LocalStack to be ready
  run: timeout 120 bash -c 'until curl -f http://localhost:4566/_localstack/health; do sleep 2; done'
```

### Build tarda demasiado
- Usa `cache: maven` (ya está configurado)
- Considera compilar en paralelo

### Errores de permisos SSH para deploy
- Verifica que `TEST_SERVER_KEY` contiene la clave privada completa
- Asegúrate que el usuario tiene permisos en `/opt/qapi/test`

### Keycloak no accesible desde GitHub
- Verifica URLs en los secretos
- Si usa VPN, considera cambiar a una clave de acceso

---

## 📈 Monitoreo Avanzado

### Badges de status en README

Agrega a tu `README.md`:

```markdown
## Status

![Test Environment CI](https://github.com/TU_USUARIO/QAPI_FacturacionRepo/actions/workflows/test.yml/badge.svg)
![Code Quality](https://github.com/TU_USUARIO/QAPI_FacturacionRepo/actions/workflows/code-quality.yml/badge.svg)
![Security](https://github.com/TU_USUARIO/QAPI_FacturacionRepo/actions/workflows/security.yml/badge.svg)
```

### Proteger main branch

En **Settings → Branches → Add rule**:

```
Branch name: main
✅ Require a pull request before merging
✅ Require status checks to pass before merging
  - Select: Test Environment CI
  - Select: Code Quality & Build
✅ Restrict who can push to matching branches
```

---

## 💡 Best Practices

✅ **Mantén workflows simples**
- Cada workflow debe tener un propósito claro

✅ **Reutiliza workflows**
- Usa composite actions para código reutilizable

✅ **Monitorea costos**
- Los runners tienen limite de minutos/mes

✅ **Actualiza dependencias regularmente**
- Usa security.yml para detectar vulnerabilidades

✅ **Documenta cambios en workflows**
- Actualiza este README si cambias el proceso

---

## 📞 Soporte

Para más info sobre GitHub Actions:
- [Documentación oficial](https://docs.github.com/en/actions)
- [Marketplace de Actions](https://github.com/marketplace?type=actions)
- [Ejemplos](https://github.com/actions)

