package cc.nuvu.qapi.model;

public class NDebitoRequest {
    private Integer secuencia;
    private Integer cuentaBancaria;
    private Integer concepto;
    private Integer valorUnitario;
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
