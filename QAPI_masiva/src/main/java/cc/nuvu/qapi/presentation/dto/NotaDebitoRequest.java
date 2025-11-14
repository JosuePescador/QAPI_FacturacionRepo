package cc.nuvu.qapi.presentation.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import cc.nuvu.qapi.model.ConceptoPrincipal;
import cc.nuvu.qapi.model.Tercero;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class NotaDebitoRequest extends Request {
    @NotNull(message = "La referencia no debe ser nula")
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String referencia;

    @NotNull(message = "El ciclo lectivo no puede ser nulo")
    @NotEmpty(message = "El ciclo lectivo no puede estar vacío")
    private String cicloLectivo;

    @NotNull(message="El nit del tercero no puede ser nulo")
    @JsonProperty("nit_tercero")
    private Long nitTercero; // nit_tercero

    @NotNull(message="La cuenta consignación no puede ser nula")
    private Long cuentaConsignacion;

    @JsonProperty("listado_cptos_principales")
    private List<ConceptoPrincipal> listadosCptosPrincipales;
    
    @NotNull(message="La inforación de terceros no puede ser nula")
    private Tercero tercero;

    public NotaDebitoRequest(String cicloLectivo, Long cuentaConsignacion, List<ConceptoPrincipal> listadosCptosPrincipales, Long nitTercero, String referencia, Tercero tercero) {
        super("NOTA-DEBITO");
        this.cicloLectivo = cicloLectivo;
        this.cuentaConsignacion = cuentaConsignacion;
        this.listadosCptosPrincipales = listadosCptosPrincipales;
        this.nitTercero = nitTercero;
        this.referencia = referencia;
        this.tercero = tercero;
    }
    
    // Sobrescribir getServicio para asegurar que siempre retorna NOTA-DEBITO
    @Override
    public String getServicio() {
        String servicio = super.getServicio();
        if (servicio == null) {
            setServicio("NOTA-DEBITO");
            return "NOTA-DEBITO";
        }
        return servicio;
    }
}