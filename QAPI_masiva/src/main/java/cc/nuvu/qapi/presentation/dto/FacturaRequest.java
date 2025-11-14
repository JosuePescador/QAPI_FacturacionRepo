package cc.nuvu.qapi.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FacturaRequest {

    @JsonProperty("facturas")
    private List<Factura> facturas;

    public List<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(List<Factura> facturas) {
        this.facturas = facturas;
    }

    public static class Factura {
        private String referencia;

        @JsonProperty("cicloLectivo")
        private String cicloElectivo;

        @JsonProperty("nit_tercero")
        private Long nitTercero;

        @JsonProperty("cuentaConsignacion")
        private Long cuentaConsignacion;

        @JsonProperty("listado_cptos_principales")
        private List<ConceptoPrincipal> listadoCptosPrincipales;

        private Tercero tercero;

        public String getReferencia() {
            return referencia;
        }

        public void setReferencia(String referencia) {
            this.referencia = referencia;
        }

        public String getCicloElectivo() {
            return cicloElectivo;
        }

        public void setCicloElectivo(String cicloElectivo) {
            this.cicloElectivo = cicloElectivo;
        }

        public Long getNitTercero() {
            return nitTercero;
        }

        public void setNitTercero(Long nitTercero) {
            this.nitTercero = nitTercero;
        }

        public Long getCuentaConsignacion() {
            return cuentaConsignacion;
        }

        public void setCuentaConsignacion(Long cuentaConsignacion) {
            this.cuentaConsignacion = cuentaConsignacion;
        }

        public List<ConceptoPrincipal> getListadoCptosPrincipales() {
            return listadoCptosPrincipales;
        }

        public void setListadoCptosPrincipales(List<ConceptoPrincipal> listadoCptosPrincipales) {
            this.listadoCptosPrincipales = listadoCptosPrincipales;
        }

        public Tercero getTercero() {
            return tercero;
        }

        public void setTercero(Tercero tercero) {
            this.tercero = tercero;
        }
    }

    public static class ConceptoPrincipal {
        private String conceptoFacturacion;
        private Integer cantidadUnidades;
        private Long valorUnitario;

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

        public Long getValorUnitario() {
            return valorUnitario;
        }

        public void setValorUnitario(Long valorUnitario) {
            this.valorUnitario = valorUnitario;
        }
    }

    public static class Tercero {
        private String claseIdentificacion;
        private Long numeroIdentificacion;
        private String descripcionAuxiliar;
        private String naturalJuridica;
        private String tipoAuxiliar;
        private String tipoRetencion;
        private Integer codigoPais;
        private Integer codigoDepartamento;
        private Integer codigoCiudad;
        private String numeroCelular;
        private String numeroTelefonico;
        private String direccion;
        private String correoElectronico;

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

        public Integer getCodigoPais() {
            return codigoPais;
        }

        public void setCodigoPais(Integer codigoPais) {
            this.codigoPais = codigoPais;
        }

        public Integer getCodigoDepartamento() {
            return codigoDepartamento;
        }

        public void setCodigoDepartamento(Integer codigoDepartamento) {
            this.codigoDepartamento = codigoDepartamento;
        }

        public Integer getCodigoCiudad() {
            return codigoCiudad;
        }

        public void setCodigoCiudad(Integer codigoCiudad) {
            this.codigoCiudad = codigoCiudad;
        }

        public String getNumeroCelular() {
            return numeroCelular;
        }

        public void setNumeroCelular(String numeroCelular) {
            this.numeroCelular = numeroCelular;
        }

        public String getNumeroTelefonico() {
            return numeroTelefonico;
        }

        public void setNumeroTelefonico(String numeroTelefonico) {
            this.numeroTelefonico = numeroTelefonico;
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
}
