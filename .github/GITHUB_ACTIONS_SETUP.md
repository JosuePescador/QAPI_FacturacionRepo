# GitHub Actions - Configuración de Secretos (rama `modules`)

Para que los workflows funcionen correctamente en esta rama, configura los siguientes secretos en el repositorio (Settings → Secrets and variables → Actions):

Requeridos para pruebas end-to-end y despliegue opcional:

- `KEYCLOAK_URL`: URL de token de Keycloak (ej: https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token)
- `KEYCLOAK_CLIENT_ID`: client_id para obtener token (ej: `apifactmasiva`)
- `KEYCLOAK_CLIENT_SECRET`: client_secret para obtener token

Opcionales (solo si quieres que el workflow haga deploy remoto):

- `TEST_SERVER_HOST`: host o IP del servidor de test
- `TEST_SERVER_USER`: usuario SSH
- `TEST_SERVER_KEY`: clave privada SSH (formato PEM)

Opcionales para análisis (security.yml):

- `SONAR_TOKEN`: token de SonarCloud (si usas Sonar)
- `SONAR_ORGANIZATION`: organización en SonarCloud

Notas:

- Si `TEST_SERVER_HOST` no está configurado, el workflow de despliegue continuará pero omitirá la transferencia remota y dejará el build/artifacts disponibles.
- Los workflows en esta rama usan los scripts de `scripts/infra-local.sh` y `scripts/iac-local.sh` para provisionar LocalStack y aplicar Terraform.
# GitHub Actions - Configuración de Secretos

Para que los workflows funcionen correctamente, necesitas configurar los siguientes secretos en tu repositorio de GitHub.

## Pasos para agregar Secretos

1. Ve a tu repositorio en GitHub
2. Haz clic en **Settings** → **Secrets and variables** → **Actions**
3. Haz clic en **New repository secret**
4. Agrega los siguientes secretos:

## Secretos Requeridos

### Para el Workflow de Test (test.yml)

```
KEYCLOAK_URL=https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token
KEYCLOAK_CLIENT_ID=apifactmasiva
KEYCLOAK_CLIENT_SECRET=2maYCBOiMthTcAWeBxc3DWvZ4kpcIYNV
```

**Nota:** Reemplaza estos valores con las credenciales reales de tu ambiente Keycloak.

## Archivos de Configuración

Los workflows usan los siguientes archivos de tu proyecto:

- `.github/workflows/test.yml` - Pipeline principal de test
- `.github/workflows/code-quality.yml` - Verificación de calidad de código
- `docker-compose.yml` - Configuración de LocalStack
- `init-localstack.sh` - Inicialización de recursos AWS
- `test-e2e.sh` - Pruebas End-to-End

## Verificación Manual

Si deseas probar localmente antes de hacer push:

```bash
# 1. Inicia LocalStack
docker-compose up -d

# 2. Inicializa recursos
./init-localstack.sh

# 3. Compila los proyectos
cd QAPI_masiva && mvn clean package -DskipTests && cd ..
cd QAPI_worker && mvn clean package -DskipTests && cd ..

# 4. Ejecuta en terminales separadas
java -jar QAPI_masiva/target/QAPI_FacturacionMasiva-1.1.1.jar --spring.profiles.active=dev
java -jar QAPI_worker/target/QAPI_FacturacionMasivaWorker-1.1.0.jar --spring.profiles.active=dev

# 5. Prueba
./test-e2e.sh
```

## Monitoreo de Workflows

Para monitorear la ejecución de los workflows:

1. Ve a tu repositorio en GitHub
2. Haz clic en la pestaña **Actions**
3. Verás el historial de todas las ejecuciones
4. Haz clic en una ejecución para ver los detalles

## Configuración Adicional (Opcional)

### Notificaciones

Si deseas recibir notificaciones cuando los workflows fallen:

1. Ve a **Settings** → **Notifications**
2. Habilita "Push notifications" para:
   - Failed workflows
   - Successful deployments

### Branches Protegidas

Para requerir que los workflows pasen antes de hacer merge:

1. Ve a **Settings** → **Branches**
2. Haz clic en **Add rule**
3. Configura:
   - Branch name pattern: `main` o `develop`
   - ✅ Require status checks to pass before merging
   - Selecciona: "Test Environment CI" y "Code Quality & Build"

## Troubleshooting

### LocalStack no está listo

Si ves errores de timeout en LocalStack, aumenta el tiempo de espera en el workflow:
```yaml
- name: Wait for LocalStack to be ready
  run: timeout 120 bash -c 'until curl -f http://localhost:4566/_localstack/health; do sleep 2; done'
```

### Errores de credenciales Keycloak

Si el token JWT no se obtiene:
- Verifica que los secretos sean correctos
- Asegúrate que el endpoint de Keycloak sea accesible desde GitHub Actions

### Pruebas lentas

Si los tests tardan mucho:
- Aumenta el tiempo de espera entre servicios
- Considera paralelizar los builds de QAPI_masiva y QAPI_worker

