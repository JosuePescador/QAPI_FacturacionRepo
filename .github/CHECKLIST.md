# ✅ Checklist de Implementación - GitHub Actions

Use este checklist para asegurarse de que todo está correctamente configurado.

## 📁 Archivos Creados

- [ ] `.github/workflows/test.yml` - Pipeline de test
- [ ] `.github/workflows/code-quality.yml` - Verificación de calidad
- [ ] `.github/workflows/deploy-test.yml` - Deploy a test
- [ ] `.github/workflows/security.yml` - Análisis de seguridad
- [ ] `.github/README.md` - Documentación principal
- [ ] `.github/GITHUB_ACTIONS_SETUP.md` - Guía de secretos
- [ ] `.github/RESUMEN.md` - Resumen visual
- [ ] `.github/FLUJO_CI_CD.md` - Diagrama de flujo
- [ ] `.github/CHECKLIST.md` - Este archivo

---

## 🔐 Configuración en GitHub

### ✅ Paso 1: Agregar Secretos

Ve a: **https://github.com/JosuePescador/QAPI_FacturacionRepo/settings/secrets/actions**

**Requeridos (DEBE hacer esto):**
- [ ] `KEYCLOAK_URL` = `https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token`
- [ ] `KEYCLOAK_CLIENT_ID` = `apifactmasiva`
- [ ] `KEYCLOAK_CLIENT_SECRET` = `2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV`

**Opcionales (para deploy):**
- [ ] `TEST_SERVER_HOST` = Tu servidor de test
- [ ] `TEST_SERVER_USER` = Usuario SSH
- [ ] `TEST_SERVER_KEY` = Clave privada SSH

**Opcionales (para SonarCloud):**
- [ ] `SONAR_TOKEN` = Token de SonarCloud
- [ ] `SONAR_ORGANIZATION` = Tu organización

---

## 📤 Commit y Push

- [ ] `git add .github/`
- [ ] `git commit -m "feat: add GitHub Actions workflows for CI/CD"`
- [ ] `git push origin scripts` (o tu rama actual)

---

## 🧪 Verificación Inicial

### ✅ Paso 1: Verificar que los workflows aparecen

1. [ ] Ve a: **https://github.com/JosuePescador/QAPI_FacturacionRepo/actions**
2. [ ] Deberías ver 4 workflows listados:
   - [ ] Test Environment CI
   - [ ] Code Quality & Build
   - [ ] Deploy to Test Environment
   - [ ] Security & Dependency Check

### ✅ Paso 2: Ejecutar primer test

1. [ ] Haz push a una rama (main, develop o scripts)
2. [ ] Ve a **Actions**
3. [ ] Espera a que se ejecute "Test Environment CI"
4. [ ] Debería ver ✅ si los secretos están bien

### ✅ Paso 3: Si falla, troubleshoot

**Si falla en token JWT:**
- [ ] Verifica los secretos de Keycloak
- [ ] Confirma que la URL sea accesible
- [ ] Check que credenciales sean correctas

**Si falla en LocalStack:**
- [ ] Revisa que Docker esté disponible en runner
- [ ] Aumenta timeout en test.yml si es necesario

**Si falla en build:**
- [ ] Verifica que Java 17 esté instalado en runner
- [ ] Revisa que pom.xml tenga configuración correcta

---

## 🎯 Configuración Avanzada (Opcional)

### Proteger la rama main

1. [ ] Ve a **Settings** → **Branches**
2. [ ] Haz clic en **Add rule**
3. [ ] Branch name pattern: `main`
4. Configura:
   - [ ] ✅ Require a pull request before merging
   - [ ] ✅ Require status checks to pass before merging
     - [ ] Selecciona "Test Environment CI"
     - [ ] Selecciona "Code Quality & Build"
   - [ ] ✅ Include administrators

### Agregar Badges al README

1. [ ] Abre `QAPI_masiva/README.md` o `QAPI_worker/README.md`
2. [ ] Agrega estas líneas al inicio:

```markdown
## CI/CD Status

[![Test Environment CI](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/test.yml/badge.svg)](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/test.yml)

[![Code Quality](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/code-quality.yml/badge.svg)](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/code-quality.yml)

[![Security](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/security.yml/badge.svg)](https://github.com/JosuePescador/QAPI_FacturacionRepo/actions/workflows/security.yml)
```

3. [ ] Commit y push

---

## 📊 Monitoreo Continuo

### Diario
- [ ] Revisar si hay tests fallando: https://github.com/JosuePescador/QAPI_FacturacionRepo/actions
- [ ] Si hay ❌, investigar y arreglar

### Semanal
- [ ] Revisar reporte de seguridad (security.yml)
- [ ] Actualizar dependencias si hay vulnerabilidades

### Mensual
- [ ] Revisar cobertura de código
- [ ] Optimizar duración de tests
- [ ] Limpiar artifacts antiguos

---

## 🚀 Tests Locales (Antes de Push)

Antes de hacer push, verifica que funcione localmente:

```bash
# 1. Inicia LocalStack
docker-compose up -d

# 2. Inicializa recursos
./init-localstack.sh

# 3. Compila proyectos
cd QAPI_masiva && mvn clean package -DskipTests && cd ..
cd QAPI_worker && mvn clean package -DskipTests && cd ..

# 4. Inicia en terminales separadas
java -jar QAPI_masiva/target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
java -jar QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev

# 5. Ejecuta test
./test-e2e.sh

# 6. Si TODO ✅, entonces haz push
git push origin tu-rama
```

Checklist:
- [ ] LocalStack iniciado
- [ ] test-e2e.sh pasó
- [ ] Datos en DynamoDB correctos

---

## 🆘 Troubleshooting

### Problema: Tests fallan en GitHub pero pasan localmente

**Soluciones:**
- [ ] Aumenta timeouts en workflows (LocalStack puede tardar más)
- [ ] Verifica variables de entorno en secrets
- [ ] Revisa permisos en archivos shell (chmod +x)

### Problema: "Permission denied: ./init-localstack.sh"

**Solución en test.yml:**
```yaml
- name: Initialize LocalStack resources
  run: |
    chmod +x init-localstack.sh
    ./init-localstack.sh
```

- [ ] Verificar que test.yml tiene `chmod +x`

### Problema: Keycloak no accesible desde Actions

**Opciones:**
- [ ] [ ] Whitelist de IPs de GitHub Actions
- [ ] [ ] Usar variable de entorno diferente para test
- [ ] [ ] Mock del token en ambiente de test

### Problema: Deploy a test no funciona

**Verificar:**
- [ ] [ ] `TEST_SERVER_KEY` tiene permisos (400)
- [ ] [ ] Usuario tiene acceso SSH
- [ ] [ ] Directorio `/opt/qapi/test/` existe en servidor
- [ ] [ ] Docker y docker-compose instalados en servidor

---

## 📝 Documentación de Referencia

Archivos de documentación disponibles:

1. [ ] `.github/README.md` - Documentación completa
2. [ ] `.github/GITHUB_ACTIONS_SETUP.md` - Guía de secretos
3. [ ] `.github/RESUMEN.md` - Resumen visual rápido
4. [ ] `.github/FLUJO_CI_CD.md` - Diagramas y flujos
5. [ ] `.github/CHECKLIST.md` - Este archivo

---

## ✨ Finalización

Una vez completados todos los pasos:

- [ ] Todos los workflows aparecen en GitHub Actions
- [ ] Al menos uno ha pasado exitosamente ✅
- [ ] Los secretos están configurados
- [ ] La documentación está disponible
- [ ] El equipo sabe dónde consultar

**Estatus: ✅ COMPLETADO**

---

## 📞 Contacto y Soporte

Si tienes dudas:

1. Revisa `.github/README.md` para info general
2. Revisa `.github/FLUJO_CI_CD.md` para ver diagramas
3. Revisa `.github/GITHUB_ACTIONS_SETUP.md` para secretos
4. Consulta [GitHub Actions Docs](https://docs.github.com/en/actions)

---

**Última actualización:** 19 de noviembre de 2025
**Estado:** ✅ Listo para usar

