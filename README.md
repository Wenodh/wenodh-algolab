# Wenodh AlgoLab

An interactive computer-science learning lab built with React, TypeScript and Vite.

## Features

- Big-O growth explorer with adjustable input size and SVG graph
- Step-through lessons with play, pause, previous/next and playback speed
- Animated array operations and sorting demonstrations
- Complexity reference cards and responsive learning layout

## Development

```bash
npm install
npm run dev
```

## Build

```bash
npm run build
npm run preview
```

## Architecture

Lessons are data-driven. The lesson player owns navigation and playback; visualizers are independent React components selected by visualization type. Add lessons in `src/data/lessons.ts` and register new visualizers in `src/components/Visualizer.tsx`.

## License

MIT
