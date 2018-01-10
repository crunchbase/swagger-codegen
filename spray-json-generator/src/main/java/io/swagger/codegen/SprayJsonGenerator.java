package io.swagger.codegen;

import io.swagger.codegen.languages.ScalaLagomServerCodegen;
import io.swagger.models.Model;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class SprayJsonGenerator extends ScalaLagomServerCodegen implements CodegenConfig {
    public SprayJsonGenerator() {
        super();
        modelPackage = "com.crunchbase.metadata.dto.generated";
        modelPropertyNaming = "original";
        embeddedTemplateDir = templateDir = "spray-json";

        // super adds a bunch of unnecessary supporting files. painstakingly remove the extras
        List<String> supportedTemplateNames = new ArrayList<>();
        supportedTemplateNames.add("gitignore.mustache");
        supportedTemplateNames.add("licenseInfo.mustache");
        supportedTemplateNames.add("model.mustache");

        List<SupportingFile> hitList = new ArrayList<>();
        for (SupportingFile file : supportingFiles) {
            if (!supportedTemplateNames.contains(file.templateFile)) {
                hitList.add(file);
            }
        }

        for (SupportingFile target : hitList) {
            supportingFiles.remove(target);
        }
    }

    @Override
    public String getName() {
        return "spray-json";
    }

    @Override
    public String getHelp() {
        return "Generates spray-json case classes and formatter factories";
    }

    // remove all api paths from swagger - we only do model generation
    @Override
    public void preprocessSwagger(Swagger swagger) {
        super.preprocessSwagger(swagger);
        swagger.setPaths(new HashMap<String, Path>(0));
    }

    // and they all go in the models folder
    @Override
    public String modelFileFolder() {
        return outputFolder;
    }

    @Override
    public CodegenModel fromModel(String name, Model model, Map<String, Model> allDefinitions) {
        CodegenModel cm = super.fromModel(name, model, allDefinitions);
        return new ExtendedCodegenModel(cm);
    }

    @Override
    public String escapeReservedWord(String name) {
        if (this.reservedWordsMappings().containsKey(name)) {
            return this.reservedWordsMappings().get(name);
        }
        return "`" + name + "`";
    }

    @Override
    public String toEnumName(CodegenProperty property) {
        return StringUtils.capitalize(property.baseName) + "Enum";
    }

    class ExtendedCodegenModel extends CodegenModel {
        public boolean hasFormatRequiredVars;
        public List<CodegenProperty> formatRequiredVars;

        public ExtendedCodegenModel(CodegenModel cm) {
            super();

            this.parent = cm.parent;
            this.parentSchema = cm.parentSchema;
            this.parentModel = cm.parentModel;
            this.interfaceModels = cm.interfaceModels;
            this.children = cm.children;
            this.name = cm.name;
            this.classname = cm.classname;
            this.title = cm.title;
            this.description = cm.description;
            this.classVarName = cm.classVarName;
            this.modelJson = cm.modelJson;
            this.dataType = cm.dataType;
            this.xmlPrefix = cm.xmlPrefix;
            this.xmlNamespace = cm.xmlNamespace;
            this.xmlName = cm.xmlName;
            this.classFilename = cm.classFilename;
            this.unescapedDescription = cm.unescapedDescription;
            this.discriminator = cm.discriminator;
            this.defaultValue = cm.defaultValue;
            this.arrayModelType = cm.arrayModelType;
            this.isAlias = cm.isAlias;
            this.vars = cm.vars;
            this.requiredVars = cm.requiredVars;
            this.optionalVars = cm.optionalVars;
            this.readOnlyVars = cm.readOnlyVars;
            this.readWriteVars = cm.readWriteVars;
            this.allVars = cm.allVars;
            this.parentVars = cm.parentVars;
            this.allowableValues = cm.allowableValues;
            this.mandatory = cm.mandatory;
            this.allMandatory = cm.allMandatory;
            this.imports = cm.imports;
            this.hasVars = cm.hasVars;
            this.emptyVars = cm.emptyVars;
            this.hasMoreModels = cm.hasMoreModels;
            this.hasEnums = cm.hasEnums;
            this.isEnum = cm.isEnum;
            this.hasRequired = cm.hasRequired;
            this.hasOptional = cm.hasOptional;
            this.isArrayModel = cm.isArrayModel;
            this.hasChildren = cm.hasChildren;
            this.hasOnlyReadOnly = cm.hasOnlyReadOnly;
            this.externalDocs = cm.externalDocs;
            this.vendorExtensions = cm.vendorExtensions;
            this.additionalPropertiesType = cm.additionalPropertiesType;

            this.formatRequiredVars = getFormatRequiredVars(cm);
            this.hasFormatRequiredVars = !this.formatRequiredVars.isEmpty();
        }
    }

    private static boolean hasVarWithType(List<CodegenProperty> vars, CodegenProperty p) {
        for (CodegenProperty v : vars) {
            if (p.datatype.equals(v.datatype)) return true;
        }
        return false;
    }

    private static List<CodegenProperty> getFormatRequiredVars(CodegenModel cm) {
        List<CodegenProperty> formatRequiredVars = new ArrayList<>();
        for (CodegenProperty v : cm.vars) {
            CodegenProperty p;
            if (v.isListContainer) {
                p = v.items;
            } else {
                p = v;
            }
            // assume / require formatters for enums(generated in templates)
            // assume / require formatters for Number & UUID (must be provided by application)
            if (!p.isPrimitiveType && !p.isEnum && !p.isNumber && !p.isUuid && !hasVarWithType(formatRequiredVars, p)) {
                CodegenProperty prop = p.clone();
                prop.hasMore = true;
                formatRequiredVars.add(prop);
            }
        }
        if (formatRequiredVars.size() > 0) {
            formatRequiredVars.get(formatRequiredVars.size() - 1).hasMore = false;
        }

        return formatRequiredVars;
    }
}
