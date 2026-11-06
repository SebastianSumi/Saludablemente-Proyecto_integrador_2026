/** Solo URLs de medios locales dev. No modifica textos ni destinos externos. */
export function localMedia(value: unknown, publicUrl: string, key = ''): unknown {
  if (Array.isArray(value)) return value.map(item => localMedia(item,publicUrl,key));
  if (typeof value === 'string' && /url(s)?$|^(foto|imagen|logo|portada|imagenes)$/i.test(key)) {
    try {
      const url=new URL(value), target=new URL(publicUrl);
      if(url.protocol==='http:' && ['localhost','127.0.0.1'].includes(url.hostname) && url.port==='9000') {
        return target.origin+url.pathname+url.search+url.hash;
      }
    } catch { /* Texto sin formato URL: conservarlo. */ }
    return value;
  }
  if(value && typeof value==='object' && Object.getPrototypeOf(value)===Object.prototype) {
    return Object.fromEntries(Object.entries(value).map(([name,item])=>[name,localMedia(item,publicUrl,name)]));
  }
  return value;
}
