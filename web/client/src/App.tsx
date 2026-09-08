import { useEffect, useRef, useState } from "react";

type Track = { name: string; url: string; size?: number };
const starterTracks: Track[] = [];

function MusicPlayer() {
  const audioRef = useRef<HTMLAudioElement>(null);
  const [tracks, setTracks] = useState<Track[]>(starterTracks);
  const [activeIndex, setActiveIndex] = useState(0);
  const [playing, setPlaying] = useState(false);
  const active = tracks[activeIndex];

  useEffect(() => () => tracks.forEach((track) => URL.revokeObjectURL(track.url)), [tracks]);
  useEffect(() => { if (audioRef.current && active) { audioRef.current.src = active.url; audioRef.current.load(); } }, [active]);
  const chooseFiles = (files: FileList | null) => {
    if (!files) return;
    const next = Array.from(files).filter((file) => file.type === "audio/mpeg" || file.name.toLowerCase().endsWith(".mp3")).map((file) => ({ name: file.name.replace(/\.mp3$/i, ""), url: URL.createObjectURL(file), size: file.size }));
    setTracks((current) => [...current, ...next]);
    if (!active) setActiveIndex(0);
  };
  const playTrack = (index: number) => { setActiveIndex(index); setPlaying(true); window.setTimeout(() => void audioRef.current?.play(), 0); };
  const toggle = () => { if (!audioRef.current || !active) return; if (playing) { audioRef.current.pause(); setPlaying(false); } else { void audioRef.current.play(); setPlaying(true); } };
  return <section className="music-page"><div className="music-topline"><div><p className="eyebrow accent">LOCAL LIBRARY</p><h2>RyzeBeats</h2><p className="music-subtitle">Your music, kept close.</p></div><label className="add-music-button">+ Add MP3s<input type="file" accept="audio/mpeg,.mp3" multiple onChange={(event) => chooseFiles(event.target.files)} /></label></div><div className="player-card"><div className="album-art"><span>RB</span><i>♪</i></div><div className="now-playing"><p className="eyebrow">NOW PLAYING</p><h3>{active?.name ?? "No track selected"}</h3><p>{active ? "Local MP3" : "Choose files to start listening"}</p><div className="fake-progress"><span /></div><div className="player-controls"><button aria-label="Previous track" onClick={() => playTrack(Math.max(0, activeIndex - 1))}>‹‹</button><button className="play-button" aria-label={playing ? "Pause" : "Play"} onClick={toggle}>{playing ? "Ⅱ" : "▶"}</button><button aria-label="Next track" onClick={() => playTrack(Math.min(tracks.length - 1, activeIndex + 1))}>››</button></div></div></div><audio ref={audioRef} onEnded={() => activeIndex < tracks.length - 1 ? playTrack(activeIndex + 1) : setPlaying(false)} onPause={() => setPlaying(false)} onPlay={() => setPlaying(true)} /><div className="library-heading"><div><p className="eyebrow">YOUR FILES</p><h3>{tracks.length ? `${tracks.length} tracks` : "Library is empty"}</h3></div><span className="music-format">MP3</span></div>{tracks.length ? <div className="track-list">{tracks.map((track, index) => <button className={`track-row ${index === activeIndex ? "selected" : ""}`} key={`${track.name}-${index}`} onClick={() => playTrack(index)}><span className="track-number">{String(index + 1).padStart(2, "0")}</span><span className="track-name">{track.name}<small>{track.size ? `${(track.size / 1048576).toFixed(1)} MB` : "Local audio"}</small></span><span className="track-action">{index === activeIndex && playing ? "Ⅱ" : "▶"}</span></button>)}</div> : <label className="empty-library">Drop MP3 files here or <span>browse your device</span><input type="file" accept="audio/mpeg,.mp3" multiple onChange={(event) => chooseFiles(event.target.files)} /></label>}</section>;
}

function Home() { return <><header className="topbar"><div className="brand-lockup"><div className="brand-mark">♪</div><div><p className="eyebrow">MINIMAL HI-FI PLAYER</p><h1>RYZEBEATS</h1></div></div><div className="topbar-status"><span className="status-dot" /> Local audio only</div></header><nav className="app-tabs" aria-label="Main navigation"><span className="active">Music</span><span>Library</span><span>Settings</span></nav><MusicPlayer /><footer className="footer-note"><span>◆</span> Play it your way <span>◆</span><small>Local files stay on your device</small></footer></>; }
export default function App() { return <main className="app-shell"><div className="pixel-grid" aria-hidden="true" /><Home /></main>; }
