package upeu.edu.pe.Saludabemente.personal.credencial.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.exception.ResourceNotFoundException;
import upeu.edu.pe.Saludabemente.personal.credencial.dto.response.CredencialResponse;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.CredencialPrograma;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.EstadoCredencial;
import upeu.edu.pe.Saludabemente.personal.credencial.mapper.CredencialMapper;
import upeu.edu.pe.Saludabemente.personal.credencial.repository.CredencialRepository;
import upeu.edu.pe.Saludabemente.personal.credencial.service.CredencialService;
import upeu.edu.pe.Saludabemente.personal.credencial.util.HashUtil;
import upeu.edu.pe.Saludabemente.personal.credencial.util.QrGeneratorUtil;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Persona;
import upeu.edu.pe.Saludabemente.personal.persona.repository.PersonaRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredencialServiceImpl implements CredencialService {

    private final CredencialRepository credencialRepository;
    private final PersonaRepository personaRepository;      // ← cambiado: era PersonaService
    private final CredencialMapper credencialMapper;
    private final HashUtil hashUtil;
    private final QrGeneratorUtil qrGeneratorUtil;

    @Override
    @Transactional
    public CredencialResponse generarCredencial(Long idPersona, boolean incluirQr) {
        Persona persona = personaRepository.findById(idPersona)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Persona no encontrada: " + idPersona));

        // Regla: no puede haber dos credenciales ACTIVAS para la misma persona.
        // Si ya hay una, se anonimiza antes de crear la nueva.
        credencialRepository.actualizarEstadoPorPersona(idPersona, EstadoCredencial.ANONIMIZADA);

        String token = generarToken();
        String hash = hashUtil.generarHashSeguro(token);

        CredencialPrograma credencial = CredencialPrograma.builder()
                .persona(persona)
                .codigoQrHash(hash)
                .fechaEmision(LocalDateTime.now())
                .estado(EstadoCredencial.ACTIVA)
                .build();

        CredencialPrograma guardada = credencialRepository.save(credencial);

        String qrBase64 = null;
        if (incluirQr) {
            qrBase64 = qrGeneratorUtil.generarMatrizBase64(token);
        }
        return new CredencialResponse(
                guardada.getId(),
                guardada.getPersona().getId(),
                guardada.getCodigoQrHash(),
                guardada.getFechaEmision(),
                guardada.getEstado(),
                qrBase64
        );
    }

    @Override
    @Transactional
    public CredencialResponse rotarCredencial(Long idPersona, boolean incluirQr) {
        return generarCredencial(idPersona, incluirQr);
    }

    @Override
    @Transactional
    public void anonimizarCredenciales(Long idPersona) {
        credencialRepository.actualizarEstadoPorPersona(idPersona, EstadoCredencial.ANONIMIZADA);
    }

    /**
     * Genera un token único que se codifica en el QR.
     * UUID v4 + timestamp en milisegundos garantiza unicidad incluso
     * en generaciones casi simultáneas.
     */
    private String generarToken() {
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}