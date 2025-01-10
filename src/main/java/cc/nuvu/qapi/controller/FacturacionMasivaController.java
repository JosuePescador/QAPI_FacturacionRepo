package cc.nuvu.qapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
@RequestMapping("/api")
public class FacturacionMasivaController {
	
	@GetMapping("version")
	public String ObtenerVersion() {
		return "1.0.0";
	}

}