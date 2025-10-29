package cc.nuvu.qapi.dto;

import java.util.List;

import cc.nuvu.qapi.model.ConceptoPrincipal;
import cc.nuvu.qapi.model.Tercero;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotaDebitoRequest {
    @NotNull(message = "La referencia no debe ser nula")
    @NotEmpty(message = "El nombre no puede estar vacío")
    private String referencia;

    @NotNull(message = "El ciclo lectivo no puede ser nulo")
    @NotEmpty(message = "El ciclo lectivo no puede estar vacío")
    private String cicloLectivo;
    private Long nitTercero; // nit_tercero
    private Long cuentaConsignacion;
    private List<ConceptoPrincipal> listadosCptosPrincipales;
    private Tercero tercero;
}