import com.tunnelvisionlabs.postgresql.PostgreSqlLexer;
import com.tunnelvisionlabs.postgresql.PostgreSqlLexerUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.junit.Assert;
import org.junit.Test;

public class BasicTests {

	public void TestSampleInputs() throws IOException {
		String input = loadSample("req", "UTF-8");

		PostgreSqlLexer lexer = PostgreSqlLexerUtils.createLexer(new ANTLRInputStream(input));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		tokens.fill();

		Token previousToken = null;
		for (Token token : tokens.getTokens()) {
			if (previousToken != null) {
				Assert.assertEquals(previousToken.getStopIndex() + 1, token.getStartIndex());
			}

			Assert.assertNotEquals(PostgreSqlLexer.ErrorCharacter, token.getType());
			previousToken = token;
		}

		Assert.assertEquals(Lexer.DEFAULT_MODE, lexer._mode);
		Assert.assertTrue(lexer._modeStack.isEmpty());
	}

	protected String loadSample(String fileName, String encoding) throws IOException
	{
		if ( fileName==null ) {
			return null;
		}

		int size = 1024 * 1024;
		InputStreamReader isr;
		InputStream fis =  new FileInputStream(fileName);
		if ( encoding!=null ) {
			isr = new InputStreamReader(fis, encoding);
		}
		else {
			isr = new InputStreamReader(fis);
		}
		try {
			char[] data = new char[size];
			int n = isr.read(data);
			return new String(data, 0, n);
		}
		finally {
			isr.close();
		}
	}
}
