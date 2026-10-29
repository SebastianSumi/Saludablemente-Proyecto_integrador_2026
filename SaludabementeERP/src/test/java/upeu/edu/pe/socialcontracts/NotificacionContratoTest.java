package upeu.edu.pe.socialcontracts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import upeu.edu.pe.Saludabemente.notificaciones.api.*;
import upeu.edu.pe.Saludabemente.notificaciones.api.social.*;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificacionContratoTest {
    @ParameterizedTest
    @EnumSource(TipoAvisoSocial.class)
    void puenteUsaPersonaRealCodigoYVariablesSinEmail(TipoAvisoSocial tipo) {
        NotificacionApi notificaciones = mock(NotificacionApi.class);
        var api = new SocialNotificacionApiImpl(notificaciones);
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");
        api.enviar(new SocialAvisoDto(id, tipo, 1002L, 601L, 701L, 301L));
        verify(notificaciones).enviar(1002L, tipo.name(), "social", 601L,
                Map.of("idEvento", id.toString(), "idPost", 601L, "idComentario", 701L, "idEquipo", 301L), null);
    }

    @Test void avisoIncompletoNoLlegaATransporte() {
        NotificacionApi notificaciones = mock(NotificacionApi.class);
        var api = new SocialNotificacionApiImpl(notificaciones);
        assertThrows(BusinessRuleException.class, () -> api.enviar(null));
        assertThrows(BusinessRuleException.class, () -> api.enviar(new SocialAvisoDto(UUID.randomUUID(),
                TipoAvisoSocial.SOCIAL_COMENTARIO_POST, 1002L, 601L, null, null)));
        assertThrows(BusinessRuleException.class, () -> api.enviar(new SocialAvisoDto(UUID.randomUUID(),
                TipoAvisoSocial.SOCIAL_NUEVO_POST_EQUIPO, 1002L, 601L, null, null)));
        verifyNoInteractions(notificaciones);
    }

    @Test void falloDeTransporteSePropagaNoSeDeclaraEntrega() {
        NotificacionApi notificaciones = mock(NotificacionApi.class);
        doThrow(new IllegalStateException("Broker no disponible")).when(notificaciones)
                .enviar(anyLong(), anyString(), anyString(), anyLong(), anyMap(), isNull());
        var api = new SocialNotificacionApiImpl(notificaciones);
        assertThrows(IllegalStateException.class, () -> api.enviar(new SocialAvisoDto(UUID.randomUUID(),
                TipoAvisoSocial.SOCIAL_REACCION_POST, 1002L, 601L, null, null)));
    }
}
