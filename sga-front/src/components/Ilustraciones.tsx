// Ilustraciones vectoriales propias: paisaje del Valle de Cocora con palmas de cera y domos.

/** Estrellas en posiciones fijas (pseudoaleatorias) para que el cielo no cambie entre visitas. */
function estrellas(cantidad: number, ancho: number, alto: number) {
  const puntos: { x: number; y: number; r: number; o: number }[] = [];
  let semilla = 7;
  const azar = () => {
    semilla = (semilla * 9301 + 49297) % 233280;
    return semilla / 233280;
  };
  for (let i = 0; i < cantidad; i++) {
    puntos.push({ x: azar() * ancho, y: azar() * alto, r: 0.6 + azar() * 1.4, o: 0.35 + azar() * 0.6 });
  }
  return puntos;
}

const CIELO = estrellas(90, 1200, 260);

function Palma({ x, base, alto, escala = 1 }: { x: number; base: number; alto: number; escala?: number }) {
  const tope = base - alto;
  const hojas = [-70, -40, -12, 15, 42, 72];
  return (
    <g className="palma">
      <path
        d={`M${x - 2.2 * escala} ${base} C ${x - 1.5 * escala} ${base - alto * 0.5}, ${x + 2 * escala} ${tope + 30}, ${x} ${tope}
            L ${x + 1.6 * escala} ${tope} C ${x + 3.4 * escala} ${tope + 30}, ${x + 1 * escala} ${base - alto * 0.5}, ${x + 2.2 * escala} ${base} Z`}
      />
      {hojas.map((angulo) => (
        <path
          key={angulo}
          transform={`rotate(${angulo} ${x} ${tope})`}
          d={`M ${x} ${tope} q ${10 * escala} ${-16 * escala} ${4 * escala} ${-38 * escala} q ${-6 * escala} ${16 * escala} ${-4 * escala} ${38 * escala} Z`}
        />
      ))}
    </g>
  );
}

function DomoPaisaje({ x, y, r, luz = true }: { x: number; y: number; r: number; luz?: boolean }) {
  return (
    <g>
      {luz && <ellipse cx={x} cy={y} rx={r * 1.9} ry={r * 0.55} fill="url(#halo)" />}
      <path d={`M ${x - r} ${y} A ${r} ${r} 0 0 1 ${x + r} ${y} Z`} fill="#f4efe3" />
      <path
        d={`M ${x - r} ${y} L ${x - r * 0.5} ${y - r * 0.86} L ${x} ${y} L ${x + r * 0.5} ${y - r * 0.86} L ${x + r} ${y}
            M ${x - r * 0.5} ${y - r * 0.86} L ${x + r * 0.5} ${y - r * 0.86} M ${x - r * 0.87} ${y - r * 0.5} L ${x + r * 0.87} ${y - r * 0.5}`}
        fill="none"
        stroke="#c9b99a"
        strokeWidth={1.2}
      />
      {luz && <path d={`M ${x - r * 0.28} ${y} A ${r * 0.28} ${r * 0.34} 0 0 1 ${x + r * 0.28} ${y} Z`} fill="#e8973a" />}
    </g>
  );
}

export function PaisajeCocora() {
  return (
    <svg className="paisaje" viewBox="0 0 1200 520" preserveAspectRatio="xMidYMax slice" aria-hidden="true">
      <defs>
        <linearGradient id="cielo" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#0b221f" />
          <stop offset="0.65" stopColor="#173d36" />
          <stop offset="1" stopColor="#2a5a4f" />
        </linearGradient>
        <radialGradient id="halo">
          <stop offset="0" stopColor="#e8973a" stopOpacity="0.55" />
          <stop offset="1" stopColor="#e8973a" stopOpacity="0" />
        </radialGradient>
        <linearGradient id="niebla" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#eef2ec" stopOpacity="0" />
          <stop offset="1" stopColor="#eef2ec" stopOpacity="1" />
        </linearGradient>
      </defs>
      <rect width="1200" height="520" fill="url(#cielo)" />
      {CIELO.map((e, i) => (
        <circle key={i} cx={e.x} cy={e.y} r={e.r} fill="#f4efe3" opacity={e.o} />
      ))}
      <circle cx="960" cy="92" r="30" fill="#f4efe3" opacity="0.92" />
      <circle cx="972" cy="84" r="30" fill="#0f2b27" />

      <path d="M0 330 C 160 250 300 300 430 262 C 560 224 650 290 790 250 C 930 210 1060 268 1200 236 L1200 520 L0 520 Z" fill="#1d4740" />
      <g fill="#163a34">
        <Palma x={150} base={318} alto={150} escala={1} />
        <Palma x={238} base={300} alto={190} escala={1.1} />
        <Palma x={830} base={262} alto={170} escala={1} />
        <Palma x={1105} base={252} alto={200} escala={1.15} />
      </g>
      <path d="M0 390 C 200 330 340 380 520 345 C 700 310 860 370 1030 338 C 1110 323 1160 330 1200 326 L1200 520 L0 520 Z" fill="#265a50" />
      <g fill="#1d4a42">
        <Palma x={60} base={392} alto={210} escala={1.3} />
        <Palma x={640} base={346} alto={230} escala={1.35} />
      </g>
      <DomoPaisaje x={720} y={352} r={34} />
      <DomoPaisaje x={850} y={358} r={44} />
      <DomoPaisaje x={985} y={344} r={30} />
      <DomoPaisaje x={1105} y={338} r={38} luz={false} />
      <path d="M0 440 C 240 400 420 450 640 418 C 860 386 1000 430 1200 410 L1200 520 L0 520 Z" fill="#2f6a5d" />
      <rect y="380" width="1200" height="140" fill="url(#niebla)" />
    </svg>
  );
}

/** Retrato de cada domo según su dotación: jacuzzi (vapor), malla catamarán, fogata o proyector. */
export function RetratoDomo({
  id,
  jacuzzi,
  malla,
  fogata,
  proyector,
}: {
  id: string;
  jacuzzi: boolean;
  malla: boolean;
  fogata: boolean;
  proyector: boolean;
}) {
  const n = Number(id.replace(/\D/g, '')) || 1;
  const cielos = ['#1d4740', '#264d5a', '#3a4a33', '#4a3a4f', '#2c4a3a'];
  const fondo = cielos[(n - 1) % cielos.length];
  const g = `g-${id}`;
  return (
    <svg className="retrato" viewBox="0 0 320 200" role="img" aria-label={`Ilustración del ${id}`}>
      <defs>
        <linearGradient id={g} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor={fondo} />
          <stop offset="1" stopColor="#0f2b27" />
        </linearGradient>
      </defs>
      <rect width="320" height="200" fill={`url(#${g})`} />
      <path d="M0 150 C 80 128 150 146 220 132 C 260 124 300 130 320 128 L320 200 L0 200 Z" fill="#163a34" />
      <g fill="#123029">
        <Palma x={40 + (n * 13) % 30} base={150} alto={104} escala={0.8} />
        <Palma x={284 - (n * 7) % 24} base={132} alto={86} escala={0.7} />
      </g>
      <ellipse cx="160" cy="158" rx="96" ry="22" fill="url(#halo-retrato)" />
      <defs>
        <radialGradient id="halo-retrato">
          <stop offset="0" stopColor="#e8973a" stopOpacity="0.45" />
          <stop offset="1" stopColor="#e8973a" stopOpacity="0" />
        </radialGradient>
      </defs>
      <path d="M 100 158 A 60 60 0 0 1 220 158 Z" fill="#f4efe3" />
      <path
        d="M100 158 L130 106 L160 158 L190 106 L220 158 M130 106 L190 106 M108 128 L212 128 M160 98 L130 106 M160 98 L190 106"
        fill="none"
        stroke="#c9b99a"
        strokeWidth="1.4"
      />
      <path d="M 146 158 A 14 18 0 0 1 174 158 Z" fill="#e8973a" />
      {jacuzzi && (
        <g>
          <ellipse cx="252" cy="166" rx="26" ry="8" fill="#7fb3ad" stroke="#c9b99a" strokeWidth="1.5" />
          <path d="M240 156 q -6 -10 0 -18 q 6 -8 0 -16 M254 154 q -6 -10 0 -18 q 6 -8 0 -16" fill="none" stroke="#eef2ec" strokeOpacity=".55" strokeWidth="2" strokeLinecap="round" />
        </g>
      )}
      {malla && (
        <g stroke="#c9b99a" strokeWidth="1" fill="none">
          <path d="M40 172 L96 160 M40 172 L48 186 M96 160 L100 174" />
          <path d="M44 176 L98 164 M46 180 L99 168 M50 172 L56 186 M62 170 L68 184 M74 167 L80 181 M86 164 L92 178" opacity=".7" />
        </g>
      )}
      {fogata && (
        <g>
          <path d="M60 176 L84 170 M62 170 L82 177" stroke="#7a5a3a" strokeWidth="4" strokeLinecap="round" />
          <path d="M72 170 q -10 -12 -2 -24 q 2 10 8 12 q 4 -8 0 -16 q 12 10 6 28 Z" fill="#e8973a" />
        </g>
      )}
      {proyector && (
        <g>
          <path d="M226 120 L300 96 L300 150 Z" fill="#f4efe3" opacity=".12" />
          <rect x="218" y="114" width="14" height="10" rx="2" fill="#c9b99a" />
        </g>
      )}
    </svg>
  );
}

export function MarcaDomo({ tamano = 30 }: { tamano?: number }) {
  return (
    <svg width={tamano} height={tamano} viewBox="0 0 64 64" aria-hidden="true">
      <path d="M8 48a24 24 0 0 1 48 0z" fill="#e8973a" />
      <path d="M8 48 32 24 56 48M20 36l12 12 12-12M32 24v24" fill="none" stroke="currentColor" strokeWidth="3" strokeLinejoin="round" />
    </svg>
  );
}
