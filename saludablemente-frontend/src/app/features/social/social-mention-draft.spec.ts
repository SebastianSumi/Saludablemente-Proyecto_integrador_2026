import {
  editMentionDraft,
  encodeMentions,
  insertMention,
  mentionDraft,
} from './social-mention-draft';

describe('Social · menciones legibles con referencia estable', () => {
  it('muestra nombres y conserva los tokens contractuales de actividades y equipos', () => {
    const raw = 'Ven a @actividad{5} con @equipo{30}';
    const draft = mentionDraft(raw, [
      {
        tipoMencion: 'ACTIVIDAD',
        refId: 5,
        displayName: 'Primera actividad',
        avatarUrl: null,
        url: null,
      },
      { tipoMencion: 'EQUIPO', refId: 30, displayName: 'Bienestar', avatarUrl: null, url: null },
    ]);
    expect(draft.text).toBe('Ven a @Primera actividad con @Bienestar');
    expect(encodeMentions(draft)).toBe(raw);
  });
  it('mantiene referencias al escribir antes, después y entre menciones', () => {
    const draft = insertMention(mentionDraft('Hola '), 5, 5, 'Ana Torres', '@ana.torres');
    const before = editMentionDraft(draft, 'Hoy ' + draft.text);
    const edited = editMentionDraft(before, before.text + 'gracias');
    expect(encodeMentions(edited)).toBe('Hoy Hola @ana.torres gracias');
  });
  it('eliminar o modificar una etiqueta quita su referencia sin restaurar el ID', () => {
    const draft = insertMention(mentionDraft(''), 0, 0, 'Actividad saludable', '@actividad{5}');
    const edited = editMentionDraft(draft, '@Actividad nueva ');
    expect(edited.ranges).toHaveLength(0);
    expect(encodeMentions(edited)).toBe('@Actividad nueva ');
    expect(encodeMentions(editMentionDraft(draft, ''))).toBe('');
  });
  it('reemplaza la selección sin mencionar todas las apariciones del mismo nombre', () => {
    const draft = insertMention(mentionDraft('Ana y Ana'), 0, 3, 'Ana', '@ana');
    expect(encodeMentions(draft)).toBe('@ana  y Ana');
    expect(draft.ranges).toHaveLength(1);
  });
  it('ofrece una etiqueta de respaldo sin exponer llaves ni IDs', () => {
    const draft = mentionDraft('@actividad{5} y @equipo{30}');
    expect(draft.text).toBe('@Actividad y @Equipo');
    expect(encodeMentions(draft)).toBe('@actividad{5} y @equipo{30}');
  });
});
