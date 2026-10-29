package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaClinica;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.entity.EtiquetaParametro;
import upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.repository.EtiquetaClinicaRepository;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;

import java.util.List;

@Component
@Order(102)
@RequiredArgsConstructor
public class DataSeederEtiquetas implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederEtiquetas.class);

    private final EtiquetaClinicaRepository etiquetaRepository;
    private final ParametroEvaluacionRepository parametroRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DataSeederEtiquetas: verificando estado de la BD ===");

        int creadas = 0;

        creadas += seedSiNoExiste(
            "METABOLICA",
            "Metabolica",
            "Alteraciones del metabolismo: glucosa, colesterol, trigliceridos, grasa visceral",
            "#856404",
            List.of("GLUCOSA", "COLESTEROL", "TRIGLICERIDOS", "GRASA_VISCERAL")
        );

        creadas += seedSiNoExiste(
            "ANTROPOMETRICA",
            "Antropometrica",
            "Medidas corporales: IMC, perimetro abdominal, grasa corporal",
            "#2e7d5b",
            List.of("IMC", "PERIMETRO_ABDOMINAL", "GRASA_CORPORAL")
        );

        creadas += seedSiNoExiste(
            "CARDIOVASCULAR",
            "Cardiovascular",
            "Presion arterial alterada",
            "#a01020",
            List.of("PRESION_ARTERIAL")
        );

        creadas += seedSiNoExiste(
            "COMPOSICION_CORPORAL",
            "Composicion corporal",
            "Distribucion de grasa y masa muscular",
            "#084298",
            List.of("GRASA_CORPORAL", "MASA_MUSCULAR", "GRASA_VISCERAL")
        );

        creadas += seedSiNoExiste(
            "RENDIMIENTO_FISICO",
            "Rendimiento fisico",
            "Resultados de pruebas fisicas",
            "#b25900",
            List.of("ABDOMINALES", "PLANCHAS", "SALTA_SOGA", "SALTO_SIN_IMPULSO", "CARRERA_400M")
        );

        log.info("DataSeederEtiquetas: {} etiquetas nuevas creadas", creadas);
        log.info("=== DataSeederEtiquetas: carga finalizada ===");
    }

    private int seedSiNoExiste(String codigo, String nombre, String descripcion,
                               String color, List<String> codigosParametros) {

        if (etiquetaRepository.existsByCodigo(codigo)) {
            return 0;
        }

        EtiquetaClinica e = new EtiquetaClinica();
        e.setCodigo(codigo);
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setColorHex(color);
        e.setEstado("ACTIVO");

        for (String codParam : codigosParametros) {
            ParametroEvaluacion param = parametroRepository.findByCodigo(codParam)
                    .orElseThrow(() -> new IllegalStateException(
                            "Parametro no encontrado: " + codParam));

            EtiquetaParametro ep = new EtiquetaParametro();
            ep.setEtiqueta(e);
            ep.setIdParametro(param.getId());
            ep.setParametroCodigo(param.getCodigo());
            ep.setParametroNombre(param.getNombre());
            ep.setParametroUnidad(param.getUnidad());

            e.getParametros().add(ep);
        }

        // Hibernate inserta etiqueta -> parametros en cascada
        etiquetaRepository.save(e);
        log.info("  [+] Etiqueta '{}' creada con {} parametros", codigo, codigosParametros.size());
        return 1;
    }
}
