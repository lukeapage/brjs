package org.bladerunnerjs.core.plugin.bundlesource.js;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.bladerunnerjs.model.AliasDefinition;
import org.bladerunnerjs.model.AssetLocation;
import org.bladerunnerjs.model.SourceFile;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.utility.FileModifiedChecker;

public class NodeJsSourceFile implements SourceFile {
	private final File assetFile;
	private List<String> requirePaths;
	private List<String> aliasNames;
	private final AssetContainer assetContainer;
	private List<SourceFile> dependentSourceFiles;
	private List<AliasDefinition> aliases;
	private FileModifiedChecker fileModifiedChecker;
	private String requirePath;
	private File srcDir;
	
	public NodeJsSourceFile(AssetContainer assetContainer, File file) {
		this.assetContainer = assetContainer;
		this.requirePath = assetContainer.file("src").toURI().relativize(file.toURI()).getPath().replaceAll("\\.js$", "");
		assetFile = file;
		srcDir = file.getParentFile();
		fileModifiedChecker = new FileModifiedChecker(assetFile);
	}
	
	@Override
	public List<SourceFile> getDependentSourceFiles() throws ModelOperationException {
		if (fileModifiedChecker.fileModifiedSinceLastCheck()) {
			recalculateDependencies();
		}
		
		return dependentSourceFiles;
	}
	
	@Override
	public List<AliasDefinition> getAliases() throws ModelOperationException {
		if (fileModifiedChecker.fileModifiedSinceLastCheck()) {
			recalculateDependencies();
		}
		
		return aliases;
	}
	
	@Override
	public Reader getReader() throws FileNotFoundException {
		return new BufferedReader( new FileReader(assetFile) );
	}
	
	@Override
	public String getRequirePath() {
		return requirePath;
	}
	
	@Override
	public List<AssetLocation> getAssetLocations() {
		return assetContainer.getAssetLocations(srcDir);
	}
	
	@Override
	public List<SourceFile> getOrderDependentSourceFiles() throws ModelOperationException {
		return new ArrayList<>();
	}
	
	@Override
	public AssetContainer getAssetContainer() {
		return assetContainer;
	}
	
	@Override
	public File getUnderlyingFile() {
		return assetFile;
	}
	
	private void recalculateDependencies() throws ModelOperationException {
		requirePaths = new ArrayList<>();
		aliasNames = new ArrayList<>();
		
		try {
			StringWriter stringWriter = new StringWriter();
			IOUtils.copy(getReader(), stringWriter);
			
			Matcher m = Pattern.compile("(require|br\\.alias|caplin\\.alias)\\([\"']([^)]+)[\"']\\)").matcher(stringWriter.toString());
			
			while(m.find()) {
				boolean isRequirePath = m.group(1).startsWith("require");
				String methodArgument = m.group(2);
				
				if(isRequirePath) {
					requirePaths.add(methodArgument);
				}
				else {
					aliasNames.add(methodArgument);
				}
			}
			
			dependentSourceFiles = new ArrayList<>();
			for(String requirePath : requirePaths) {
				dependentSourceFiles.add(assetContainer.sourceFile(requirePath));
			}
			
			aliases = new ArrayList<>();
			for(@SuppressWarnings("unused") String aliasName : aliasNames) {
				// TODO: how do I get the AliasDefinition instance?
				// aliases.add( .... )
			}
		}
		catch(IOException e) {
			throw new ModelOperationException(e);
		}
	}
}