type Props = { values?: number[]; highlight?: number[] }
export default function Visualizer({ values = [8,3,7,1,9], highlight = [] }: Props) {
  const max = Math.max(...values, 1)
  return <div className="array-visual" aria-label="Array visualization">{values.map((v,i)=><div className="array-item" key={i}><div className={highlight.includes(i)?'array-bar highlighted':'array-bar'} style={{height:`${Math.max(18,(v/max)*100)}%`}}><span>{v}</span></div><small>{i}</small></div>)}</div>
}