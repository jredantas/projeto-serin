package br.unifor.mia.serin.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {

	public static String getContent(String fileName) throws IOException {
		
		InputStream inInsertStatement = FileUtil.class.getClassLoader().getResourceAsStream(fileName);

		BufferedReader stream = new BufferedReader(new InputStreamReader(inInsertStatement));

		StringBuilder fileContent = new StringBuilder();

		String currentLine = stream.readLine();

		while (currentLine != null) {
			fileContent.append(currentLine);
			currentLine = stream.readLine();
		}

		return fileContent.toString();
	}
}
