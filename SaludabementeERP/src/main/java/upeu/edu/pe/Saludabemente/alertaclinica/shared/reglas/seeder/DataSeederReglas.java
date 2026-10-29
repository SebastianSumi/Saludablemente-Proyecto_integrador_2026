package upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.seeder;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.aptitud_fisica.repository.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.DireccionRiesgo;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.EstadoRegla;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.SexoBiologico;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.enums.clasificaciones.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.entity.*;
import upeu.edu.pe.Saludabemente.alertaclinica.shared.reglas.nutricional.repository.*;

import java.math.BigDecimal;

@Component
@Order(100)
@RequiredArgsConstructor
public class DataSeederReglas implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederReglas.class);

    // Nutricionales
    private final ReglaImcRepository imcRepo;
    private final ReglaPerimetroAbdominalRepository perimetroRepo;
    private final ReglaPorcentajeGrasaRepository grasaRepo;
    private final ReglaMasaMuscularRepository musculoRepo;
    private final ReglaGrasaVisceralRepository visceralRepo;
    private final ReglaGlucosaRepository glucosaRepo;
    private final ReglaColesterolRepository colesterolRepo;
    private final ReglaTrigliceridosRepository trigliceridosRepo;
    private final ReglaPresionArterialRepository presionRepo;

    // Aptitud fisica
    private final ReglaAbdominalesRepository abdominalesRepo;
    private final ReglaSaltoSinImpulsoRepository saltoRepo;
    private final ReglaPlanchasRepository planchasRepo;
    private final ReglaSaltaSogaRepository sogaRepo;
    private final ReglaCarrera400mRepository carreraRepo;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== DataSeederReglas: verificando estado de la BD ===");

        if (imcRepo.count() > 0) {
            log.info("DataSeederReglas: la BD ya tiene reglas cargadas. Se omite el seed.");
            return;
        }

        seedImc();
        seedPerimetro();
        seedPorcentajeGrasa();
        seedMasaMuscular();
        seedGrasaVisceral();
        seedGlucosa();
        seedColesterol();
        seedTrigliceridos();
        seedPresion();
        seedAbdominales();
        seedSaltoSinImpulso();
        seedPlanchas();
        seedSaltaSoga();
        seedCarrera400m();

        log.info("DataSeederReglas: 152 reglas OMS/OMRON cargadas");
        log.info("=== DataSeederReglas: carga finalizada ===");
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    // ============================================================
    // 1. REGLA_IMC — 8
    // ============================================================
    private void seedImc() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            imcRepo.save(nuevaImc(sexo, 18, 100, "0.00", "18.49", 2, ClasificacionImc.DELGADEZ));
            imcRepo.save(nuevaImc(sexo, 18, 100, "18.50", "24.99", 0, ClasificacionImc.NORMAL));
            imcRepo.save(nuevaImc(sexo, 18, 100, "25.00", "29.99", 1, ClasificacionImc.SOBREPESO));
            imcRepo.save(nuevaImc(sexo, 18, 100, "30.00", "60.00", 2, ClasificacionImc.OBESIDAD));
        }
        log.info("  regla_imc: 8 reglas");
    }

    private ReglaImc nuevaImc(SexoBiologico sexo, int eMin, int eMax,
                              String vMin, String vMax, int puntos, ClasificacionImc clas) {
        ReglaImc r = new ReglaImc();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setImcMin(bd(vMin)); r.setImcMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 2. REGLA_PERIMETRO_ABDOMINAL — 6
    // ============================================================
    private void seedPerimetro() {
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.M, 18, 100, "0.00", "93.99", 0, ClasificacionPerimetroAbdominal.RIESGO_BAJO));
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.M, 18, 100, "94.00", "101.99", 1, ClasificacionPerimetroAbdominal.RIESGO_ALTO));
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.M, 18, 100, "102.00", "300.00", 2, ClasificacionPerimetroAbdominal.RIESGO_MUY_ALTO));
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.F, 18, 100, "0.00", "79.99", 0, ClasificacionPerimetroAbdominal.RIESGO_BAJO));
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.F, 18, 100, "80.00", "87.99", 1, ClasificacionPerimetroAbdominal.RIESGO_ALTO));
        perimetroRepo.save(nuevaPerimetro(SexoBiologico.F, 18, 100, "88.00", "300.00", 2, ClasificacionPerimetroAbdominal.RIESGO_MUY_ALTO));
        log.info("  regla_perimetro_abdominal: 6 reglas");
    }

    private ReglaPerimetroAbdominal nuevaPerimetro(SexoBiologico sexo, int eMin, int eMax,
                                                   String vMin, String vMax, int puntos,
                                                   ClasificacionPerimetroAbdominal clas) {
        ReglaPerimetroAbdominal r = new ReglaPerimetroAbdominal();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setPerimetroCmMin(bd(vMin)); r.setPerimetroCmMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 3. REGLA_PORCENTAJE_GRASA — 24
    // ============================================================
    private void seedPorcentajeGrasa() {
        addGrasa(SexoBiologico.F, 20, 39, "0.00", "20.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.F, 20, 39, "21.00", "32.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.F, 20, 39, "33.00", "38.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.F, 20, 39, "39.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        addGrasa(SexoBiologico.F, 40, 59, "0.00", "22.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.F, 40, 59, "23.00", "33.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.F, 40, 59, "34.00", "39.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.F, 40, 59, "40.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        addGrasa(SexoBiologico.F, 60, 80, "0.00", "23.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.F, 60, 80, "24.00", "35.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.F, 60, 80, "36.00", "41.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.F, 60, 80, "42.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        addGrasa(SexoBiologico.M, 20, 39, "0.00", "7.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.M, 20, 39, "8.00", "19.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.M, 20, 39, "20.00", "24.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.M, 20, 39, "25.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        addGrasa(SexoBiologico.M, 40, 59, "0.00", "10.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.M, 40, 59, "11.00", "21.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.M, 40, 59, "22.00", "27.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.M, 40, 59, "28.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        addGrasa(SexoBiologico.M, 60, 80, "0.00", "12.99", 1, ClasificacionPorcentajeGrasa.BAJO);
        addGrasa(SexoBiologico.M, 60, 80, "13.00", "24.99", 0, ClasificacionPorcentajeGrasa.NORMAL);
        addGrasa(SexoBiologico.M, 60, 80, "25.00", "29.99", 1, ClasificacionPorcentajeGrasa.ALTO);
        addGrasa(SexoBiologico.M, 60, 80, "30.00", "100.00", 2, ClasificacionPorcentajeGrasa.MUY_ALTO);

        log.info("  regla_porcentaje_grasa: 24 reglas");
    }

    private void addGrasa(SexoBiologico sexo, int eMin, int eMax,
                          String vMin, String vMax, int puntos,
                          ClasificacionPorcentajeGrasa clas) {
        ReglaPorcentajeGrasa r = new ReglaPorcentajeGrasa();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setPorcentajeMin(bd(vMin)); r.setPorcentajeMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        grasaRepo.save(r);
    }

    // ============================================================
    // 4. REGLA_MASA_MUSCULAR — 24
    // ============================================================
    private void seedMasaMuscular() {
        addMusculo(SexoBiologico.F, 18, 39, "0.00", "24.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.F, 18, 39, "24.30", "30.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.F, 18, 39, "30.40", "35.30", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.F, 18, 39, "35.40", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        addMusculo(SexoBiologico.F, 40, 59, "0.00", "24.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.F, 40, 59, "24.30", "30.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.F, 40, 59, "30.40", "35.30", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.F, 40, 59, "35.40", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        addMusculo(SexoBiologico.F, 60, 80, "0.00", "24.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.F, 60, 80, "24.30", "30.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.F, 60, 80, "30.40", "35.30", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.F, 60, 80, "35.40", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        addMusculo(SexoBiologico.M, 18, 39, "0.00", "33.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.M, 18, 39, "33.30", "39.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.M, 18, 39, "39.40", "43.60", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.M, 18, 39, "43.70", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        addMusculo(SexoBiologico.M, 40, 59, "0.00", "33.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.M, 40, 59, "33.30", "39.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.M, 40, 59, "39.40", "43.60", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.M, 40, 59, "43.70", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        addMusculo(SexoBiologico.M, 60, 80, "0.00", "33.29", 1, ClasificacionMasaMuscular.BAJO);
        addMusculo(SexoBiologico.M, 60, 80, "33.30", "39.30", 0, ClasificacionMasaMuscular.NORMAL);
        addMusculo(SexoBiologico.M, 60, 80, "39.40", "43.60", 0, ClasificacionMasaMuscular.ELEVADO);
        addMusculo(SexoBiologico.M, 60, 80, "43.70", "100.00", 0, ClasificacionMasaMuscular.MUY_ELEVADO);

        log.info("  regla_masa_muscular: 24 reglas");
    }

    private void addMusculo(SexoBiologico sexo, int eMin, int eMax,
                            String vMin, String vMax, int puntos,
                            ClasificacionMasaMuscular clas) {
        ReglaMasaMuscular r = new ReglaMasaMuscular();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setPorcentajeMin(bd(vMin)); r.setPorcentajeMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        musculoRepo.save(r);
    }

    // ============================================================
    // 5. REGLA_GRASA_VISCERAL — 6
    // ============================================================
    private void seedGrasaVisceral() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            visceralRepo.save(nuevaVisceral(sexo, 18, 100, "1.00", "9.00", 0, ClasificacionGrasaVisceral.NORMAL));
            visceralRepo.save(nuevaVisceral(sexo, 18, 100, "10.00", "14.00", 1, ClasificacionGrasaVisceral.ALTO));
            visceralRepo.save(nuevaVisceral(sexo, 18, 100, "15.00", "50.00", 2, ClasificacionGrasaVisceral.MUY_ALTO));
        }
        log.info("  regla_grasa_visceral: 6 reglas");
    }

    private ReglaGrasaVisceral nuevaVisceral(SexoBiologico sexo, int eMin, int eMax,
                                             String vMin, String vMax, int puntos,
                                             ClasificacionGrasaVisceral clas) {
        ReglaGrasaVisceral r = new ReglaGrasaVisceral();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setNivelMin(bd(vMin)); r.setNivelMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 6. REGLA_GLUCOSA — 8
    // NOTA: valor maximo 1000.00 exige precision 6,2 en la entidad
    // ============================================================
    private void seedGlucosa() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            glucosaRepo.save(nuevaGlucosa(sexo, 18, 100, "0.00", "69.99", 1, ClasificacionGlucosa.HIPOGLUCEMIA));
            glucosaRepo.save(nuevaGlucosa(sexo, 18, 100, "70.00", "109.99", 0, ClasificacionGlucosa.NORMAL));
            glucosaRepo.save(nuevaGlucosa(sexo, 18, 100, "110.00", "125.99", 1, ClasificacionGlucosa.PREDIABETES));
            glucosaRepo.save(nuevaGlucosa(sexo, 18, 100, "126.00", "1000.00", 2, ClasificacionGlucosa.DIABETES));
        }
        log.info("  regla_glucosa: 8 reglas");
    }

    private ReglaGlucosa nuevaGlucosa(SexoBiologico sexo, int eMin, int eMax,
                                      String vMin, String vMax, int puntos,
                                      ClasificacionGlucosa clas) {
        ReglaGlucosa r = new ReglaGlucosa();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setGlucosaMgDlMin(bd(vMin)); r.setGlucosaMgDlMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 7. REGLA_COLESTEROL — 6
    // NOTA: valor maximo 1000.00 exige precision 6,2 en la entidad
    // ============================================================
    private void seedColesterol() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            colesterolRepo.save(nuevaColesterol(sexo, 18, 100, "0.00", "199.99", 0, ClasificacionColesterol.DESEABLE));
            colesterolRepo.save(nuevaColesterol(sexo, 18, 100, "200.00", "239.99", 1, ClasificacionColesterol.LIMITE));
            colesterolRepo.save(nuevaColesterol(sexo, 18, 100, "240.00", "1000.00", 2, ClasificacionColesterol.ALTO));
        }
        log.info("  regla_colesterol: 6 reglas");
    }

    private ReglaColesterol nuevaColesterol(SexoBiologico sexo, int eMin, int eMax,
                                            String vMin, String vMax, int puntos,
                                            ClasificacionColesterol clas) {
        ReglaColesterol r = new ReglaColesterol();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setColesterolMgDlMin(bd(vMin)); r.setColesterolMgDlMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 8. REGLA_TRIGLICERIDOS — 8
    // ============================================================
    private void seedTrigliceridos() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            trigliceridosRepo.save(nuevaTrigliceridos(sexo, 18, 100, "0.00", "149.99", 0, ClasificacionTrigliceridos.NORMAL));
            trigliceridosRepo.save(nuevaTrigliceridos(sexo, 18, 100, "150.00", "199.99", 1, ClasificacionTrigliceridos.LIMITE));
            trigliceridosRepo.save(nuevaTrigliceridos(sexo, 18, 100, "200.00", "499.99", 2, ClasificacionTrigliceridos.ALTO));
            trigliceridosRepo.save(nuevaTrigliceridos(sexo, 18, 100, "500.00", "2000.00", 3, ClasificacionTrigliceridos.MUY_ALTO));
        }
        log.info("  regla_trigliceridos: 8 reglas");
    }

    private ReglaTrigliceridos nuevaTrigliceridos(SexoBiologico sexo, int eMin, int eMax,
                                                  String vMin, String vMax, int puntos,
                                                  ClasificacionTrigliceridos clas) {
        ReglaTrigliceridos r = new ReglaTrigliceridos();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setTrigliceridosMgDlMin(bd(vMin)); r.setTrigliceridosMgDlMax(bd(vMax));
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 9. REGLA_PRESION_ARTERIAL — 12
    // ============================================================
    private void seedPresion() {
        for (SexoBiologico sexo : new SexoBiologico[]{SexoBiologico.M, SexoBiologico.F}) {
            presionRepo.save(nuevaPresion(sexo, 18, 100, 0, 89, 0, 59, 1, ClasificacionPresionArterial.HIPOTENSION));
            presionRepo.save(nuevaPresion(sexo, 18, 100, 90, 119, 60, 79, 0, ClasificacionPresionArterial.NORMAL));
            presionRepo.save(nuevaPresion(sexo, 18, 100, 120, 129, 60, 79, 1, ClasificacionPresionArterial.ELEVADA));
            presionRepo.save(nuevaPresion(sexo, 18, 100, 130, 139, 80, 89, 2, ClasificacionPresionArterial.HIPERTENSION_1));
            presionRepo.save(nuevaPresion(sexo, 18, 100, 140, 179, 90, 120, 3, ClasificacionPresionArterial.HIPERTENSION_2));
            presionRepo.save(nuevaPresion(sexo, 18, 100, 180, 300, 120, 200, 4, ClasificacionPresionArterial.CRISIS_HIPERTENSIVA));
        }
        log.info("  regla_presion_arterial: 12 reglas");
    }

    private ReglaPresionArterial nuevaPresion(SexoBiologico sexo, int eMin, int eMax,
                                              int sMin, int sMax, int dMin, int dMax,
                                              int puntos, ClasificacionPresionArterial clas) {
        ReglaPresionArterial r = new ReglaPresionArterial();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setSistolicaMmHgMin(sMin); r.setSistolicaMmHgMax(sMax);
        r.setDiastolicaMmHgMin(dMin); r.setDiastolicaMmHgMax(dMax);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        return r;
    }

    // ============================================================
    // 10. REGLA_ABDOMINALES — 10
    // ============================================================
    private void seedAbdominales() {
        addAbdominales(SexoBiologico.M, 18, 29, 0, 16, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addAbdominales(SexoBiologico.M, 18, 29, 17, 19, 1, ClasificacionPruebaFisica.BAJO);
        addAbdominales(SexoBiologico.M, 18, 29, 20, 25, 0, ClasificacionPruebaFisica.NORMAL);
        addAbdominales(SexoBiologico.M, 18, 29, 26, 30, 0, ClasificacionPruebaFisica.BUENO);
        addAbdominales(SexoBiologico.M, 18, 29, 31, 200, 0, ClasificacionPruebaFisica.EXCELENTE);

        addAbdominales(SexoBiologico.F, 18, 29, 0, 8, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addAbdominales(SexoBiologico.F, 18, 29, 9, 14, 1, ClasificacionPruebaFisica.BAJO);
        addAbdominales(SexoBiologico.F, 18, 29, 15, 20, 0, ClasificacionPruebaFisica.NORMAL);
        addAbdominales(SexoBiologico.F, 18, 29, 21, 25, 0, ClasificacionPruebaFisica.BUENO);
        addAbdominales(SexoBiologico.F, 18, 29, 26, 200, 0, ClasificacionPruebaFisica.EXCELENTE);

        log.info("  regla_abdominales: 10 reglas");
    }

    private void addAbdominales(SexoBiologico sexo, int eMin, int eMax,
                                int vMin, int vMax, int puntos, ClasificacionPruebaFisica clas) {
        ReglaAbdominales r = new ReglaAbdominales();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setRepeticionesMin(vMin); r.setRepeticionesMax(vMax);
        r.setDireccionRiesgo(DireccionRiesgo.ABAJO);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        abdominalesRepo.save(r);
    }

    // ============================================================
    // 11. REGLA_SALTO_SIN_IMPULSO — 10
    // ============================================================
    private void seedSaltoSinImpulso() {
        addSalto(SexoBiologico.M, 18, 29, "0.00", "150.00", 2, ClasificacionPruebaFisica.MUY_BAJO);
        addSalto(SexoBiologico.M, 18, 29, "150.01", "180.00", 1, ClasificacionPruebaFisica.BAJO);
        addSalto(SexoBiologico.M, 18, 29, "180.01", "210.00", 0, ClasificacionPruebaFisica.NORMAL);
        addSalto(SexoBiologico.M, 18, 29, "210.01", "240.00", 0, ClasificacionPruebaFisica.BUENO);
        addSalto(SexoBiologico.M, 18, 29, "240.01", "400.00", 0, ClasificacionPruebaFisica.EXCELENTE);

        addSalto(SexoBiologico.F, 18, 29, "0.00", "120.00", 2, ClasificacionPruebaFisica.MUY_BAJO);
        addSalto(SexoBiologico.F, 18, 29, "120.01", "150.00", 1, ClasificacionPruebaFisica.BAJO);
        addSalto(SexoBiologico.F, 18, 29, "150.01", "175.00", 0, ClasificacionPruebaFisica.NORMAL);
        addSalto(SexoBiologico.F, 18, 29, "175.01", "200.00", 0, ClasificacionPruebaFisica.BUENO);
        addSalto(SexoBiologico.F, 18, 29, "200.01", "400.00", 0, ClasificacionPruebaFisica.EXCELENTE);

        log.info("  regla_salto_sin_impulso: 10 reglas");
    }

    private void addSalto(SexoBiologico sexo, int eMin, int eMax,
                          String vMin, String vMax, int puntos, ClasificacionPruebaFisica clas) {
        ReglaSaltoSinImpulso r = new ReglaSaltoSinImpulso();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setDistanciaCmMin(bd(vMin)); r.setDistanciaCmMax(bd(vMax));
        r.setDireccionRiesgo(DireccionRiesgo.ABAJO);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        saltoRepo.save(r);
    }

    // ============================================================
    // 12. REGLA_PLANCHAS — 10
    // ============================================================
    private void seedPlanchas() {
        addPlanchas(SexoBiologico.M, 18, 29, 0, 9, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addPlanchas(SexoBiologico.M, 18, 29, 10, 14, 1, ClasificacionPruebaFisica.BAJO);
        addPlanchas(SexoBiologico.M, 18, 29, 15, 20, 0, ClasificacionPruebaFisica.NORMAL);
        addPlanchas(SexoBiologico.M, 18, 29, 21, 25, 0, ClasificacionPruebaFisica.BUENO);
        addPlanchas(SexoBiologico.M, 18, 29, 26, 300, 0, ClasificacionPruebaFisica.EXCELENTE);

        addPlanchas(SexoBiologico.F, 18, 29, 0, 5, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addPlanchas(SexoBiologico.F, 18, 29, 6, 9, 1, ClasificacionPruebaFisica.BAJO);
        addPlanchas(SexoBiologico.F, 18, 29, 10, 14, 0, ClasificacionPruebaFisica.NORMAL);
        addPlanchas(SexoBiologico.F, 18, 29, 15, 19, 0, ClasificacionPruebaFisica.BUENO);
        addPlanchas(SexoBiologico.F, 18, 29, 20, 300, 0, ClasificacionPruebaFisica.EXCELENTE);

        log.info("  regla_planchas: 10 reglas");
    }

    private void addPlanchas(SexoBiologico sexo, int eMin, int eMax,
                             int vMin, int vMax, int puntos, ClasificacionPruebaFisica clas) {
        ReglaPlanchas r = new ReglaPlanchas();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setRepeticionesMin(vMin); r.setRepeticionesMax(vMax);
        r.setDireccionRiesgo(DireccionRiesgo.ABAJO);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        planchasRepo.save(r);
    }

    // ============================================================
    // 13. REGLA_SALTA_SOGA — 10
    // ============================================================
    private void seedSaltaSoga() {
        addSoga(SexoBiologico.M, 18, 29, 0, 29, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addSoga(SexoBiologico.M, 18, 29, 30, 39, 1, ClasificacionPruebaFisica.BAJO);
        addSoga(SexoBiologico.M, 18, 29, 40, 49, 0, ClasificacionPruebaFisica.NORMAL);
        addSoga(SexoBiologico.M, 18, 29, 50, 59, 0, ClasificacionPruebaFisica.BUENO);
        addSoga(SexoBiologico.M, 18, 29, 60, 500, 0, ClasificacionPruebaFisica.EXCELENTE);

        addSoga(SexoBiologico.F, 18, 29, 0, 24, 2, ClasificacionPruebaFisica.MUY_BAJO);
        addSoga(SexoBiologico.F, 18, 29, 25, 34, 1, ClasificacionPruebaFisica.BAJO);
        addSoga(SexoBiologico.F, 18, 29, 35, 44, 0, ClasificacionPruebaFisica.NORMAL);
        addSoga(SexoBiologico.F, 18, 29, 45, 54, 0, ClasificacionPruebaFisica.BUENO);
        addSoga(SexoBiologico.F, 18, 29, 55, 500, 0, ClasificacionPruebaFisica.EXCELENTE);

        log.info("  regla_salta_soga: 10 reglas");
    }

    private void addSoga(SexoBiologico sexo, int eMin, int eMax,
                         int vMin, int vMax, int puntos, ClasificacionPruebaFisica clas) {
        ReglaSaltaSoga r = new ReglaSaltaSoga();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setSaltosMin(vMin); r.setSaltosMax(vMax);
        r.setDireccionRiesgo(DireccionRiesgo.ABAJO);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        sogaRepo.save(r);
    }

    // ============================================================
    // 14. REGLA_CARRERA_400M — 10
    // ============================================================
    private void seedCarrera400m() {
        addCarrera(SexoBiologico.M, 18, 29, 30, 59, 0, ClasificacionCarrera400m.EXCELENTE);
        addCarrera(SexoBiologico.M, 18, 29, 60, 75, 0, ClasificacionCarrera400m.BUENO);
        addCarrera(SexoBiologico.M, 18, 29, 76, 90, 0, ClasificacionCarrera400m.NORMAL);
        addCarrera(SexoBiologico.M, 18, 29, 91, 110, 1, ClasificacionCarrera400m.LENTO);
        addCarrera(SexoBiologico.M, 18, 29, 111, 900, 2, ClasificacionCarrera400m.MUY_LENTO);

        addCarrera(SexoBiologico.F, 18, 29, 30, 69, 0, ClasificacionCarrera400m.EXCELENTE);
        addCarrera(SexoBiologico.F, 18, 29, 70, 85, 0, ClasificacionCarrera400m.BUENO);
        addCarrera(SexoBiologico.F, 18, 29, 86, 105, 0, ClasificacionCarrera400m.NORMAL);
        addCarrera(SexoBiologico.F, 18, 29, 106, 125, 1, ClasificacionCarrera400m.LENTO);
        addCarrera(SexoBiologico.F, 18, 29, 126, 900, 2, ClasificacionCarrera400m.MUY_LENTO);

        log.info("  regla_carrera_400m: 10 reglas");
    }

    private void addCarrera(SexoBiologico sexo, int eMin, int eMax,
                            int vMin, int vMax, int puntos, ClasificacionCarrera400m clas) {
        ReglaCarrera400m r = new ReglaCarrera400m();
        r.setSexo(sexo); r.setEdadMin(eMin); r.setEdadMax(eMax);
        r.setTiempoSegundosMin(vMin); r.setTiempoSegundosMax(vMax);
        r.setDireccionRiesgo(DireccionRiesgo.ARRIBA);
        r.setPuntosRiesgo(puntos); r.setClasificacion(clas);
        r.setEstado(EstadoRegla.ACTIVO);
        carreraRepo.save(r);
    }
}