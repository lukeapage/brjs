package org.bladerunnerjs.testing.specutility.engine;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bladerunnerjs.model.AssetContainer;
import org.bladerunnerjs.model.JsLib;
import org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs.NamespacedJsContentPlugin;
import org.bladerunnerjs.plugin.plugins.bundlers.nodejs.NodeJsContentPlugin;
import org.bladerunnerjs.utility.JsStyleUtility;


public abstract class AssetContainerBuilder<N extends AssetContainer> extends NodeBuilder<N>
{
	
	private AssetContainer node;
	
	public AssetContainerBuilder(SpecTest specTest, N node)
	{
		super(specTest, node);
		this.node = node;
	}
	
	public BuilderChainer hasPackageStyle(String packagePath, String jsStyle) {
		JsStyleUtility.setJsStyle(node.file(packagePath), jsStyle);
		return builderChainer;
	}
	
	public BuilderChainer hasNamespacedJsPackageStyle(String packagePath) {
		return hasPackageStyle(packagePath, NamespacedJsContentPlugin.JS_STYLE);
	}
	
	public BuilderChainer hasNamespacedJsPackageStyle() {
		return hasNamespacedJsPackageStyle("");
	}
	
	public BuilderChainer hasNodeJsPackageStyle(String packagePath) {
		return hasPackageStyle(packagePath, NodeJsContentPlugin.JS_STYLE);
	}
	
	public BuilderChainer hasNodeJsPackageStyle() {
		return hasNodeJsPackageStyle("");
	}
	
	public BuilderChainer resourceFileContains(String resourceFileName, String contents) throws Exception 
	{
		FileUtils.write(node.assetLocation("resources").file(resourceFileName), contents, "UTF-8");
		
		return builderChainer;
	}
	
	public BuilderChainer hasClass(String className) throws Exception
	{
		FileUtils.write(getSourceFile(className), getClassBody(className), "UTF-8");
		return builderChainer;
	}
	
	public BuilderChainer hasClasses(String... classNames) throws Exception
	{
		for(String className : classNames) {
			hasClass(className);
		}
		return builderChainer;
	}
	
	public BuilderChainer hasTestClass(String className) throws Exception
	{
		FileUtils.write(getTestSourceFile(className), getClassBody(className), "UTF-8");
		return builderChainer;
	}
	
	public BuilderChainer hasTestClasses(String... classNames) throws Exception
	{
		for(String className : classNames) {
			hasTestClass(className);
		}
		return builderChainer;
	}

	
	
	public BuilderChainer classRefersTo(String sourceClass, String... referencedClasses) throws Exception
	{
		File sourceFile = getSourceFile(sourceClass);
		return classRefersTo(sourceClass, sourceFile, referencedClasses);
	}
	
	public BuilderChainer testClassRefersTo(String sourceClass, String... referencedClasses) throws Exception
	{
		File sourceFile = getTestSourceFile(sourceClass);
		return classRefersTo(sourceClass, sourceFile, referencedClasses);
	}
	
	public BuilderChainer classDependsOn(String dependentClass, String referencedClass) throws Exception {
		File dependentSourceFile = getSourceFile(dependentClass);
		return classDependsOn(dependentClass, referencedClass, dependentSourceFile);
	}
	
	public BuilderChainer testClassDependsOn(String dependentClass, String referencedClass) throws Exception {
		File dependentSourceFile = getTestSourceFile(dependentClass);
		return classDependsOn(dependentClass, referencedClass, dependentSourceFile);
	}
	
	public BuilderChainer classRequires(String sourceClass, String dependencyClass) throws Exception {
		File sourceFile = getSourceFile(sourceClass);
		return classRequires(sourceClass, dependencyClass, sourceFile);
	}
	
	public BuilderChainer testClassRequires(String sourceClass, String dependencyClass) throws Exception {
		File sourceFile = getTestSourceFile(sourceClass);
		return classRequires(sourceClass, dependencyClass, sourceFile);
	}
	
	public BuilderChainer classFileHasContent(String sourceClass, String content) throws Exception
	{
		File sourceFile = getSourceFile(sourceClass);
		FileUtils.write(sourceFile, content, "UTF-8");
		
		return builderChainer;
	}
	
	public BuilderChainer classRefersToThirdpartyLib(String sourceClass, JsLib thirdpartyLib) throws Exception
	{
		File sourceFile = getSourceFile(sourceClass);
		String jsStyle = JsStyleUtility.getJsStyle(sourceFile.getParentFile());
		
		if(!jsStyle.equals(NamespacedJsContentPlugin.JS_STYLE)) {
			throw new RuntimeException("classRefersToThirdpartyLib() can only be used if packageOfStyle() has been set to '" + NamespacedJsContentPlugin.JS_STYLE + "'");
		}
		
		FileUtils.write(sourceFile, "br.Core.thirdparty('"+thirdpartyLib.getName()+"');", "UTF-8", true);
		
		return builderChainer;
	}
	
	public BuilderChainer classRequiresThirdpartyLib(String sourceClass, JsLib thirdpartyLib) throws Exception
	{
		File sourceFile = getSourceFile(sourceClass);
		String jsStyle = JsStyleUtility.getJsStyle(sourceFile.getParentFile());
		
		if(!jsStyle.equals(NodeJsContentPlugin.JS_STYLE)) {
			throw new RuntimeException("classRequiresThirdpartyLib() can only be used if packageOfStyle() has not been used, or has been set to 'node.js' for dir '"+sourceFile.getParentFile().getPath()+"'");
		}
		
		FileUtils.write(sourceFile, "require('"+thirdpartyLib.getName()+"');", "UTF-8", true);
		
		return builderChainer;
	}
	
	public BuilderChainer hasBeenPopulated() throws Exception
	{
		node.populate();
		return builderChainer;
	}
	
	
	
	
	
	
	protected File getSourceFile(String sourceClass) {
		return node.assetLocation("src").file(sourceClass.replaceAll("\\.", "/") + ".js");
	}
	
	protected File getTestSourceFile(String sourceClass)
	{
		return node.assetLocation("src-test").file(sourceClass.replaceAll("\\.", "/") + ".js");		
	}
	
	
	private BuilderChainer classRefersTo(String sourceClass, File sourceFile, String... referencedClasses) throws Exception
	{
		String jsStyle = JsStyleUtility.getJsStyle(sourceFile.getParentFile());
		
		if(!jsStyle.equals(NamespacedJsContentPlugin.JS_STYLE)) {
			throw new RuntimeException("classRefersTo() can only be used if packageOfStyle() has been set to '" + NamespacedJsContentPlugin.JS_STYLE + "'");
		}
		
		String classReferencesContent = "";
		for(String referencedClass : referencedClasses)
		{
			classReferencesContent += getClassBody(sourceClass) + "var obj = new " + referencedClass + "();\n";
		}
		
		FileUtils.write(sourceFile, classReferencesContent, "UTF-8");
		
		return builderChainer;
	}
	
	private BuilderChainer classDependsOn(String dependentClass, String referencedClass, File dependentSourceFile) throws Exception
	{
		String jsStyle = JsStyleUtility.getJsStyle(dependentSourceFile.getParentFile());
		
		if(!jsStyle.equals(NamespacedJsContentPlugin.JS_STYLE)) {
			throw new RuntimeException("classDependsOn() can only be used if packageOfStyle() has been set to '" + NamespacedJsContentPlugin.JS_STYLE + "'");
		}
		
		FileUtils.write(dependentSourceFile, getCaplinJsClassBody(dependentClass, referencedClass), "UTF-8");
		
		return builderChainer;
	}
	
	private BuilderChainer classRequires(String sourceClass, String dependencyClass, File sourceFile) throws Exception
	{
		String jsStyle = JsStyleUtility.getJsStyle(sourceFile.getParentFile());
		
		if(!jsStyle.equals(NodeJsContentPlugin.JS_STYLE)) {
			throw new RuntimeException("classRequires() can only be used if packageOfStyle() has not been used, or has been set to 'node.js' for dir '"+sourceFile.getParentFile().getPath()+"'");
		}
		
		FileUtils.write(sourceFile, getNodeJsClassBody(sourceClass, dependencyClass), "UTF-8");
		
		return builderChainer;
	}
	
	private String getClassBody(String className) {
		File sourceFile = getSourceFile(className);
		String jsStyle = JsStyleUtility.getJsStyle(sourceFile.getParentFile());
		String classBody;
		
		if(jsStyle.equals(NodeJsContentPlugin.JS_STYLE)) {
			classBody = className + " = function() {\n};\nmodule.exports = " + className + ";\n";
		}
		else if(jsStyle.equals(NamespacedJsContentPlugin.JS_STYLE)) {
			classBody = className + " = function() {\n};\n";
		}
		else {
			throw new RuntimeException("'" + jsStyle + "' is an unsupported js style");
		}
		
		return classBody;
	}
	
	private String getNodeJsClassBody(String sourceClass, String destClass) {
		String classRef = sourceClass.substring(sourceClass.lastIndexOf('.') + 1);
		String destClassRequirePath = destClass.replaceAll("([\\w])\\.", "$1/").replaceAll("\\.js$", "");
		return "var " + classRef + " = require('" + destClassRequirePath + "');\n" + getClassBody(sourceClass);
	}
	
	private String getCaplinJsClassBody(String sourceClass, String destClass) {
		return getClassBody(sourceClass) + "br.Core.extend(" + sourceClass + ", " + destClass + ");\n";
	}
	
}
