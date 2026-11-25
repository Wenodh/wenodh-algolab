type Props = { lines?: string[]; activeLine?: number }
export default function CodePanel({ lines = [], activeLine = -1 }: Props) {
  return <div className="code-panel"><div className="code-top"><span className="traffic"><i/><i/><i/></span><span>example.ts</span><span className="code-lang">TYPESCRIPT</span></div><pre>{lines.map((line,i)=><div key={i} className={i===activeLine?'code-line active':'code-line'}><span className="line-no">{i+1}</span><code>{line || ' '}</code></div>)}</pre></div>
}