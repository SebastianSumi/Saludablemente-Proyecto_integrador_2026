package upeu.edu.pe.Saludabemente.personal.credencial.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.personal.credencial.util.HashUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HashUtilTest {

    private HashUtil hashUtil;

    @BeforeEach
    void setUp() {
        hashUtil = new HashUtil("salt-de-prueba");
    }

    @Test
    void mismoTokenConMismoSalt_devuelveMismoHash() {
        String hash1 = hashUtil.generarHashSeguro("token-abc");
        String hash2 = hashUtil.generarHashSeguro("token-abc");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void distintoSalt_devuelveDistintoHash() {
        HashUtil otro = new HashUtil("otro-salt");
        String hash1 = hashUtil.generarHashSeguro("token-abc");
        String hash2 = otro.generarHashSeguro("token-abc");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void hashTiene64CaracteresHexadecimales() {
        String hash = hashUtil.generarHashSeguro("cualquier-token");
        assertThat(hash).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    void tokenVacio_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> hashUtil.generarHashSeguro(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tokenNulo_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> hashUtil.generarHashSeguro(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}