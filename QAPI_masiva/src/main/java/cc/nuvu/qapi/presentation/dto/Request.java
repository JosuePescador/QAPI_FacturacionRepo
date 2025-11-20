package cc.nuvu.qapi.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public abstract class Request {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String servicio;

    public Request(String servicio) {
        this.servicio = servicio;
    }
    
    protected void setServicio(String servicio) {
        this.servicio = servicio;
    }
}
