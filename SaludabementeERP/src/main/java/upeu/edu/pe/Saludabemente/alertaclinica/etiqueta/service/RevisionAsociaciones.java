package upeu.edu.pe.Saludabemente.alertaclinica.etiqueta.service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import upeu.edu.pe.Saludabemente.exception.BusinessRuleException;
/** Revision determinista del estado leido, sin nuevas columnas. */
public final class RevisionAsociaciones {
 private RevisionAsociaciones() {}
 public static String calcular(Object datos) {
  try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(String.valueOf(datos).getBytes(StandardCharsets.UTF_8))); }
  catch (NoSuchAlgorithmException ex) { throw new IllegalStateException(ex); }
 }
 public static void validar(String recibida, String actual) {
  if (recibida == null || recibida.isBlank()) throw new IllegalArgumentException("La revision es obligatoria para editar; consulte el recurso primero");
  if (!actual.equals(recibida)) throw new BusinessRuleException("Las asociaciones o datos cambiaron. Recargue antes de guardar");
 }
}
