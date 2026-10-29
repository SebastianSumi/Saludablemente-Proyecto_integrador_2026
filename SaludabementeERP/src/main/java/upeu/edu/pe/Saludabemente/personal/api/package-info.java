/**
 * API publica del modulo Personal, expuesta a otros modulos del sistema.
 *
 * <h3>Contrato expuesto</h3>
 * <ul>
 *   <li>{@code PersonalApiService} — persona para Asistencias y Seguridad.</li>
 *   <li>{@code PersonalActividadesApiService} — persona para Actividades.</li>
 *   <li>{@code CredencialApiService} — verificacion QR para Asistencias.</li>
 *   <li>{@code PersonalSnapshotDTO} — DTO de persona (Asistencias y Seguridad).</li>
 *   <li>{@code PersonaActividadesDTO} — DTO de persona (Actividades).</li>
 *   <li>{@code VerificacionQrResponse} — respuesta de verificacion QR.</li>
 *   <li>{@code VerificarQrRequest} — payload del endpoint HTTP de verificacion.</li>
 * </ul>
 *
 * <h3>Consumidores</h3>
 * <ul>
 *   <li><b>Asistencias</b>: PersonalApiService + CredencialApiService.</li>
 *   <li><b>Actividades</b>: PersonalActividadesApiService.</li>
 *   <li><b>Seguridad</b>: PersonalApiService.</li>
 * </ul>
 *
 * <h3>NO exponer</h3>
 * <ul>
 *   <li>Entidades JPA ({@code Persona}, {@code CredencialPrograma}).</li>
 *   <li>Repositorios (regla S3 de Spring Modulith).</li>
 *   <li>Servicios internos ({@code PersonaService},
 *       {@code CredencialService}, {@code PreferenciaService}).</li>
 *   <li>DTOs internos ({@code PersonaResponse}, {@code PersonaRequest}).</li>
 *   <li>El enum {@code Sexo} del paquete {@code personal.persona.entity}.</li>
 * </ul>
 *
 * <h3>Regla de integracion</h3>
 * El consumidor NUNCA accede a la base de datos de Personal. Siempre
 * consulta via los servicios de este paquete.
 */
@org.springframework.modulith.NamedInterface("personal-api")
package upeu.edu.pe.Saludabemente.personal.api;