import { useMemo, useState } from 'react'
import { complexities, operationCount, type Complexity } from '../data/lessons'

const MAX_N = 32
const W = 720
const H = 300
const PAD = { top: 18, right: 18, bottom: 34, left: 48 }

export default function BigOVisualizer() {
  const [n, setN] = useState(12)
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
  return <section className="panel graph-panel">
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
}