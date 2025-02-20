import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";
import { SudokuCellProps } from "@/types/sudoku";

export function SudokuCell({
    value,
    isSelected,
    isInitial,
    isError,
    onClick,
    onChange,
}: SudokuCellProps) {
    return (
        <Input
            type="number"
            min={1}
            max={9}
            value={value || ''}
            onChange={(e) => onChange(parseInt(e.target.value) || 0)}
            onClick={onClick}
            className={cn(
                "w-12 h-12 text-center text-lg font-semibold transition-all",
                "focus:ring-2 focus:ring-primary",
                isSelected && "bg-primary/20",
                isInitial && "bg-muted font-bold",
                isError && "bg-destructive/20",
                !value && "text-muted-foreground"
            )}
            readOnly={isInitial}
        />
    );
}