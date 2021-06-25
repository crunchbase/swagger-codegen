package com.crunchbase.codegen;

import com.google.common.base.Strings;
import io.swagger.codegen.*;
import io.swagger.codegen.mustache.*;
import io.swagger.models.*;
import io.swagger.models.properties.*;

import java.util.*;
import java.io.File;

public class CirceGenerator extends DefaultCodegen implements CodegenConfig {

  // source folder where to write the files
  protected String sourceFolder = "src/main/scala";
  protected String packageName = "com.crunchbase";

  /**
   * Configures the type of generator.
   *
   * @return  the CodegenType for this generator
   * @see     io.swagger.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -l flag.
   *
   * @return the friendly name for the generator
   */
  public String getName() {
    return "circe";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   *
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a circe client library.";
  }

  public CirceGenerator() {
    super();

    // set the output folder here
    outputFolder = "generated-code/circe";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model.mustache", // the template to use
      ".scala");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "circe";

    /**
     * Model Package.  Optional, if needed, this can be used in templates
     */
    modelPackage = packageName + ".http.codegen";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords = new HashSet<String>(
      Arrays.asList(
        // Scala
        "abstract", "case", "catch", "class", "def",
        "do", "else", "extends", "false", "final",
        "finally", "for", "forSome", "if", "implicit",
        "import", "lazy", "match", "new", "null",
        "object", "override", "package", "private", "protected",
        "return", "sealed", "super", "this", "throw",
        "trait", "try", "true", "type", "val",
        "var", "while", "with", "yield",
        // Scala-interop languages keywords
        "abstract", "continue", "switch", "assert",
        "default", "synchronized", "goto",
        "break", "double", "implements", "byte",
        "public", "throws", "enum", "instanceof", "transient",
        "int", "short", "char", "interface", "static",
        "void", "finally", "long", "strictfp", "volatile", "const", "float",
        "native")
    );

    defaultIncludes = new HashSet<String>(
      Arrays.asList("double",
        "Int",
        "Long",
        "Float",
        "Double",
        "char",
        "float",
        "String",
        "boolean",
        "Boolean",
        "Double",
        "Integer",
        "Long",
        "Float",
        "List",
        "Set",
        "Map")
    ) ;

    typeMapping = new HashMap<String, String>();
    typeMapping.put("string", "String");
    typeMapping.put("boolean", "Boolean");
    typeMapping.put("integer", "Int");
    typeMapping.put("float", "Float");
    typeMapping.put("long", "Long");
    typeMapping.put("double", "Double");
    typeMapping.put("number", "BigDecimal");
    typeMapping.put("date-time", "OffsetDateTime");
    typeMapping.put("date", "LocalDate");
    typeMapping.put("file", "File");
    typeMapping.put("array", "Seq");
    typeMapping.put("list", "List");
    typeMapping.put("map", "Map");
    typeMapping.put("object", "Json");
    typeMapping.put("binary", "Array[Byte]");
    typeMapping.put("Date", "LocalDate");
    typeMapping.put("DateTime", "OffsetDateTime");

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("titlecase", new TitlecaseLambda());
    additionalProperties.put("lowercase", new LowercaseLambda());
    additionalProperties.put("modelPackage", modelPackage());

    if (additionalProperties.containsKey(CodegenConstants.PACKAGE_NAME)) {
      setPackageName((String) additionalProperties.get(CodegenConstants.PACKAGE_NAME));
    } else {
      additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
    }

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    supportingFiles.add(new SupportingFile("gitignore.mustache", sourceFolder, ".gitignore"));
    supportingFiles.add(new SupportingFile("licenseInfo.mustache", sourceFolder, "licenseInfo"));

    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
            Arrays.asList(
                    "String",
                    "boolean",
                    "Boolean",
                    "Double",
                    "Int",
                    "Long",
                    "Float",
                    "Object",
                    "List",
                    "Seq",
                    "Map")
    );
    instantiationTypes.put("array", "ListBuffer");
    instantiationTypes.put("map", "Map");

    importMapping = new HashMap<String, String>();
    importMapping.put("BigDecimal", "java.math.BigDecimal");
    importMapping.put("UUID", "java.util.UUID");
    importMapping.put("File", "java.io.File");
    importMapping.put("Date", "java.util.Date");
    importMapping.put("Timestamp", "java.sql.Timestamp");
    importMapping.put("Map", "scala.collection.immutable.Map");
    importMapping.put("HashMap", "scala.collection.immutable.HashMap");
    importMapping.put("Seq", "scala.collection.immutable.Seq");
    importMapping.put("ArrayBuffer", "scala.collection.mutable.ArrayBuffer");
    importMapping.put("OffsetDateTime", "java.time.OffsetDateTime");
    importMapping.put("LocalDateTime", "java.time.LocalDateTime");
    importMapping.put("LocalDate", "java.time.LocalDate");
    importMapping.put("LocalTime", "java.time.LocalTime");
    importMapping.remove("Seq");
    importMapping.remove("List");
    importMapping.remove("Set");
    importMapping.remove("Map");

    cliOptions.clear();
    cliOptions.add(new CliOption(CodegenConstants.PACKAGE_NAME, "Models package name (e.g. io.swagger).")
      .defaultValue(this.packageName));
    cliOptions.add(new CliOption(CodegenConstants.MODEL_PACKAGE, CodegenConstants.MODEL_PACKAGE_DESC));
  }

  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reserved words
   *
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    if (this.reservedWordsMappings().containsKey(name)) {
      return this.reservedWordsMappings().get(name);
    }
    return "`" + name + "`";
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + File.separator + sourceFolder + File.separator + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Convert Swagger Model object to Codegen Model object
   *
   * @param name           the name of the model
   * @param model          Swagger Model object
   * @param allDefinitions a map of all Swagger models from the spec
   * @return Codegen Model object
   */
  @Override
  public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
    CodegenModel codegenModel = super.fromModel(name, model, allDefinitions);
    return codegenModel;
  }

  @Override
  public void processOpts() {
    super.processOpts();

    this.setModelPackage(sanitizePackageName(modelPackage));
    if (additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
      this.additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
    }
  }

  @Override
  public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
    Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
    List<CodegenOperation> operationList = (List<CodegenOperation>) operations.get("operation");
    for (CodegenOperation op : operationList) {
      op.httpMethod = op.httpMethod.toLowerCase();

      String path = new String(op.path);
      // remove first /
      if (path.startsWith("/")) {
        path = path.substring(1);
      }
      // remove last /
      if (path.endsWith("/")) {
        path = path.substring(0, path.length()-1);
      }

      String[] items = path.split("/", -1);
      String scalaPath = "";
      int pathParamIndex = 0;

      for (int i = 0; i < items.length; ++i) {
        if (items[i].matches("^\\{(.*)\\}$")) { // wrap in {}
          // find the datatype of the parameter
          final CodegenParameter cp = op.pathParams.get(pathParamIndex);

          // TODO: Handle non-primitives…
          scalaPath = scalaPath + cp.dataType.toLowerCase();

          pathParamIndex++;
        } else {
          scalaPath = scalaPath + "\"" + items[i] + "\"";
        }

        if (i != items.length -1) {
          scalaPath = scalaPath + " :: ";
        }
      }

      for (CodegenParameter p : op.allParams) {
        // TODO: This hacky, should be converted to mappings if possible to keep it clean.
        // This could also be done using template imports
        if(Boolean.TRUE.equals(p.isPrimitiveType)) {
          p.vendorExtensions.put("x-codegen-normalized-path-type", p.dataType.toLowerCase());
          p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType);
        } else if(Boolean.TRUE.equals(p.isBodyParam)) {
          p.vendorExtensions.put("x-codegen-normalized-path-type", "jsonBody["+ p.dataType + "]");
          p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType);
        } else if(Boolean.TRUE.equals(p.isContainer) || Boolean.TRUE.equals(p.isListContainer)) {
          p.vendorExtensions.put("x-codegen-normalized-path-type", "params(\""+ p.paramName + "\")");
          p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType.replaceAll("^[^\\[]+", "Seq"));
        } else if(Boolean.TRUE.equals(p.isFile)) {
          p.vendorExtensions.put("x-codegen-normalized-path-type", "fileUpload(\""+ p.paramName + "\")");
          p.vendorExtensions.put("x-codegen-normalized-input-type", "FileUpload");
        } else {
          p.vendorExtensions.put("x-codegen-normalized-path-type", p.dataType);
          p.vendorExtensions.put("x-codegen-normalized-input-type", p.dataType);
        }
      }

      op.vendorExtensions.put("x-codegen-path", scalaPath);
    }
    return objs;
  }

  /**
   * Optional - type declaration.  This is a String which is used by the templates to instantiate your
   * types.  There is typically special handling for different property types
   *
   * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
   */
  @Override
  public String getTypeDeclaration(Property p) {
    if(p instanceof ArrayProperty) {
      ArrayProperty ap = (ArrayProperty) p;
      Property inner = ap.getItems();
      return getSwaggerType(p) + "[" + getTypeDeclaration(inner) + "]";
    }
    else if (p instanceof MapProperty) {
      MapProperty mp = (MapProperty) p;
      Property inner = mp.getAdditionalProperties();
      return getSwaggerType(p) + "[String, " + getTypeDeclaration(inner) + "]";
    }
    return super.getTypeDeclaration(p);
  }

  /**
   * Optional - swagger type conversion.  This is used to map swagger types in a `Property` into
   * either language specific types via `typeMapping` or into complex models if there is not a mapping.
   *
   * @return a string value of the type or complex model for this property
   * @see io.swagger.models.properties.Property
   */
  @Override
  public String getSwaggerType(Property p) {
    String swaggerType = super.getSwaggerType(p);
    String type = null;
    if (typeMapping.containsKey(swaggerType)) {
      type = typeMapping.get(swaggerType);
      if(languageSpecificPrimitives.contains(type))
        return toModelName(type);
    } else {
      type = swaggerType;
    }
    return toModelName(type);
  }

  @Override
  public String escapeQuotationMark(String input) {
    // remove " to avoid code injection
    return input.replace("\"", "");
  }

  @Override
  public String escapeUnsafeCharacters(String input) {
    return input.replace("*/", "*_/").replace("/*", "/_*");
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  private String sanitizePackageName(String packageName) {
    packageName = packageName.trim();
    packageName = packageName.replaceAll("[^a-zA-Z0-9_\\.]", "_");
    if (Strings.isNullOrEmpty(packageName)) {
      return "invalidPackageName";
    }
    return packageName;
  }
}