package cc.nuvu.qapi.dto;

public class NotaRequest {
    private Integer secuencia;
    private Integer cuentaBancaria;
    private Integer concepto;
    private Integer valorUnitario;

    private Integer vigencia;
    private String tipoDocumento;
    private String estadoDocumento;
    private Integer tipoMovimiento;
    private String tipoFacturacion;
    private Integer tercero;

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
    public Integer getSecuencia() {
        return secuencia;
    }
    public void setSecuencia(Integer secuencia) {
        this.secuencia = secuencia;
    }
    public Integer getCuentaBancaria() {
        return cuentaBancaria;
    }
    public void setCuentaBancaria(Integer cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
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
