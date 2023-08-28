package com.beagledata.gaea.workbench.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.beagledata.gaea.ruleengine.annotation.FactLabel;
import com.beagledata.gaea.ruleengine.annotation.PassingDirection;
import com.beagledata.gaea.ruleengine.annotation.Required;
import com.beagledata.gaea.ruleengine.common.Constants;
import com.beagledata.gaea.ruleengine.exception.RuleException;
import com.beagledata.gaea.workbench.rule.define.Fact;
import com.beagledata.gaea.workbench.rule.define.Field;
import com.beagledata.gaea.workbench.rule.util.AssetsCache;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.kie.api.builder.KieFileSystem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Java源码生成器
 * <p>
 * Created by liulu on 2018/10/10.
 */
public class JavaCodeGenerator {
	/**
	 * 使用基本类型
	 */
	public static boolean USE_PRIMITIVE_TYPE = true;
	/**
	 * 使用默认值
	 */
	public static boolean USE_DEFAULT_VALUE = true;

	private KieFileSystem kfs;
	private Fact fact;
	private StringBuilder code;
	/**
	 * 依赖的Fact集合
	 */
	private Set<Fact> depFacts = new HashSet<>();

	public JavaCodeGenerator(KieFileSystem kfs, Fact fact) {
		this.kfs = kfs;
		this.fact = fact;
		code = new StringBuilder();
	}

	/**
	 * @author liulu
	 * 2018/10/10 上午 11:16
	 */
	public void gen() {
		String path = String.format("src/main/java/%s.java", fact.getImport().replace(".", "/"));
		KieFileSystemImpl kfsi = (KieFileSystemImpl) kfs;
		MemoryFileSystem mfs = kfsi.asMemoryFileSystem();
		if (mfs.existsFile(path)) {
			return;
		}

		dumpPackage();
		dumpImports();
		dumpClassAnnotation();
		dumpClassName();
		dumpFields();
		dumpGetSets();
		dumpEqualsHashCode();
		dumpToString();

		kfs.write(path, code.toString());

		depFacts.forEach(fact -> new JavaCodeGenerator(kfs, fact).gen());
	}


	private void dumpPackage() {
		code.append("package ").append(fact.getPackageName()).append(";\n\n");
	}

	private void dumpImports() {
		Set<String> imports = new HashSet<>();
		imports.add(JSON.class.getName());
		imports.add(FactLabel.class.getName());
		imports.add(PassingDirection.class.getName());
		imports.add(JsonIgnore.class.getName());
		imports.add(JsonProperty.class.getName());
		imports.add(JSONField.class.getName());
		imports.add(Required.class.getName());
		imports.add(BigDecimal.class.getName());
		imports.add(Date.class.getName());
		imports.add(List.class.getName());
		imports.add(ArrayList.class.getName());
		imports.add(Set.class.getName());
		imports.add(HashSet.class.getName());
		imports.add(Constants.class.getName());
		for (Field field : fact.getFields()) {
			if (field.isObjectType()) {
				String subTypeUuid = field.getSubType();
				if (StringUtils.isNotBlank(subTypeUuid)) {
					Fact referFact = AssetsCache.getFactByUuid(subTypeUuid);
					imports.add(referFact.getImport());
				}
			}
		}
		for (String importStr : imports) {
			code.append("import ").append(importStr).append(";\n");
		}
		code.append("\n");
	}

	private void dumpClassAnnotation() {
		code.append("@FactLabel(\"").append(fact.getName()).append("\")\n");
		code.append("@PassingDirection(PassingDirection.Direction.").append(fact.getDirection()).append(")\n");
	}

	private void dumpClassName() {
		code.append("public class ").append(fact.getClassName()).append(" {\n");
	}

	private void dumpFields() {
		for (Field field : fact.getFields()) {
			dumpField(field);
		}
		code.append("\n");
	}

	private void dumpField(Field field) {
		if (field.isDeriveType()) {
			return;
		}

		String fieldType = getFieldType(field);
		dumpFieldAnnotation(field);
		code.append("    private ").append(fieldType).append(" ").append(field.getName());

		if (USE_DEFAULT_VALUE) {
			String defaultValue = getFieldDefaultValue(field);
			if (defaultValue != null) {
				code.append(String.format(" = %s", defaultValue));
			}
		}
		code.append(";\n");
	}

	private void dumpFieldAnnotation(Field field) {
		code.append("    @JSONField(name = \"").append(field.getName()).append("\")\n");
		code.append("    @FactLabel(\"").append(field.getLabel().replace("\\","\\\\")).append("\")\n");
		code.append("    @PassingDirection(PassingDirection.Direction.").append(field.getDirection()).append(")\n");
		if (field.isRequired() && (PassingDirection.Direction.IN.equals(field.getDirection())
				|| PassingDirection.Direction.IN_OUT.equals(field.getDirection()))) {
			code.append("    @Required\n");
		}
	}

	private String getFieldType(Field field) {
		if (Field.Type.List.equals(field.getType())) {
			return String.format("List<%s>", getCollectionElementType(field));
		} else if (Field.Type.Set.equals(field.getType())) {
			return String.format("Set<%s>", getCollectionElementType(field));
		}

		if (USE_PRIMITIVE_TYPE) {
			if (Field.Type.Integer.equals(field.getType())) {
				return "int";
			} else if (Field.Type.Double.equals(field.getType())) {
				return "double";
			}  else if (Field.Type.Long.equals(field.getType())) {
				return "long";
			} else if (Field.Type.Boolean.equals(field.getType())) {
				return "boolean";
			}
		}

		if (field.isObjectType()) {
			Fact referFact = AssetsCache.getFactByUuid(field.getSubType());
			if (referFact == null) {
				throw new RuleException(field.getLabel() + "的子类型找不到");
			}
			depFacts.add(referFact);
			return referFact.getClassName();
		}
		return field.getType().name();
	}

	private String getFieldDefaultValue(Field field) {
		if (Field.Type.String.equals(field.getType())) {
			return "\"\"";
		} else if (Field.Type.BigDecimal.equals(field.getType())) {
			return "new BigDecimal(0)";
		} else if (Field.Type.Date.equals(field.getType())) {
			return "null";
		} else if (Field.Type.List.equals(field.getType())) {
			return "new ArrayList<>()";
		} else if (Field.Type.Set.equals(field.getType())) {
			return "new HashSet<>()";
		} else if (field.isObjectType()) {
			Fact referFact = AssetsCache.getFactByUuid(field.getSubType());
			if (referFact != null) {
				return String.format("new %s()", referFact.getClassName());
			}
		}
		return null;
	}

	private String getCollectionElementType(Field field) {
		if (field.getSubType().length() == 32) {
			Fact subFact = AssetsCache.getFactByUuid(field.getSubType());
			depFacts.add(subFact);
			return subFact.getImport();
		}
		return field.getSubType();
	}

	private void dumpGetSets() {
		for (Field field : fact.getFields()) {
			if (field.isDeriveType()) {
				continue;
			}
			dumpGetSet(field);
		}
		code.append("\n");
	}

	private void dumpGetSet(Field field) {
		if (PassingDirection.Direction.OUT.equals(field.getDirection())
				|| PassingDirection.Direction.IN_OUT.equals(field.getDirection())) {
			//输出
			code.append("    @JsonProperty(\"").append(field.getName()).append("\")\n");
		}
		if (PassingDirection.Direction.IN.equals(field.getDirection())
				|| PassingDirection.Direction.NONE.equals(field.getDirection())) {
			//不输出
			code.append("    @JsonIgnore\n");
		}
		code.append("    @JSONField(name = \"").append(field.getName()).append("\")\n");
		code.append("    public ")
				.append(getFieldType(field))
				.append(" get")
				.append(field.getName().substring(0, 1).toUpperCase())
				.append(field.getName().substring(1))
				.append("() {\n");

		if (Field.Type.BigDecimal.equals(field.getType())) {
			code.append("        if (")
					.append(field.getName())
					.append(" != null && ")
					.append(field.getName())
					.append(".scale() > Constants.BIGDECIMAL_SCALE) {\n")
					.append("        ")
					.append(field.getName())
					.append(" = ")
					.append(field.getName())
					.append(".setScale(Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND);\n")
					.append(" }\n");
		}
		code.append("        return ").append(field.getName()).append(";\n")
				.append("    }\n");
		if (PassingDirection.Direction.OUT.equals(field.getDirection())
				|| PassingDirection.Direction.IN_OUT.equals(field.getDirection())) {
			//输出
			code.append("    @JsonProperty(\"").append(field.getName()).append("\")\n");
		}
		if (PassingDirection.Direction.IN.equals(field.getDirection())
				|| PassingDirection.Direction.NONE.equals(field.getDirection())) {
			//不输出
			code.append("    @JsonIgnore\n");
		}
		code.append("    @JSONField(name = \"").append(field.getName()).append("\")\n");
		code.append("    public void set")
				.append(field.getName().substring(0, 1).toUpperCase())
				.append(field.getName().substring(1))
				.append("(").append(getFieldType(field)).append(" ").append(field.getName()).append(") {\n")
				.append("        this.").append(field.getName()).append(" = ").append(field.getName()).append(";\n")
				.append("    }\n");
	}

	private void dumpToString() {
		code.append("    public String toString() {\n");
		code.append("        return JSON.toJSONString(this);\n");
		code.append("    }\n");
		code.append("}");
	}

	private void dumpEqualsHashCode() {
		code.append("    public int hashCode() {\n");
		code.append("        return this.toString().hashCode();\n");
		code.append("    }\n");

		code.append("    public boolean equals(Object obj) {\n");
		code.append("        if (this == obj) {\n");
		code.append("            return true;\n");
		code.append("        }\n");
		code.append("        if (obj == null) {\n");
		code.append("            return false;\n");
		code.append("        }\n");
		code.append("        if (getClass() != obj.getClass()) {\n");
		code.append("            return false;\n");
		code.append("        }\n");
		code.append("        return this.toString().equals(obj.toString());\n");
		code.append("    }\n");
		code.append("\n");
	}
}
