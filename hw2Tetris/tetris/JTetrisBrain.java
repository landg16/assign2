package tetris;

import java.awt.Dimension;
import java.util.Random;

import javax.swing.*;

public class JTetrisBrain extends JTetris {
	
	private Brain brn;
	private Brain.Move mv;
	private JCheckBox check;
	private JLabel affirmative;
	private JSlider slider;
	private int curr;
	
	JTetrisBrain(int pixels) {
		super(pixels);
		brn = new DefaultBrain();
		curr = 0;
		mv = null;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {
			//DO NOTHING
		}

		JTetrisBrain tetris = new JTetrisBrain(16);
		JFrame frame = JTetris.createFrame(tetris);
		frame.setVisible(true);
	}
	
	@Override
	public JComponent createControlPanel(){
		JComponent panel = super.createControlPanel();
		
		//CheckBox
		panel.add(new JLabel("Brain:"));
		check = new JCheckBox("Brain active");
		panel.add(check);
		
		//Labels
		JLabel adversary = new JLabel("Adversary:");
		affirmative = new JLabel("Okey");
		panel.add(adversary);
		
		//Slider
		slider = new JSlider(0, 100, 0);
		Dimension dm = new Dimension(100,15);
		slider.setPreferredSize(dm);
		panel.add(slider);
		
		//Add Label
		panel.add(affirmative);
		
		return panel;
	}
	
	@Override
	public Piece pickNextPiece() {
		Random rand = new Random();
		int randNum = rand.nextInt(99) + 1;
		if(randNum<slider.getValue()){
			affirmative.setText("*Okey*");
			Piece pc[] = Piece.getPieces();
			double bestScore = Integer.MIN_VALUE;
			int index = 0;
			for(int i = 0; i<pc.length; i++){
				Brain.Move move = brn.bestMove(super.board, pc[i], super.board.getHeight(), null);
				bestScore = (move.score > bestScore) ? move.score : bestScore;
				index = (move.score > bestScore) ? i : index;
			}
			return pc[index];
		}
		affirmative.setText("Okey");
		return super.pickNextPiece();
	}
	
	@Override
	public void tick(int verb) {
		if(check.isSelected() && verb == DOWN){
			if(curr != super.count) {
				super.board.undo();
				count = super.count;
				mv = brn.bestMove(super.board, super.currentPiece, super.board.getHeight(), mv);
			}
			if(mv != null){
				if(!currentPiece.equals(mv.piece)) super.tick(ROTATE);
				if(mv.x > currentX) super.tick(RIGHT);
				else if(mv.x < currentX) super.tick(LEFT);
			}
		}
		super.tick(verb);
	}
	
}
