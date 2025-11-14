package cc.nuvu.qapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ConceptoPrincipal {
    private String conceptoFacturacion;
    private String cantidadUnidades;
    private String valorUnitario;
}