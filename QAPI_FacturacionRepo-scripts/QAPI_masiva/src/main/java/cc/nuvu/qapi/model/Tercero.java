package cc.nuvu.qapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Tercero {
    private String claseIdentificacion;
    private Long numeroIdentificacion;
    private String descripcionAuxiliar;
    private String naturalJuridica;
    private String tipoAuxiliar;
    private String tipoRetencion;
    private Integer codigoPais;
    private Integer codigoDepartamento;
    private Integer codigoCiudad;
    private String celular;
    private String telefono;
    private String direccion;
    private String correoElectronico;
}
