package fr.wonder.commons.streams.compression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.IOException;

public class ZipDecompresser {
	
	private final File zipFile;
	
	public ZipDecompresser(File zipFile) {
		this.zipFile = zipFile;
	}
	
	public void decompress(File destination) throws IOException {
		try (ZipFile zipFile = new ZipFile(this.zipFile)) {
			Enumeration<?> enu = zipFile.entries();
			while (enu.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enu.nextElement();
				String name = zipEntry.getName();
				
				File file = new File(destination, name);
				if(name.endsWith("/")) {
					file.mkdirs();
					continue;
				}
				
				File parent = file.getParentFile();
				if(parent != null) {
					parent.mkdirs();
				}
				
				try (InputStream is = zipFile.getInputStream(zipEntry);
					FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(is.readAllBytes());
				}
			}
		}
	}

}
