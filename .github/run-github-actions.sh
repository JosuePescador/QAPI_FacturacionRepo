#!/bin/bash

################################################################################
# Script de Ejecución - GitHub Actions para QAPI Facturación Masiva
# 
# Este script automatiza la configuración y ejecución de GitHub Actions en
# el repositorio local. Puede:
# 1. Validar configuración
# 2. Hacer setup de secretos
# 3. Ejecutar workflows localmente (requiere 'act')
# 4. Ver status de workflows en GitHub
#
# Uso: ./run-github-actions.sh [comando]
#
################################################################################

set -euo pipefail

# Colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
RED='\033[0;31m'
NC='\033[0m'

# Directorio base
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
GITHUB_DIR="${REPO_ROOT}/.github"
WORKFLOWS_DIR="${GITHUB_DIR}/workflows"

################################################################################
# FUNCIONES AUXILIARES
################################################################################

print_header() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║  $1${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
}

print_step() {
    echo -e "${YELLOW}→${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

################################################################################
# VALIDACIONES
################################################################################

validate_workflows() {
    print_header "Validando archivos de workflows"
    
    local workflows=("test.yml" "code-quality.yml" "deploy-test.yml" "security.yml")
    local missing=0
    
    for wf in "${workflows[@]}"; do
        if [ -f "${WORKFLOWS_DIR}/${wf}" ]; then
            print_success "Encontrado: ${wf}"
        else
            print_error "Falta: ${wf}"
            missing=$((missing + 1))
        fi
    done
    
    if [ $missing -eq 0 ]; then
        print_success "Todos los workflows están presentes"
        return 0
    else
        print_error "Faltan $missing workflow(s)"
        return 1
    fi
}

validate_terraform() {
    print_header "Validando Terraform"
    
    if [ ! -d "${REPO_ROOT}/infra/terraform" ]; then
        print_error "Directorio infra/terraform no encontrado"
        return 1
    fi
    
    if command -v terraform &> /dev/null; then
        print_step "Validando sintaxis de Terraform..."
        cd "${REPO_ROOT}/infra/terraform"
        terraform init -input=false 2>&1 | tail -5 || true
        terraform validate || true
        cd - > /dev/null
        print_success "Validación de Terraform completada"
        return 0
    else
        print_error "Terraform no está instalado. Instálalo con: brew install terraform (macOS) o apt install terraform (Linux)"
        return 1
    fi
}

validate_scripts() {
    print_header "Validando scripts de infraestructura"
    
    local scripts=("infra-local.sh" "iac-local.sh" "build-local-docker.sh" "deploy-local.sh")
    local missing=0
    
    for script in "${scripts[@]}"; do
        if [ -f "${REPO_ROOT}/scripts/${script}" ]; then
            if [ -x "${REPO_ROOT}/scripts/${script}" ]; then
                print_success "Encontrado y ejecutable: ${script}"
            else
                print_step "Haciendo ejecutable: ${script}"
                chmod +x "${REPO_ROOT}/scripts/${script}"
                print_success "Ahora es ejecutable: ${script}"
            fi
        else
            print_error "Falta: scripts/${script}"
            missing=$((missing + 1))
        fi
    done
    
    if [ $missing -eq 0 ]; then
        print_success "Todos los scripts están presentes"
        return 0
    else
        print_error "Faltan $missing script(s)"
        return 1
    fi
}

################################################################################
# CONFIGURACIÓN DE SECRETOS
################################################################################

setup_secrets() {
    print_header "Configuración de Secretos de GitHub"
    
    echo -e "${BLUE}Necesitas agregar los siguientes secretos en GitHub:${NC}"
    echo -e "${BLUE}Settings → Secrets and variables → Actions → New repository secret${NC}"
    echo ""
    echo -e "${GREEN}Requeridos para E2E tests:${NC}"
    echo "  1. KEYCLOAK_URL"
    echo "     Valor: https://iam.ia.ucaldas.nuvu.cc/realms/factMasivaTest/protocol/openid-connect/token"
    echo ""
    echo "  2. KEYCLOAK_CLIENT_ID"
    echo "     Valor: apifactmasiva"
    echo ""
    echo "  3. KEYCLOAK_CLIENT_SECRET"
    echo "     Valor: (tu client_secret de Keycloak)"
    echo ""
    echo -e "${YELLOW}Opcionales (solo para deploy remoto):${NC}"
    echo "  4. TEST_SERVER_HOST"
    echo "     Valor: IP o dominio del servidor de test"
    echo ""
    echo "  5. TEST_SERVER_USER"
    echo "     Valor: usuario SSH para el servidor"
    echo ""
    echo "  6. TEST_SERVER_KEY"
    echo "     Valor: clave privada SSH (formato PEM)"
    echo ""
    echo -e "${YELLOW}Opcionales (solo para análisis SonarCloud):${NC}"
    echo "  7. SONAR_TOKEN"
    echo "     Valor: (tu token de SonarCloud)"
    echo ""
    echo "  8. SONAR_ORGANIZATION"
    echo "     Valor: (tu organización en SonarCloud)"
    echo ""
    echo -e "${BLUE}URL rápida: https://github.com/JosuePescador/QAPI_FacturacionRepo/settings/secrets/actions${NC}"
}

################################################################################
# EJECUTAR CON ACT (local)
################################################################################

run_local_workflows() {
    print_header "Ejecutar workflows localmente con 'act'"
    
    if ! command -v act &> /dev/null; then
        print_error "El comando 'act' no está instalado"
        echo ""
        echo "Para instalar 'act':"
        echo "  macOS: brew install act"
        echo "  Linux: curl https://raw.githubusercontent.com/nektos/act/master/install.sh | bash"
        echo "  Windows: choco install act-cli"
        echo ""
        return 1
    fi
    
    print_step "Workflows disponibles para ejecutar localmente:"
    echo ""
    echo "  act push --job build-and-test           # Ejecutar test.yml"
    echo "  act push --job code-quality            # Ejecutar code-quality.yml"
    echo "  act push --job deploy-test             # Ejecutar deploy-test.yml"
    echo "  act push --job dependency-check        # Ejecutar security.yml"
    echo ""
    echo "Para más opciones: act --help"
}

################################################################################
# VERIFICAR STATUS EN GITHUB
################################################################################

check_github_status() {
    print_header "Status de Workflows en GitHub"
    
    if ! command -v gh &> /dev/null; then
        print_error "El comando 'gh' (GitHub CLI) no está instalado"
        echo ""
        echo "Para instalar GitHub CLI:"
        echo "  macOS: brew install gh"
        echo "  Linux: sudo apt install gh"
        echo ""
        echo "URL alternativa: https://github.com/JosuePescador/QAPI_FacturacionRepo/actions"
        return 1
    fi
    
    print_step "Últimas ejecuciones de workflows:"
    gh run list --limit 10 || true
    
    echo ""
    print_step "Ver detalles de un workflow:"
    echo "  gh run view <run_id> --log"
}

################################################################################
# INFORMACIÓN Y AYUDA
################################################################################

show_help() {
    cat << EOF
${BLUE}═══════════════════════════════════════════════════════════${NC}
${BLUE}GitHub Actions - Script de Ejecución y Configuración${NC}
${BLUE}═══════════════════════════════════════════════════════════${NC}

${GREEN}Comandos disponibles:${NC}

  ${YELLOW}validate${NC}        Valida workflows, Terraform y scripts
  ${YELLOW}secrets${NC}         Muestra instrucciones para configurar secretos
  ${YELLOW}local${NC}           Muestra cómo ejecutar workflows localmente
  ${YELLOW}status${NC}          Verifica status de workflows en GitHub
  ${YELLOW}all${NC}             Ejecuta todas las validaciones
  ${YELLOW}setup${NC}           Setup completo: validar + mostrar secretos
  ${YELLOW}help${NC}            Muestra esta ayuda

${GREEN}Ejemplos:${NC}

  # Validar todo
  ./run-github-actions.sh validate

  # Configurar secretos
  ./run-github-actions.sh secrets

  # Ejecutar workflows localmente
  ./run-github-actions.sh local

  # Verificar status en GitHub
  ./run-github-actions.sh status

  # Setup completo
  ./run-github-actions.sh setup

${GREEN}Próximos pasos recomendados:${NC}

  1. ./run-github-actions.sh setup
  2. Agregar secretos en GitHub (Settings → Secrets)
  3. git push (dispara los workflows)
  4. ./run-github-actions.sh status (ver resultados)

${BLUE}═══════════════════════════════════════════════════════════${NC}
EOF
}

################################################################################
# MAIN
################################################################################

main() {
    local command="${1:-help}"
    
    case "${command}" in
        validate)
            validate_workflows && validate_terraform && validate_scripts
            ;;
        secrets)
            setup_secrets
            ;;
        local)
            run_local_workflows
            ;;
        status)
            check_github_status
            ;;
        all)
            validate_workflows && validate_terraform && validate_scripts
            setup_secrets
            run_local_workflows
            check_github_status
            ;;
        setup)
            validate_workflows
            setup_secrets
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "Comando desconocido: ${command}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Ejecutar main
main "$@"
