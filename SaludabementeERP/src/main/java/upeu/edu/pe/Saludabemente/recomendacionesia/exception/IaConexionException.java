package upeu.edu.pe.Saludabemente.recomendacionesia.exception;

public class IaConexionException extends RuntimeException {

    public IaConexionException(String mensaje) {
        super(mensaje);
    }

    public IaConexionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}