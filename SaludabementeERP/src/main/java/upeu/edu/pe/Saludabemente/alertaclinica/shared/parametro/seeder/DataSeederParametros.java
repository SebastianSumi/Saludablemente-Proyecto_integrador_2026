package upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.entity.ParametroEvaluacion;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.parametro.repository.ParametroEvaluacionRepository;

@Component
@Order(98)
@RequiredArgsConstructor
public class DataSeederParametros implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederParametros.class);

    private final ParametroEvaluacionRepository parametroRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DataSeederParametros: verificando estado de la BD ===");

        int creados = 0;

        creados += seedSiNoExiste("IMC", "Indice de masa corporal", "kg/m2",
                "Peso dividido por la estatura al cuadrado");
        creados += seedSiNoExiste("PERIMETRO_ABDOMINAL", "Perimetro abdominal", "cm",
                "Medida de la circunferencia de la cintura");
        creados += seedSiNoExiste("GRASA_CORPORAL", "Porcentaje de grasa corporal", "%",
                "Proporcion de grasa en el cuerpo total");
        creados += seedSiNoExiste("MASA_MUSCULAR", "Porcentaje de masa muscular", "%",
                "Proporcion de musculo en el cuerpo total");
        creados += seedSiNoExiste("GRASA_VISCERAL", "Nivel de grasa visceral", "escala OMRON",
                "Grasa que rodea los organos internos");
        creados += seedSiNoExiste("GLUCOSA", "Glucosa en ayunas", "mg/dL",
                "Nivel de azucar en sangre en ayunas");
        creados += seedSiNoExiste("COLESTEROL", "Colesterol total", "mg/dL",
                "Nivel de colesterol en sangre");
        creados += seedSiNoExiste("TRIGLICERIDOS", "Trigliceridos", "mg/dL",
                "Nivel de trigliceridos en sangre");
        creados += seedSiNoExiste("PRESION_ARTERIAL", "Presion arterial", "mmHg",
                "Presion sistolica y diastolica");
        creados += seedSiNoExiste("ABDOMINALES", "Abdominales en 30 segundos", "repeticiones",
                "Repeticiones de abdominales completadas en 30 segundos");
        creados += seedSiNoExiste("PLANCHAS", "Planchas en 30 segundos", "repeticiones",
                "Repeticiones de planchas completadas en 30 segundos");
        creados += seedSiNoExiste("SALTA_SOGA", "Salta soga en 30 segundos", "repeticiones",
                "Saltos de soga completados en 30 segundos");
        creados += seedSiNoExiste("SALTO_SIN_IMPULSO", "Salto sin impulso", "cm",
                "Distancia horizontal del salto sin carrera previa");
        creados += seedSiNoExiste("CARRERA_400M", "Carrera de 400 metros", "segundos",
                "Tiempo en recorrer 400 metros");

        log.info("DataSeederParametros: {} parametros nuevos creados", creados);
        log.info("=== DataSeederParametros: carga finalizada ===");
    }

    private int seedSiNoExiste(String codigo, String nombre, String unidad, String descripcion) {
        if (parametroRepository.existsByCodigo(codigo)) {
            return 0;
        }
        ParametroEvaluacion p = new ParametroEvaluacion();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setUnidad(unidad);
        p.setDescripcion(descripcion);
        p.setEstado("ACTIVO");
        parametroRepository.save(p);
        log.info("  [+] Parametro '{}' creado", codigo);
        return 1;
    }
}
