import { useMemo, useState } from 'react'
import { complexities, operationCount, type Complexity } from '../data/lessons'

const MAX_N = 32
const W = 720
const H = 300
const PAD = { top: 18, right: 18, bottom: 34, left: 48 }

const staircases: { id: Complexity; label: string; x: number; y: number; width: number; height: number }[] = [
  { id: 'O(1)', label: 'Constant', x: 34, y: 350, width: 130, height: 48 },
  { id: 'O(log n)', label: 'Logarithmic', x: 209, y: 285, width: 130, height: 55 },
  { id: 'O(n)', label: 'Linear', x: 384, y: 220, width: 130, height: 62 },
  { id: 'O(n²)', label: 'Quadratic', x: 559, y: 155, width: 130, height: 69 },
]

function ComplexityStaircase({ n, onChange }: { n: number; onChange: (value: number) => void }) {
  return <section className="complexity-scene" aria-label="Isometric comparison of algorithm growth">
    <div className="scene-heading">
      <div><span className="eyebrow">THE BIG-O LANDSCAPE</span><h3>Same input. Different amount of work.</h3></div>
      <span className="scene-live"><i aria-hidden="true"/> LIVE MODEL</span>
    </div>
    <div className="scene-canvas">
      <svg className="staircase-svg" viewBox="0 0 840 450" role="img" aria-label={`For input size ${n}: constant time has 1 illustrative operation, logarithmic has ${Math.max(1, Math.ceil(Math.log2(n)))} levels, linear has ${n}, and quadratic has ${n*n} operations.`}>
        <defs>
          <pattern id="algo-iso-grid" width="42" height="24" patternUnits="userSpaceOnUse" patternTransform="skewY(-28)">
            <path d="M 42 0 L 0 0 0 24" fill="none" stroke="#ffffff" strokeOpacity=".075" strokeWidth="1"/>
          </pattern>
          <pattern id="algo-stripe-fill" width="9" height="9" patternUnits="userSpaceOnUse" patternTransform="rotate(38)">
            <rect width="9" height="9" fill="#d95f48"/>
            <path d="M0 0V9" stroke="#ffe0d4" strokeWidth="3" opacity=".8"/>
          </pattern>
          <linearGradient id="algo-top-face" x1="0" y1="0" x2="1" y2="1"><stop stopColor="#f8f5ec"/><stop offset="1" stopColor="#c9c7c0"/></linearGradient>
          <filter id="algo-scene-shadow" x="-30%" y="-30%" width="160%" height="180%"><feGaussianBlur stdDeviation="8"/></filter>
        </defs>
        <rect width="840" height="450" fill="url(#algo-iso-grid)" opacity=".9"/>
        <ellipse cx="410" cy="395" rx="340" ry="32" fill="#000" opacity=".36" filter="url(#algo-scene-shadow)"/>
        {staircases.map((item, index) => {
          const { x, y, width, height } = item
          const depth = 22
          const count = Math.max(1, Math.round(operationCount(item.id, n)))
          const visibleCubes = Math.min(count, 4)
          const top = `${x},${y} ${x + width/2},${y-depth} ${x+width},${y} ${x+width/2},${y+depth}`
          const left = `${x},${y} ${x+width/2},${y+depth} ${x+width/2},${y+depth+height} ${x},${y+height}`
          const right = `${x+width/2},${y+depth} ${x+width},${y} ${x+width},${y+height} ${x+width/2},${y+depth+height}`
          return <g key={item.id} className="stair-block" style={{ '--stair-delay': `${index * 130}ms` } as React.CSSProperties}>
            <title>{item.label}: {count.toLocaleString()} illustrative {count === 1 ? 'operation' : 'operations'}</title>
            <polygon points={left} fill="#272a30" stroke="#d8d6d0" strokeWidth="1.4"/>
            <polygon points={right} fill="url(#algo-stripe-fill)" stroke="#f1e9dd" strokeWidth="1.4"/>
            <polygon points={top} fill="url(#algo-top-face)" stroke="#fffaf0" strokeWidth="1.5"/>
            {Array.from({ length: visibleCubes }, (_, cube) => {
              const cx = x + 18 + (cube % 2) * 42 + Math.floor(cube / 2) * 7
              const cy = y - 5 - Math.floor(cube / 2) * 7
              return <g key={cube} className="scene-cube">
                <polygon points={`${cx},${cy} ${cx+17},${cy-9} ${cx+34},${cy} ${cx+17},${cy+9}`} fill="#f6f2e8" stroke="#34363a" strokeWidth="1"/>
                <polygon points={`${cx},${cy} ${cx+17},${cy+9} ${cx+17},${cy+24} ${cx},${cy+15}`} fill="#b9b7b0" stroke="#34363a" strokeWidth="1"/>
                <polygon points={`${cx+17},${cy+9} ${cx+34},${cy} ${cx+34},${cy+15} ${cx+17},${cy+24}`} fill="url(#algo-stripe-fill)" stroke="#34363a" strokeWidth="1"/>
              </g>
            })}
            {count > visibleCubes && <text x={x+width/2} y={y+7} textAnchor="middle" className="cube-overflow">+{(count-visibleCubes).toLocaleString()}</text>}
            <text x={x+width/2} y={y+depth+height-11} textAnchor="middle" className="stair-complexity">{item.id}</text>
            <g className="step-pill">
              <rect x={x+width-3} y={y+depth+height-17} width="137" height="42" rx="12"/>
              <text x={x+width+65} y={y+depth+height+10} textAnchor="middle">{count.toLocaleString()} {count===1?'op':'ops'}</text>
            </g>
          </g>
        })}
        <g className="input-callout">
          <path d="M 303 83 L 650 143" stroke="#e76a50" strokeWidth="2.5" fill="none"/>
          <circle cx="650" cy="143" r="7" fill="#e76a50"/>
          <rect x="150" y="37" width="170" height="56" rx="15" fill="#d95f48"/>
          <text x="235" y="73" textAnchor="middle">n = {n}</text>
        </g>
      </svg>
    </div>
    <div className="scene-slider">
      <label htmlFor="scene-input">Input size <strong>n = {n}</strong></label>
      <input id="scene-input" type="range" min="1" max={MAX_N} value={n} onChange={e => onChange(Number(e.target.value))} aria-label="Set input size for all Big-O visualizations"/>
    </div>
    <p className="scene-caption">The blocks are a visual metaphor; operation counts are illustrative growth models, not measured runtime benchmarks.</p>
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
      <div className="panel-heading"><div><span className="eyebrow">GROWTH EXPLORER</span><h2>How does work scale?</h2><p>Toggle curves and change the shared input size to compare growth.</p></div><button className={normalised ? 'small-toggle active' : 'small-toggle'} onClick={() => setNormalised(v => !v)} aria-pressed={normalised}>Normalise curves</button></div>
      <div className="chart-wrap">
        <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Chart comparing Big-O complexity growth rates">
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
      <div className="chart-controls"><label htmlFor="input-size">Input size <strong>n = {n}</strong></label><input id="input-size" type="range" min="1" max={MAX_N} value={n} onChange={e=>setN(Number(e.target.value))} aria-label="Set shared input size"/><span className="mono muted">1 — {MAX_N}</span></div>
      <div className="complexity-legend">{complexities.map(c => <button key={c.id} onClick={()=>toggle(c.id)} aria-pressed={selected.includes(c.id)} className={selected.includes(c.id)?'legend-chip selected':'legend-chip'} style={{'--series':c.color} as React.CSSProperties}><i/>{c.id}</button>)}</div>
      <div className="metrics-grid">{complexities.filter(c=>selected.includes(c.id)).map(c=><div className="metric" key={c.id}><span style={{color:c.color}}>{c.id}</span><strong>{operationCount(c.id,n).toLocaleString('en-US',{maximumFractionDigits:1})}</strong><small>{c.label}</small></div>)}</div>
      <p className="fine-print">These are illustrative operation-count models, not benchmark timings. Exact counts depend on the algorithm and input; exponential values are capped for display.</p>
    </section>
  </>
}
