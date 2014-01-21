package org.bladerunnerjs.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bladerunnerjs.aliasing.AliasOverride;
import org.bladerunnerjs.model.exception.ModelOperationException;
import org.bladerunnerjs.model.exception.request.BundlerFileProcessingException;
import org.bladerunnerjs.utility.EmptyTrieKeyException;
import org.bladerunnerjs.utility.FileModifiedChecker;
import org.bladerunnerjs.utility.Trie;
import org.bladerunnerjs.utility.TrieKeyAlreadyExistsException;

/**
 * A linked asset file that refers to another AssetFile using a fully qualified name such as 'my.package.myClass'
 *
 */
public class FullyQualifiedLinkedAsset implements LinkedAsset {
	private File assetFile;
	private Set<SourceModule> dependentSourceModules;
	private List<String> aliases;
	private FileModifiedChecker fileModifiedChecker;
	private AssetLocation assetLocation;
	
	public void initialize(AssetLocation assetLocation, File assetFile)
	{
		this.assetLocation = assetLocation;
		this.assetFile = assetFile;
		fileModifiedChecker = new FileModifiedChecker(assetFile);
	}
	
	@Override
	public Reader getReader() throws FileNotFoundException {
		return new BufferedReader( new FileReader(assetFile) );
	}
	
	@Override
	public List<SourceModule> getDependentSourceModules(BundlableNode bundlableNode) throws ModelOperationException {
		if(fileModifiedChecker.fileModifiedSinceLastCheck()) {
			recalculateDependencies();
		}
		
		return new ArrayList<SourceModule>( dependentSourceModules );
	}

	@Override
	public List<String> getAliasNames() throws ModelOperationException {
		if(fileModifiedChecker.fileModifiedSinceLastCheck()) {
			recalculateDependencies();
		}
		
		return aliases;
	}
	
	@Override
	public File getUnderlyingFile() {
		return assetFile;
	}
	
	@Override
	public String getAssetName() {
		return assetFile.getName();
	}
	
	@Override
	public String getAssetPath() {
		return assetFile.getPath();
	}
	
	private void recalculateDependencies() throws ModelOperationException {
		dependentSourceModules = new HashSet<>();
		aliases = new ArrayList<>();
		Trie<Object> trie = createTrie();
		
		try {
			try(Reader reader = getReader()) {
				for(Object match : trie.getMatches(reader)) {
					if(match instanceof SourceModule) {
						dependentSourceModules.add((SourceModule) match);
					}
					else if(match instanceof ClassSourceModule) {
						dependentSourceModules.add(((ClassSourceModule) match).getSourceModule());
					}
					else {
						String matchString = (String) match;
						if (matchString.length() > 0)
						{
							aliases.add((String) match);							
						}
					}
				}
			}
		}
		catch(IOException e) {
			throw new ModelOperationException(e);
		}
	}
	
	private Trie<Object> createTrie() throws ModelOperationException {
		Trie<Object> trie = new Trie<Object>();
		
		for(AssetContainer assetContainer : assetLocation.getAssetContainer().getApp().getAllAssetContainers()) {
			try {
				if(assetContainer instanceof BundlableNode) {
					BundlableNode bundlableNode = (BundlableNode) assetContainer;
					
					for(AliasOverride aliasOverride : bundlableNode.aliasesFile().aliasOverrides()) {
						if(!trie.containsKey(aliasOverride.getName())) {
							trie.add(aliasOverride.getName(), aliasOverride.getName());
						}
					}
				}
				
				for(SourceModule sourceModule : assetContainer.sourceModules()) {
					ClassSourceModule classSourceModule = new ClassSourceModule(sourceModule);
					
					if (!sourceModule.getUnderlyingFile().equals(assetFile)) {
	    				trie.add(sourceModule.getRequirePath(), sourceModule);
	    				if (sourceModule.getNamespacedName() != null)
	    				{
	    					trie.add(sourceModule.getNamespacedName(), classSourceModule);
	    				}
					}
				}
				
				for(AssetLocation assetLocation : assetContainer.assetLocations()) {
					for(String aliasName : assetLocation.aliasDefinitionsFile().aliasNames()) {
						if(!trie.containsKey("'" + aliasName + "'")) {
							trie.add("'" + aliasName + "'", aliasName);
							trie.add("\"" + aliasName + "\"", aliasName);
						}
					}
				}
			}
			catch (TrieKeyAlreadyExistsException | EmptyTrieKeyException | BundlerFileProcessingException ex) {
				throw new ModelOperationException(ex);
			}
		}
		
		return trie;
	}

	@Override
	public AssetLocation getAssetLocation()
	{
		return assetLocation;
	}
}
