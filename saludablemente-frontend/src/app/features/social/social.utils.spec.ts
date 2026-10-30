import { HttpErrorResponse } from '@angular/common/http';
import {
  adjuntoValido,
  errorSocial,
  imagenesValidas,
  rutaMencion,
  urlsImagenes,
} from './social.utils';
describe('Social · validación y seguridad del cliente', () => {
  it('normaliza URLs sin fabricar claves de almacenamiento', () => {
    expect(
      urlsImagenes(' https://example.invalid/a.png\n\nhttps://example.invalid/b.png '),
    ).toEqual(['https://example.invalid/a.png', 'https://example.invalid/b.png']);
  });
  it.each(['javascript:alert(1)', 'data:image/png;base64,AA', 'file:///imagen.png', 'no es URL'])(
    'rechaza referencia de post %s',
    (url) => expect(imagenesValidas([url])).toBe(false),
  );
  it('acepta HTTP/HTTPS y limita cada URL a 500 caracteres', () => {
    expect(imagenesValidas(['https://example.invalid/foto.webp'])).toBe(true);
    expect(imagenesValidas(['https://example.invalid/' + 'a'.repeat(500)])).toBe(false);
  });
  it('valida MIME, extensión y tamaño local de adjuntos', () => {
    expect(adjuntoValido(new File(['a'], 'foto.png', { type: 'image/png' }))).toBe(true);
    expect(adjuntoValido(new File(['a'], 'foto.svg', { type: 'image/svg+xml' }))).toBe(false);
    expect(adjuntoValido(new File(['a'], 'foto.exe', { type: 'image/png' }))).toBe(false);
    expect(adjuntoValido(new File([], 'foto.png', { type: 'image/png' }))).toBe(false);
    expect(
      adjuntoValido(
        new File([new Uint8Array(5 * 1024 * 1024 + 1)], 'foto.webp', { type: 'image/webp' }),
      ),
    ).toBe(false);
  });
  it.each([
    ['USUARIO', 101, '/social/usuarios/101'],
    ['EQUIPO', 301, '/social/equipos/301'],
    ['ACTIVIDAD', 401, '/social/actividades/401'],
  ] as const)('mención %s usa ruta de pantalla, no la URL API', (tipo, id, path) =>
    expect(rutaMencion(tipo, id)).toBe(path),
  );
  it('muestra el mensaje de negocio 409 y nunca contenido HTML como DOM', () =>
    expect(
      errorSocial(
        new HttpErrorResponse({ status: 409, error: { message: 'Ya compartió esta publicación' } }),
      ),
    ).toBe('Ya compartió esta publicación'));
  it.each([0, 401, 403, 404])('traduce el estado %s sin filtrar cuerpo técnico', (status) => {
    const text = errorSocial(
      new HttpErrorResponse({ status, error: '<html>Traza interna</html>' }),
    );
    expect(text).not.toContain('<html>');
    expect(text.length).toBeGreaterThan(10);
  });
});
