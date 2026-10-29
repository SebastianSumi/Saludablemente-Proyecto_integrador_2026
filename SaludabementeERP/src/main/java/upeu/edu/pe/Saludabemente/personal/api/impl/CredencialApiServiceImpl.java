package upeu.edu.pe.Saludabemente.personal.api.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.personal.api.CredencialApiService;
import upeu.edu.pe.Saludabemente.personal.api.VerificacionQrResponse;
import upeu.edu.pe.Saludabemente.personal.credencial.entity.EstadoCredencial;
import upeu.edu.pe.Saludabemente.personal.credencial.repository.CredencialRepository;

@Service
@RequiredArgsConstructor
public class CredencialApiServiceImpl implements CredencialApiService {

    private final CredencialRepository credencialRepository;

    @Override
    @Transactional(readOnly = true)
    public VerificacionQrResponse verificarQrParaAsistencia(String codigoQrHash) {
        if (codigoQrHash == null || codigoQrHash.isBlank()) {
            return VerificacionQrResponse.invalido("Hash vacío");
        }

        return credencialRepository
                .findByCodigoQrHashAndEstado(codigoQrHash, EstadoCredencial.ACTIVA)
                .map(c -> VerificacionQrResponse.valido(c.getPersona().getId()))
                .orElseGet(() -> VerificacionQrResponse.invalido(
                        "QR inválido, anonimizado o inexistente"));
    }
}