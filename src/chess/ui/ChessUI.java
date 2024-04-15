package chess.ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessConstants;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/* for drop and drag style of input or click */
enum inputType { Drag, Click }

public class ChessUI extends JFrame {
    private final ChessGame game = new ChessGame();
    private final SquareComponent[][] squares = new SquareComponent[ChessConstants.MAX_ROW][ChessConstants.MAX_COL];

    private final inputType input;

    private final JLayeredPane layeredPane = new JLayeredPane();
    private final JPanel chessBoard = new JPanel(new GridLayout(ChessConstants.MAX_ROW, ChessConstants.MAX_COL));

    private ChessBoard.Position selectedSquare;

    public ChessUI() {
        input = inputType.Click;

        Dimension boardSize = new Dimension(500, 500);


        // test engine
        game.addEngine("test_chess_engines/stockfish-windows-x86-64.exe");


        // setup layered pane
        getContentPane().add(layeredPane);

        layeredPane.setPreferredSize(boardSize);
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        if (input == inputType.Drag) {
            MouseHandlerDragInput mHandle = new MouseHandlerDragInput(layeredPane);

            layeredPane.addMouseMotionListener(mHandle);
            layeredPane.addMouseListener(mHandle);
        }


        // setup chessboard
        chessBoard.setPreferredSize(boardSize);
        chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);
        List<SquareComponent> clickInputHlList = input == inputType.Click ? new ArrayList<>() : null;

        for (int row = ChessConstants.MIN_ROW; row < ChessConstants.MAX_ROW; row++) {
            for (int col = ChessConstants.MIN_COL; col < ChessConstants.MAX_COL; col++) {
                SquareComponent square = input == inputType.Click
                        ? new SquareComponent(row, col, clickInputHlList)
                        : new SquareComponent(row, col);

                ChessPiece piece = game.getBoard().getPiece(row, col);

                if (piece != null) square.setText(piece.getSymbol());

                chessBoard.add(square);
                squares[row][col] = square;
            }
        }
        selectedSquare = null;


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        pack();


//        init();
//        setTitle("Chess Game");
//        setLayout(new GridLayout(chess.ChessConstants.MAX_ROW, chess.ChessConstants.MAX_COL));
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setVisible(true);
//
//        pack();
    }

//    private void init() {
//        for (int row = chess.ChessConstants.MIN_ROW; row < chess.ChessConstants.MAX_ROW; row++) {
//            for (int col = chess.ChessConstants.MIN_COL; col < chess.ChessConstants.MAX_COL; col++) {
//            }
//        }
//        selectedSquare = null;
//    }

    private boolean moveIfLegal(ChessBoard.Position oldPosition, ChessBoard.Position newPosition) {
        boolean moved = game.moveIfLegal(oldPosition, newPosition);
        if (moved) {
            if (game.getEnPassantMove()) {
                int row = game.getBoard().getEnPassantPiece().row();
                int col = game.getBoard().getEnPassantPiece().col();
                squares[row][col].setText(null);
            }
            squares[oldPosition.row()][oldPosition.col()].setText(null);
            squares[newPosition.row()][newPosition.col()]
                        .setText(game.getBoard().getPiece(newPosition).getSymbol());
        }
        return moved;
    }

    private class SquareComponent extends JLabel {
        private static final int DEFAULT_WIDTH = 70;
        private static final int DEFAULT_HEIGHT = 70;

        private final int row;
        private final int col;

        // only used for click input
        MouseHandlerClickInput.Highlight highlight;


        public ChessPiece getPiece() {
            return game.getBoard().getPiece(row, col);
        }

        public ChessBoard.Position getPosition() {
            return new ChessBoard.Position(row, col);
        }

        // constructor for drag style input
        public SquareComponent(int row, int col) {
            this.row = row;
            this.col = col;

            setOpaque(true); // for JLabel

            setBackground((row + col) % 2 == 0 ? Color.LIGHT_GRAY : new Color(125, 135, 150));

            setHorizontalAlignment(SwingConstants.CENTER);
            setVerticalAlignment(SwingConstants.CENTER);
            setFont(new Font("Serif", Font.BOLD, 32));
        }

        // constructor for click style input
        public SquareComponent(int row, int col, List<SquareComponent> l) {
            this(row, col);

            addMouseListener(new MouseHandlerClickInput(row, col, l));

            highlight = new MouseHandlerClickInput.Highlight();
            highlight.setOpaque(false);
            highlight.setSize(70, 70);
            highlight.setVisible(false);

            add(highlight, JLayeredPane.DRAG_LAYER);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        }
    }

    private class MouseHandlerClickInput extends MouseInputAdapter {
        final ChessBoard.Position position;
        List<SquareComponent> hlSquares;

        MouseHandlerClickInput(int row, int col, List<SquareComponent> l) {
            position = new ChessBoard.Position(row, col);
            hlSquares = l;
        }

        ChessPiece getChessPiece() {
            return game.getBoard().getPiece(position);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (selectedSquare == null) {
                ChessPiece p = getChessPiece();

                if (p != null && game.isWhiteTurn() == p.isWhite()) {
                    if (hlLegalMoves(position)) {
                        selectedSquare = position;
                    } else return;
                }
            } else {
                hlClear();
                boolean moved = moveIfLegal(selectedSquare, position);
                selectedSquare = null;

                if (moved) {
                    setCursor(Cursor.getDefaultCursor());
                    // check game state
                    // check checkmates
                    // check for check
                }
            }
            mouseEntered(e);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            // display cursor has hand when in a square that can be selected or
            // if selected a square that is highlighted
            if (selectedSquare == null) {
                ChessPiece cp = getChessPiece();

                setCursor(cp != null && cp.isWhite() == game.isWhiteTurn()
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            } else {
                setCursor(hlSquares.contains(e.getComponent())
                    ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        }

        private boolean hlLegalMoves(ChessBoard.Position position) {
            if (game.isInCheck()) {
                for (ChessBoard.Position[] p: game.getInCheckPossibleMoves()) {
                    if (p[0].equals(position)) {
                        SquareComponent square = squares[p[1].row()][p[1].col()];
                        hlSquares.add(square);
                        square.highlight.setVisible(true);
                    }
                }
            } else for (ChessBoard.Position move : game.getBoard()
                    .getPiece(position)
                    .getLegalMoves())
            {
                SquareComponent square = squares[move.row()][move.col()];
                hlSquares.add(square);
                square.highlight.setVisible(true);
            }
            return !hlSquares.isEmpty();
        }

        private void hlClear() {
            hlSquares.forEach(sq -> sq.highlight.setVisible(false));
            hlSquares.clear();
        }

        private static class Highlight extends JComponent {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;

                g2.setColor(new Color(0, 0, 0, 120));

                //g2.fillRect(20, 20, 20, 20);
                g2.fillOval(20, 20, 20, 20);
            }
        }
    }

    private class MouseHandlerDragInput extends MouseInputAdapter {
        private final DragComponent dragPiece;
        private SquareComponent currentSquare;

        MouseHandlerDragInput(JLayeredPane pane) {
            dragPiece = new DragComponent();
            currentSquare = new SquareComponent(-1, -1);
            pane.add(dragPiece, JLayeredPane.DRAG_LAYER);
        }

        @Override
        public void mousePressed(MouseEvent e) {

            Component c = chessBoard.findComponentAt(e.getPoint());
            if (!(c instanceof JLabel)) return;

            ChessPiece cp = ((SquareComponent) c).getPiece();

            if (cp != null && game.isWhiteTurn() == cp.isWhite()) {
                dragPiece.setLocation(e.getX() - 70, e.getY() - 70);

                dragPiece.setText(cp.getSymbol());
                dragPiece.setVisible(true);

                selectedSquare = cp.getPosition();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedSquare == null) return;

            Component c = chessBoard.findComponentAt(e.getPoint());

            if (c instanceof JLabel) {
                currentSquare.setBorder(BorderFactory.createEmptyBorder());
                currentSquare = (SquareComponent) c;

                /* feedback only if square is a valid move */
                // if (game.getBoard().getPiece(selectedSquare).isValidMove(currentSquare.getPosition()))

                /* feedback for every square except pieces of the same color */
                // if (currentSquare.getPiece() == null || game.getBoard().getPiece(selectedSquare).isWhite() != currentSquare.getPiece().isWhite())

                currentSquare.setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));
            }
            dragPiece.setLocation(e.getX() - 70, e.getY() - 70);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedSquare == null) return;

            Component c = chessBoard.findComponentAt(e.getPoint());

            dragPiece.setVisible(false);
            mouseMoved(e);

            if (c instanceof JLabel) {
                SquareComponent square = (SquareComponent) c;

                boolean moved = moveIfLegal(selectedSquare, square.getPosition());

                if (moved) {
                    // check game state
                    // check checkmates
                    // check for check
                }
            }
            currentSquare.setBorder(BorderFactory.createEmptyBorder());
            selectedSquare = null;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // set cursor to a hand when hovering over a piece that can be moved (dragged)

            Component c = chessBoard.findComponentAt(e.getPoint());
            if (!(c instanceof JLabel)) return;

            ChessPiece cp = ((SquareComponent) c).getPiece();

            setCursor(cp != null && game.isWhiteTurn() == cp.isWhite()
                ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }

        private static class DragComponent extends JLabel {
            public DragComponent() {
                setOpaque(false);

                setHorizontalAlignment(SwingConstants.TRAILING);
                setVerticalAlignment(SwingConstants.BOTTOM);

                setFont(new Font("Serif", Font.BOLD, 32));
                setSize(new Dimension(70, 70));
            }
        }
    }
}