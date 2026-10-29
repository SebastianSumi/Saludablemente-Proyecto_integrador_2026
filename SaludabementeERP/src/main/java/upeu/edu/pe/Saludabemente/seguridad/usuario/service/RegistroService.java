package upeu.edu.pe.Saludabemente.seguridad.usuario.service;

import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.RegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.request.VerificarRegistroRequest;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.UsuarioResponse;
import upeu.edu.pe.Saludabemente.seguridad.usuario.dto.response.VerificarRegistroResponse;

/**
 * Autorregistro público de cuentas.
 *
 * <p>El rol se fija en servidor (Colaborador): el cliente nunca elige roles.
 * Delega la creación en {@code UsuarioService#crear}, reutilizando sus
 * validaciones y el cifrado local de contraseña.</p>
 */
public interface RegistroService {

    /**
     * Paso 1: verifica documento + email sin crear nada.
     *
     * @throws upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException
     *         si la persona no existe en RRHH (404)
     * @throws upeu.edu.pe.Saludabemente.exception.BusinessRuleException
     *         si ya tiene cuenta, el email está en uso o está inactiva (409)
     */
    VerificarRegistroResponse verificar(VerificarRegistroRequest request);

    /**
     * Paso 2: crea la cuenta con rol Colaborador.
     */
    UsuarioResponse registrar(RegistroRequest request);
}
