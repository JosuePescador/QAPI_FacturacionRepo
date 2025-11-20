# 🚀 GitHub Actions - QAPI Facturación Masiva (rama `actions`)

Este directorio contiene toda la configuración y documentación de GitHub Actions para el proyecto QAPI Facturación Masiva.

## 📋 Contenido

```
.github/
├── workflows/
│   ├── test.yml              # ✅ Test Environment CI (+ Terraform + LocalStack)
│   ├── code-quality.yml      # ✅ Code Quality & Build (+ Terraform validate)
│   ├── deploy-test.yml       # 🚀 Deploy to Test (condicional)
│   └── security.yml          # 🔒 Security & Dependencies (OWASP + SonarCloud)
├── run-github-actions.sh     # ⚙️  Script helper (validar, secretos, local)
├── GITHUB_ACTIONS_SETUP.md   # 📖 Guía de configuración de secretos
└── README.md                 # 📖 Este archivo
```

## ⚡ Quick Start (30 segundos)

```bash
# 1. Hacer el script ejecutable
chmod +x .github/run-github-actions.sh

# 2. Ver setup completo
./.github/run-github-actions.sh setup

# 3. Agregar secretos en GitHub (Settings → Secrets and variables → Actions)
#    Ver instrucciones en GITHUB_ACTIONS_SETUP.md

# 4. Hacer push para disparar workflows
git push origin <tu-rama>

# 5. Ver status
./.github/run-github-actions.sh status
```

## 🎯 Workflows

### 1. **Test Environment CI** (`test.yml`)
**Dispara en:** Push a main/develop/scripts/actions + Pull Requests

```
Pasos:
1. Checkout código
2. Setup Java 17
3. Setup Terraform
4. Provisionar infraestructura (infra-local.sh)
5. Aplicar IaC con Terraform (iac-local.sh)
6. Compilar QAPI_masiva + QAPI_worker
7. Levantar servicios en background
8. Ejecutar pruebas E2E
9. Verificar datos en DynamoDB
10. Cargar artifacts
```

**Duración:** ~10-15 minutos
**Artifacts:** `test-results` (target/ de ambos proyectos)

### 2. **Code Quality & Build** (`code-quality.yml`)
**Dispara en:** Push a main/develop/scripts/actions + Pull Requests

```
Pasos:
1. Checkout código
2. Setup Java 17
3. Setup Terraform
4. Validar Terraform (fmt + validate)
5. mvn clean verify (QAPI_masiva) -DskipTests
6. mvn clean verify (QAPI_worker) -DskipTests
7. Cargar reportes de cobertura
```

**Duración:** ~5-8 minutos
**Artifacts:** `coverage-reports` (target/site/ si existen)

### 3. **Deploy to Test** (`deploy-test.yml`)
**Dispara en:** Push a develop/modules + workflow_dispatch manual

```
Pasos:
1. Checkout código
2. Setup Java 17
3. Build QAPI_masiva (profile: test)
4. Build QAPI_worker (profile: test)
5. Crear artifacts para deploy
6. Upload artifacts
7. Deploy remoto SSH (CONDICIONAL - solo si TEST_SERVER_HOST está configurado)
8. Smoke tests (si deploy fue exitoso)
9. Notificación final
```

**Duración:** ~8-12 minutos
**Artifacts:** `test-deployment` (JARs + docker-compose.yml + init scripts)

**Nota:** Si `TEST_SERVER_HOST` no está configurado como secreto, este step se saltea automáticamente.

### 4. **Security & Dependency Check** (`security.yml`)
**Dispara en:** Push + Pull Requests + Semanal (domingo 00:00)

```
Pasos:
1. OWASP Dependency Check (QAPI_masiva)
2. OWASP Dependency Check (QAPI_worker)
3. SonarCloud Analysis (opcional - si SONAR_TOKEN configurado)
4. Cargar reportes
```

**Duración:** ~8-12 minutos
**Artifacts:** `dependency-check-reports` (HTMLs con resultados)

## 🔐 Configurar Secretos

Ve a: **GitHub → Settings → Secrets and variables → Actions → New repository secret**

### Requeridos (E2E tests):

| Nombre | Ejemplo |
|--------|---------|
| `KEYCLOAK_URL` | `https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token` |
| `KEYCLOAK_CLIENT_ID` | `apifactmasiva` |
| `KEYCLOAK_CLIENT_SECRET` | `2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV` |

### Opcionales (Deploy remoto):

| Nombre | Descripción |
|--------|-------------|
| `TEST_SERVER_HOST` | IP o dominio del servidor de test |
| `TEST_SERVER_USER` | Usuario SSH |
| `TEST_SERVER_KEY` | Clave privada SSH (formato PEM) |

### Opcionales (SonarCloud):

| Nombre | Descripción |
|--------|-------------|
| `SONAR_TOKEN` | Token de SonarCloud |
| `SONAR_ORGANIZATION` | Organización en SonarCloud |

## ⚙️ Helper Script

Usa `run-github-actions.sh` para validar y gestionar workflows:

```bash
# Validar workflows, Terraform y scripts
./.github/run-github-actions.sh validate

# Ver instrucciones de secretos
./.github/run-github-actions.sh secrets

# Ejecutar workflows localmente (requiere 'act')
./.github/run-github-actions.sh local

# Ver status en GitHub (requiere 'gh' CLI)
./.github/run-github-actions.sh status

# Setup completo
./.github/run-github-actions.sh setup

# Ver ayuda
./.github/run-github-actions.sh help
```

## 🧪 Ejecutar Localmente

### Con Docker + LocalStack (sin CI):

```bash
# 1. Levantar infraestructura
docker-compose up -d

# 2. Ejecutar IaC
cd infra/terraform
terraform init
terraform apply -auto-approve
cd ../..

# 3. Compilar
cd QAPI_masiva && mvn clean package -DskipTests && cd ..
cd QAPI_worker && mvn clean package -DskipTests && cd ..

# 4. Levantar servicios
java -jar QAPI_masiva/target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev &
java -jar QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev &

# 5. Ejecutar tests
./scripts/test/test-e2e.sh
```

### Con `act` (simular GitHub Actions):

```bash
# Instalar act
brew install act  # macOS
# o
apt install act   # Linux

# Ejecutar workflow
act push --job build-and-test

# Con secrets locales
act push --job build-and-test -s KEYCLOAK_CLIENT_SECRET=xxx
```

## 📊 Branches configurados

| Rama | test.yml | code-quality | deploy-test | security |
|------|----------|--------------|-------------|----------|
| `main` | ✅ | ✅ | ❌ | ✅ |
| `develop` | ✅ | ✅ | ✅ | ✅ |
| `scripts` | ✅ | ✅ | ❌ | ✅ |
| `actions` | ✅ | ✅ | ✅ | ✅ |

## 🔗 Links Útiles

- **Actions Dashboard:** https://github.com/JosuePescador/QAPI_FacturacionRepo/actions
- **Secrets Setup:** https://github.com/JosuePescador/QAPI_FacturacionRepo/settings/secrets/actions
- **GitHub Actions Docs:** https://docs.github.com/en/actions
- **Terraform Docs:** https://www.terraform.io/docs
- **LocalStack Docs:** https://docs.localstack.cloud

## 🆘 Troubleshooting

### LocalStack no inicia en CI
- Aumenta timeout en test.yml: `timeout 180 bash -c ...`
- Verifica que Docker esté disponible en el runner

### Tests fallan por base de datos Oracle
- Tests están configurados con `-DskipTests` en CI
- Para habilitar tests reales, usa TestContainers o H2

### Deploy remoto falla
- Verifica que `TEST_SERVER_HOST` secret esté configurado
- Si no está, el workflow continúa pero saltea el deploy

### Keycloak no accesible desde GitHub
- Verifica URLs en los secretos
- Whitelist IPs de GitHub Actions (si es necesario)

## 📝 Próximos Pasos

1. ✅ Revisar workflows en `.github/workflows/`
2. ✅ Hacer `.github/run-github-actions.sh` ejecutable: `chmod +x .github/run-github-actions.sh`
3. ⏳ Agregar secretos en GitHub
4. ⏳ Hacer `git push` para disparar workflows
5. ⏳ Verificar status en GitHub Actions dashboard

---

**Última actualización:** 20 de noviembre de 2025
**Rama:** `actions`
**Estado:** ✅ Listo para usar

