import { useEffect, useMemo, useState } from 'react'
import { Activity, ArrowLeft, ArrowRight, BookOpen, Check, ChevronDown, Code2, Command, GitBranch, GraduationCap, Layers3, Pause, Play, RotateCcw, Search, Sparkles, Timer, Zap } from 'lucide-react'
import BigOVisualizer from './components/BigOVisualizer'
import CodePanel from './components/CodePanel'
import Visualizer from './components/Visualizer'
import { lessons } from './data/lessons'

export default function App() {
  const [activeId, setActiveId] = useState('big-o')
  const [stepIndex, setStepIndex] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [speed, setSpeed] = useState(1)
  const [query, setQuery] = useState('')
  const lesson = lessons.find(item => item.id === activeId) ?? lessons[0]
  const step = lesson.steps[stepIndex]
  const filtered = useMemo(() => lessons.filter(l => `${l.title} ${l.category}`.toLowerCase().includes(query.toLowerCase())), [query])
  const progress = ((stepIndex + 1) / lesson.steps.length) * 100

  useEffect(() => { setStepIndex(0); setPlaying(false) }, [activeId])
  useEffect(() => {
    if (!playing) return
    const timer = window.setInterval(() => setStepIndex(current => {
      if (current >= lesson.steps.length - 1) { setPlaying(false); return current }
      return current + 1
    }), 2600 / speed)
    return () => window.clearInterval(timer)
  }, [playing, speed, lesson.steps.length])
  const selectLesson = (id: string) => { setActiveId(id); setStepIndex(0); setPlaying(false) }
  const next = () => setStepIndex(s => Math.min(lesson.steps.length - 1, s + 1))
  const previous = () => setStepIndex(s => Math.max(0, s - 1))

  return <div className="app-shell">
    <aside className="sidebar">
      <a className="brand" href="#"><span className="brand-mark"><Activity size={19}/></span><span>wenodh<span className="brand-light">/</span>algolab</span></a>
      <div className="workspace-label">LEARNING PATH</div>
      <div className="course-card"><div className="course-icon"><GraduationCap size={18}/></div><div><strong>Computer Science</strong><small>Foundations · 4 lessons</small></div><ChevronDown size={15}/></div>
      <div className="sidebar-section"><div className="section-title">EXPLORE TOPICS <span>04</span></div>
        <div className="topic-list">{filtered.map((item,i)=><button key={item.id} onClick={()=>selectLesson(item.id)} className={activeId===item.id?'topic active':'topic'}><span className="topic-index">{String(i+1).padStart(2,'0')}</span><span className="topic-copy"><strong>{item.title}</strong><small>{item.category}</small></span>{activeId===item.id&&<span className="active-dot"/>}</button>)}</div>
        {filtered.length===0&&<p className="empty-search">No lessons match your search.</p>}
      </div>
      <div className="sidebar-bottom"><div className="streak"><span className="streak-icon"><Zap size={16}/></span><div><strong>Keep exploring</strong><small>One concept at a time.</small></div></div><div className="profile"><div className="avatar">W</div><div><strong>Wenodh Academy</strong><small>Independent learning lab</small></div><span className="profile-menu">···</span></div></div>
    </aside>

    <main className="main-area">
      <header className="topbar"><div className="breadcrumbs"><span>Learn</span><span>/</span><strong>{lesson.category}</strong><span>/</span><strong>{lesson.title}</strong></div><div className="top-actions"><span className="status"><i/> INTERACTIVE MODE</span><button className="icon-button" aria-label="Search lessons" onClick={()=>document.getElementById('lesson-search')?.focus()}><Search size={17}/></button><div className="top-avatar">W</div></div></header>
      <div className="content">
        <div className="page-intro"><div><div className="eyebrow"><span className="eyebrow-dot"/> YOUR INTERACTIVE STUDY SPACE</div><h1>Learn by <em>visualizing.</em></h1><p>Complex ideas, made tangible. Explore the building blocks of computer science, one step at a time.</p></div><div className="intro-mark"><div className="orbit orbit-one"/><div className="orbit orbit-two"/><div className="orbit-core"><Command size={26}/></div><span className="orbit-node node-a"/><span className="orbit-node node-b"/></div></div>

        <div className="search-row"><div className="search-box"><Search size={16}/><input id="lesson-search" value={query} onChange={e=>setQuery(e.target.value)} placeholder="Find a concept or lesson..."/><kbd>⌘ K</kbd></div><span className="lesson-count"><BookOpen size={14}/> {lessons.length} interactive lessons</span></div>

        <div className="lesson-heading"><div><div className="eyebrow">{lesson.category.toUpperCase()} <span className="dot-sep">·</span> LESSON {String(lessons.findIndex(l=>l.id===lesson.id)+1).padStart(2,'0')}</div><h2>{lesson.title}</h2><p>{lesson.description}</p></div><div className="duration"><Timer size={15}/>{lesson.duration}</div></div>

        {lesson.id==='big-o' ? <BigOVisualizer/> : <section className="panel lesson-visual-panel"><div className="panel-heading"><div><span className="eyebrow">LIVE VISUALIZATION</span><h2>{step.title}</h2><p>{step.body}</p></div><span className="step-count">STEP {stepIndex+1} / {lesson.steps.length}</span></div><Visualizer values={step.values} highlight={step.highlight}/></section>}

        <div className="lesson-grid">
          <section className="panel explanation-panel"><div className="panel-heading compact"><div><span className="eyebrow">WALKTHROUGH</span><h2>{step.title}</h2></div><span className="step-count">{stepIndex+1} of {lesson.steps.length}</span></div><p className="explanation">{step.body}</p><div className="step-progress"><div style={{width:`${progress}%`}}/></div><div className="player-controls"><button className="control-secondary" onClick={previous} disabled={stepIndex===0}><ArrowLeft size={15}/> Previous</button><div className="play-cluster"><button className="play-button" onClick={()=>setPlaying(v=>!v)}>{playing?<Pause size={16} fill="currentColor"/>:<Play size={16} fill="currentColor"/>}{playing?'Pause':'Play'}</button><button className="reset-button" title="Restart lesson" onClick={()=>{setStepIndex(0);setPlaying(false)}}><RotateCcw size={15}/></button></div><button className="control-primary" onClick={next} disabled={stepIndex===lesson.steps.length-1}>Next <ArrowRight size={15}/></button></div><div className="speed-row"><span>PLAYBACK SPEED</span><div>{[0.5,1,1.5,2].map(v=><button key={v} className={speed===v?'speed active':'speed'} onClick={()=>setSpeed(v)}>{v}×</button>)}</div></div></section>
          <section className="panel code-section"><div className="panel-heading compact"><div><span className="eyebrow"><Code2 size={12}/> UNDER THE HOOD</span><h2>Code walkthrough</h2></div><span className="code-badge">JS / TS</span></div><CodePanel lines={step.code} activeLine={step.activeLine}/><div className="code-foot"><span><span className="live-dot"/> Current step highlighted</span><span>Read · Trace · Understand</span></div></section>
        </div>
        <footer className="footer"><span>WENODH ALGOLAB <span className="footer-sep">/</span> LEARN. THINK. BUILD.</span><span>Built for curious minds <Sparkles size={12}/></span></footer>
      </div>
    </main>
  </div>
}