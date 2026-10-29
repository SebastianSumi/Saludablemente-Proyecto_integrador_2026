package upeu.edu.pe.Saludabemente.seguridad.api.social;

import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;

/** Mantiene el 404 existente; permite al adaptador Social traducir solo inactividad a 409. */
public class UsuarioSocialInactivoException extends ResourceNotFoundException {
    public UsuarioSocialInactivoException(String mensaje) {
        super(mensaje);
    }
}
