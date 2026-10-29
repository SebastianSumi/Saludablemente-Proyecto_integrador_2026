import {
  Component,
  ElementRef,
  computed,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { TuiButton, TuiIcon } from '@taiga-ui/core';
import { TuiButtonLoading } from '@taiga-ui/kit';
import * as L from 'leaflet';
import { GeolocalizacionService } from '../../../core/services/geolocalizacion-service';

export interface SedePin {
  latitud: number;
  longitud: number;
}

const CENTRO_DEFAULT: SedePin = { latitud: -12.0463, longitud: -77.0428 };

@Component({
  selector: 'app-mapa-sede',
  imports: [TuiButton, TuiButtonLoading, TuiIcon],
  templateUrl: './mapa-sede.html',
  styleUrl: './mapa-sede.css',
})
export class MapaSede {
  private readonly geo = inject(GeolocalizacionService);

  /** Pin inicial (sede ya guardada al editar). */
  readonly inicial = input<SedePin | null>(null);
  /** Radio geocerca en metros (editable desde el padre). */
  readonly tolerancia = input<number>(100);
  readonly titulo = input<string>('Sede de la actividad');

  readonly sedeChange = output<SedePin>();
  readonly ubicando = signal(false);

  protected readonly pin = signal<SedePin | null>(null);
  protected readonly enlaceMaps = computed(() => {
    const p = this.pin();
    return p ? GeolocalizacionService.enlaceGoogleMaps(p.latitud, p.longitud) : null;
  });

  private readonly mapaDiv = viewChild<ElementRef<HTMLElement>>('mapa');
  private mapa: L.Map | null = null;
  private marcador: L.Marker | null = null;
  private circulo: L.Circle | null = null;

  constructor() {
    effect(() => {
      const t = this.tolerancia();
      if (this.circulo && Number.isFinite(t) && t > 0) this.circulo.setRadius(t);
    });
    // La sede inicial puede llegar tarde (edición async): centrar al recibirla.
    effect(() => {
      const ini = this.inicial();
      if (ini && this.mapa && !this.pin()) this.fijarPin(ini, false);
    });
  }

  ngAfterViewInit(): void {
    const el = this.mapaDiv()?.nativeElement;
    if (!el) return;
    const inicio = this.inicial() ?? CENTRO_DEFAULT;
    // zoomControl fuera: el default (arriba-izquierda) quedaba tapado por la barra.
    this.mapa = L.map(el, { zoomControl: false }).setView(
      [inicio.latitud, inicio.longitud],
      this.inicial() ? 17 : 13
    );
    L.control.zoom({ position: 'topright' }).addTo(this.mapa);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap',
    }).addTo(this.mapa);
    // Fix tamaño cuando el tab/contenedor se muestra tarde (centra bien el mapa).
    for (const ms of [200, 600, 1200]) {
      setTimeout(() => this.mapa?.invalidateSize(), ms);
    }
    this.mapa.on('click', (e: L.LeafletMouseEvent) => {
      this.fijarPin({ latitud: e.latlng.lat, longitud: e.latlng.lng });
    });
    if (this.inicial()) this.fijarPin(this.inicial()!, false);
  }

  ngOnDestroy(): void {
    this.mapa?.remove();
    this.mapa = null;
  }

  /** Fija pin + círculo y notifica al padre. */
  private fijarPin(p: SedePin, emitir = true): void {
    if (!this.mapa) return;
    const lat = Math.max(-90, Math.min(90, p.latitud));
    const lon = Math.max(-180, Math.min(180, p.longitud));
    const pin = { latitud: Number(lat.toFixed(6)), longitud: Number(lon.toFixed(6)) };
    this.pin.set(pin);
    const icono = L.divIcon({
      className: 'sede-pin',
      html: '<span></span>',
      iconSize: [30, 30],
      iconAnchor: [15, 28],
    });
    if (!this.marcador) {
      this.marcador = L.marker([pin.latitud, pin.longitud], { draggable: true, icon: icono }).addTo(this.mapa);
      this.marcador.on('dragend', () => {
        const ll = this.marcador!.getLatLng();
        this.fijarPin({ latitud: ll.lat, longitud: ll.lng });
      });
    } else {
      this.marcador.setLatLng([pin.latitud, pin.longitud]);
    }
    const radio = Number.isFinite(this.tolerancia()) && this.tolerancia() > 0 ? this.tolerancia() : 100;
    if (!this.circulo) {
      this.circulo = L.circle([pin.latitud, pin.longitud], { radius: radio }).addTo(this.mapa);
    } else {
      this.circulo.setLatLng([pin.latitud, pin.longitud]);
      this.circulo.setRadius(radio);
    }
    this.mapa.setView([pin.latitud, pin.longitud], Math.max(this.mapa.getZoom(), 16));
    if (emitir) this.sedeChange.emit(pin);
  }

  usarMiUbicacion(): void {
    this.ubicando.set(true);
    this.geo.detectar().subscribe({
      next: (u) => {
        this.ubicando.set(false);
        this.fijarPin({ latitud: u.latitud, longitud: u.longitud });
      },
      error: () => this.ubicando.set(false),
    });
  }

  async compartir(): Promise<void> {
    const p = this.pin();
    if (!p) return;
    const texto = GeolocalizacionService.textoCompartir(this.titulo(), p.latitud, p.longitud);
    try {
      if (typeof navigator.share === 'function') {
        await navigator.share({ title: this.titulo(), text: texto });
        return;
      }
      throw new Error('sin share');
    } catch {
      try {
        await navigator.clipboard.writeText(texto);
      } catch {
        window.prompt('Copia la ubicación:', texto);
      }
    }
  }
}
