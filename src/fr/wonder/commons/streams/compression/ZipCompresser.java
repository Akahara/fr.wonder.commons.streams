package fr.wonder.commons.streams.compression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.wonder.commons.files.FilesUtils;

public class ZipCompresser extends SimpleFileVisitor<Path> {
	
	private ZipOutputStream stream;
	private final Path sourceDir;
	
	public ZipCompresser(File sourceDir) {
		this.sourceDir = sourceDir.toPath();
	}
	
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
		Path targetFile = sourceDir.relativize(file);
		stream.putNextEntry(new ZipEntry(targetFile.toString()));
		byte[] bytes = FilesUtils.readBytes(file.toFile());
		stream.write(bytes, 0, bytes.length);
		stream.closeEntry();
		return FileVisitResult.CONTINUE;
	}
	
	public void compress(File target) throws IOException {
		stream = new ZipOutputStream(new FileOutputStream(target));
		Files.walkFileTree(sourceDir, this);
		stream.close();
	}
	
}
