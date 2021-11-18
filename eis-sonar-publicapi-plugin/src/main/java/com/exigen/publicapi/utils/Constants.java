package com.exigen.publicapi.utils;

public class Constants {

	public static final String DAO_CLASS_POSTFIX_LOWER_CASE = "dao";
	public static final String DAO_CLASS_POSTFIX_CAMEL_CASE = "Dao";
	public static final String DAO_CLASS_POSTFIX_UPPER_CASE = "DAO";

	public static final String ENTITY_ANNOTATION_NAME = "Entity";

	public static final String LIST_VARIABLE_PATH = "java.util.List";
	public static final String ENTITY_ANNOTATION_PATH = "javax.persistence.Entity";
	public static final String ONE_TO_MANY_ANNOTATION_PATH = "javax.persistence.OneToMany";
	public static final String JOIN_COLUMN_ANNOTATION_PATH = "javax.persistence.JoinColumn";
	public static final String ORDER_COLUMN_ANNOTATION_PATH = "javax.persistence.OrderColumn";
	public static final String INDEX_COLUMN_ANNOTATION_PATH = "org.hibernate.annotations.IndexColumn";
	public static final String CAN_EXTEND_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.CanExtend";
	public static final String CAN_INVOKE_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.CanInvoke";
	public static final String DISCRIMINATOR_COLUMN_ANNOTATION_PATH = "javax.persistence.DiscriminatorColumn";
	public static final String INTERNAL_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.Internal";
	public static final String OVERRIDE_ANNOTATION_PATH = "java.lang.Override";
	public static final String SPI_ANNOTATION_PATH = "com.exigen.ipb.base.annotations.SPI";
}
