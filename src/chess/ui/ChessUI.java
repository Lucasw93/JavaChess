package chess.ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessConstants;
import chess.engine.ChessEngine;
import chess.pieces.Bishop;
import chess.pieces.Knight;
import chess.pieces.Queen;
import chess.pieces.Rook;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;
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

    /*
     * Engine
     */
    private final List<ChessEngine> engineList = new ArrayList<>();
    private ChessEngine blackEngine;
    private ChessEngine whiteEngine;
    private EngineHandler engineHandler;


    public ChessUI() {
        input = inputType.Drag;

        Dimension boardSize = new Dimension(500, 500);

        /*
         * Test Engine
         */
//        addEngine("test_chess_engines/stockfish-windows-x86-64.exe", "Test engine");
//        useEngine(false, 0);


        // setup layered pane
        getContentPane().add(layeredPane);

        layeredPane.setPreferredSize(boardSize);
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);


        // setup chessboard
        if (input == inputType.Drag) {
            MouseHandlerDragInput mHandler = new MouseHandlerDragInput(getLayeredPane());

            chessBoard.addMouseMotionListener(mHandler);
            chessBoard.addMouseListener(mHandler);
        }

        chessBoard.setPreferredSize(boardSize);
        chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);
        List<SquareComponent> clickInputHlList = input == inputType.Click ? new ArrayList<>() : null;

        for (int row = ChessConstants.MIN_ROW; row < ChessConstants.MAX_ROW; row++) {
            for (int col = ChessConstants.MIN_COL; col < ChessConstants.MAX_COL; col++) {
                SquareComponent square = input == inputType.Click
                        ? new SquareComponent(row, col, clickInputHlList) : new SquareComponent(row, col);

                ChessPiece piece = game.getBoard().getPiece(row, col);

                if (piece != null) square.setText(piece.getSymbol());

                chessBoard.add(square);
                squares[row][col] = square;
            }
        }
        selectedSquare = null;

        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
        setVisible(true);
    }


    private boolean moveIfLegal(ChessBoard.Position oldPosition, ChessBoard.Position newPosition) {
        boolean moved = game.moveIfLegal(oldPosition, newPosition);
        if (moved) {
            if (game.isEnPassantMove()) {
                int row = game.getBoard().getEnPassantPiece().row();
                int col = game.getBoard().getEnPassantPiece().col();
                squares[row][col].setText(null);
            } else if (game.hasPromotion()) {
                new PromotionDialog(oldPosition, newPosition);
            }
            squares[oldPosition.row()][oldPosition.col()].setText(null);
            squares[newPosition.row()][newPosition.col()]
                        .setText(game.getBoard().getPiece(newPosition).getSymbol());

            if (game.hasCheck()) {
                if (game.hasCheckMate()) {
                    switch (getGameOverOptions()) {
                        case 0: resetGame();
                            return false;
                        case 1: System.out.println("TODO");
                            return false;
                    }
                } else {
                    JOptionPane.showMessageDialog( this,
                            (game.isWhiteTurn() ? "White " : "Black ") + "King in Check",
                            null, JOptionPane.INFORMATION_MESSAGE);
                }
            }
            startTurn(game.isWhiteTurn());
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
        final List<SquareComponent> hlSquares;

        MouseHandlerClickInput(int row, int col, List<SquareComponent> l) {
            position = new ChessBoard.Position(row, col);
            hlSquares = l;
        }

        ChessPiece getChessPiece() {
            return game.getBoard().getPiece(position);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (hasEngine(game.isWhiteTurn())) return;

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
            if (hasEngine(game.isWhiteTurn())) return;

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
            for (ChessBoard.Position move : game.getBoard()
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
            public void paintComponent(Graphics g) {
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
            if (hasEngine(game.isWhiteTurn())) return;

            Component c = chessBoard.findComponentAt(e.getPoint());

            if (c instanceof SquareComponent) {
                ChessPiece cp = ((SquareComponent) c).getPiece();

                if (cp != null && game.isWhiteTurn() == cp.isWhite()) {
                    dragPiece.setLocation(e.getX() - 70, e.getY() - 70);

                    dragPiece.setText(cp.getSymbol());
                    dragPiece.setVisible(true);

                    selectedSquare = cp.getPosition();
                }
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (selectedSquare == null) return;

            dragPiece.setLocation(e.getX() - 70, e.getY() - 70);

            Component c = chessBoard.findComponentAt(e.getPoint());

            if (c == currentSquare) return;

            if (c instanceof SquareComponent) {
                currentSquare.setBorder(BorderFactory.createEmptyBorder());
                currentSquare = (SquareComponent) c;
                currentSquare.setBorder(BorderFactory.createLineBorder(Color.GREEN, 4));

                /* feedback only if square is a legal move */
                // if (game.getBoard().getPiece(selectedSquare).isLegalMove(currentSquare.getPosition()))

                /* feedback for every square except pieces of the same color */
                // if (currentSquare.getPiece() == null ||
                //         game.getBoard().getPiece(selectedSquare).isWhite() != currentSquare.getPiece().isWhite())
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedSquare == null) return;

            mouseMoved(e);

            Component c = chessBoard.findComponentAt(e.getPoint());

            if (c instanceof SquareComponent) {
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
            dragPiece.setVisible(false);
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            // set cursor to a hand when hovering over a piece that can be moved (dragged)
            if (hasEngine(game.isWhiteTurn())) return;

            Component c = chessBoard.findComponentAt(e.getPoint());

            if (c instanceof SquareComponent) {
                ChessPiece cp = ((SquareComponent) c).getPiece();

                setCursor(cp != null && game.isWhiteTurn() == cp.isWhite()
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
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

    private class PromotionDialog extends JDialog {
        JPanel panel = new JPanel();
        ButtonGroup group = new ButtonGroup();
        Class<?> promotion = Queen.class;

        public PromotionDialog(ChessBoard.Position oldpos, ChessBoard.Position newpos) {
            addRadioButton("Queen", Queen.class);
            addRadioButton("Rook", Rook.class);
            addRadioButton("Bishop", Bishop.class);
            addRadioButton("Knight", Knight.class);

            JPanel okPanel = new JPanel();
            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> {
                game.promote(oldpos, newpos, promotion);
                dispose();
            });
            okPanel.add(okButton);

            add(panel, BorderLayout.NORTH);
            add(okPanel, BorderLayout.SOUTH);

            getRootPane().setDefaultButton(okButton);
            getRootPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            setUndecorated(true);
            pack();

            setLocationRelativeTo(ChessUI.this);
            setModalityType(ModalityType.TOOLKIT_MODAL);
            setVisible(true);
        }

        private void addRadioButton(String name, Class<?> c) {
            JRadioButton b = new JRadioButton(name, c.equals(Queen.class));
            group.add(b);
            panel.add(b);
            b.addActionListener(e -> promotion = c);
        }
    }

    private int getGameOverOptions() {
        JLabel message = new JLabel("Checkmate: " +
                ((game.isWhiteTurn() ? "Black " : "White ") + "Wins "));

        message.setFont(new Font("SansSerif", Font.BOLD, 18));

        String[] options = {"New Game",
                            "Analysis"};
        return JOptionPane.showOptionDialog(this, message, null,
                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                options, options[1]);
    }

    private void resetGame() {
        game.resetGame();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece p = game.getBoard().getPiece(row, col);

                squares[row][col].setText(p == null ? null : p.getSymbol());
            }
        }
    }


    /*
     * Engine
     */
    private class EngineHandler implements ActionListener, Runnable {
        private ChessEngine.GoResult result;
        private String error;

        public boolean hasError() {
            return error != null;
        }

        public String error() {
            return error;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (result == null) return;

            String[] bestmove = result.bestmove().split(" ");
            String move = bestmove[1];

            if (move.length() == 4) {
                ChessBoard.Position oldpos = new ChessBoard.Position(move.substring(0, 2));
                ChessBoard.Position newpos = new ChessBoard.Position(move.substring(2, 4));
                moveIfLegal(oldpos, newpos);
            } else if (move.length() == 5) {
                /*
                 * promotion
                 */
            } else {
                throw new RuntimeException();
            }
            result = null;

            Component c = findComponentAt(getMousePosition());

            if (c instanceof SquareComponent) {
                ChessPiece cp = ((SquareComponent) c).getPiece();

                setCursor(cp != null && game.isWhiteTurn() == cp.isWhite()
                        ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        }

        @Override
        public void run() {
            ChessEngine engine = game.isWhiteTurn() ? whiteEngine : blackEngine;
            try {
                engine.position(String.join(" ", game.log()));

                result = engine.go(2000);

                actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            } catch (IOException e) {
                // todo, show the error message in the EDT
                error = e.getMessage();
            }
        }
    }

    private void startTurn(boolean white) {
        if (hasEngine(white)) new Thread(engineHandler).start();
    }

    private boolean hasEngine(boolean white) {
        return white ? whiteEngine != null : blackEngine != null;
    }

    private void addEngine(String path, String name) {
        try {
            engineList.add(new ChessEngine(path, this.game));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void useEngine(boolean white, int id) {
        if (white) {
            whiteEngine = engineList.get(id);
        } else {
            blackEngine = engineList.get(id);
        }
        if (engineHandler == null) engineHandler = new EngineHandler();
    }
}