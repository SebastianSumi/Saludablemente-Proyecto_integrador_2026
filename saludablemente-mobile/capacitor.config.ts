import type { CapacitorConfig } from '@capacitor/cli';
const config: CapacitorConfig = {
  appId: 'pe.edu.upeu.saludablemente.mobile', appName: 'Saludablemente',
  loggingBehavior: 'none',
  webDir: 'dist/saludablemente-mobile/browser',
  server: { androidScheme: 'https' },
  plugins: { CapacitorHttp: { enabled: true } },
  android: { allowMixedContent: process.env['MOBILE_LOCAL'] === '1' },
};
export default config;
