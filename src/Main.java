import chess.ui.ChessUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(ChessUI::new);

    }
}