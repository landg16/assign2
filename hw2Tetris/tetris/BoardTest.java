package tetris;

import static org.junit.Assert.*;

import org.junit.*;

public class BoardTest {
	Board b;
	Piece pyr1, pyr2, pyr3, pyr4, s, sRotated;

	// This shows how to build things in setUp() to re-use
	// across tests.

	// In this case, setUp() makes shapes,
	// and also a 3X6 board, with pyr placed at the bottom,
	// ready to be used by tests.
	@Before
	public void setUp() throws Exception {
		b = new Board(3, 6);

		pyr1 = new Piece(Piece.PYRAMID_STR);
		pyr2 = pyr1.computeNextRotation();
		pyr3 = pyr2.computeNextRotation();
		pyr4 = pyr3.computeNextRotation();

		s = new Piece(Piece.S1_STR);
		sRotated = s.computeNextRotation();

		b.place(pyr1, 0, 0);
	}

	// Check the basic width/height/max after the one placement
	@Test
	public void testSample1() {
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(0, b.getRowWidth(2));
		assertEquals(3, b.getWidth());
		assertEquals(6, b.getHeight());
	}

	// Place sRotated into the board, then check some measures
	@Test
	public void testSample2() {
		b.commit();
		int result = b.place(sRotated, 1, 1);
		assertEquals(Board.PLACE_OK, result);
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(4, b.getColumnHeight(1));
		assertEquals(3, b.getColumnHeight(2));
		assertEquals(4, b.getMaxHeight());
	}

	// Make  more tests, by putting together longer series of
	// place, clearRows, undo, place ... checking a few col/row/max
	// numbers that the board looks right after the operations.

	@Test
	public void testSimpleSizes() {
		assertEquals(2, b.getMaxHeight());
		assertEquals(0, b.getRowWidth(2));
		assertEquals(3, b.getRowWidth(0));
		assertEquals(1, b.getRowWidth(1));
		assertEquals(1, b.getColumnHeight(0));
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(1, b.getColumnHeight(2));
	}

	@Test
	public void testHeight() {
		Piece pc1 = new Piece(Piece.S1_STR);
		Piece pc2 = new Piece(Piece.STICK_STR);
		Piece pc3 = new Piece(Piece.S2_STR);

		assertEquals(2, b.dropHeight(pc1, 0));
		assertEquals(1, b.dropHeight(pc2, 2));
		assertEquals(2, b.dropHeight(pc3, 0));

		Piece pc1_next = pc1.computeNextRotation();
		assertEquals(1, b.dropHeight(pc1_next, 1));
		b.commit();
		b.place(pyr3, 1, 1);
		assertEquals(2, b.getColumnHeight(1));
		assertEquals(2, b.getMaxHeight());
		assertEquals(1, b.getRowWidth(1));
		assertEquals(2, b.dropHeight(pc1, 0));
	}

	Board b1;
	@Before
	public void setUp2() throws Exception {
		b1 = new Board(8, 12);
		Piece pc1 = new Piece(Piece.STICK_STR).computeNextRotation();
		Piece pc2 = new Piece(Piece.S2_STR);
		b1.place(pc1, 0, 0);
		b1.commit();
		b1.place(pc2, 0, 1);
		b1.commit();
	}

	@Test
	public void testPlacing() {
		Piece pc1 = new Piece(Piece.STICK_STR).computeNextRotation();
		assertEquals(Board.PLACE_ROW_FILLED, b1.place(pc1,4,0));
		b1.commit();
		assertEquals(Board.PLACE_OK, b1.place(pyr1, 3, 1));
		b1.commit();
		assertEquals(Board.PLACE_BAD, b1.place(pc1, 0,0));
		b1.commit();
		assertEquals(Board.PLACE_OUT_BOUNDS, b1.place(pc1, 8, 0));
		b1.commit();
		assertEquals(Board.PLACE_OUT_BOUNDS, b1.place(pc1, 0, 12));
		b1.commit();

		String toCompare = "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|        |\n" +
						   "|++  +   |\n" +
						   "| +++++  |\n" +
						   "|++++++++|\n" +
						   "----------";
		assertEquals(toCompare, b1.toString());
	}

	@Test
	public void testClear() {
		b1.commit();
		b1.clearRows();
		assertEquals(3, b1.getColumnHeight(0));
		assertEquals(4, b1.getRowWidth(0));
		assertEquals(3, b1.dropHeight(pyr4, 0));

		assertTrue(b1.getGrid(0,0));
		assertTrue(b1.getGrid(3,0));
		assertTrue(b1.getGrid(2,1));
	}

	Board b2;
	@Before
	public void setUp3(){
		b2 = new Board(6, 8);

		Piece stick = new Piece(Piece.STICK_STR).computeNextRotation();
		Piece box = new Piece(Piece.SQUARE_STR);

		b2.place(stick, 0,0);
		b2.commit();
		b2.place(box, 4,0);
		b2.commit();
	}

	@Test
	public void testTetrisBoard() {
		Piece stick = new Piece(Piece.STICK_STR).computeNextRotation();
		assertEquals(6, b2.getWidth());
		assertEquals(8, b2.getHeight());
		b2.clearRows();
		b2.commit();
		b2.place(stick,0,0);
		b2.commit();
		b2.clearRows();
		assertEquals(0, b2.getMaxHeight());
		assertEquals(0, b2.getRowWidth(0));
		assertEquals(0, b2.getColumnHeight(0));

		b2.commit();
		b2.place(stick, 0, 0);
		b2.commit();
		b2.undo();
		assertEquals(1, b2.getMaxHeight());
		assertEquals(4, b2.getRowWidth(0));
		assertEquals(1, b2.getColumnHeight(0));
	}
}
