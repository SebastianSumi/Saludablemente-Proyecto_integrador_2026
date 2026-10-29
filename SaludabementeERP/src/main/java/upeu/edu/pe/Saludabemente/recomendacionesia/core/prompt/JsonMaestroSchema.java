package upeu.edu.pe.Saludabemente.recomendacionesia.core.prompt;

public final class JsonMaestroSchema {

    private JsonMaestroSchema() {}

    /** Schema de los 4 nodos de Nutrición. */
    public static final String SCHEMA_NUTRICION = """
    {
      "type": "object",
      "properties": {
        "interpretacion_inteligente": {
          "type": "object",
          "properties": {
            "resumenEjecutivo":        { "type": "string" },
            "fortalezas":              { "type": "array", "items": { "type": "string" } },
            "aspectosCriticos":        { "type": "array", "items": { "type": "string" } },
            "factoresRiesgoDirectos":  { "type": "array", "items": { "type": "string" } },
            "habitosPositivosProbables": { "type": "array", "items": { "type": "string" } },
            "factoresAsociados":       { "type": "array", "items": { "type": "string" } },
            "cambiosPrincipales":      { "type": "string" },
            "explicacionClinica":      { "type": "string" },
            "nivelCondicionNutricional": { "type": "string" },
            "nivelAptitudFisica":      { "type": "string" },
            "triage": {
              "type": "object",
              "properties": {
                "prioridadAlta":  { "type": "array", "items": { "type": "string" } },
                "prioridadMedia": { "type": "array", "items": { "type": "string" } },
                "prioridadBaja":  { "type": "array", "items": { "type": "string" } }
              },
              "required": ["prioridadAlta", "prioridadMedia", "prioridadBaja"]
            },
            "resumenAccion": { "type": "string" }
          },
          "required": ["resumenEjecutivo","fortalezas","aspectosCriticos",
                       "factoresRiesgoDirectos","habitosPositivosProbables",
                       "factoresAsociados","cambiosPrincipales","explicacionClinica",
                       "nivelCondicionNutricional","nivelAptitudFisica","triage","resumenAccion"]
        },
        "objetivos_inteligentes": {
          "type": "object",
          "properties": {
            "objetivoMacro": { "type": "string" },
            "metas": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "tipoMeta":            { "type": "string" },
                  "descripcion":         { "type": "string" },
                  "valorInicial":        { "type": "number" },
                  "valorObjetivo":       { "type": "number" },
                  "fechaLimiteSugerida": { "type": "string" },
                  "pilar":               { "type": "string" }
                },
                "required": ["tipoMeta","descripcion","valorInicial",
                             "valorObjetivo","fechaLimiteSugerida","pilar"]
              }
            }
          },
          "required": ["objetivoMacro", "metas"]
        },
        "sistema_puntuacion": {
          "type": "object",
          "properties": {
            "puntuacionNutricion":           { "type": "number" },
            "puntuacionComposicionCorporal": { "type": "number" },
            "puntuacionResistencia":         { "type": "number" },
            "puntuacionFuerza":              { "type": "number" },
            "puntuacionFlexibilidad":        { "type": "number" },
            "indiceGeneral":                 { "type": "number" },
            "semaforo":                      { "type": "string" }
          },
          "required": ["puntuacionNutricion","puntuacionComposicionCorporal",
                       "puntuacionResistencia","puntuacionFuerza","puntuacionFlexibilidad",
                       "indiceGeneral","semaforo"]
        },
        "recomendacion_nutricional": {
          "type": "object",
          "properties": {
            "distribucionComidas":  { "type": "string" },
            "requerimientoHidrico": { "type": "string" },
            "macronutrientes": {
              "type": "object",
              "properties": {
                "proteinas":     { "type": "string" },
                "carbohidratos": { "type": "string" },
                "grasas":        { "type": "string" },
                "fibra":         { "type": "string" }
              },
              "required": ["proteinas","carbohidratos","grasas","fibra"]
            },
            "alimentosSugeridos": { "type": "array", "items": { "type": "string" } },
            "sustituciones": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "enLugarDe": { "type": "string" },
                  "usar":      { "type": "string" }
                },
                "required": ["enLugarDe", "usar"]
              }
            },
            "prePostEsfuerzo": { "type": "string" }
          },
          "required": ["distribucionComidas","requerimientoHidrico","macronutrientes",
                       "alimentosSugeridos","sustituciones","prePostEsfuerzo"]
        }
      },
      "required": ["interpretacion_inteligente","objetivos_inteligentes",
                   "sistema_puntuacion","recomendacion_nutricional"]
    }
    """;

    /** Schema de los 4 nodos de Ejercicio. */
    public static final String SCHEMA_EJERCICIO = """
    {
      "type": "object",
      "properties": {
        "comparacion_historica": {
          "type": ["object", "null"],
          "properties": {
            "narrativaGlobal":       { "type": "string" },
            "indicadoresMejorados":  { "type": "array", "items": { "type": "string" } },
            "indicadoresEstancados": { "type": "array", "items": { "type": "string" } },
            "indicadoresEmpeorados": { "type": "array", "items": { "type": "string" } },
            "recordsPersonales":     { "type": "array", "items": { "type": "string" } }
          }
        },
        "recomendacion_ejercicio": {
          "type": ["object", "null"],
          "properties": {
            "prioridadEntrenamiento": { "type": "string" },
            "prescripcionBase": {
              "type": "object",
              "properties": {
                "frecuenciaSemanal": { "type": "string" },
                "duracionSesion":    { "type": "string" },
                "tiposEjercicio":    { "type": "array", "items": { "type": "string" } }
              },
              "required": ["frecuenciaSemanal","duracionSesion","tiposEjercicio"]
            },
            "proteccionPaciente": { "type": "string" },
            "justificacion":      { "type": "string" }
          }
        },
        "plan_seguimiento": {
          "type": "object",
          "properties": {
            "mes1":     { "$ref": "#/definitions/hito" },
            "meses2a3": { "$ref": "#/definitions/hito" },
            "meses4a5": { "$ref": "#/definitions/hito" },
            "mes6":     { "$ref": "#/definitions/hito" }
          },
          "required": ["mes1","meses2a3","meses4a5","mes6"]
        },
        "prediccion_progreso": {
          "type": ["object", "null"],
          "properties": {
            "proyeccionEstadistica": { "type": "string" }
          }
        }
      },
      "required": ["comparacion_historica","recomendacion_ejercicio",
                   "plan_seguimiento","prediccion_progreso"],
      "definitions": {
        "hito": {
          "type": "object",
          "properties": {
            "fase":     { "type": "string" },
            "acciones": { "type": "array", "items": { "type": "string" } }
          },
          "required": ["fase", "acciones"]
        }
      }
    }
    """;
}