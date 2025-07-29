package cc.nuvu.qapi.model;


public class FacturaRequest {
    private String referencia;
    private String cicloLectivo;
    //TODO Crear Logica para usar 5 y 11 en movimiento
    private Integer movimiento;
    private Long auxiliar;
    private String observacion;
    private String dependencia;

    // Conceptos Principales
    private String conceptoFacturacion;
    private Integer cantidadUnidades;
    private Integer valorUnitario;

    // Tercero
    private String claseIdentificacion;
    private Long numeroIdentificacion;
    private Long documentoAlterno;
    private String descripcionAuxiliar;
    private String naturalJuridica;
    private String tipoAuxiliar;
    private String tipoRetencion;
    private String centroCostosAsociado;
    private Integer pais;
    private Integer departamento;
    private Integer ciudad;
    private String celular;
    private String telefono;
    private String direccion;
    private String correoElectronico;

    public String getReferencia() {
        return referencia;
    }
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
    public String getCicloLectivo() {
        return cicloLectivo;
    }
    public void setCicloLectivo(String cicloLectivo) {
        this.cicloLectivo = cicloLectivo;
    }
    public Integer getMovimiento() {
        return movimiento;
    }
    public void setMovimiento(Integer movimiento) {
        this.movimiento = movimiento;
    }
    public Long getAuxiliar() {
        return auxiliar;
    }
    public void setAuxiliar(Long auxiliar) {
        this.auxiliar = auxiliar;
    }
    public String getObservacion() {
        return observacion;
    }
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    public String getDependencia() {
        return dependencia;
    }
    public void setDependencia(String dependencia) {
        this.dependencia = dependencia;
    }
    public String getConceptoFacturacion() {
        return conceptoFacturacion;
    }
    public void setConceptoFacturacion(String conceptoFacturacion) {
        this.conceptoFacturacion = conceptoFacturacion;
    }
        public Integer getCantidadUnidades() {
        return cantidadUnidades;
    }
    public void setCantidadUnidades(Integer cantidadUnidades) {
        this.cantidadUnidades = cantidadUnidades;
    }
    public Integer getValorUnitario() {
        return valorUnitario;
    }
    public void setValorUnitario(Integer valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
    public String getClaseIdentificacion() {
        return claseIdentificacion;
    }
    public void setClaseIdentificacion(String claseIdentificacion) {
        this.claseIdentificacion = claseIdentificacion;
    }
    public Long getNumeroIdentificacion() {
        return numeroIdentificacion;
    }
    public void setNumeroIdentificacion(Long numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }
    public Long getDocumentoAlterno() {
        return documentoAlterno;
    }
    public void setDocumentoAlterno(Long documentoAlterno) {
        this.documentoAlterno = documentoAlterno;
    }
    public String getDescripcionAuxiliar() {
        return descripcionAuxiliar;
    }
    public void setDescripcionAuxiliar(String descripcionAuxiliar) {
        this.descripcionAuxiliar = descripcionAuxiliar;
    }
    public String getNaturalJuridica() {
        return naturalJuridica;
    }
    public void setNaturalJuridica(String naturalJuridica) {
        this.naturalJuridica = naturalJuridica;
    }
        public String getTipoAuxiliar() {
        return tipoAuxiliar;
    }
    public void setTipoAuxiliar(String tipoAuxiliar) {
        this.tipoAuxiliar = tipoAuxiliar;
    }
    public String getTipoRetencion() {
        return tipoRetencion;
    }
    public void setTipoRetencion(String tipoRetencion) {
        this.tipoRetencion = tipoRetencion;
    }
    public String getCentroCostosAsociado() {
        return centroCostosAsociado;
    }
    public void setCentroCostosAsociado(String centroCostosAsociado) {
        this.centroCostosAsociado = centroCostosAsociado;
    }
    public Integer getPais() {
        return pais;
    }
    public void setPais(Integer pais) {
        this.pais = pais;
    }
    public Integer getDepartamento() {
        return departamento;
    }
    public void setDepartamento(Integer departamento) {
        this.departamento = departamento;
    }
    public Integer getCiudad() {
        return ciudad;
    }
    public void setCiudad(Integer ciudad) {
        this.ciudad = ciudad;
    }
    public String getCelular() {
        return celular;
    }
    public void setCelular(String celular) {
        this.celular = celular;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public String getCorreoElectronico() {
        return correoElectronico;
    }
    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }
}
