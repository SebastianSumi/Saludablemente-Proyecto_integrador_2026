package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.alerta.repository.AlertaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.dto.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.mapper.EtiquetaClinicaMapper;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.*;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;
import upeu.edu.pe.Saludabemente.exception.*;

import java.util.*;
import static org.assertj.core.api.Assertions.*;

/** Contexto aislado: transacciones reales y H2, sin Oracle, Keycloak ni servicios externos. */
@SpringJUnitConfig(AsociacionClinicaIntegrationTest.Config.class)
class AsociacionClinicaIntegrationTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {EtiquetaClinicaRepository.class,
            AlertaClinicaRepository.class, ParametroEvaluacionRepository.class})
    @Import({AsociacionClinicaServiceImpl.class, EtiquetaClinicaServiceImpl.class,
            ConsultaEtiquetaServiceImpl.class, BloqueoAsociaciones.class, EtiquetaClinicaMapper.class,
            upeu.edu.pe.Saludabemente.alertaclinica.alerta.service.AlertaClinicaServiceImpl.class})
    static class Config {
        @Bean
        upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper alertaMapper() {
            return org.mapstruct.factory.Mappers.getMapper(upeu.edu.pe.Saludabemente.alertaclinica.alerta.mapper.AlertaClinicaMapper.class);
        }
        @Bean
        javax.sql.DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:asociaciones;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS SLB_ALERTAS", "sa", "");
        }
        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(javax.sql.DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            factory.setPackagesToScan("upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity",
                    "upeu.edu.pe.Saludabemente.alertaclinica.alerta.entity",
                    "upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity");
            factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
            return factory;
        }
        @Bean
        PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    @Autowired AsociacionClinicaService service;
    @Autowired EtiquetaClinicaService catalogo;
    @Autowired ConsultaEtiquetaService consultas;
    @Autowired EtiquetaClinicaRepository etiquetas;
    @Autowired ParametroEvaluacionRepository parametros;
    @Autowired AlertaClinicaRepository alertas;
    @Autowired AlertaEtiquetaRepository asociaciones;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired upeu.edu.pe.Saludabemente.alertaclinica.alerta.service.AlertaClinicaService alertaService;
    Long etiquetaId, segundaEtiquetaId, alertaId, parametroId;

    @BeforeEach
    void preparar() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            asociaciones.deleteAll(); alertas.deleteAll(); etiquetas.deleteAll(); parametros.deleteAll();
        });
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var parametro = new ParametroEvaluacion();
            parametro.setCodigo("IMC"); parametro.setNombre("Indice de masa corporal"); parametro.setUnidad("kg/m2");
            parametroId = parametros.saveAndFlush(parametro).getId();
            etiquetaId = etiqueta("RIESGO"); segundaEtiquetaId = etiqueta("SEGUIMIENTO");
            var alerta = new AlertaClinica();
            alerta.setPersonaId(100L); alerta.setEvaluacionOrigenId(200L);
            alerta.setTipoEvaluacionOrigen(TipoEvaluacionOrigen.NUTRICIONAL);
            alerta.setPuntajeTotal(5); alerta.setNivelSeveridad(SeveridadAlerta.CRITICA);
            alertaId = alertas.saveAndFlush(alerta).getIdAlerta();
        });
    }

    Long etiqueta(String codigo) {
        var e = new EtiquetaClinica(); e.setCodigo(codigo); e.setNombre(codigo); e.setColorHex("#123456");
        return etiquetas.saveAndFlush(e).getId();
    }

    @Test
    void guardaVariasEtiquetasYLaVistaInversaConservaHistoria() {
        var inicial = service.etiquetasDeAlerta(alertaId);
        var guardado = service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(inicial.revision(),
                List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(segundaEtiquetaId))));
        assertThat(guardado.detalles()).hasSize(2).allSatisfy(d -> {
            assertThat(d.asignadaPor()).isEqualTo("MANUAL"); assertThat(d.fechaAsignacion()).isNotNull();
        });
        var fecha = guardado.detalles().getFirst().fechaAsignacion();
        var inversa = service.alertasDeEtiqueta(etiquetaId);
        service.guardarAlertasDeEtiqueta(etiquetaId, new GuardarAlertasRequest(inversa.revision(), List.of(new AlertaDetalleRequest(alertaId))));
        assertThat(service.etiquetasDeAlerta(alertaId).detalles().getFirst().fechaAsignacion()).isEqualTo(fecha);
        var otra = service.alertasDeEtiqueta(segundaEtiquetaId);
        service.guardarAlertasDeEtiqueta(segundaEtiquetaId, new GuardarAlertasRequest(otra.revision(), List.of()));
        assertThat(service.etiquetasDeAlerta(alertaId).detalles()).hasSize(1);
    }

    @Test
    void errorEnUnaFilaNoGuardaNingunaYRechazaDuplicados() {
        var revision = service.etiquetasDeAlerta(alertaId).revision();
        assertThatThrownBy(() -> service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(revision,
                List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(999999L)))))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(service.etiquetasDeAlerta(alertaId).detalles()).isEmpty();
        assertThatThrownBy(() -> service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(revision,
                List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(etiquetaId)))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void motorNoSeRetiraDesdeNingunLado() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            var a = new AlertaEtiqueta(); a.setAlerta(alertas.findById(alertaId).orElseThrow());
            a.setIdEtiqueta(etiquetaId); a.setEtiquetaCodigo("RIESGO"); a.setEtiquetaNombre("RIESGO");
            a.setEtiquetaColorHex("#123456"); asociaciones.saveAndFlush(a);
        });
        var directa = service.etiquetasDeAlerta(alertaId);
        assertThatThrownBy(() -> service.guardarEtiquetasDeAlerta(alertaId,
                new GuardarEtiquetasRequest(directa.revision(), List.of()))).isInstanceOf(BusinessRuleException.class);
        var inversa = service.alertasDeEtiqueta(etiquetaId);
        assertThatThrownBy(() -> service.guardarAlertasDeEtiqueta(etiquetaId,
                new GuardarAlertasRequest(inversa.revision(), List.of()))).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> alertaService.quitarEtiqueta(alertaId, etiquetaId)).isInstanceOf(BusinessRuleException.class);
        assertThat(service.etiquetasDeAlerta(alertaId).detalles()).hasSize(1);
    }

    @Test
    void rechazaRevisionObsoletaYEtiquetaInactiva() {
        var revision = service.etiquetasDeAlerta(alertaId).revision();
        service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(revision, List.of(new EtiquetaDetalleRequest(etiquetaId))));
        assertThatThrownBy(() -> service.guardarEtiquetasDeAlerta(alertaId,
                new GuardarEtiquetasRequest(revision, List.of()))).isInstanceOf(BusinessRuleException.class);
        catalogo.desactivar(segundaEtiquetaId);
        assertThatThrownBy(() -> service.guardarEtiquetasDeAlerta(alertaId,
                new GuardarEtiquetasRequest(service.etiquetasDeAlerta(alertaId).revision(),
                        List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(segundaEtiquetaId)))))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(service.etiquetasDeAlerta(alertaId).detalles()).hasSize(1);
    }

    @Test
    void parametrosSeEditanDesdeAmbosLadosSinPerderFotocopia() {
        var inicial = service.parametrosDeEtiqueta(etiquetaId);
        service.guardarParametrosDeEtiqueta(etiquetaId, new GuardarParametrosRequest(inicial.revision(), List.of(new ParametroDetalleRequest(parametroId))));
        var inversa = service.etiquetasDeParametro(parametroId);
        assertThat(inversa.detalles()).extracting(EtiquetaResumenDto::id).containsExactly(etiquetaId);
        service.guardarEtiquetasDeParametro(parametroId, new GuardarEtiquetasRequest(inversa.revision(),
                List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(segundaEtiquetaId))));
        assertThat(service.parametrosDeEtiqueta(segundaEtiquetaId).detalles()).hasSize(1);
        var revision = service.etiquetasDeParametro(parametroId).revision();
        service.guardarEtiquetasDeParametro(parametroId, new GuardarEtiquetasRequest(revision, List.of()));
        assertThat(service.parametrosDeEtiqueta(etiquetaId).detalles()).isEmpty();
        assertThat(service.parametrosDeEtiqueta(segundaEtiquetaId).detalles()).isEmpty();
    }

    @Test
    void catalogoUsaDetalleCompuestoYProtegeRevision() {
        var creada = catalogo.crear(new EtiquetaClinicaRequestDto("NUEVA", "Nueva", null, "#123456",
                List.of(new ParametroDetalleRequest(parametroId)), null));
        assertThat(creada.parametros()).hasSize(1);
        var editada = catalogo.actualizar(creada.id(), new EtiquetaClinicaRequestDto("NUEVA", "Editada", null,
                "#654321", List.of(), creada.revision()));
        assertThat(editada.parametros()).isEmpty();
        assertThatThrownBy(() -> catalogo.actualizar(creada.id(), new EtiquetaClinicaRequestDto("NUEVA", "Vieja", null,
                "#123456", List.of(), creada.revision()))).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void unaExcepcionPosteriorALaPersistenciaRevierteTodaLaOperacion() {
        var revision = service.etiquetasDeAlerta(alertaId).revision();
        assertThatThrownBy(() -> new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(revision,
                    List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(segundaEtiquetaId))));
            assertThat(asociaciones.findEtiquetasByAlerta(alertaId)).hasSize(2);
            throw new BusinessRuleException("Fallo posterior a flush");
        })).isInstanceOf(BusinessRuleException.class);
        assertThat(service.etiquetasDeAlerta(alertaId).detalles()).isEmpty();
    }

    @Test
    void agregarAlertaDesdeEtiquetaFuncionaYLaFotocopiaNoCambiaAlRenombrar() {
        var inicial = service.alertasDeEtiqueta(etiquetaId);
        service.guardarAlertasDeEtiqueta(etiquetaId, new GuardarAlertasRequest(inicial.revision(),
                List.of(new AlertaDetalleRequest(alertaId))));
        var etiqueta = catalogo.obtener(etiquetaId);
        catalogo.actualizar(etiquetaId, new EtiquetaClinicaRequestDto(etiqueta.codigo(), "Nombre nuevo", null,
                "#654321", List.of(), etiqueta.revision()));
        var asignada = service.etiquetasDeAlerta(alertaId).detalles().getFirst();
        assertThat(asignada.nombre()).isEqualTo("RIESGO");
        assertThat(asignada.colorHex()).isEqualTo("#123456");
    }

    @Test
    void revisionDeParametrosDetectaEdicionDesdeElLadoInverso() {
        var inicial = service.parametrosDeEtiqueta(etiquetaId);
        var inversa = service.etiquetasDeParametro(parametroId);
        service.guardarEtiquetasDeParametro(parametroId, new GuardarEtiquetasRequest(inversa.revision(),
                List.of(new EtiquetaDetalleRequest(etiquetaId))));
        assertThatThrownBy(() -> service.guardarParametrosDeEtiqueta(etiquetaId,
                new GuardarParametrosRequest(inicial.revision(), List.of()))).isInstanceOf(BusinessRuleException.class);
        assertThat(service.parametrosDeEtiqueta(etiquetaId).detalles()).hasSize(1);
    }

    @Test
    void dosGuardadosSimultaneosNoSeSobrescriben() throws Exception {
        var revision = service.etiquetasDeAlerta(alertaId).revision();
        var inicio = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var resultados = new ArrayList<java.util.concurrent.Future<Boolean>>();
            for (Long id : List.of(etiquetaId, segundaEtiquetaId)) {
                resultados.add(executor.submit(() -> {
                    inicio.await();
                    try {
                        service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(revision,
                                List.of(new EtiquetaDetalleRequest(id))));
                        return true;
                    } catch (BusinessRuleException ex) { return false; }
                }));
            }
            inicio.countDown();
            int exitos = 0;
            for (var resultado : resultados) if (resultado.get(15, java.util.concurrent.TimeUnit.SECONDS)) exitos++;
            assertThat(exitos).isEqualTo(1);
            assertThat(service.etiquetasDeAlerta(alertaId).detalles()).hasSize(1);
        }
    }

    @Test
    void opcionesPaginadasYFiltrosDeReporteSonCoherentes() {
        var pagina = alertaService.buscarOpciones(100L, EstadoAlerta.PENDIENTE, SeveridadAlerta.CRITICA, null, null, 0, 10);
        assertThat(pagina.getTotalElements()).isEqualTo(1);
        service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(service.etiquetasDeAlerta(alertaId).revision(),
                List.of(new EtiquetaDetalleRequest(etiquetaId))));
        var reporte = consultas.reporte(null, etiquetaId, null, EstadoAlerta.ATENDIDA, null, null, null);
        assertThat(reporte.cantidadAsociaciones()).isZero();
        assertThat(reporte.alertasUnicas()).isZero();
        assertThat(reporte.detalles()).hasSize(1);
        assertThatThrownBy(() -> consultas.reporte(null, null, null, null, null,
                java.time.LocalDateTime.now(), java.time.LocalDateTime.now().minusDays(1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void consultaPropiaFiltraLaPertenenciaEnBaseDeDatos() {
        assertThat(alertas.findByIdAlertaAndPersonaId(alertaId, 100L)).isPresent();
        assertThat(alertas.findByIdAlertaAndPersonaId(alertaId, 999L)).isEmpty();
    }

    @Test
    void reporteCuentaAlertasUnicasYConsultaPaginadaTieneTotalesReales() {
        service.guardarEtiquetasDeAlerta(alertaId, new GuardarEtiquetasRequest(service.etiquetasDeAlerta(alertaId).revision(),
                List.of(new EtiquetaDetalleRequest(etiquetaId), new EtiquetaDetalleRequest(segundaEtiquetaId))));
        var reporte = consultas.reporte(null, null, null, null, null, null, null);
        assertThat(reporte.cantidadAsociaciones()).isEqualTo(2);
        assertThat(reporte.alertasUnicas()).isEqualTo(1);
        assertThat(reporte.alertasCriticasUnicas()).isEqualTo(1);
        var pagina = consultas.buscar("ACTIVO", null, null, 0, 1);
        assertThat(pagina.getTotalElements()).isEqualTo(2);
        assertThat(pagina.getContent()).hasSize(1);
        assertThat(pagina.getContent().getFirst().cantidadAlertas()).isEqualTo(1);
    }
}
