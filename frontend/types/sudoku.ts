// Tipos básicos
export type CellValue = number;
export type BoardSize = 4 | 9 | 16;

// Estado de una celda
export interface CellState {
    value: CellValue;
    isInitial: boolean;
    isError?: boolean;
}


// Posición en el tablero
export interface CellPosition {
    row: number;
    col: number;
}

// Estado del tablero
export interface BoardState {
    cells: CellState[][];
    selectedCell: CellPosition | null;
    size: BoardSize;
}

// Props para el componente SudokuCell
export interface SudokuCellProps {
    value: CellValue;
    isSelected: boolean;
    isInitial: boolean;
    isError?: boolean;
    onClick: () => void;
    onChange: (value: CellValue) => void;
}
export interface GameInfo {
    gameId: string;
    size: number;
    players: number;
}
// Props para el componente SudokuBoard
export interface SudokuBoardProps {
    size?: BoardSize;
    isMyTurn: boolean;
    board:BoardState;
    playerId: number | null;
    onMove: (row: number, col: number, value: number) => void
}

// Respuesta del servidor
export interface GameInitResponse {
    playerId: number;
    board: CellValue[][];
    isMyTurn: boolean;
}

export interface GameMoveResponse {
    valid: boolean;
    board: CellValue[][];
    error?: string;
}

// Estado del juego
export interface GameState {
    playerId: number | null;
    isMyTurn: boolean;
    board: BoardState;
    error: string | null;
    loading: boolean;
}