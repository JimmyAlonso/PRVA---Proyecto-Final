// Fotografías de Unsplash (licencia Unsplash: uso libre, sin costo). Se sirven desde su CDN
// con el tamaño justo para cada lugar de la página.

export function urlFoto(id: string, ancho = 1200, alto?: number): string {
  const tam = alto ? `&h=${alto}&fit=crop` : '';
  return `https://images.unsplash.com/photo-${id}?w=${ancho}${tam}&q=75&auto=format`;
}

export const FOTOS = {
  portada: '1693357483341-204143afdeb7', // Palmas de cera entre la niebla, Valle de Cocora
  cocoraCaballos: '1568796687013-b5a1eb6b46c6',
  cocoraValle: '1578398192993-b64a40ecb690',
  cocoraNiebla: '1737914692143-d5680f67ccba',
  cocoraPalmas: '1768407683196-521a0764da27',
  cocoraBruma: '1778188985212-12dd634aefc1',
  salentoCalle: '1570197820006-3e46f5ee01bb',
  salentoPueblo: '1611781638782-0d3cae1cd22e',
  willys: '1697818634560-27ddca612f88',
  cabalgata: '1565013239498-60fc88ef3392',
  jacuzzi: '1734596438499-331802439a2a',
  jacuzziBosque: '1734596438204-a968213446d6',
  rio: '1725570516660-ecad13675ad7',
  fogataAmigos: '1478131143081-80f7f84ca84d',
  fogata: '1464808646948-8f732deb6e4b',
  fogataEstrellas: '1482355347028-ff60443f60fe',
  estrellas: '1504851149312-7a075b496cc7',
  malla: '1647836318567-c7666479601b',
  interiorDomo: '1786676213689-0d200a5863f8',
  domoNoche: '1783696521772-e3ba2c4ef0b5',
};

/** Foto principal de cada domo. */
export const FOTO_DOMO: Record<string, { id: string; texto: string }> = {
  'DOM-01': { id: '1641569618527-68e26db76db0', texto: 'Domo sobre una terraza de madera con vista a las montañas' },
  'DOM-02': { id: '1698918160268-34254701cfed', texto: 'Domo entre árboles con terraza sobre el valle' },
  'DOM-03': { id: '1791362704204-16b0735eb589', texto: 'Domos en la colina con un caballo pastando al frente' },
  'DOM-04': { id: '1783696521772-e3ba2c4ef0b5', texto: 'Domo iluminado por dentro en la noche' },
  'DOM-05': { id: '1772423847112-c978f25f7a61', texto: 'Domo en una ladera verde junto al sendero' },
};

const RESERVA = FOTO_DOMO['DOM-01'];

export function fotoDeDomo(idDomo: string) {
  return FOTO_DOMO[idDomo] ?? RESERVA;
}

export function FotoDomo({ idDomo, ancho = 800, className = 'foto-domo' }: { idDomo: string; ancho?: number; className?: string }) {
  const f = fotoDeDomo(idDomo);
  return (
    <img
      className={className}
      src={urlFoto(f.id, ancho, Math.round(ancho * 0.66))}
      srcSet={`${urlFoto(f.id, Math.round(ancho / 2), Math.round(ancho * 0.33))} ${Math.round(ancho / 2)}w, ${urlFoto(f.id, ancho, Math.round(ancho * 0.66))} ${ancho}w`}
      sizes={`(max-width: 640px) 100vw, ${ancho}px`}
      alt={f.texto}
      loading="lazy"
      decoding="async"
    />
  );
}

export function Foto({ id, alt, ancho = 900, alto, className, eager = false }: {
  id: string;
  alt: string;
  ancho?: number;
  alto?: number;
  className?: string;
  eager?: boolean;
}) {
  return (
    <img
      className={className}
      src={urlFoto(id, ancho, alto)}
      alt={alt}
      loading={eager ? 'eager' : 'lazy'}
      decoding="async"
    />
  );
}

/** Banda superior con fotografía para las páginas internas. */
export function CabeceraFoto({ id, alt, titulo, children }: { id: string; alt: string; titulo: string; children?: import('react').ReactNode }) {
  return (
    <header className="cabecera-foto">
      <img src={urlFoto(id, 1800, 560)} alt={alt} />
      <div className="contenedor cabecera-foto__texto">
        <h1>{titulo}</h1>
        {children}
      </div>
    </header>
  );
}
