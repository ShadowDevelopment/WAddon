package me.wiedzmin137.waddon.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigCopy {
	private String fileName;
	private File dataFolder;
	private File file;
	private YamlConfiguration yamlConf;
	
	public ConfigCopy(JavaPlugin plugin, String fileName) throws Exception {
		this.fileName = fileName;
		this.dataFolder = plugin.getDataFolder();
		this.file = new File(dataFolder, fileName);
		
		if (!dataFolder.exists()) {
			dataFolder.mkdir();
		}
	}
	
	public void checkFile(File out) throws Exception {
		if (!out.exists()) {
			InputStream fis = ConfigCopy.class.getResourceAsStream("/resources/" + out.getName());
			FileOutputStream fos = new FileOutputStream(out);
			try {
				byte[] buf = new byte[1024];
				int i = 0;
				while ((i = fis.read(buf)) != -1) {
					fos.write(buf, 0, i);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (fis != null) {
					fis.close();
				}
				if (fos != null) {
					fos.close();
				}
			}
		}
		yamlConf = YamlConfiguration.loadConfiguration(getFile());
	}
	
	public YamlConfiguration getYAML() { return yamlConf; }
	public String getName() { return fileName; }
	public File getFile() { return file; }
}
