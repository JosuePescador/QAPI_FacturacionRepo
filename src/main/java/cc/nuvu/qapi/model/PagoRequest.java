package cc.nuvu.qapi.model;

public class PagoRequest {
    private Integer cuentaBancaria;
    private String operacionEfectivoCaja;
    private String areaResponsabilidad;
    private Integer beneficiario;
    private Integer valor;
    private String referencia;
    
    public Integer getCuentaBancaria() {
        return cuentaBancaria;
    }
    public void setCuentaBancaria(Integer cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }
    public String getOperacionEfectivoCaja() {
        return operacionEfectivoCaja;
    }
    public void setOperacionEfectivoCaja(String operacionEfectivoCaja) {
        this.operacionEfectivoCaja = operacionEfectivoCaja;
    }
    public String getAreaResponsabilidad() {
        return areaResponsabilidad;
    }
    public void setAreaResponsabilidad(String areaResponsabilidad) {
        this.areaResponsabilidad = areaResponsabilidad;
    }
    public Integer getBeneficiario() {
        return beneficiario;
    }
    public void setBeneficiario(Integer beneficiario) {
        this.beneficiario = beneficiario;
    }
    public Integer getValor() {
        return valor;
    }
    public void setValor(Integer valor) {
        this.valor = valor;
    }
    public String getReferencia() {
        return referencia;
    }
    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }
}
