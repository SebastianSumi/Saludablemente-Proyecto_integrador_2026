package upeu.edu.pe.Saludabemente.personal.persona.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import upeu.edu.pe.Saludabemente.personal.persona.dto.request.PersonaRequest;
import upeu.edu.pe.Saludabemente.personal.persona.entity.Sexo;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PersonaRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private PersonaRequest crearRequest(String nombres, String dni, String celular, LocalDate fechaNac) {
        return new PersonaRequest(
                nombres,
                "Lazaro",
                null,
                dni,
                celular,
                fechaNac,
                Sexo.F,
                null,
                null,
                null,
                null,
                null
        );
    }

    // -------------------------------------------------------------------
    // Caso feliz
    // -------------------------------------------------------------------

    @Test
    void requestConDatosValidos_noTieneViolaciones() {
        PersonaRequest request = crearRequest("Ana", "12345678", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    // -------------------------------------------------------------------
    // Celular
    // -------------------------------------------------------------------

    @Test
    void celularConFormatoInvalido_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "12345678", "abc", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("celular");
    }

    // -------------------------------------------------------------------
    // Fecha de nacimiento
    // -------------------------------------------------------------------

    @Test
    void fechaNacimientoFutura_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "12345678", "951357327", LocalDate.now().plusDays(1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("fechaNacimiento");
    }

    // -------------------------------------------------------------------
    // Nombres
    // -------------------------------------------------------------------

    @Test
    void nombresVacios_tieneViolacion() {
        PersonaRequest request = crearRequest("", "12345678", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("nombres");
    }

    // -------------------------------------------------------------------
    // Documento de identidad — 8 dígitos exactos
    // -------------------------------------------------------------------

    @Test
    void documentoIdentidadVacio_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);

        // "" dispara 2 violaciones simultáneas sobre el mismo campo:
        //   1) @NotBlank  -> no puede estar vacío
        //   2) @Pattern   -> no tiene 8 dígitos
        // Es el comportamiento estándar de Bean Validation.
        assertThat(violations).hasSize(2);
        assertThat(violations)
                .allMatch(v -> v.getPropertyPath().toString().equals("documentoIdentidad"));
    }

    @Test
    void documentoIdentidadConLetras_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "1234567A", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("documentoIdentidad");
    }

    @Test
    void documentoIdentidadMenosDe8Digitos_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "1234567", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("documentoIdentidad");
    }

    @Test
    void documentoIdentidadMasDe8Digitos_tieneViolacion() {
        PersonaRequest request = crearRequest("Ana", "123456789", "951357327", LocalDate.of(1990, 1, 1));
        Set<ConstraintViolation<PersonaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("documentoIdentidad");
    }
}