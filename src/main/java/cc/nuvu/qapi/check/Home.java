package cc.nuvu.qapi.check;

public class Home {

    // Método para generar mensaje de éxito para Factura
    public static String generarMensajeFactura(int cantidad) {
        return "Se han procesado " + cantidad + " facturas y se han enviado a la cola.";
    }

    // Método para generar mensaje de éxito para Pago
    public static String generarMensajePago(int cantidad) {
        return "Se han procesado " + cantidad + " pagos y se han enviado a la cola.";
    }

    // Método para generar mensaje de éxito para Pago Factura
    public static String generarMensajePagoFactura(int cantidad) {
        return "Se han procesado " + cantidad + " pagos-factura y se han enviado a la cola.";
    }

    // Método para generar mensaje de éxito para Nota Débito/Crédito
    public static String generarMensajeNota(int cantidad) {
        return "Se han procesado " + cantidad + " notas débito/crédito y se han enviado a la cola.";
    }
}
