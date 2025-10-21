package cc.nuvu.qapi.model;

import java.util.List;
import java.util.Map;

public class FacturaRequest {
    private String referencia;
    private String cicloLectivo;
    private Long auxiliar;
    private Long cuentaConsignacion;

    // Lista de conceptos (usando Map directamente)
    private List<Map<String, Object>> conceptosPrincipales;

    // Tercero
    private String claseIdentificacion;
    private Long numeroIdentificacion;
    private String descripcionAuxiliar;
    private String naturalJuridica;
    private String tipoAuxiliar;
    private String tipoRetencion;
    private Integer pais;
    private Integer departamento;
    private Integer ciudad;
    private String celular;
    private String telefono;
    private String direccion;
    private String correoElectronico;

    // Getters y Setters

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


    public Long getAuxiliar() {
        return auxiliar;
    }

    public void setAuxiliar(Long auxiliar) {
        this.auxiliar = auxiliar;
    }

    public Long getCuentaConsignacion() {
        return cuentaConsignacion;
    }

    public void setCuentaConsignacion(Long cuentaConsignacion) {
        this.cuentaConsignacion = cuentaConsignacion;
    }

    public List<Map<String, Object>> getConceptosPrincipales() {
        return conceptosPrincipales;
    }

    public void setConceptosPrincipales(List<Map<String, Object>> conceptosPrincipales) {
        this.conceptosPrincipales = conceptosPrincipales;
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
