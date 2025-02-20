import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { BoardSize, GameInfo } from '@/types/sudoku';
import { Loader2 } from 'lucide-react';

interface GameLobbyProps {
    onCreateGame: (size: BoardSize) => void;
    onJoinGame: (gameId: string) => void;  // Modificar para incluir gameId
    availableGames: GameInfo[];  // Cambiar a array de GameInfo
    loading: boolean;
}

const GameLobby: React.FC<GameLobbyProps> = ({
    onCreateGame,
    onJoinGame,
    availableGames,
    loading
}) => {
    return (
        <Card className="w-full">
            <CardHeader>
                <CardTitle>Sudoku Multijugador</CardTitle>
            </CardHeader>
            <CardContent>
                <Tabs defaultValue="create">
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="create">Crear Partida</TabsTrigger>
                        <TabsTrigger value="join">Unirse a Partida</TabsTrigger>
                    </TabsList>

                    <TabsContent value="create">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4">
                            <Button
                                onClick={() => onCreateGame(4)}
                                disabled={loading}
                            >
                                {loading ? (
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                ) : 'Matriz 4x4'}
                            </Button>
                            <Button
                                onClick={() => onCreateGame(9)}
                                disabled={loading}
                            >
                                {loading ? (
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                ) : 'Matriz 9x9'}
                            </Button>
                            <Button
                                onClick={() => onCreateGame(16)}
                                disabled={loading}
                            >
                                {loading ? (
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                ) : 'Matriz 16x16'}
                            </Button>
                        </div>
                    </TabsContent>

                    <TabsContent value="join" className="mt-4">
                        <div className="space-y-4">
                            <p className="text-center text-muted-foreground">
                                {availableGames.length > 0
                                    ? `Hay ${availableGames.length} partida${availableGames.length !== 1 ? 's' : ''} disponible${availableGames.length !== 1 ? 's' : ''}`
                                    : 'No hay partidas disponibles'}
                            </p>
                            {availableGames.map(game => (
                                <Button
                                    key={game.gameId}
                                    onClick={() => onJoinGame(game.gameId)}
                                    disabled={loading}
                                    className="w-full mb-2"
                                >
                                    {loading ? (
                                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    ) : `Unirse a partida ${game.size}x${game.size}`}
                                </Button>
                            ))}
                        </div>
                    </TabsContent>
                </Tabs>
            </CardContent>
        </Card>
    );
};

export default GameLobby;