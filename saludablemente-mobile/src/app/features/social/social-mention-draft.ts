import { Mencion } from './social.model';

export interface MentionRange {
  start: number;
  end: number;
  token: string;
}
export interface MentionDraft {
  text: string;
  ranges: MentionRange[];
}

/** La etiqueta visible se edita como texto; solo rangos intactos conservan su referencia. */
export function mentionDraft(raw: string, mentions: Mencion[] = []): MentionDraft {
  const ranges: MentionRange[] = [];
  const text = raw.replace(/@(actividad|equipo)\{(\d+)\}/g, (token, type, id, offset) => {
    const mention = mentions.find(
      (m) => m.tipoMencion === type.toUpperCase() && m.refId === Number(id),
    );
    const label = '@' + (mention?.displayName || (type === 'actividad' ? 'Actividad' : 'Equipo'));
    const shift = ranges.reduce((sum, r) => sum + r.end - r.start - r.token.length, 0);
    const start = offset + shift;
    ranges.push({ start, end: start + label.length, token });
    return label;
  });
  return { text, ranges };
}

export function encodeMentions(draft: MentionDraft): string {
  let raw = draft.text;
  for (const r of [...draft.ranges].sort((a, b) => b.start - a.start))
    raw = raw.slice(0, r.start) + r.token + raw.slice(r.end);
  return raw;
}

export function editMentionDraft(draft: MentionDraft, text: string): MentionDraft {
  let start = 0;
  while (start < draft.text.length && start < text.length && draft.text[start] === text[start])
    start++;
  let oldEnd = draft.text.length,
    newEnd = text.length;
  while (oldEnd > start && newEnd > start && draft.text[oldEnd - 1] === text[newEnd - 1]) {
    oldEnd--;
    newEnd--;
  }
  const delta = newEnd - oldEnd;
  return {
    text,
    ranges: draft.ranges.flatMap((r) =>
      r.end <= start
        ? [r]
        : r.start >= oldEnd
          ? [{ ...r, start: r.start + delta, end: r.end + delta }]
          : [],
    ),
  };
}

export function insertMention(
  draft: MentionDraft,
  start: number,
  end: number,
  name: string,
  token: string,
): MentionDraft {
  const prefix = start && !/\s/.test(draft.text[start - 1]) ? ' ' : '';
  const label = '@' + name;
  const inserted = prefix + label + ' ';
  const next = editMentionDraft(
    draft,
    draft.text.slice(0, start) + inserted + draft.text.slice(end),
  );
  next.ranges.push({
    start: start + prefix.length,
    end: start + prefix.length + label.length,
    token,
  });
  return next;
}
