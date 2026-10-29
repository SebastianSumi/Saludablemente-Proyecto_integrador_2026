package upeu.edu.pe.Saludabemente.noticias.shared.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.noticias.categoria.entity.CategoriaEntity;
import upeu.edu.pe.Saludabemente.noticias.categoria.repository.CategoriaRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoriaSeeder implements ApplicationRunner {

    private final CategoriaRepository categoriaRepository;

    private static final List<CategoriaEntity> CATEGORIAS_OFICIALES = List.of(
            CategoriaEntity.builder()
                    .nombre("Nutrición e Hidratación")
                    .descripcion("Consejos de nutrición y hábitos saludables")
                    .colorHex("#4CAF50")
                    .icono("nutrition")
                    .orden(1)
                    .esSistema(true)
                    .activo(true)
                    .build(),
            CategoriaEntity.builder()
                    .nombre("Actividad Física y Movimiento")
                    .descripcion("Rutinas, pausas activas y deporte")
                    .colorHex("#2196F3")
                    .icono("activity")
                    .orden(2)
                    .esSistema(true)
                    .activo(true)
                    .build(),
            CategoriaEntity.builder()
                    .nombre("Descanso y Salud Mental")
                    .descripcion("Bienestar emocional y descanso")
                    .colorHex("#9C27B0")
                    .icono("mental-health")
                    .orden(3)
                    .esSistema(true)
                    .activo(true)
                    .build(),
            CategoriaEntity.builder()
                    .nombre("Comunicados Generales / Institucionales")
                    .descripcion("Avisos oficiales de la organización")
                    .colorHex("#FF9800")
                    .icono("announcement")
                    .orden(4)
                    .esSistema(true)
                    .activo(true)
                    .build()
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int insertadas = 0;
        for (CategoriaEntity cat : CATEGORIAS_OFICIALES) {
            if (!categoriaRepository.existsByNombre(cat.getNombre())) {
                categoriaRepository.save(cat);
                insertadas++;
            }
        }
        if (insertadas > 0) {
            log.info("Seeder: {} categorías oficiales de Noticias insertadas", insertadas);
        } else {
            log.debug("Seeder: categorías oficiales de Noticias ya existentes");
        }
    }
}