package org.bladerunnerjs.spec.bundling.aspect;

import org.bladerunnerjs.aliasing.aliasdefinitions.AliasDefinitionsFile;
import org.bladerunnerjs.aliasing.aliases.AliasesFile;
import org.bladerunnerjs.model.App;
import org.bladerunnerjs.model.AppConf;
import org.bladerunnerjs.model.Aspect;
import org.bladerunnerjs.model.Blade;
import org.bladerunnerjs.model.Bladeset;
import org.bladerunnerjs.model.JsLib;
import org.bladerunnerjs.model.exception.UnresolvableRelativeRequirePathException;
import org.bladerunnerjs.model.exception.UnresolvableRequirePathException;
import org.bladerunnerjs.model.exception.request.BundlerProcessingException;
import org.bladerunnerjs.plugin.plugins.bundlers.namespacedjs.NamespacedJsBundlerContentPlugin;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


//TODO: why don't we get a namespace exception when we define classes outside of the namespace (e.g. 'appns' when the default namespace is 'appns')?
//TODO: we should fail-fast if somebody uses unquoted() in a logging assertion as it is only meant for exceptions where we can't easily ascertain the parameters
public class AspectBundlingOfAspectSource extends SpecTest {
	private App app;
	private AppConf appConf;
	private Aspect aspect;
	private AliasesFile aspectAliasesFile;
	private Bladeset bladeset;
	private Blade blade;
	private JsLib bootstrapLib;
	private AliasDefinitionsFile bladeAliasDefinitionsFile;
	private StringBuffer response = new StringBuffer();
	
	@Before
	public void initTestObjects() throws Exception
	{
		given(brjs).automaticallyFindsBundlers()
			.and(brjs).automaticallyFindsMinifiers()
			.and(brjs).hasBeenCreated();
		
			app = brjs.app("app1");
			appConf = app.appConf();
			aspect = app.aspect("default");
			aspectAliasesFile = aspect.aliasesFile();
			bladeset = app.bladeset("bs");
			blade = bladeset.blade("b1");
			
			bootstrapLib = app.jsLib("bootstrap");
			bladeAliasDefinitionsFile = blade.assetLocation("src").aliasDefinitionsFile();
	}
	
	@Test
	public void weBundleBootstrapIfItExists() throws Exception {
		given(exceptions).arentCaught();
		
		given(aspect).hasClass("appns.Class1")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(bootstrapLib).hasBeenCreated()
			.and(bootstrapLib).containsFileWithContents("library.manifest", "js: bootstrap.js")
			.and(bootstrapLib).containsFileWithContents("bootstrap.js", "// this is bootstrap");
		when(app).requestReceived("/default-aspect/thirdparty/bundle.js", response);
		then(response).containsText("// bootstrap");
//		then(response).containsText("// this is bootstrap"); // TODO: Ask AB to find out why the contents of the library aren't currently emitted
	}
	
	@Test
	public void weDontBundleBootstrapIfThereIsNoSourceToBundle() throws Exception {
		given(aspect).indexPageHasContent("the page")
			.and(bootstrapLib).hasBeenCreated()
			.and(bootstrapLib).containsFileWithContents("library.manifest", "js: bootstrap.js")
			.and(bootstrapLib).containsFileWithContents("bootstrap.js", "// this is bootstrap");
		when(app).requestReceived("/default-aspect/thirdparty/bundle.js", response);
		then(response).isEmpty();
	}
	
	@Test
	public void weBundleAnAspectClassIfItIsReferredToInTheIndexPage() throws Exception {
		given(aspect).hasClass("appns.Class1")
			.and(aspect).indexPageRefersTo("appns.Class1");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1");
	}
	
	@Test
	public void weBundleAClassIfItsAliasIsReferredToInTheIndexPage() throws Exception {
		given(aspect).hasClass("appns.Class1")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class1")
			.and(aspect).indexPageRefersTo("the-alias");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1");
	}
	
	@Test
	public void weAlsoBundleAClassIfTheAliasIsDefinedInABladeAliasDefinitionsXml() throws Exception {
		given(appConf).hasNamespace("appns")
			.and(aspect).hasClass("appns.Class1")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.Class1")
			.and(aspect).indexPageRefersTo("\"appns.bs.b1.the-alias\"");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1");
	}
	
	// TODO: get this class working once we add support for requiring aliases (Adam I suggests we can do the same for services and HTML templates too)
	@Ignore
	@Test
	public void weBundleAClassIfItsAliasIsReferredToFromAnotherNodeJsClass() throws Exception {
		given(aspect).hasClasses("appns.Class1", "appns.Class2")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(aspect).classRequires("appns.Class1", "the-alias");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1", "appns.Class2");
	}
	
	@Test
	public void weBundleAClassIfItsAliasIsReferredToFromAnotherNamespacedClass() throws Exception {
		given(aspect).hasPackageStyle(NamespacedJsBundlerContentPlugin.JS_STYLE)
			.and(aspect).hasClasses("appns.Class1", "appns.Class2")
			.and(aspectAliasesFile).hasAlias("the-alias", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(aspect).classRefersTo("appns.Class1", "the-alias");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1", "appns.Class2");
	}
	
	@Test
	public void weDoNotBundleAClassIfADefinedAliasIsNotReferenced() throws Exception {
		given(appConf).hasNamespace("appns")
			.and(aspect).hasClass("appns.Class1")
			.and(bladeAliasDefinitionsFile).hasAlias("appns.bs.b1.the-alias", "appns.Class1");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).doesNotContainClasses("appns.Class1");
	}
	
	@Test
	public void requirePathsCanBeRelative() throws Exception {
		given(aspect).hasClasses("appns.Class1", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.Class1")
			.and(aspect).classRequires("appns.Class1", "./Class2");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.Class1", "appns.Class2");
	}
	
	@Test
	public void relativeRequirePathsWorkInChildPackages() throws Exception {
		given(aspect).hasClasses("appns.pkg.Class1", "appns.pkg.Class2")
			.and(aspect).indexPageRefersTo("appns.pkg.Class1")
			.and(aspect).classRequires("appns.pkg.Class1", "./Class2");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.pkg.Class1", "appns.pkg.Class2");
	}
	
	@Test
	public void relativeRequirePathsCanPointToTheParentDirectory() throws Exception {
		given(aspect).hasClasses("appns.pkg.Class1", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.pkg.Class1")
			.and(aspect).classRequires("appns.pkg.Class1", "../Class2");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.pkg.Class1", "appns.Class2");
	}
	
	@Test
	public void relativeRequirePathsCanPointToAnyLevelParentDirectory() throws Exception {
		given(aspect).hasClasses("appns.pkg.pkg2.Class1", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.pkg.pkg2.Class1")
			.and(aspect).classRequires("appns.pkg.pkg2.Class1", "../../Class2");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsClasses("appns.pkg.pkg2.Class1", "appns.Class2");
	}
	
	@Test
	public void exceptionIsThrownIfRelativeRequirePathGoesAboveRoot() throws Exception {
		given(aspect).hasClasses("appns.pkg.pkg2.Class1", "appns.Class2")
			.and(aspect).indexPageRefersTo("appns.pkg.pkg2.Class1")
			.and(aspect).classRequires("appns.pkg.pkg2.Class1", "../../../../Class2");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(exceptions).verifyException(UnresolvableRelativeRequirePathException.class, "appns/pkg/pkg2", "../../../../Class2")
			.whereTopLevelExceptionIs(BundlerProcessingException.class);
	}
	
	@Test
	public void requireCallCanHaveSingleQuotes() throws Exception {
		given(aspect).containsFileWithContents("src/appns/Class1.js", "appns.Class1 = function(){}; require('appns/Class2')")
			.and(aspect).containsFileWithContents("src/appns/Class2.js", "appns.Class2 = function(){};")
			.and(aspect).indexPageRefersTo("appns/Class1");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsText("appns.Class2 = function(){};");
	}
	
	@Test
	public void requireCallCanHaveDoubleQuotes() throws Exception {
		given(aspect).containsFileWithContents("src/appns/Class1.js", "appns.Class1 = function(){}; require(\"appns/Class2\")")
		.and(aspect).containsFileWithContents("src/appns/Class2.js", "appns.Class2 = function(){};")
		.and(aspect).indexPageRefersTo("appns/Class1");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsText("appns.Class2 = function(){};");
	}
	
	@Test
	public void requireCallCanHaveSpacesBeforeQuotes() throws Exception {
		given(aspect).containsFileWithContents("src/appns/Class1.js", "appns.Class1 = function(){}; require( \"appns/Class2\")")
		.and(aspect).containsFileWithContents("src/appns/Class2.js", "appns.Class2 = function(){};")
		.and(aspect).indexPageRefersTo("appns/Class1");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsText("appns.Class2 = function(){};");
	}
	
	@Test
	public void weBundleFilesRequiredFromAnAspect() throws Exception {
		given(aspect).containsFileWithContents("src/appns/App.js", "var App = function() {};  module.exports = App;")
			.and(aspect).indexPageHasContent("var App = require('appns/App')");
		when(app).requestReceived("/default-aspect/js/dev/en_GB/combined/bundle.js", response);
		then(response).containsText("var App = function() {};  module.exports = App;");
	}
}
