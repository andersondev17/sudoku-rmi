import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
    BoardState,
    CellValue,
    GameInitResponse,
    GameMoveResponse,
    SudokuBoardProps
} from '@/types/sudoku';
import { useEffect, useState } from 'react';
import { SudokuCell } from './SudokuCell';

export default function SudokuBoard({ size = 9 }: SudokuBoardProps) {
    const [socket, setSocket] = useState<WebSocket | null>(null);
    const [gameStatus, setGameStatus] = useState<string>("");
    const [boardState, setBoardState] = useState<BoardState>({
        cells: Array(size).fill(null).map(() => 
            Array(size).fill(null).map(() => ({
                value: 0,
                isInitial: false
            }))
        ),
        selectedCell: null,
        size
    });
    const [playerId, setPlayerId] = useState<number | null>(null);
    const [isMyTurn, setIsMyTurn] = useState<boolean>(false);

    useEffect(() => {
        initializeGame();
    }, []);

    const initializeGame = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/game/init', {
                method: 'POST',
            });
            const data: GameInitResponse = await response.json();
            
            setPlayerId(data.playerId);
            setIsMyTurn(data.isMyTurn);
            
            // Convertir el tablero plano a estado de celdas
            setBoardState(prev => ({
                ...prev,
                cells: data.board.map(row => 
                    row.map(value => ({
                        value,
                        isInitial: value !== 0
                    }))
                )
            }));
        } catch (error) {
            console.error('Error initializing game:', error);
        }
    };

    const handleCellClick = (row: number, col: number) => {
        if (!boardState.cells[row][col].isInitial) {
            setBoardState(prev => ({
                ...prev,
                selectedCell: { row, col }
            }));
        }
    };

    const handleCellChange = async (row: number, col: number, value: CellValue) => {
        if (!playerId || boardState.cells[row][col].isInitial) return;
    
        try {
            const response = await fetch('http://localhost:8080/api/game/move', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    playerId,
                    row,
                    col,
                    value
                }),
            });
    
            const data: GameMoveResponse = await response.json();
            if (data.valid) {
                setBoardState(prev => ({
                    ...prev,
                    cells: data.board!.map((row, i) => 
                        row.map((value, j) => ({
                            value,
                            isInitial: prev.cells[i][j].isInitial
                        }))
                    )
                }));
                setIsMyTurn(false);
            }
        } catch (error) {
            console.error('Error making move:', error);
        }
    };

    return (
        <Card className="w-full max-w-4xl mx-auto">
            <CardContent className="p-6">
                <div className="grid grid-cols-9 gap-0.5 bg-gray-200 p-2 rounded-lg">
                    {boardState.cells.map((row, i) =>
                        row.map((cell, j) => (
                            <SudokuCell
                                key={`${i}-${j}`}
                                value={cell.value}
                                isSelected={boardState.selectedCell?.row === i && 
                                          boardState.selectedCell?.col === j}
                                isInitial={cell.isInitial}
                                isError={cell.isError}
                                onChange={(value) => handleCellChange(i, j, value)}
                                onClick={() => handleCellClick(i, j)}
                            />
                        ))
                    )}
                </div>
                <div className="mt-6 grid grid-cols-5 gap-2 justify-center">
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                        <Button
                            key={num}
                            onClick={() => {
                                if (boardState.selectedCell) {
                                    handleCellChange(
                                        boardState.selectedCell.row,
                                        boardState.selectedCell.col,
                                        num
                                    );
                                }
                            }}
                            variant="outline"
                            className="w-12 h-12 text-lg font-semibold"
                            disabled={!isMyTurn || !boardState.selectedCell}
                        >
                            {num}
                        </Button>
                    ))}
                </div>
            </CardContent>
        </Card>
    );
}