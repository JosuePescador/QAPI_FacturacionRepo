package cc.nuvu.qapi.presentation.dto;

import lombok.Getter;

@Getter
public abstract class Request {
    private String servicio;

    public Request(String servicio) {
        this.servicio = servicio;
    }
}
