import { useMemo, useState } from 'react'
import { complexities, operationCount, type Complexity } from '../data/lessons'

const MAX_N = 32
const W = 720
const H = 300
const PAD = { top: 18, right: 18, bottom: 34, left: 48 }

const staircases: { id: Complexity; label: string; x: number; y: number; width: number; height: number; count: (n: number) => number }[] = [
  { id: 'O(1)', label: 'Constant', x: 52, y: 318, width: 145, height: 50, count: () => 1 },
  { id: 'O(log n)', label: 'Logarithmic', x: 190, y: 263, width: 145, height: 58, count: n => Math.max(1, Math.ceil(Math.log2(n))) },
  { id: 'O(n)', label: 'Linear', x: 328, y: 207, width: 145, height: 66, count: n => n },
  { id: 'O(n²)', label: 'Quadratic', x: 466, y: 150, width: 145, height: 74, count: n => n * n },
]

function ComplexityStaircase({ n, onChange }: { n: number; onChange: (value: number) => void }) {
  return <section className="complexity-scene" aria-label="Animated isometric comparison of algorithm step counts">
    <div className="scene-heading"><div><span className="eyebrow">THE BIG-O LANDSCAPE</span><h3>Same input. Different amount of work.</h3></div><span className="scene-live"><i/> LIVE MODEL</span></div>
    <div className="scene-canvas">
      <svg className="staircase-svg" viewBox="0 0 720 430" role="img" aria-label={`At input size ${n}, constant takes 1 step, logarithmic takes ${staircases[1].count(n)}, linear takes ${n}, and quadratic takes ${n*n} steps.`}>
        <defs>
          <pattern id="iso-grid" width="42" height="24" patternUnits="userSpaceOnUse" patternTransform="skewY(-28)">
            <path d="M 42 0 L 0 0 0 24" fill="none" stroke="#ffffff" strokeOpacity=".075" strokeWidth="1"/>
          </pattern>
          <pattern id="stripe-fill" width="9" height="9" patternUnits="userSpaceOnUse" patternTransform="rotate(38)">
            <rect width="9" height="9" fill="#d95f48"/>
            <path d="M0 0V9" stroke="#ffe0d4" strokeWidth="3" opacity=".8"/>
          </pattern>
          <linearGradient id="top-face" x1="0" y1="0" x2="1" y2="1"><stop stopColor="#f8f5ec"/><stop offset="1" stopColor="#c9c7c0"/></linearGradient>
          <filter id="scene-shadow" x="-30%" y="-30%" width="160%" height="180%"><feGaussianBlur stdDeviation="8"/></filter>
        </defs>
        <rect width="720" height="430" fill="url(#iso-grid)" opacity=".9"/>
        <ellipse cx="350" cy="367" rx="300" ry="35" fill="#000" opacity=".36" filter="url(#scene-shadow)"/>
        {staircases.map((item, index) => {
          const { x, y, width, height } = item
          const depth = 24
          const top = `${x},${y} ${x + width/2},${y-depth} ${x+width},${y} ${x+width/2},${y+depth}`
          const left = `${x},${y} ${x+width/2},${y+depth} ${x+width/2},${y+depth+height} ${x},${y+height}`
          const right = `${x+width/2},${y+depth} ${x+width},${y} ${x+width},${y+height} ${x+width/2},${y+depth+height}`
          const cubes = index === 3 ? 4 : index === 2 ? 3 : index === 1 ? 2 : 1
          return <g key={item.id} className="stair-block" style={{ '--stair-delay': `${index * 130}ms` } as React.CSSProperties}>
            <polygon points={left} fill="#272a30" stroke="#d8d6d0" strokeWidth="1.4"/>
            <polygon points={right} fill="url(#stripe-fill)" stroke="#f1e9dd" strokeWidth="1.4"/>
            <polygon points={top} fill="url(#top-face)" stroke="#fffaf0" strokeWidth="1.5"/>
            {Array.from({ length: cubes }, (_, cube) => {
              const cx = x + 25 + (cube % 2) * 45 + Math.floor(cube / 2) * 12
              const cy = y - 5 - Math.floor(cube / 2) * 8
              return <g key={cube} className="scene-cube">
                <polygon points={`${cx},${cy} ${cx+18},${cy-10} ${cx+36},${cy} ${cx+18},${cy+10}`} fill="#f6f2e8" stroke="#34363a" strokeWidth="1"/>
                <polygon points={`${cx},${cy} ${cx+18},${cy+10} ${cx+18},${cy+27} ${cx},${cy+17}`} fill="#b9b7b0" stroke="#34363a" strokeWidth="1"/>
                <polygon points={`${cx+18},${cy+10} ${cx+36},${cy} ${cx+36},${cy+17} ${cx+18},${cy+27}`} fill="url(#stripe-fill)" stroke="#34363a" strokeWidth="1"/>
              </g>
            })}
            <text x={x+width/2} y={y+depth+height-12} textAnchor="middle" className="stair-complexity">{item.id}</text>
            <g className="step-pill">
              <rect x={x+width-5} y={y+depth+height-16} width="122" height="43" rx="13"/>
              <text x={x+width+56} y={y+depth+height+12} textAnchor="middle">{item.count(n).toLocaleString()} {item.count(n)===1?'step':'steps'}</text>
            </g>
          </g>
        })}
        <g className="input-callout">
          <path d="M 302 83 L 432 165" stroke="#e76a50" strokeWidth="2.5" fill="none"/>
          <circle cx="432" cy="165" r="7" fill="#e76a50"/>
          <rect x="150" y="37" width="170" height="56" rx="15" fill="#d95f48"/>
          <text x="235" y="73" textAnchor="middle">n = {n}</text>
        </g>
      </svg>
    </div>
    <div className="scene-slider"><label htmlFor="scene-input">Change input size <strong>n = {n}</strong></label><input id="scene-input" type="range" min="2" max="12" value={n} onChange={e => onChange(Number(e.target.value))} /></div>
    <p className="scene-caption">Illustrative step counts. The staircase shows how quickly work can grow as the input gets larger.</p>
  </section>
}

export default function BigOVisualizer() {
  const [n, setN] = useState(2)
  const [selected, setSelected] = useState<Complexity[]>(['O(1)', 'O(log n)', 'O(n)', 'O(n log n)', 'O(n²)'])
  const [normalised, setNormalised] = useState(false)
  const plot = useMemo(() => {
    const maxRaw = Math.max(...complexities.filter(c => selected.includes(c.id)).map(c => operationCount(c.id, MAX_N)), 1)
    const maxY = normalised ? 1 : maxRaw
    const x = (i: number) => PAD.left + (i / MAX_N) * (W - PAD.left - PAD.right)
    const y = (v: number) => H - PAD.bottom - (Math.min(v, maxY) / maxY) * (H - PAD.top - PAD.bottom)
    return { x, y, maxY }
  }, [selected, normalised])
  const toggle = (id: Complexity) => setSelected(current => current.includes(id) ? current.filter(x => x !== id) : [...current, id])
  return <>
    <ComplexityStaircase n={n} onChange={setN}/>
    <section className="panel graph-panel">
      <div className="panel-heading"><div><span className="eyebrow">GROWTH EXPLORER</span><h2>How does work scale?</h2><p>Toggle curves and change the input size to compare growth.</p></div><button className={normalised ? 'small-toggle active' : 'small-toggle'} onClick={() => setNormalised(v => !v)}>Normalise curves</button></div>
      <div className="chart-wrap">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Chart comparing Big O complexity growth rates">
          {[0, .25, .5, .75, 1].map(t => <g key={t}><line x1={PAD.left} x2={W-PAD.right} y1={PAD.top+t*(H-PAD.top-PAD.bottom)} y2={PAD.top+t*(H-PAD.top-PAD.bottom)} className="gridline"/><text x={PAD.left-10} y={PAD.top+t*(H-PAD.top-PAD.bottom)+4} className="axis-label" textAnchor="end">{normalised ? (1-t).toFixed(2) : Math.round(plot.maxY*(1-t))}</text></g>)}
          {[0,8,16,24,32].map(t => <g key={t}><line x1={plot.x(t)} x2={plot.x(t)} y1={PAD.top} y2={H-PAD.bottom} className="gridline vertical"/><text x={plot.x(t)} y={H-9} className="axis-label" textAnchor="middle">{t}</text></g>)}
          {complexities.filter(c => selected.includes(c.id)).map(c => {
            const vals = Array.from({length: MAX_N+1}, (_,i) => operationCount(c.id, Math.max(1,i)))
            const denom = normalised ? operationCount(c.id, MAX_N) : 1
            const d = vals.map((v,i) => `${i===0?'M':'L'} ${plot.x(i)} ${plot.y(v/denom)}`).join(' ')
            return <path key={c.id} d={d} fill="none" stroke={c.color} strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" opacity=".95" />
          })}
          {complexities.filter(c => selected.includes(c.id)).map(c => <circle key={c.id} cx={plot.x(n)} cy={plot.y(operationCount(c.id,n)/(normalised?operationCount(c.id,MAX_N):1))} r="4.5" fill={c.color} stroke="#0c1224" strokeWidth="2"/>)}
        </svg>
        <div className="axis-title">Input size (n)</div>
      </div>
      <div className="chart-controls"><label htmlFor="input-size">Input size <strong>n = {n}</strong></label><input id="input-size" type="range" min="1" max="32" value={n} onChange={e=>setN(Number(e.target.value))}/><span className="mono muted">1 — 32</span></div>
      <div className="complexity-legend">{complexities.map(c => <button key={c.id} onClick={()=>toggle(c.id)} className={selected.includes(c.id)?'legend-chip selected':'legend-chip'} style={{'--series':c.color} as React.CSSProperties}><i/>{c.id}</button>)}</div>
      <div className="metrics-grid">{complexities.filter(c=>selected.includes(c.id)).map(c=><div className="metric" key={c.id}><span style={{color:c.color}}>{c.id}</span><strong>{operationCount(c.id,n).toLocaleString('en-US',{maximumFractionDigits:1})}</strong><small>{c.label}</small></div>)}</div>
      <p className="fine-print">Illustrative operation counts, not benchmark timings. Exponential values are capped for display.</p>
    </section>
  </>
}
