package fr.irwin.uge.internals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ASCIITable
{
    /**
     * Represents chars composing the ASCII table [bottom][up][left][right].
     */
    private static final char[][][][] CHARS = {{{{' ', '╶'}, {'╴', '─'}}, {{'╵', '└'}, {'┘', '┴'}}}, {{{'╷', '┌'}, {'┐', '┬'}}, {{'│', '├'}, {'┤', '┼'}}}};
    private static final char HORIZONTAL = CHARS[0][0][1][1];
    private static final char VERTICAL = CHARS[1][1][0][0];

    /**
     * Represents a list of rows, themselves representing a list of cells.
     */
    private final List<List<String>> table = new ArrayList<>();
    /**
     * Represents width of each column.
     */
    private final List<Integer> colWidths = new ArrayList<>();

    private int row = 0;
    private int col = 0;
    private int currentRow = -1;
    private int currentCol = -1;

    /**
     * @return new instance of ASCIITable.
     */
    public static ASCIITable of()
    {
        return new ASCIITable();
    }

    /**
     * @return current instance of ASCIITable.
     */
    public ASCIITable nextRow()
    {
        currentRow = addRow() - 1;
        currentCol = -1;
        return this;
    }

    /**
     * @return current instance of ASCIITable.
     */
    public ASCIITable nextCell()
    {
        currentCol++;
        if (currentCol == col)
        {
            currentCol = addColumn() - 1;
        }
        return this;
    }

    /**
     * Adds an empty cell to the current row.
     *
     * @return current instance of ASCIITable.
     */
    public ASCIITable blank()
    {
        return setText("§");
    }

    /**
     * Adds a new cell to the current row.
     *
     * @param text text to put in the cell.
     * @return current instance of ASCIITable.
     */
    public ASCIITable setText(String text)
    {
        table.get(currentRow).set(currentCol, text);
        if (text.length() > colWidths.get(currentCol))
        {
            colWidths.set(currentCol, text.length());
        }
        return this;
    }

    /**
     * @return whether every cell of the table is empty.
     */
    public boolean isEmpty()
    {
        for (int row = 0; row < this.row; row++)
        {
            for (int col = 0; col < this.col; col++)
            {
                if (!empty(row, col))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Adds a new row to the bottom, filled with blank cells.
     *
     * @return new row count.
     */
    private int addRow()
    {
        table.add(new ArrayList<>());
        table.get(row).addAll(Collections.nCopies(col, ""));
        return ++row;
    }

    /**
     * Adds a new column to the right, filled with blank cells.
     *
     * @return new column count.
     */
    private int addColumn()
    {
        table.forEach(r -> r.add(""));
        colWidths.add(0);
        return ++col;
    }

    /**
     * @return whether the cell at [{@code row}, {@code col}] is empty.
     */
    private boolean empty(int row, int col)
    {
        if (row < 0 || row >= this.row)
        {
            return true;
        }
        if (col < 0 || col >= this.col)
        {
            return true;
        }
        return table.get(row).get(col).trim().isEmpty();
    }

    /**
     * @return returns the upper horizontal line of the {@code n}th row.
     */
    private String getHorizontalLine(int n)
    {
        StringBuilder builder = new StringBuilder();
        for (int col = 0; col < this.col; col++)
        {
            builder.append(getIntersect(n, col));
            if (!empty(n - 1, col) || !empty(n, col))
            {
                builder.append(String.valueOf(HORIZONTAL).repeat(colWidths.get(col)));
            }
            else
            {
                builder.append(String.valueOf(' ').repeat(colWidths.get(col)));
            }
        }
        builder.append(getIntersect(n, this.col));
        return builder.toString();
    }

    private char getIntersect(int row, int col)
    {
        int up = empty(row - 1, col - 1) && empty(row - 1, col) ? 0 : 1;
        int bottom = empty(row, col - 1) && empty(row, col) ? 0 : 1;
        int left = empty(row - 1, col - 1) && empty(row, col - 1) ? 0 : 1;
        int right = empty(row - 1, col) && empty(row, col) ? 0 : 1;
        return CHARS[bottom][up][left][right];
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (int row = 0; row < this.row; row++)
        {
            builder.append(getHorizontalLine(row));
            builder.append('\n');
            for (int col = 0; col < this.col; col++)
            {
                String cellValue = table.get(row).get(col).replace('§', ' ');
                builder.append((!empty(row, col) || !empty(row, col - 1)) ? VERTICAL : ' ');
                builder.append(cellValue).append(String.valueOf(' ').repeat(colWidths.get(col) - cellValue.length()));
            }
            builder.append(!empty(row, col - 1) ? VERTICAL : ' ');
            builder.append('\n');
        }
        builder.append(getHorizontalLine(row));
        return builder.toString();
    }
}
