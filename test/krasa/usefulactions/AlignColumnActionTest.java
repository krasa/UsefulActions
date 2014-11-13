package krasa.usefulactions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AlignColumnActionTest {

	String notFormattedText = "" + "|foo 1|foooooooooo 2|   foo 3|foo 4 \n"
			+ "|val 11| val 12|val 13| val 14|val 15|\n"
			+ "|foooooooooo| val 22|val 33|val34|fooooooooooooooo                       |";

	String expectedText = "| foo 1       | foooooooooo 2 | foo 3  | foo 4\n"
			+ "| val 11      | val 12        | val 13 | val 14 | val 15           |\n"
			+ "| foooooooooo | val 22        | val 33 | val34  | fooooooooooooooo |\n";

	@Test
	public void testSpace1() {
		String process = new AlignColumnAction(false).reformat(" ", "foo      bar");
		assertThat(process, is("foo bar\n"));
	}

	@Test
	public void test() {
		String process = new AlignColumnAction(false).reformat("|", "foo 1     | foo 2");
		assertThat(process, is("foo 1 | foo 2\n"));
	}

	@Test
	public void test2() {
		String process = new AlignColumnAction(false).reformat("|", "foo 1     | foo 2|");
		assertThat(process, is("foo 1 | foo 2 |\n"));
	}

	@Test
	public void test3() {
		String process = new AlignColumnAction(false).reformat("|", "|foo 1     | foo 2");
		assertThat(process, is("| foo 1 | foo 2\n"));
	}

	@Test
	public void test4() {
		String process = new AlignColumnAction(false).reformat("|", "| foo 1 | foo 2\n| foo 1 | foo 2\n");
		assertThat(process, is("| foo 1 | foo 2\n| foo 1 | foo 2\n"));
	}

	@Test
	public void test5() {
		String process = new AlignColumnAction(false).reformat("|", "| foo 1 | foo 2| foo 3\n| foo 1 | foo 2\n");
		assertThat(process, is("| foo 1 | foo 2 | foo 3\n| foo 1 | foo 2\n"));
	}

	@Test
	public void test6() {
		String process = new AlignColumnAction(false).reformat("|", "| foo 1 | foo 2\n| foo 1 | foo 2 | foo 3\n");
		assertThat(process, is("| foo 1 | foo 2\n| foo 1 | foo 2 | foo 3\n"));
	}

	@Test
	public void test7() {
		String process = new AlignColumnAction(false).reformat("|", notFormattedText);
		System.out.println(process);

		assertThat(process, is(expectedText));

	}
}
