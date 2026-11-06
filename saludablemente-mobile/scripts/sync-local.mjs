import { spawnSync } from 'node:child_process';
const result=spawnSync(process.execPath,['node_modules/@capacitor/cli/bin/capacitor','sync','android'],{stdio:'inherit',env:{...process.env,MOBILE_LOCAL:'1'}});
process.exit(result.status ?? 1);
