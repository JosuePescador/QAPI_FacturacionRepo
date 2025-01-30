package cc.nuvu.qapi.model;

public class NCreditoRequest {
    private Integer vigencia;
    private String tipoDocumento;
    private String estadoDocumento;
    private Integer tipoMovimiento;
    private Integer cuentaBancaria;
    private String tipoFacturacion;
    private Integer tercero;
    private Integer concepto;
    private Integer valorUnitario;
    
    public Integer getVigencia() {
        return vigencia;
    }
    public void setVigencia(Integer vigencia) {
        this.vigencia = vigencia;
    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public String getEstadoDocumento() {
        return estadoDocumento;
    }
    public void setEstadoDocumento(String estadoDocumento) {
        this.estadoDocumento = estadoDocumento;
    }
    public Integer getTipoMovimiento() {
        return tipoMovimiento;
    }
    public void setTipoMovimiento(Integer tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
    public Integer getCuentaBancaria() {
        return cuentaBancaria;
    }
    public void setCuentaBancaria(Integer cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }
    public String getTipoFacturacion() {
        return tipoFacturacion;
    }
    public void setTipoFacturacion(String tipoFacturacion) {
        this.tipoFacturacion = tipoFacturacion;
    }
    public Integer getTercero() {
        return tercero;
    }
    public void setTercero(Integer tercero) {
        this.tercero = tercero;
    }
    public Integer getConcepto() {
        return concepto;
    }
    public void setConcepto(Integer concepto) {
        this.concepto = concepto;
    }
    public Integer getValorUnitario() {
        return valorUnitario;
    }
    public void setValorUnitario(Integer valorUnitario) {
        this.valorUnitario = valorUnitario;
    }
}
