#!/bin/bash

################################################################################
# Script de Ejecución - QAPI FacturacionMasiva
# Automatiza la compilación, configuración y ejecución del proyecto
################################################################################

set -e  # Exit on error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funciones
print_header() {
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${NC} $1"
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

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Verificar requisitos
check_requirements() {
    print_header "Verificando Requisitos"
    
    # Java
    if ! command -v java &> /dev/null; then
        print_error "Java no está instalado"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | head -1)
    print_success "Java encontrado: $JAVA_VERSION"
    
    # Maven o Maven Wrapper
    if [ ! -f "mvnw" ]; then
        if ! command -v mvn &> /dev/null; then
            print_error "Maven ni Maven Wrapper encontrados"
            exit 1
        fi
        MVN_CMD="mvn"
    else
        MVN_CMD="./mvnw"
    fi
    print_success "Maven encontrado: $MVN_CMD"
    
    echo ""
}

# Compilar proyecto
build_project() {
    print_header "Compilando Proyecto"
    
    print_step "Ejecutando: $MVN_CMD clean package -DskipTests"
    
    if $MVN_CMD clean package -DskipTests; then
        print_success "Compilación completada"
        
        # Verificar JAR
        if [ -f "target/QAPI_FacturacionMasiva-1.1.1.jar" ]; then
            JAR_SIZE=$(du -h target/QAPI_FacturacionMasiva-1.1.1.jar | cut -f1)
            print_success "JAR generado correctamente (${JAR_SIZE})"
        fi
    else
        print_error "La compilación falló"
        exit 1
    fi
    
    echo ""
}

# Configurar variables de entorno
setup_environment() {
    print_header "Configuración de Entorno"
    
    # Perfil Spring
    if [ -z "$SPRING_PROFILES_ACTIVE" ]; then
        PROFILE="dev"
        print_info "Usando perfil por defecto: dev"
    else
        PROFILE="$SPRING_PROFILES_ACTIVE"
        print_info "Perfil configurado: $PROFILE"
    fi
    
    # AWS
    print_info "Verificando credenciales AWS..."
    if [ -z "$AWS_ACCESS_KEY_ID" ]; then
        print_info "⚠️  AWS_ACCESS_KEY_ID no está configurada"
        if [ -f "$HOME/.aws/credentials" ]; then
            print_success "Se encontró archivo ~/.aws/credentials"
        else
            print_info "Para usar SQS/DynamoDB, configura:"
            print_info "  export AWS_ACCESS_KEY_ID='tu_key'"
            print_info "  export AWS_SECRET_ACCESS_KEY='tu_secret'"
        fi
    else
        print_success "AWS_ACCESS_KEY_ID configurada"
    fi
    
    # Puerto
    if [ -z "$SERVER_PORT" ]; then
        PORT="8080"
    else
        PORT="$SERVER_PORT"
    fi
    print_info "Puerto: $PORT"
    
    echo ""
}

# Ejecutar aplicación
run_application() {
    print_header "Iniciando Aplicación"
    
    print_step "Ejecutando JAR con perfil: $PROFILE"
    print_info "La aplicación estará disponible en: http://localhost:${PORT}"
    print_info "Swagger UI: http://localhost:${PORT}/docs"
    print_info "Health Check: http://localhost:${PORT}/actuator/health"
    echo ""
    print_info "Presiona Ctrl+C para detener"
    echo ""
    
    java -jar target/QAPI_FacturacionMasiva-1.1.1.jar \
        --spring.profiles.active="$PROFILE" \
        --server.port="$PORT"
}

# Validar instalación
validate_setup() {
    print_header "Validando Instalación"
    
    print_step "Esperando que la aplicación inicie (10 segundos)..."
    sleep 10
    
    # Health Check
    print_step "Realizando Health Check..."
    if curl -s -f http://localhost:${PORT}/actuator/health > /dev/null 2>&1; then
        print_success "✓ Health Check OK"
    else
        print_error "✗ Health Check falló - verifica los logs"
    fi
    
    # Swagger
    print_step "Verificando Swagger..."
    if curl -s -f http://localhost:${PORT}/docs > /dev/null 2>&1; then
        print_success "✓ Swagger disponible en /docs"
    else
        print_info "ℹ Swagger puede estar en /swagger-ui.html"
    fi
    
    echo ""
}

# Main Menu
show_menu() {
    print_header "QAPI FacturacionMasiva - Menu de Ejecución"
    
    echo ""
    echo "  1) Compilar y Ejecutar (Recomendado)"
    echo "  2) Solo Compilar"
    echo "  3) Solo Ejecutar (JAR existente)"
    echo "  4) Compilar con Tests"
    echo "  5) Ejecutar Tests"
    echo "  6) Limpiar (clean)"
    echo "  7) Salir"
    echo ""
    
    read -p "Selecciona una opción (1-7): " OPTION
    echo ""
}

# Main
main() {
    clear
    print_header "QAPI FacturacionMasiva v1.1.1"
    print_info "Sistema de Facturación Masiva - AWS SQS + DynamoDB"
    echo ""
    
    # Cambiar al directorio del proyecto si es necesario
    PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    cd "$PROJECT_DIR"
    
    check_requirements
    
    case "${1:-0}" in
        1)
            # Compilar y ejecutar
            build_project
            setup_environment
            run_application
            ;;
        2)
            # Solo compilar
            build_project
            print_success "Proyecto compilado. JAR ubicado en: target/QAPI_FacturacionMasiva-1.1.1.jar"
            ;;
        3)
            # Solo ejecutar
            if [ ! -f "target/QAPI_FacturacionMasiva-1.1.1.jar" ]; then
                print_error "JAR no encontrado. Compila primero con: ./run.sh 2"
                exit 1
            fi
            setup_environment
            run_application
            ;;
        4)
            # Compilar con tests
            print_header "Compilando con Tests"
            print_step "Ejecutando: $MVN_CMD clean package"
            if $MVN_CMD clean package; then
                print_success "Compilación completada (con tests)"
            else
                print_error "La compilación falló"
                exit 1
            fi
            ;;
        5)
            # Solo tests
            print_header "Ejecutando Tests"
            print_step "Ejecutando: $MVN_CMD test"
            $MVN_CMD test
            ;;
        6)
            # Clean
            print_header "Limpiando Proyecto"
            print_step "Ejecutando: $MVN_CMD clean"
            $MVN_CMD clean
            print_success "Proyecto limpiado"
            ;;
        7|"")
            print_info "Saliendo..."
            exit 0
            ;;
        *)
            print_error "Opción inválida"
            exit 1
            ;;
    esac
}

# Ejecutar con argumento o mostrar menu
if [ $# -eq 0 ]; then
    show_menu
    main "$OPTION"
else
    main "$1"
fi
