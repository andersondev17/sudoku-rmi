'use client';

import dynamic from 'next/dynamic';

// Importamos el componente de forma dinámica para evitar problemas de SSR
const GameManager = dynamic(() => import('@/components/GameManager'), {
  ssr: false
});
export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-background to-muted p-8">
      <div className="container mx-auto max-w-4xl">
        <h1 className="text-4xl font-bold text-center mb-8 text-foreground">
          Sudoku Multiplayer
        </h1>
        <GameManager />
      </div>
    </div>
  );
}