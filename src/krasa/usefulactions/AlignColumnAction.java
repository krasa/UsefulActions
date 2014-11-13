package krasa.usefulactions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;

public class AlignColumnAction extends EditorAction {
	private String lastSeparator = ",";

	protected AlignColumnAction() {
		this(true);
	}

	protected AlignColumnAction(boolean setupHandler) {
		super(null);
		if (setupHandler) {
			this.setupHandler(new EditorWriteActionHandler(false) {

				public void executeWriteAction(Editor editor, DataContext dataContext) {
					String separator = chooseSeparator();
					if (separator == null)
						return;

					List<CaretState> caretsAndSelections = editor.getCaretModel().getCaretsAndSelections();

					if (caretsAndSelections.size() > 1) {
						processMultiCaret(editor, separator, caretsAndSelections);
					} else if (caretsAndSelections.size() == 1) {
						processSingleSelection(editor, separator, caretsAndSelections);
					}
				}

				private String chooseSeparator() {
					String separator = Messages.showInputDialog("Separator", "Separator", Messages.getQuestionIcon(),
							lastSeparator, null);
					if (separator != null) {
						if (separator.equals("")) {
							separator = " ";
						}
						lastSeparator = separator;
					} else {
						return null;
					}
					return separator;
				}

				private void processSingleSelection(Editor editor, String separator,
						List<CaretState> caretsAndSelections) {
					CaretState caretsAndSelection = caretsAndSelections.get(0);
					LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
					LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
					String text = editor.getDocument().getText(
							new TextRange(editor.logicalPositionToOffset(selectionStart),
									editor.logicalPositionToOffset(selectionEnd)));

					String charSequence = reformat(separator, text);
					editor.getDocument().replaceString(editor.logicalPositionToOffset(selectionStart),
							editor.logicalPositionToOffset(selectionEnd), charSequence);
				}

				private void processMultiCaret(Editor editor, String separator, List<CaretState> caretsAndSelections) {
					List<Line> lines = new ArrayList<Line>();
					for (CaretState caretsAndSelection : caretsAndSelections) {
						LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
						LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
						String text = editor.getDocument().getText(
								new TextRange(editor.logicalPositionToOffset(selectionStart),
										editor.logicalPositionToOffset(selectionEnd)));
						lines.add(new Line(separator, text));
					}

					process(lines);
					for (int i = lines.size() - 1; i >= 0; i--) {
						Line line = lines.get(i);
						CaretState caretsAndSelection = caretsAndSelections.get(i);
						LogicalPosition selectionStart = caretsAndSelection.getSelectionStart();
						LogicalPosition selectionEnd = caretsAndSelection.getSelectionEnd();
						editor.getDocument().replaceString(editor.logicalPositionToOffset(selectionStart),
								editor.logicalPositionToOffset(selectionEnd), line.sb.toString().trim());
					}
				}
			});
		}

	}

	protected String reformat(String separator, String text) {
		List<Line> lines = toLines(separator, text);
		process(lines);
		return toString(lines);
	}

	private String toString(List<Line> lines) {
		StringBuilder sb = new StringBuilder();
		for (Line s : lines) {
			sb.append(s.sb.toString().trim()).append("\n");
		}
		return sb.toString();
	}

	private List<Line> toLines(String separator, String text) {
		List<Line> lines = new ArrayList<Line>();
		String[] split = text.split("\n");
		for (String s : split) {
			lines.add(new Line(separator, s + "\n"));
		}
		return lines;
	}

	private void process(List<Line> lines) {
		boolean process = true;
		while (process) {
			process = false;
			int maxLength = 0;
			for (Line line : lines) {
				line.appendText();
				maxLength = Math.max(maxLength, line.getLength());
			}
			for (Line line : lines) {
				line.appendSpace(maxLength);
			}
			for (Line line : lines) {
				line.appendSeparator();
			}

			for (Line line : lines) {
				line.next();
			}

			for (Line line : lines) {
				process = process || line.hasNext();
			}
		}
	}

	class Line {

		private StringBuilder sb = new StringBuilder();
		private final String[] split;
		private int index = 0;
		private boolean empty = false;
		private String separator;

		public Line(String separator, String textPart) {
			this.separator = separator;
			if (separator.equals(" ")) {
				split = StringUtils.splitByWholeSeparator(textPart, separator);
			} else {
				split = StringUtils.splitByWholeSeparatorPreserveAllTokens(textPart, separator);
			}
		}

		public void appendText() {
			if (!empty) {
				sb.append(split[index].trim());
			}
		}

		public void appendSpace(int maxLength) {
			if (!empty) {
				int appendSpaces = Math.max(0, maxLength - sb.length());
				for (int j = 0; j < appendSpaces; j++) {
					sb.append(" ");
				}
			}
		}

		public void appendSeparator() {
			if (!empty) {
				if (!split[index].endsWith("\n")) {
					if (!separator.equals(" ")) {
						sb.append(" ");
					}
					sb.append(separator);
					if (!separator.equals(" ")) {
						sb.append(" ");
					}
				}
			}
		}

		public int getLength() {
			return sb.length();
		}

		public boolean hasNext() {
			return !empty;
		}

		public void next() {
			index++;
			empty = !(index < split.length);
		}
	}

}
