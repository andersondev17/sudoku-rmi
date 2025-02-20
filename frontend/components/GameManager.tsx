import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { BoardSize, BoardState, CellValue, GameState } from '@/types/sudoku';
import { ArrowLeft } from 'lucide-react';
import { useCallback, useEffect, useState } from 'react';
import GameLobby from './GameLobby';
import SudokuBoard from './SudokuBoard';


interface GameInfo {
    gameId: string;
    size: number;
    players: number;
}
interface GameUpdateData {
    type: 'GAME_START' | 'TURN_UPDATE' | 'GAME_END' | 'PLAYER_JOINED' | 'AVAILABLE_GAMES';
    playerId?: number;
    board?: CellValue[][];
    isMyTurn?: boolean;
    gamesList?: GameInfo[];  // Añadir esta propiedad
    winner?: number;
    availableGames?: number;
    gameId?: string;
}

interface GameStateExtended extends GameState {
    gameMode: BoardSize | null;
    message: string;
    connected: boolean;
    currentBoard: BoardState | null;
    availableGames: GameInfo[]; 
}

const GameManager = () => {
    const [websocket, setWebsocket] = useState<WebSocket | null>(null);
    const [gameState, setGameState] = useState<GameStateExtended>({
        playerId: null,
        isMyTurn: false,
        board: {
            cells: [],
            selectedCell: null,
            size: 9
        },
        error: null,
        loading: false,
        gameMode: null,
        message: '',
        connected: false,
        currentBoard: null,
        availableGames: []
    });

    useEffect(() => {
        const connectWebSocket = () => {
            const ws = new WebSocket('ws://localhost:8025/websockets/game');

            ws.onopen = () => {
                console.log('Conectado al servidor WebSocket');
                setGameState(prev => ({ ...prev, connected: true, error: null }));
                // Solicitar partidas disponibles al conectar
                ws.send(JSON.stringify({ type: 'GET_AVAILABLE_GAMES' }));
            };

            ws.onclose = () => {
                console.log('Desconectado del servidor WebSocket');
                setGameState(prev => ({
                    ...prev,
                    connected: false,
                    error: 'Conexión perdida. Reconectando...'
                }));
                setTimeout(connectWebSocket, 3000);
            };

            ws.onerror = (error) => {
                console.error('Error WebSocket:', error);
                setGameState(prev => ({
                    ...prev,
                    error: 'Error de conexión'
                }));
            };

            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data) as GameUpdateData;
                    handleGameUpdate(data);
                } catch (error) {
                    console.error('Error al procesar mensaje:', error);
                }
            };

            setWebsocket(ws);
        };

        connectWebSocket();

        return () => {
            if (websocket?.readyState === WebSocket.OPEN) {
                websocket.close();
            }
        };
    }, []);

    const handleGameUpdate = useCallback((data: GameUpdateData) => {
        switch (data.type) {
            case 'AVAILABLE_GAMES':
                setGameState(prev => ({
                    ...prev,
                    availableGames: data.gamesList || []
                }));
                break;
            case 'PLAYER_JOINED':
                setGameState(prev => ({
                    ...prev,
                    message: '¡Jugador 2 se ha unido! Iniciando juego...',
                    loading: false
                }));
                break;
            case 'GAME_START':
                setGameState(prev => ({
                    ...prev,
                    playerId: data.playerId || null,
                    currentBoard: data.board ? {
                        cells: data.board.map(row => row.map(value => ({
                            value,
                            isInitial: value !== 0,
                            isError: false
                        }))),
                        selectedCell: null,
                        size: prev.gameMode || 9
                    } : null,
                    isMyTurn: data.isMyTurn || false,
                    message: 'Juego iniciado',
                    loading: false
                }));
                break;
            case 'TURN_UPDATE':
                if (data.playerId && gameState.playerId) {
                    setGameState(prev => ({
                        ...prev,
                        isMyTurn: data.playerId === prev.playerId,
                        message: data.playerId === prev.playerId ? 'Tu turno!' : 'Turno del oponente'
                    }));
                }
                break;
            case 'GAME_END':
                if (data.winner && gameState.playerId) {
                    setGameState(prev => ({
                        ...prev,
                        message: data.winner === prev.playerId ? '¡Ganaste!' : 'Perdiste',
                        isMyTurn: false
                    }));
                }
                break;
        }
    }, [gameState.playerId]);

    const createGame = (size: BoardSize) => {
        if (websocket?.readyState === WebSocket.OPEN) {
            websocket.send(JSON.stringify({
                type: 'CREATE_GAME',
                size: size
            }));
            setGameState(prev => ({
                ...prev,
                gameMode: size,
                message: 'Esperando otro jugador...',
                loading: true
            }));
        }
    };

    const joinGame = (gameId: string) => {
        if (websocket?.readyState === WebSocket.OPEN) {
            websocket.send(JSON.stringify({
                type: 'JOIN_GAME',
                gameId
            }));
            setGameState(prev => ({
                ...prev,
                message: 'Uniéndose a la partida...',
                loading: true
            }));
        }
    };

    const handleMove = (row: number, col: number, value: number) => {
        if (websocket?.readyState === WebSocket.OPEN) {
            websocket.send(JSON.stringify({
                type: 'MAKE_MOVE',
                playerId: gameState.playerId,
                row,
                col,
                value
            }));
        }
    };

    const resetGame = () => {
        setGameState(prev => ({
            ...prev,
            gameMode: null,
            currentBoard: null,
            message: '',
            loading: false
        }));
        // Solicitar actualización de partidas disponibles
        if (websocket?.readyState === WebSocket.OPEN) {
            websocket.send(JSON.stringify({ type: 'GET_AVAILABLE_GAMES' }));
        }
    };

    return (
        <div className="w-full max-w-4xl mx-auto space-y-6">
            {gameState.message && (
                <Alert>
                    <AlertTitle>Estado del Juego</AlertTitle>
                    <AlertDescription>{gameState.message}</AlertDescription>
                </Alert>
            )}

            {gameState.error && (
                <Alert variant="destructive">
                    <AlertTitle>Error</AlertTitle>
                    <AlertDescription>{gameState.error}</AlertDescription>
                </Alert>
            )}

            {gameState.gameMode && (
                <Button
                    variant="outline"
                    onClick={resetGame}
                    className="mb-4"
                >
                    <ArrowLeft className="mr-2 h-4 w-4" />
                    Volver al Inicio
                </Button>
            )}

            {!gameState.gameMode ? (
                <GameLobby 
                    onCreateGame={createGame}
                    onJoinGame={joinGame}
                    availableGames={gameState.availableGames}
                    loading={gameState.loading}
                />
            ) : (
                gameState.currentBoard && (
                    <SudokuBoard
                        size={gameState.gameMode}
                        board={gameState.currentBoard}
                        isMyTurn={gameState.isMyTurn}
                        onMove={handleMove}
                        playerId={gameState.playerId || 0}
                    />
                )
            )}
        </div>
    );
};

export default GameManager;