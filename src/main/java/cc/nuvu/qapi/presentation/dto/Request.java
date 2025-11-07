package cc.nuvu.qapi.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public abstract class Request {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String servicio;

    public Request(String servicio) {
        this.servicio = servicio;
    }
}
