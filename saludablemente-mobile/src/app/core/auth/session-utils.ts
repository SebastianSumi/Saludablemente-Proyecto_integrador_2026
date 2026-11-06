/** Claims de presentación; la autorización efectiva corresponde al backend. */
export function decodeClaims(token: string): Record<string, unknown> {
  try {
    const part = token.split('.')[1];
    if (!part) return {};
    const base64 = part.replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')), c => c.charCodeAt(0));
    const decoded = JSON.parse(new TextDecoder().decode(bytes));
    return decoded !== null && typeof decoded === 'object' && !Array.isArray(decoded) ? decoded : {};
  } catch { return {}; }
}
export function safeReturnUrl(value?: string): string {
  return value?.startsWith('/') && !value.startsWith('//') && !value.includes('\\') ? value : '/';
}
export function isApiRequest(value: string, apiBase: string): boolean {
  if (!apiBase) return false;
  try {
    const base = new URL(apiBase), target = new URL(value, base);
    return target.origin === base.origin && target.pathname.startsWith(base.pathname.replace(/\/$/, '') + '/api/');
  } catch { return false; }
}
