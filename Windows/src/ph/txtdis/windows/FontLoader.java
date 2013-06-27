package ph.txtdis.windows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.swt.widgets.Display;

public class FontLoader {
	public FontLoader(Display display) {
		String path = System.getProperty("java.io.tmpdir");
		String slash = System.getProperty("file.separator");
		String font = "UbuntuMono.ttf";
		File file = new File(path + slash + font);
		InputStream is = getClass().getResourceAsStream("fonts" + slash + font);
		try {
			OutputStream os = new FileOutputStream(file);
			int read=0;
			byte[] bytes = new byte[1024];
			while((read = is.read(bytes))!= -1){
				os.write(bytes, 0, read);
			}
			is.close();
			os.flush();
			os.close();	
		} catch (IOException e) {
			//e.printStackTrace();
		}
		file.deleteOnExit();
	}
}
