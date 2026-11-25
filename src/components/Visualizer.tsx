import { useEffect, useState } from 'react'

type Props = { values?: number[]; highlight?: number[] }

export default function Visualizer({ values = [8,3,7,1,9], highlight = [] }: Props) {
  const [visible, setVisible] = useState(false)
  useEffect(() => {
    setVisible(false)
    const frame = window.requestAnimationFrame(() => setVisible(true))
    return () => window.cancelAnimationFrame(frame)
  }, [values.join(',')])
  const max = Math.max(...values, 1)
  return <div className={`array-visual ${visible ? 'array-visual-ready' : ''}`} aria-label="Array visualization">
    {values.map((v,i)=><div className="array-item" key={i} style={{'--bar-delay': `${i*75}ms`} as React.CSSProperties}>
      <div className={highlight.includes(i)?'array-bar highlighted':'array-bar'} style={{height:`${Math.max(18,(v/max)*100)}%`}}><span>{v}</span><i className="bar-shine"/></div>
      <small>{i}</small>
    </div>)}
  </div>
}
