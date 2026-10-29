import {
  IMG_ALERTAS,
  IMG_DASHBOARD_CLINICO,
  imagenPortada,
  imagenRegla,
} from './ilustracion-alerta';

describe('ilustracion-alerta', () => {
  it('mapea los 9 slugs de reglas a su imagen', () => {
    const slugs = [
      'perimetro-abdominal',
      'porcentaje-grasa',
      'masa-muscular',
      'imc',
      'grasa-visceral',
      'glucosa',
      'colesterol',
      'trigliceridos',
      'presion-arterial',
    ];
    const urls = new Set(slugs.map((s) => imagenRegla(s)));
    expect(urls.size).toBe(9);
    for (const u of urls) {
      expect(u.endsWith('.webp')).toBe(true);
      expect(u.startsWith('/img/AlertasClinicas/')).toBe(true);
      expect(u).not.toContain(' ');
    }
  });

  it('slug desconocido o vacío usa la genérica', () => {
    expect(imagenRegla('otra-cosa')).toBe(IMG_ALERTAS);
    expect(imagenRegla('')).toBe(IMG_ALERTAS);
    expect(imagenRegla(null)).toBe(IMG_ALERTAS);
  });

  it('portada y dashboard apuntan a webp codificados', () => {
    expect(imagenPortada()).toBe(IMG_ALERTAS);
    expect(IMG_DASHBOARD_CLINICO.endsWith('.webp')).toBe(true);
  });
});
