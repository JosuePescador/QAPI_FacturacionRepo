# 📑 Índice - GitHub Actions (.github/)

```
.github/
│
├── 🔧 HERRAMIENTAS EJECUTABLES
│   └── run-github-actions.sh         ⚙️  Script helper (validar, secretos, ejecutar)
│
├── 📖 DOCUMENTACIÓN
│   ├── README_WORKFLOWS.md           📘 Guía completa de workflows (START HERE)
│   ├── GITHUB_ACTIONS_SETUP.md       🔐 Configuración de secretos
│   └── README.md                     📊 Visión general rápida
│
└── 🔄 WORKFLOWS
    ├── test.yml                      ✅ Test Environment CI (+ Terraform + LocalStack)
    ├── code-quality.yml              ✅ Code Quality & Build (+ Terraform validate)
    ├── deploy-test.yml               🚀 Deploy to Test (condicional)
    └── security.yml                  🔒 Security & Dependencies (OWASP + SonarCloud)
```

---

## 🚀 COMIENZA AQUÍ

### 1️⃣ Hacer script ejecutable (si no está)
```bash
chmod +x .github/run-github-actions.sh
```

### 2️⃣ Ver guía completa
```bash
cat .github/README_WORKFLOWS.md
# O simplemente
./.github/run-github-actions.sh help
```

### 3️⃣ Validar configuración local
```bash
./.github/run-github-actions.sh validate
```

### 4️⃣ Agregar secretos en GitHub
```bash
./.github/run-github-actions.sh secrets
# Luego ve a: https://github.com/JosuePescador/QAPI_FacturacionRepo/settings/secrets/actions
```

### 5️⃣ Hacer push
```bash
git add .github/
git commit -m "chore: add github actions configuration"
git push origin actions
```

### 6️⃣ Ver status
```bash
./.github/run-github-actions.sh status
# O directamente en: https://github.com/JosuePescador/QAPI_FacturacionRepo/actions
```

---

## 📋 Archivos por Propósito

### Para Aprender
- **README_WORKFLOWS.md** — Guía completa con todos los detalles
- **GITHUB_ACTIONS_SETUP.md** — Solo sobre secretos

### Para Ejecutar/Validar
- **run-github-actions.sh** — Script helper para todo

### Para Entender el Flujo
- **test.yml** — Provisión de infra + tests E2E
- **code-quality.yml** — Validación de código y Terraform
- **security.yml** — Análisis de seguridad
- **deploy-test.yml** — Deployment condicional

---

## 🎯 Comandos Rápidos

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

---

## 🔐 Secretos Requeridos

**En GitHub → Settings → Secrets and variables → Actions:**

```
KEYCLOAK_URL=https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token
KEYCLOAK_CLIENT_ID=apifactmasiva
KEYCLOAK_CLIENT_SECRET=<tu-secret>
```

**Opcionales (deploy remoto):**
```
TEST_SERVER_HOST=<IP-o-dominio>
TEST_SERVER_USER=<usuario-ssh>
TEST_SERVER_KEY=<clave-privada-ssh>
```

---

## 📊 Workflows en un Vistazo

| Workflow | Dispara | Duración | Hace |
|----------|---------|----------|------|
| **test.yml** | Push + PR | 10-15m | Test E2E + Terraform + LocalStack |
| **code-quality.yml** | Push + PR | 5-8m | Validate Terraform + Build Maven |
| **deploy-test.yml** | Push develop | 8-12m | Build + Deploy remoto (condicional) |
| **security.yml** | Push + PR + Semanal | 8-12m | OWASP + SonarCloud (opcional) |

---

## 🆘 Ayuda Rápida

### Pregunta: ¿Cómo hago que los workflows funcionen?
**Respuesta:** 
1. Agrega los 3 secretos de Keycloak en GitHub
2. Haz push
3. Ve a Actions para ver resultados

### Pregunta: ¿Los tests que me falta?
**Respuesta:** Tests están skippados en CI porque requieren Oracle DB. Si quieres tests reales, usa TestContainers o H2.

### Pregunta: ¿Cómo deshabilito el deploy remoto?
**Respuesta:** No agregues los secretos `TEST_SERVER_HOST`, `TEST_SERVER_USER`, `TEST_SERVER_KEY`. El workflow continuará pero saltará el deploy.

### Pregunta: ¿Puedo ejecutar workflows localmente?
**Respuesta:** Sí, instala `act` y usa: `act push --job build-and-test`

---

## 📞 Recursos

- **GitHub Actions Docs:** https://docs.github.com/en/actions
- **Terraform Docs:** https://www.terraform.io/docs
- **LocalStack Docs:** https://docs.localstack.cloud
- **Act GitHub:** https://github.com/nektos/act

---

**Última actualización:** 20 de noviembre de 2025  
**Estado:** ✅ Listo para usar  
**Rama:** `actions`

